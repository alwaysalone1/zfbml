# Zfbml Aggregate

Android/TV first aggregate anime player prototype.

This repository implements the foundation from the product plan:

- Kotlin + Compose Android app shell.
- Built-in `SourceProvider` interfaces plus Animeko-compatible online source subscription loading.
- Online HLS/MP4 route aggregation first, with real BT/RSS providers kept as fallback resources.
- JSON rule based source loading for user imported sources.
- Unified danmaku model, provider interface, Bilibili XML parser, and Canvas based renderer.
- Media3 player engine abstraction with ExoPlayer as the default engine.
- Media3 offline download service skeleton and advanced download provider abstraction.
- BitTorrent as an experimental auxiliary stream protocol, with magnet parsing, scoring, `libtorrent4j`, and a local HTTP Range proxy.

## Build

Open the folder in Android Studio, or run:

```powershell
.\gradlew.bat :app:assembleDebug
```

The project targets Android SDK 36 and uses the JBR bundled with the local Android Studio install.

## 版本更新 / Version Notes

### v0.2.16

中文：
- 重构播放页为沉浸式视频叠层：顶部番名/集数/线路信息、中央播放与 10 秒快进快退、底部进度条和时间显示。
- 底部控制区从厚重面板改为半透明渐变控制栏，线路切换、弹幕开关、离线缓存和弹幕密度更接近主流视频 App。
- 统一本轮版本号与请求 UA 到 `0.2.16`，便于后续排查真实线路和资源加载问题。

English:

- Reworked the player page into an immersive video overlay with title/episode/route metadata, center play controls, 10-second seek controls, and a timed progress bar.
- Replaced the heavy bottom panel with a translucent gradient control bar for route switching, danmaku toggling, offline cache, and danmaku density.
- Updated the app version and request user agents to `0.2.16` for clearer route and asset diagnostics.

### v0.2.15

中文：
- 播放器现在会接收详情页解析出的整组播放线路，在播放页底部提供横向线路切换入口。
- 当前非 BT 线路播放失败时，会按解析排序自动尝试下一条可用线路，减少黑屏后必须返回详情页重选的情况。
- 模拟器复测真实 `omofun111` 在线线路，确认仍能渲染 1080p 视频首帧并持续播放。

English:

- The player now receives the full route set resolved on the detail page and exposes in-player route switching.
- When a non-BT route fails, playback automatically tries the next available route in resolved order instead of forcing the user back to the detail page.
- Re-verified a real `omofun111` online route on the emulator, including 1080p first-frame rendering and continued playback.

### v0.2.14

中文：
- 修正播放器无画面的高风险路径：保留适合 Compose 叠层的 `TextureView`，关闭不透明 shutter，并在首帧前显示加载状态。
- ExoPlayer 状态增加首帧、视频尺寸和播放状态诊断，日志会明确区分“拿到轨道但没出首帧”和“源解析失败”。
- 固定 ExoPlayer 实例并只更新 HTTP headers，避免切换线路时 PlayerView 仍绑定旧 player 导致只有声音/进度没有画面。
- 降低起播缓冲阈值，避免真实在线源长时间黑屏；同等匹配下优先 HLS/DASH 线路，并过滤 404/403 的失效线路。

English:

- Hardened the no-picture path by keeping the Compose-friendly `TextureView`, making the player shutter transparent, and showing startup status until the first video frame renders.
- Added first-frame, video-size, and playback-state diagnostics so logs can distinguish source failures from render/surface failures.
- Kept a stable ExoPlayer instance and update HTTP headers in place so PlayerView does not stay bound to a released player.
- Reduced startup buffer thresholds, prefer HLS/DASH routes when matches are otherwise equivalent, and filter dead 404/403 routes before playback.

### v0.2.13

中文：

- 把 Animeko 在线源的关键解析逻辑拆成可单测的内部工具，覆盖搜索词清洗、标题匹配、集数识别、MacCMS 地址提取和普通 HTML 视频地址提取。
- 修正 fallback 在线源的线路过滤规则，避免错误线路名正则导致应屏蔽线路仍被匹配。
- 模拟器实测：`葬送的芙莉莲` 详情页解析出 `omofun111` 的 MP4/HLS 在线线路，点击 MP4 后播放器进入 `READY / isPlaying=true`。

English:

- Extracted the key Animeko online-source parsing logic into testable internal helpers covering search keyword cleanup, title matching, episode parsing, MacCMS URL extraction, and generic HTML video URL extraction.
- Fixed fallback online-source channel filtering so intentionally blocked lines are not matched by a loose regex.
- Emulator verified: `Frieren` detail page resolves `omofun111` MP4/HLS online routes, and tapping the MP4 route reaches `READY / isPlaying=true`.

### v0.2.12

中文：

- 接入 Animeko 兼容的在线视频源订阅，默认读取 `css1.json` 中的 `web-selector` 线路配置。
- Bangumi 条目详情页现在优先解析在线视频线路，成功拿到 HLS/MP4 后直接进入 Media3 播放；BT/RSS 只作为无在线线路时的补充。
- 新增详情页线路来源切换条，可按 `omofun111`、`叽哔动漫`、`稀饭动漫` 等来源筛选播放线路。
- 已在模拟器验证真实在线线路可以进入播放器并达到 `READY / isPlaying=true`。

English:

- Added Animeko-compatible online source subscription loading, using `web-selector` route configs from `css1.json`.
- Bangumi detail pages now resolve online video routes first and hand HLS/MP4 streams directly to Media3; BT/RSS is kept as a fallback when no online route is available.
- Added route source chips on the detail page so lines can be filtered by providers such as `omofun111`, `jibim`, and `xifan`.
- Verified on the emulator that real online routes enter the player and reach `READY / isPlaying=true`.

### v0.2.6

中文：

- 搜索结果改为优先展示更适合手机边下边播的资源：MP4、AVC/H.264、AAC、1080p/720p、单集、体积适中、高 seed。
- 降低 HEVC/x265、10bit、AV1、BDRip、合集、超大体积资源的排序，避免默认点进难以直接播放的资源。
- 搜索卡片现在直接显示真实 `magnet` 或 `.torrent` 链接，方便确认是否拿到了正确 BT 播放入口。

English:

- Search results now prioritize mobile-friendly streaming candidates: MP4, AVC/H.264, AAC, 1080p/720p, single episodes, moderate file size, and higher seed count.
- HEVC/x265, 10bit, AV1, BDRip, batch, and oversized resources are ranked lower to avoid defaulting to hard-to-stream items.
- Search cards now show the real `magnet` or `.torrent` URL so the BT playback entry can be verified directly.

### v0.2.5

中文：

- 调整 BT 播放桥接：拿到种子元数据并选中文件后立即建立本地 HTTP Range 播放代理，不再等到固定缓冲量才交给播放器。
- 播放器请求哪个字节范围，本地代理就通知 libtorrent 优先下载对应 piece，提升 MP4/MKV 头部和尾部索引读取成功率。
- 为 BT 会话补充常用 tracker、强制 DHT/Tracker announce，并增加 `ZfbmlTorrent` 日志。
- 播放占位页增加“元数据”和“播放代理”状态，方便判断卡在搜 peer、下分片还是播放器解码。

English:

- Changed the BT bridge to expose the local HTTP Range playback proxy as soon as torrent metadata and file selection are ready.
- The local proxy now tells libtorrent to prioritize the byte range requested by the player, improving startup for MP4/MKV header and tail-index reads.
- Added common trackers, forced DHT/tracker announces, and `ZfbmlTorrent` logs for BT sessions.
- Added metadata/proxy readiness indicators to the BT placeholder screen to make playback failures easier to diagnose.

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

## Online Sources and BitTorrent Track

The current playback priority is online first, BT fallback:

- `AnimekoOnlineSourceProvider` loads Animeko-compatible `web-selector` configs, searches multiple video sites with Bangumi titles and aliases, parses episode/channel pages, and resolves HLS/MP4 playback URLs.
- `MediaRouteResolver` returns playable online routes first; BT/RSS routes are queried only when online resolution fails.
- `StreamProtocol.HLS` and `StreamProtocol.PROGRESSIVE` are handed directly to Media3 for normal video-app playback.
- `StreamProtocol.BITTORRENT` still represents magnet/torrent resources separately from HTTP streams, but this path is currently experimental because emulator testing showed crash/stability issues on BT playback.
- `TorrentSourceProvider` and `RssTorrentSourceProvider` remain available for pasted magnet links, torrent URLs, and public BT/RSS indexes while the online route quality is evaluated.

Before BT is promoted again, the production implementation should add crash isolation, persisted resume data, per-file selection UI, richer source filtering, current-position piece reprioritization, and source health telemetry.
