# Zfbml Aggregate

Android/TV first aggregate video player prototype.

This repository implements the foundation from the product plan:

- Kotlin + Compose Android app shell.
- Built-in `SourceProvider` interfaces and demo providers.
- JSON rule based source loading for user imported sources.
- Unified danmaku model, provider interface, Bilibili XML parser, and Canvas based renderer.
- Media3 player engine abstraction with ExoPlayer as the default engine.
- Media3 offline download service skeleton and advanced download provider abstraction.
- WebView cookie auth session storage scoped by platform domain.
- BitTorrent as a first-class stream protocol, with magnet parsing, scoring, and a `libtorrent4j` backed runtime.

## Build

Open the folder in Android Studio, or run:

```powershell
.\gradlew.bat :app:assembleDebug
```

The project targets Android SDK 36 and uses the JBR bundled with the local Android Studio install.

## 版本更新 / Version Notes

### v0.2.0

中文：

- BT 播放链路从 `file://` 升级为本地 HTTP Range 代理，Media3 可以通过 `http://127.0.0.1` 请求正在下载的视频文件。
- 本地代理支持 `Range`、`Content-Range`、稳定 URL 复用，以及对增长中文件的短时等待。
- BT 引擎会在播放器请求新字节范围时重新提高对应 piece 的优先级，为 seek 和边下边播打基础。

English:

- Upgraded the BT playback path from `file://` to a local HTTP Range proxy so Media3 can request the actively downloading video through `http://127.0.0.1`.
- The local proxy supports `Range`, `Content-Range`, stable URL reuse, and short waits for growing files.
- The BT engine reprioritizes pieces when the player requests a new byte range, laying the groundwork for seek-aware streaming.

## Rule Source Shape

Rule sources live as JSON and can be imported at runtime later. See:

```text
app/src/main/assets/rules/demo_rule.json
```

The first implementation supports CSS selectors through Jsoup. XPath fields are preserved in the data model so a full XPath engine can be added without changing the public rule schema.

## BitTorrent Playback Track

BT is treated as the primary long-term playback route, similar to Animeko's model:

- `StreamProtocol.BITTORRENT` represents magnet/torrent resources separately from HTTP streams.
- `TorrentSourceProvider` accepts pasted magnet links or torrent URLs and resolves them into standard `MediaStream` objects.
- `LibtorrentEngine` starts a native libtorrent session, adds magnet or HTTP `.torrent` resources, resolves metadata, selects the largest video-like file, and prioritizes its pieces.
- Once enough opening data is buffered, the engine exposes a local `file://` URL to Media3. A local HTTP range proxy remains the next step for stronger seek behavior and containers that dislike growing files.

The production implementation should add persisted resume data, per-file selection UI, current-position piece reprioritization, and an embedded range server before this is considered a polished BT streaming path.
