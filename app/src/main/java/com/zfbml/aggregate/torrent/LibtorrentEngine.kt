package com.zfbml.aggregate.torrent

import android.content.Context
import android.util.Log
import com.zfbml.aggregate.network.Ipv4FirstDns
import com.zfbml.aggregate.source.MediaStream
import com.zfbml.aggregate.source.StreamProtocol
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.libtorrent4j.AddTorrentParams
import org.libtorrent4j.AlertListener
import org.libtorrent4j.AnnounceEntry
import org.libtorrent4j.PieceIndexBitfield
import org.libtorrent4j.Sha1Hash
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SessionParams
import org.libtorrent4j.SettingsPack
import org.libtorrent4j.TcpEndpoint
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.Vectors
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.MetadataReceivedAlert
import org.libtorrent4j.alerts.PieceFinishedAlert
import org.libtorrent4j.alerts.SaveResumeDataAlert
import org.libtorrent4j.alerts.SaveResumeDataFailedAlert
import org.libtorrent4j.alerts.StateUpdateAlert
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.alerts.TorrentCheckedAlert
import org.libtorrent4j.alerts.TorrentErrorAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import org.libtorrent4j.swig.error_code
import org.libtorrent4j.swig.torrent_flags_t
import org.libtorrent4j.swig.libtorrent

class LibtorrentEngine(
    context: Context,
) : TorrentEngine {
    private val appContext = context.applicationContext
    private val sessionManager = SessionManager(false)
    private val httpClient = OkHttpClient.Builder()
        .dns(Ipv4FirstDns)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val rootDir: File = appContext.getExternalFilesDir("torrents")
        ?: File(appContext.filesDir, "torrents")
    private val fileServer = LocalTorrentFileServer()
    private val handleLock = Any()
    private val downloadedPieces: MutableSet<Int> = Collections.newSetFromMap(ConcurrentHashMap())
    // libtorrent4j 2.1.0-39 crashes inside saveResumeData() on both emulator x86_64
    // and this ARM64 test device, so keep fastresume disabled until the native wrapper
    // path is replaced or isolated behind a safer implementation.
    private val fastResumeEnabled = false

    private val _state = MutableStateFlow(TorrentEngineState())
    override val state: StateFlow<TorrentEngineState> = _state

    @Volatile
    private var currentStream: MediaStream? = null

    @Volatile
    private var currentHandle: TorrentHandle? = null

    @Volatile
    private var currentInfoHash: String? = null

    @Volatile
    private var currentSaveDir: File? = null

    @Volatile
    private var currentTorrentInfo: TorrentInfo? = null

    @Volatile
    private var currentResumeFile: File? = null

    @Volatile
    private var lastStatusLogAtMs: Long = 0L

    @Volatile
    private var lastMetadataNudgeAtMs: Long = 0L

    @Volatile
    private var lastResumeDataRequestAtMs: Long = 0L

    private var monitorJob: Job? = null
    private var listenerInstalled = false

    private val listener = object : AlertListener {
        override fun types(): IntArray = intArrayOf(
            AlertType.ADD_TORRENT.swig(),
            AlertType.METADATA_RECEIVED.swig(),
            AlertType.STATE_UPDATE.swig(),
            AlertType.TRACKER_REPLY.swig(),
            AlertType.TRACKER_ERROR.swig(),
            AlertType.PEER_CONNECT.swig(),
            AlertType.PEER_DISCONNECTED.swig(),
            AlertType.PIECE_FINISHED.swig(),
            AlertType.TORRENT_CHECKED.swig(),
            AlertType.TORRENT_ERROR.swig(),
            AlertType.TORRENT_FINISHED.swig(),
            AlertType.SAVE_RESUME_DATA.swig(),
            AlertType.SAVE_RESUME_DATA_FAILED.swig(),
            AlertType.FASTRESUME_REJECTED.swig(),
            AlertType.TORRENT_RESUMED.swig(),
        )

        override fun alert(alert: Alert<*>) {
            when (alert) {
                is AddTorrentAlert -> handleAddTorrent(alert)
                is StateUpdateAlert -> handleStateUpdate(alert)
                is PieceFinishedAlert -> handlePieceFinished(alert)
                is SaveResumeDataAlert -> handleSaveResumeData(alert)
                is SaveResumeDataFailedAlert -> Log.w(TAG, "save resume data failed: ${alert.error().getMessage()}")
                is MetadataReceivedAlert,
                is TorrentCheckedAlert,
                is TorrentFinishedAlert -> refreshFromAlert(alert)
                is TorrentErrorAlert -> updateError(alert.error().getMessage().ifBlank { alert.message() })
                else -> logAlert(alert)
            }
        }
    }

    override suspend fun prepare(stream: MediaStream): TorrentPlaybackPlan = withContext(Dispatchers.IO) {
        val initialPlan = TorrentPlaybackPlan(stream = stream)
        if (stream.protocol != StreamProtocol.BITTORRENT) {
            val message = "TorrentEngine received a non-BT stream: ${stream.protocol}"
            _state.value = TorrentEngineState(stream = stream, plan = initialPlan, errorMessage = message)
            return@withContext initialPlan
        }

        monitorJob?.cancel()
        lastMetadataNudgeAtMs = 0L
        lastResumeDataRequestAtMs = 0L
        downloadedPieces.clear()
        currentStream = stream
        currentHandle = null
        currentTorrentInfo = null
        currentInfoHash = stream.metadata["infoHash"] ?: MagnetLink.parse(stream.url)?.infoHash
        currentSaveDir = streamSaveDir(stream).also { it.mkdirs() }
        currentResumeFile = if (fastResumeEnabled) resumeDataFile(currentSaveDir ?: rootDir) else null
        _state.value = TorrentEngineState(
            stream = stream,
            plan = initialPlan,
            isPreparing = true,
            status = "starting",
            resumeDataBytes = currentResumeFile?.length()?.takeIf { currentResumeFile?.exists() == true } ?: 0L,
        )
        Log.i(TAG, "prepare stream provider=${stream.providerId} url=${stream.url.take(160)}")

        runCatching {
            ensureSession()
            addTorrent(stream, currentSaveDir ?: rootDir)
            startMonitor()
        }.onFailure { throwable ->
            updateError(throwable.message ?: throwable::class.java.simpleName)
        }

        initialPlan
    }

    override fun release() {
        monitorJob?.cancel()
        monitorJob = null
        requestResumeData(currentHandle, force = true)
        currentStream = null
        currentHandle = null
        currentInfoHash = null
        currentTorrentInfo = null
        currentSaveDir = null
        currentResumeFile = null
        lastMetadataNudgeAtMs = 0L
        lastResumeDataRequestAtMs = 0L
        downloadedPieces.clear()
        fileServer.close()
        _state.value = TorrentEngineState()
    }

    @Synchronized
    private fun ensureSession() {
        if (!listenerInstalled) {
            sessionManager.addListener(listener)
            listenerInstalled = true
        }
        if (!sessionManager.isRunning) {
            sessionManager.start(playbackSessionParams())
            Log.i(TAG, "session started listen=${sessionManager.listenInterfaces()}")
        }
        if (sessionManager.isPaused) {
            sessionManager.resume()
        }
        if (!sessionManager.isDhtRunning) {
            sessionManager.startDht()
        }
    }

    private fun addTorrent(stream: MediaStream, saveDir: File) {
        val url = stream.url.trim()
        if (url.startsWith("magnet:", ignoreCase = true)) {
            addMagnetTorrent(url, saveDir)
            return
        }

        if (url.startsWith("http", ignoreCase = true) && url.endsWith(".torrent", ignoreCase = true)) {
            val torrentFile = File(saveDir, "source.torrent")
            downloadTorrentFile(url, torrentFile)
            Log.i(TAG, "add torrent file bytes=${torrentFile.length()} url=${url.take(160)}")
            val torrentInfo = TorrentInfo(torrentFile)
            currentTorrentInfo = torrentInfo
            sessionManager.download(
                torrentInfo,
                saveDir,
                if (fastResumeEnabled) resumeDataFile(saveDir).takeIf { it.exists() && it.length() > 0L } else null,
                null,
                emptyList<TcpEndpoint>(),
                playbackTorrentFlags(),
            )
            return
        }

        throw IllegalArgumentException("Only magnet links and HTTP .torrent URLs are supported by the bundled BT engine.")
    }

    private fun addMagnetTorrent(url: String, saveDir: File) {
        val resumeFile = resumeDataFile(saveDir)
        if (addTorrentFromResumeData(resumeFile, saveDir)) {
            return
        }
        val enrichedMagnet = enrichedMagnet(url)
        Log.i(TAG, "add magnet torrent")
        sessionManager.download(enrichedMagnet, saveDir, playbackTorrentFlags())
    }

    private fun addTorrentFromResumeData(resumeFile: File, saveDir: File): Boolean {
        if (!fastResumeEnabled || !resumeFile.exists() || resumeFile.length() <= 0L) {
            return false
        }
        return synchronized(handleLock) {
            runCatching {
                val ec = error_code()
                val params = libtorrent.read_resume_data_ex(Vectors.bytes2byte_vector(resumeFile.readBytes()), ec)
                if (ec.value() != 0) {
                    Log.w(TAG, "ignore invalid fastresume: ${ec.message()}")
                    return@synchronized false
                }
                params.setSave_path(saveDir.absolutePath)
                params.setFlags(params.getFlags().or_(playbackTorrentFlags()))
                sessionManager.swig().async_add_torrent(params)
                Log.i(TAG, "add torrent from fastresume bytes=${resumeFile.length()}")
                true
            }.getOrElse { throwable ->
                Log.w(TAG, "failed to add torrent from fastresume", throwable)
                false
            }
        }
    }

    private fun downloadTorrentFile(url: String, outputFile: File) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/x-bittorrent, application/octet-stream, */*")
            .build()
        httpClient.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code} while downloading torrent file" }
            outputFile.outputStream().use { output ->
                response.body.byteStream().use { input -> input.copyTo(output) }
            }
        }
        require(outputFile.length() > 0L) { "Downloaded torrent file is empty" }
    }

    private fun enrichedMagnet(url: String): String {
        val magnet = MagnetLink.parse(url) ?: return url
        val trackers = (magnet.trackers + DEFAULT_TRACKERS).distinctBy { it.lowercase() }
        return MagnetLink.build(
            infoHash = magnet.infoHash ?: return url,
            displayName = magnet.displayName,
            trackers = trackers,
        )
    }

    private fun handleAddTorrent(alert: AddTorrentAlert) {
        val error = alert.error()
        if (error.isError) {
            updateError(error.getMessage().ifBlank { alert.message() })
            return
        }
        val handle = resolveHandle(alert.handle())
        if (handle == null) {
            updateError("Torrent was added but the native handle is not valid.")
            return
        }
        currentHandle = handle
        if (currentTorrentInfo == null) {
            currentTorrentInfo = safeHandleCall(handle) { it.torrentFile() }
        }
        seedTrackers(handle)
        Log.i(TAG, "torrent added")
        refreshFromHandle(handle)
    }

    private fun handlePieceFinished(alert: PieceFinishedAlert) {
        currentHandle = resolveHandle(alert.handle())
        downloadedPieces += alert.pieceIndex()
        requestResumeData(currentHandle, force = false)
    }

    private fun handleSaveResumeData(alert: SaveResumeDataAlert) {
        if (!fastResumeEnabled) {
            return
        }
        val resumeFile = currentResumeFile ?: return
        runCatching {
            val bytes = AddTorrentParams.writeResumeDataBuf(alert.params())
            require(bytes.isNotEmpty()) { "empty resume data" }
            resumeFile.parentFile?.mkdirs()
            val tempFile = File(resumeFile.parentFile, "${resumeFile.name}.tmp")
            tempFile.writeBytes(bytes)
            if (resumeFile.exists()) {
                resumeFile.delete()
            }
            require(tempFile.renameTo(resumeFile)) { "failed to replace resume data file" }
            Log.i(TAG, "saved fastresume bytes=${bytes.size}")
            _state.value = _state.value.copy(resumeDataBytes = bytes.size.toLong())
        }.onFailure { throwable ->
            Log.w(TAG, "failed to save fastresume", throwable)
        }
    }

    private fun seedTrackers(handle: TorrentHandle) {
        val trackerUrls = mutableListOf<String>()
        val validHandle = resolveHandle(handle) ?: return
        synchronized(handleLock) {
            val existingTrackers = runCatching { validHandle.trackers().map { it.url() } }
                .getOrDefault(emptyList())
            val mergedTrackers = (existingTrackers + DEFAULT_TRACKERS)
                .distinctBy { it.lowercase() }
                .sortedWith(compareBy<String> { !it.startsWith("http", ignoreCase = true) }.thenBy { it })
            val entries = mergedTrackers.map { tracker ->
                AnnounceEntry(tracker).apply {
                    tier((if (tracker.startsWith("http", ignoreCase = true)) 0 else 1).toShort())
                    failLimit(2.toShort())
                }
            }
            runCatching { validHandle.replaceTrackers(entries) }
            trackerUrls += mergedTrackers
            runCatching { validHandle.forceDHTAnnounce() }
            mergedTrackers
                .filter { it.startsWith("http", ignoreCase = true) }
                .take(8)
                .forEach { tracker ->
                    runCatching { validHandle.forceReannounce(0, tracker, TorrentHandle.IGNORE_MIN_INTERVAL) }
                }
            runCatching { validHandle.forceReannounce(0, 0, TorrentHandle.IGNORE_MIN_INTERVAL) }
        }
        Log.i(TAG, "seeded trackers count=${trackerUrls.size} httpFirst=${trackerUrls.take(5).joinToString()}")
    }

    private fun refreshFromAlert(alert: Alert<*>) {
        val handle = resolveHandle((alert as? TorrentAlert<*>)?.handle()) ?: return
        currentHandle = handle
        if (alert is MetadataReceivedAlert && currentTorrentInfo == null) {
            currentTorrentInfo = safeHandleCall(handle) { it.torrentFile() }
            requestResumeData(handle, force = true)
        }
        refreshFromHandle(handle)
    }

    private fun refreshFromHandle(handle: TorrentHandle?) {
        scope.launch {
            updateFromHandle(handle)
        }
    }

    private fun handleStateUpdate(alert: StateUpdateAlert) {
        val torrentStatus = alert.status().firstOrNull() ?: return
        val previous = _state.value
        val plan = previous.plan
        val hasMetadata = torrentStatus.hasMetadata()
        mergeDownloadedPieces(torrentStatus.pieces())
        val statusLabel = if (plan?.localPlaybackUrl != null) {
            "ready"
        } else {
            torrentStatus.state().name.lowercase()
        }
        val progressPercent = (torrentStatus.progress().coerceIn(0f, 1f) * 100f)

        _state.value = previous.copy(
            isPreparing = !hasMetadata,
            isReady = plan?.localPlaybackUrl != null,
            hasMetadata = hasMetadata,
            status = statusLabel,
            progressPercent = progressPercent,
            downloadRateBytesPerSecond = torrentStatus.downloadRate(),
            uploadRateBytesPerSecond = torrentStatus.uploadRate(),
            connectedPeers = torrentStatus.numPeers(),
            connectedSeeds = torrentStatus.numSeeds(),
            resumeDataBytes = previous.resumeDataBytes,
            errorMessage = torrentStatus.errorCode().takeIf { it.isError }?.getMessage()?.takeIf { it.isNotBlank() }
                ?: previous.errorMessage,
        )
    }

    private fun logAlert(alert: Alert<*>) {
        when (alert.type()) {
            AlertType.TRACKER_REPLY,
            AlertType.TRACKER_ERROR -> Log.i(TAG, "${alert.what()}: ${alert.message()}")
            else -> Unit
        }
    }

    private fun startMonitor() {
        monitorJob = scope.launch {
            while (isActive) {
                runCatching { sessionManager.postTorrentUpdates() }
                val handle = resolveHandle()
                updateFromHandle(handle)
                requestResumeData(handle, force = false)
                delay(1_000)
            }
        }
    }

    private fun updateFromHandle(handle: TorrentHandle?) {
        val stream = currentStream ?: return
        val saveDir = currentSaveDir ?: return
        val torrentInfo = currentTorrentInfo
        val hasMetadata = torrentInfo != null
        val previous = _state.value
        val plan = if (torrentInfo != null) {
            buildPlan(stream, torrentInfo, saveDir)
        } else {
            nudgeMetadata(resolveHandle(handle))
            previous.plan ?: TorrentPlaybackPlan(stream)
        }
        val statusLabel = when {
            plan.localPlaybackUrl != null -> "ready"
            hasMetadata -> previous.status
                ?.takeUnless { it == "starting" || it == "metadata" }
                ?: "buffering"
            else -> "metadata"
        }
        logStatus(statusLabel, hasMetadata, plan)

        _state.value = TorrentEngineState(
            stream = stream,
            plan = plan,
            isPreparing = !hasMetadata,
            isReady = plan.localPlaybackUrl != null,
            hasMetadata = hasMetadata,
            status = statusLabel,
            progressPercent = plan.selectedFileProgressPercent,
            selectedFileProgressPercent = plan.selectedFileProgressPercent,
            downloadRateBytesPerSecond = previous.downloadRateBytesPerSecond,
            uploadRateBytesPerSecond = previous.uploadRateBytesPerSecond,
            connectedPeers = previous.connectedPeers,
            connectedSeeds = previous.connectedSeeds,
            resumeDataBytes = currentResumeFile?.length()?.takeIf { currentResumeFile?.exists() == true }
                ?: previous.resumeDataBytes,
            errorMessage = previous.errorMessage,
        )
    }

    private fun buildPlan(
        stream: MediaStream,
        torrentInfo: TorrentInfo,
        saveDir: File,
    ): TorrentPlaybackPlan {
        val storage = torrentInfo.files()
        val files = (0 until storage.numFiles()).map { index ->
            TorrentFileCandidate(
                index = index,
                path = storage.filePath(index),
                name = storage.fileName(index),
                sizeBytes = storage.fileSize(index),
            )
        }
        val selected = TorrentFileSelector.choose(files)
            ?: return TorrentPlaybackPlan(stream = stream)

        val pieceLength = storage.pieceLength().coerceAtLeast(1)
        val selectedFileTorrentOffset = storage.fileOffset(selected.index)
        val localFile = File(storage.filePath(selected.index, saveDir.absolutePath))
        val hasInitialMediaData = hasInitialMediaData(localFile, selected)
        val readyBytes = minOf(
            selected.sizeBytes,
            PLAYBACK_READY_BYTES,
            maxOf(MIN_PLAYBACK_READY_BYTES, selected.sizeBytes / 50L),
        )
        val selectedProgress = contiguousDownloadedBytes(
            fileTorrentOffset = selectedFileTorrentOffset,
            fileSizeBytes = selected.sizeBytes,
            pieceLength = pieceLength,
        )
        val bufferingPercent = if (readyBytes <= 0L) {
            0f
        } else {
            ((selectedProgress.toFloat() / readyBytes.toFloat()).coerceIn(0f, 1f) * 100f)
        }
        val selectedFileProgressPercent = if (selected.sizeBytes <= 0L) {
            0f
        } else {
            ((selectedProgress.toFloat() / selected.sizeBytes.toFloat()).coerceIn(0f, 1f) * 100f)
        }
        val hasPlaybackBuffer = selectedProgress >= readyBytes && hasInitialMediaData
        val localUrl = if (hasPlaybackBuffer) {
            fileServer.serve(
                file = localFile,
                totalSizeBytes = selected.sizeBytes,
                isRangeAvailable = { start, end ->
                    isPlaybackRangeAvailable(
                        file = localFile,
                        fileSizeBytes = selected.sizeBytes,
                        fileTorrentOffset = selectedFileTorrentOffset,
                        pieceLength = pieceLength,
                        start = start,
                        end = end,
                    )
                },
            )
        } else {
            null
        }

        return TorrentPlaybackPlan(
            stream = stream,
            selectedFileName = selected.path,
            selectedFileIndex = selected.index,
            selectedFileSizeBytes = selected.sizeBytes,
            selectedFileContiguousBytes = selectedProgress,
            playbackReadyBytes = readyBytes,
            selectedFileProgressPercent = selectedFileProgressPercent,
            localPlaybackUrl = localUrl,
            bufferingPercent = bufferingPercent,
        )
    }

    private fun hasInitialMediaData(file: File, selected: TorrentFileCandidate): Boolean {
        if (!file.exists() || file.length() < MEDIA_HEADER_PROBE_BYTES) {
            return false
        }
        return runCatching {
            RandomAccessFile(file, "r").use { input ->
                val size = minOf(MEDIA_HEADER_PROBE_BYTES.toLong(), file.length()).toInt()
                val bytes = ByteArray(size)
                val read = input.read(bytes)
                if (read <= 0) {
                    return@runCatching false
                }
                when (selected.name.substringAfterLast('.', missingDelimiterValue = "").lowercase()) {
                    "mp4", "m4v", "mov" -> String(bytes, 0, read, StandardCharsets.ISO_8859_1)
                        .let { header -> header.contains("ftyp") || header.contains("moov") || header.contains("mdat") }
                    "mkv", "webm" -> read >= 4 &&
                        bytes[0] == 0x1A.toByte() &&
                        bytes[1] == 0x45.toByte() &&
                        bytes[2] == 0xDF.toByte() &&
                        bytes[3] == 0xA3.toByte()
                    "ts" -> bytes[0] == 0x47.toByte()
                    else -> bytes.take(read).any { it != 0.toByte() }
                }
            }
        }.getOrDefault(false)
    }

    private fun contiguousDownloadedBytes(fileTorrentOffset: Long, fileSizeBytes: Long, pieceLength: Int): Long {
        if (fileSizeBytes <= 0L || pieceLength <= 0) {
            return 0L
        }
        val fileEndOffset = fileTorrentOffset + fileSizeBytes
        var filePosition = 0L
        while (filePosition < fileSizeBytes) {
            val torrentOffset = fileTorrentOffset + filePosition
            val pieceIndex = (torrentOffset / pieceLength).toInt()
            if (pieceIndex !in downloadedPieces) {
                break
            }
            val nextPieceOffset = (pieceIndex.toLong() + 1L) * pieceLength.toLong()
            val availableEnd = minOf(fileEndOffset, nextPieceOffset)
            if (availableEnd <= torrentOffset) {
                break
            }
            filePosition += availableEnd - torrentOffset
        }
        return filePosition
    }

    private fun isPlaybackRangeAvailable(
        file: File,
        fileSizeBytes: Long,
        fileTorrentOffset: Long,
        pieceLength: Int,
        start: Long,
        end: Long,
    ): Boolean {
        if (pieceLength <= 0 || !file.exists() || start < 0L || start >= fileSizeBytes) {
            return false
        }
        val boundedEnd = end.coerceAtMost(fileSizeBytes - 1L)
        val startPiece = ((fileTorrentOffset + start) / pieceLength).toInt()
        val endPiece = ((fileTorrentOffset + boundedEnd) / pieceLength).toInt()
        return (startPiece..endPiece).all { piece -> piece in downloadedPieces }
    }

    private fun mergeDownloadedPieces(bitfield: PieceIndexBitfield) {
        val size = runCatching { bitfield.size() }.getOrDefault(0)
        if (size <= 0) {
            return
        }
        for (piece in 0 until size) {
            if (runCatching { bitfield.getBit(piece) }.getOrDefault(false)) {
                downloadedPieces += piece
            }
        }
    }

    private fun requestResumeData(handle: TorrentHandle?, force: Boolean) {
        val now = System.currentTimeMillis()
        val validHandle = resolveHandle(handle)
        if (
            !fastResumeEnabled ||
            validHandle == null ||
            (!force && now - lastResumeDataRequestAtMs < RESUME_DATA_SAVE_INTERVAL_MS)
        ) {
            return
        }
        lastResumeDataRequestAtMs = now
        safeHandleCall(validHandle) { it.saveResumeData(TorrentHandle.SAVE_INFO_DICT) }
            ?.let { Unit }
    }

    private fun nudgeMetadata(handle: TorrentHandle?) {
        val now = System.currentTimeMillis()
        val validHandle = resolveHandle(handle)
        if (validHandle == null || now - lastMetadataNudgeAtMs < METADATA_NUDGE_INTERVAL_MS) {
            return
        }
        lastMetadataNudgeAtMs = now
        safeHandleCall(validHandle) {
            it.forceDHTAnnounce()
            it.forceReannounce(0, 0, TorrentHandle.IGNORE_MIN_INTERVAL)
        }
    }

    private fun resolveHandle(candidate: TorrentHandle? = null): TorrentHandle? {
        return synchronized(handleLock) {
            if (candidate.isUsableHandle()) {
                candidate
            } else if (currentHandle.isUsableHandle()) {
                currentHandle
            } else {
                val infoHash = currentInfoHash
                val found = if (infoHash.isNullOrBlank()) {
                    null
                } else {
                    runCatching { sessionManager.find(Sha1Hash.parseHex(infoHash)) }.getOrNull()
                }
                if (found.isUsableHandle()) {
                    currentHandle = found
                    found
                } else {
                    currentHandle = null
                    null
                }
            }
        }
    }

    private fun TorrentHandle?.isUsableHandle(): Boolean {
        return this != null && runCatching { isValid }.getOrDefault(false)
    }

    private fun <T> safeHandleCall(handle: TorrentHandle, block: (TorrentHandle) -> T): T? {
        return synchronized(handleLock) {
            if (!handle.isUsableHandle()) {
                null
            } else {
                runCatching { block(handle) }
                    .onFailure { throwable -> Log.w(TAG, "ignored invalid torrent handle call", throwable) }
                    .getOrNull()
            }
        }
    }

    private fun playbackTorrentFlags(): torrent_flags_t {
        return libtorrent.getDefault_flags().or_(libtorrent.getSequential_download())
    }

    private fun playbackSessionParams(): SessionParams {
        val settings = SettingsPack.defaultSettings().apply {
            setEnableDht(true)
            setEnableLsd(true)
            setDhtBootstrapNodes(DHT_BOOTSTRAP_NODES)
            connectionsLimit(500)
            activeDhtLimit(200)
            activeTrackerLimit(32)
            activeDownloads(4)
            activeLimit(8)
            alertQueueSize(4_000)
            setAnnouncePort(6881)
            listenInterfaces("0.0.0.0:6881")
        }
        return SessionParams(settings)
    }

    private fun streamSaveDir(stream: MediaStream): File {
        val id = stream.metadata["infoHash"] ?: stream.id.ifBlank { stream.url.hashCode().toString() }
        return File(rootDir, id.safePathSegment())
    }

    private fun resumeDataFile(saveDir: File): File {
        return File(saveDir, RESUME_DATA_FILE_NAME)
    }

    private fun updateError(message: String) {
        Log.e(TAG, message)
        val previous = _state.value
        _state.value = previous.copy(
            isPreparing = false,
            isReady = false,
            errorMessage = message,
        )
    }

    private fun String.safePathSegment(): String {
        return replace(Regex("[^A-Za-z0-9._-]"), "_").take(96).ifBlank { "torrent" }
    }

    private fun logStatus(status: String, hasMetadata: Boolean, plan: TorrentPlaybackPlan) {
        val now = System.currentTimeMillis()
        if (now - lastStatusLogAtMs < 5_000L) {
            return
        }
        lastStatusLogAtMs = now
        Log.i(
            TAG,
            "status=$status metadata=$hasMetadata file=${plan.selectedFileName.orEmpty()} " +
                "fileProgress=${"%.2f".format(plan.selectedFileProgressPercent)} " +
                "buffer=${"%.2f".format(plan.bufferingPercent)} urlReady=${plan.localPlaybackUrl != null}",
        )
    }

    private companion object {
        const val TAG = "ZfbmlTorrent"
        const val MIN_PLAYBACK_READY_BYTES = 16L * 1024L * 1024L
        const val PLAYBACK_READY_BYTES = 64L * 1024L * 1024L
        const val METADATA_NUDGE_INTERVAL_MS = 45_000L
        const val RESUME_DATA_SAVE_INTERVAL_MS = 30_000L
        const val MEDIA_HEADER_PROBE_BYTES = 256 * 1024
        const val RESUME_DATA_FILE_NAME = "fastresume.dat"
        const val USER_AGENT =
            "Mozilla/5.0 (Android; ZFBML) AppleWebKit/537.36 (KHTML, like Gecko) ZfbmlAggregate/0.2"
        const val DHT_BOOTSTRAP_NODES =
            "router.bittorrent.com:6881,dht.transmissionbt.com:6881,router.utorrent.com:6881,dht.libtorrent.org:25401"
        val DEFAULT_TRACKERS = listOf(
            "https://tracker.zhuqiy.com:443/announce",
            "https://tracker.yemekyedim.com:443/announce",
            "https://tracker.pmman.tech:443/announce",
            "https://tracker.nekomi.cn:443/announce",
            "https://torrents.tmtime.dev:443/announce",
            "https://tr.bangumi.moe:9696/announce",
            "https://tracker.ghostchu-services.top/announce",
            "http://nyaa.tracker.wf:7777/announce",
            "http://www.torrentsnipe.info:2701/announce",
            "http://www.genesis-sp.org:2710/announce",
            "http://tracker810.xyz:11450/announce",
            "http://tracker.zhuqiy.com:80/announce",
            "http://tr.bangumi.moe:6969/announce",
            "http://t.acg.rip:6699/announce",
            "udp://tracker.opentrackr.org:1337/announce",
            "udp://open.demonii.com:1337/announce",
            "udp://open.stealth.si:80/announce",
            "udp://open.tracker.cl:1337/announce",
            "udp://tracker.torrent.eu.org:451/announce",
            "udp://tracker.theoks.net:6969/announce",
            "udp://tracker.srv00.com:6969/announce",
            "udp://tracker.004430.xyz:1337/announce",
            "udp://torrents.tmtime.dev:6969/announce",
            "udp://explodie.org:6969/announce",
            "udp://bittorrent-tracker.e-n-c-r-y-p-t.net:1337/announce",
            "udp://6ahddutb1ucc3cp.ru:6969/announce",
            "udp://utracker.ghostchu-services.top:6969/announce",
        )
    }
}
