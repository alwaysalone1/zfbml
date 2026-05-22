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
