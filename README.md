# Zfbml Aggregate

Android/TV first aggregate anime player prototype.

This repository implements the foundation from the product plan:

- Kotlin + Compose Android app shell.
- Built-in `SourceProvider` interfaces and demo providers.
- Real BT/RSS source providers for anime torrent indexes.
- JSON rule based source loading for user imported sources.
- Unified danmaku model, provider interface, Bilibili XML parser, and Canvas based renderer.
- Media3 player engine abstraction with ExoPlayer as the default engine.
- Media3 offline download service skeleton and advanced download provider abstraction.
- BitTorrent as a first-class stream protocol, with magnet parsing, scoring, `libtorrent4j`, and a local HTTP Range proxy.

## Build

Open the folder in Android Studio, or run:

```powershell
.\gradlew.bat :app:assembleDebug
```

The project targets Android SDK 36 and uses the JBR bundled with the local Android Studio install.

## 版本更新 / Version Notes

### v0.2.4

中文：

- 修复真实 BT 源播放启动慢的问题，BT 文件达到较小头部缓冲后就交给播放器尝试播放。
- `.torrent` 文件下载改为 OkHttp，并带浏览器 UA，提升 Mikan、ACG.RIP、Nyaa、Bangumi Moe 等源的兼容性。
- 本地 HTTP Range 代理等待增长中文件的时间从 30 秒提升到 120 秒，降低边下边播时播放器读到未下载片段导致的中断。

English:

- Fixed slow startup for real BT source playback by handing the file to the player after a smaller initial buffer.
- Switched HTTP `.torrent` downloads to OkHttp with a browser-like UA for better source compatibility.
- Increased local HTTP Range proxy wait time for growing torrent files from 30 seconds to 120 seconds to reduce playback interruptions while streaming.

### v0.2.3

中文：

- 接入首批真实 BT/RSS 资源站：Mikan、DMHY、ACG.RIP、Nyaa、Bangumi Moe。
- 新增通用 `RssTorrentSourceProvider`，把 RSS item 统一解析为 `SearchResult`、`Episode` 和 `StreamProtocol.BITTORRENT` 播放线路。
- 支持 RSS 中的 magnet、HTTP `.torrent`、大小、seeders、info hash、字幕组、清晰度等元数据，并进入现有 BT 播放链路。
- 首页默认搜索词改为真实番名，方便直接做端到端搜索和播放测试。

English:

- Added the first real BT/RSS sources: Mikan, DMHY, ACG.RIP, Nyaa, and Bangumi Moe.
- Added a shared `RssTorrentSourceProvider` that maps RSS items into `SearchResult`, `Episode`, and `StreamProtocol.BITTORRENT` streams.
- Supports RSS magnet links, HTTP `.torrent` links, size, seeders, info hash, subgroup, and quality metadata before handing off to the existing BT playback path.
- Changed the default home search query to a real anime title for immediate end-to-end testing.

### v0.2.2

中文：

- 弹幕平台不再要求 WebView 登录授权，首页移除平台授权入口。
- 参考 `D:\Ling1` 的弹幕接口逻辑，弹幕 Provider 改为公开网页/API 搜索、匹配集数、抓取弹幕。
- 弹幕渲染、绘制、调度仍保留在本项目自研 `DanmakuSurface` 管线中，不复用 Ling1 的渲染实现。

English:

- Danmaku providers no longer require WebView login authorization, and platform auth actions were removed from the home screen.
- Ported the danmaku API flow from `D:\Ling1`: public web/API search, episode matching, and timeline fetching.
- Danmaku rendering, painting, and scheduling remain in this project's own `DanmakuSurface` pipeline rather than reusing Ling1 rendering code.

### v0.2.1

中文：

- 首页改为番剧聚合视觉，打开后优先展示搜索、线路、番剧列表和 BT/弹幕/TV 状态卡片。
- 平台账号授权入口降级到侧栏，避免首屏看起来像登录页。
- 主要按钮和播放状态文案改为中文，便于中文用户直接试用。

English:

- Updated the home screen to an anime aggregation layout, prioritizing search, sources, result shelves, and BT/danmaku/TV status cards.
- Moved platform auth actions into the side rail so the first screen no longer feels like a login page.
- Localized primary buttons and playback status text into Chinese for easier hands-on testing.

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
- `RssTorrentSourceProvider` searches public BT/RSS indexes and resolves each result into the same standard BT stream shape.
- `LibtorrentEngine` starts a native libtorrent session, adds magnet or HTTP `.torrent` resources, resolves metadata, selects the largest video-like file, and prioritizes its pieces.
- Once enough opening data is buffered, the engine exposes a local HTTP Range URL to Media3 for playback.

The production implementation should add persisted resume data, per-file selection UI, richer source filtering, current-position piece reprioritization, and source health telemetry before this is considered a polished BT streaming path.
