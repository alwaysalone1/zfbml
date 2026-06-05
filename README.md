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

### v0.2.37

中文：

- 播放器设置面板改为视频内抽屉体验：打开清晰度、倍速、线路、选集、弹幕等面板时，画面会出现轻量暗色遮罩，点击空白区域可直接收起。
- 横屏仍使用右侧抽屉，竖屏使用底部半屏面板；面板本身会消耗点击，不会误触关闭，交互更接近国内移动端视频 App。
- “更多”面板从纵向设置列表改成 3 列播放器快捷宫格，清晰度、倍速、选集、线路、弹幕、缓存都以图标入口呈现。
- 面板标题增加简短状态说明，把“播放设置”从普通系统弹窗改成播放器内控制层，减少工具感和调试感。

English:

- Reworked player option panels into in-video drawers: opening quality, speed, route, episode, danmaku, or other panels now adds a lightweight scrim, and tapping the empty area dismisses the panel.
- Landscape still uses a right-side drawer while portrait uses a bottom half panel. Panel taps are consumed so users do not accidentally close it while operating controls.
- Replaced the vertical More settings list with a 3-column player shortcut grid for quality, speed, episodes, routes, danmaku, and cache.
- Added short state subtitles to panel headers so playback settings feel like part of the player control layer instead of generic system dialogs.

### v0.2.36

中文：

- 播放页接管系统返回键：如果线路、选集、清晰度、弹幕等面板已打开，返回键会先关闭当前面板，不会直接退出播放。
- 横屏全屏时按返回键会优先退出全屏并恢复竖屏/系统栏，符合国内移动端播放器的常见操作习惯。
- 只有在竖屏且没有弹出面板时，返回键才会离开播放页回到详情页，减少误退出。

English:

- The player now handles system Back in layers: route, episode, quality, danmaku, and other panels close first instead of leaving playback immediately.
- Pressing Back in landscape fullscreen exits fullscreen and restores portrait/system bars first, matching common mobile video-player behavior.
- Back only leaves the player when already in portrait with no player panel open, reducing accidental exits.

### v0.2.35

中文：

- 横屏播放器进入真正沉浸式全屏：播放器处于横屏布局时会隐藏状态栏和导航栏，减少系统 UI 对画面的干扰。
- 竖屏布局会自动恢复系统栏，退出全屏和离开播放器时也会清理沉浸式状态，避免影响首页、详情页等普通页面。
- 全屏按钮、手动旋转横屏和退出全屏现在共用同一套系统栏同步逻辑，让横屏承担完整播放器体验，竖屏保持轻量观看。

English:

- Landscape playback now enters a real immersive fullscreen mode by hiding status and navigation bars while the player is in landscape layout.
- Portrait playback restores system bars automatically, and leaving or exiting the player clears the immersive state so normal pages are not affected.
- The fullscreen button, manual landscape rotation, and fullscreen exit now share the same system-bar sync behavior, keeping landscape as the full-control playback mode while portrait stays lightweight.

### v0.2.34

中文：

- 竖屏播放器底部精简控制行新增“全屏”入口，点击后直接请求横屏全屏播放，不再依赖用户自己旋转手机。
- 横屏播放器顶部新增退出全屏按钮，点击后恢复系统默认方向；退出播放页时也会清理方向请求，避免离开播放器后继续横屏锁定。
- 竖屏顶部叠层移除线路胶囊，线路信息统一放到视频下方的“当前线路”区域，竖屏播放器本体更接近国内移动端视频 App 的轻叠层。
- 横屏继续保留线路标签和完整功能入口，让全屏观看承担高级操作，竖屏观看保持轻量。

English:

- Added a fullscreen entry to the compact portrait player row; tapping it requests landscape fullscreen playback directly instead of relying on the user to rotate the phone manually.
- Added an exit-fullscreen button to the landscape player top overlay. Leaving the player also clears the orientation request so the rest of the app does not stay locked in landscape.
- Removed the route pill from the portrait top overlay and kept route details in the below-video current-route area, making the portrait player surface closer to lightweight China-market mobile video apps.
- Kept route status and full action density in landscape so fullscreen playback remains the place for advanced controls while portrait stays focused.

### v0.2.33

中文：

- 播放器竖屏控制层改为更精简的国内移动端视频布局：点击画面后只保留时间、弹幕输入、弹幕开关和“更多”入口，不再把清晰度、倍速、线路、选集、缓存全部铺在视频底部。
- 新增“播放设置”面板，竖屏通过“更多”进入清晰度、倍速、选集、线路、弹幕开关和缓存，避免未全屏观看时像调试工具栏。
- 横屏全屏补齐右侧快捷 dock，增加倍速和缓存入口，保留线路、选集、弹幕、清晰度等完整功能，更符合全屏观看时的高频操作密度。
- 竖屏视频下方信息区改为“标题信息 + 合集入口 + 当前线路小条 + 快速选集”，线路状态更像视频 App 的播放信息，不再像独立下载/换源工具。

English:

- Simplified the portrait player overlay into a more China-market mobile video layout: after tapping the video it now keeps only time, danmaku input, danmaku toggle, and a More entry instead of spreading quality, speed, routes, episodes, and cache across the video bottom.
- Added a Playback Settings panel so portrait users can reach quality, speed, episodes, routes, danmaku toggle, and cache through More without making the non-fullscreen player feel like a debug toolbar.
- Expanded the landscape fullscreen quick dock with speed and cache while keeping routes, episodes, danmaku, and quality, matching the denser control set expected in fullscreen playback.
- Reworked the portrait below-video area into title info, collection entry, current-route strip, and quick episodes so route state feels like normal video playback information rather than a standalone source-switching tool.

### v0.2.32

中文：

- 播放页“选集”面板升级为视频式选集列表：顶部展示当前播放条目和总集数，列表中每集都有编号、标题、当前/加载中状态和播放动作。
- 选集行改为和线路面板一致的卡片视觉，使用状态色条、编号块和动作胶囊，减少普通设置列表的感觉。
- 加载下一集时会在目标集数上显示加载状态，用户能更明确地知道正在切到哪一集。

English:

- Upgraded the in-player episode panel into a video-style episode list with a current-item summary, total episode count, numbered rows, status, and play actions.
- Episode rows now match the route panel's card language with status accents, number blocks, and action pills instead of generic settings rows.
- When switching episodes, the target row shows a loading state so users can see exactly which episode is being prepared.

### v0.2.31

中文：

- 播放页“播放线路”面板升级为卡片式线路选择，和详情页换源语言保持一致，直接标出当前、推荐、已失败、在线可播、BT 兜底等状态。
- 每条播放线路右侧新增动作标签：播放中、切到推荐、切换、重试、边下边播或暂不可选，让用户在播放时更容易判断下一步。
- WebView-only 线路在播放器换源面板中显示为暂不可选，避免误点到当前播放器无法直接承接的线路。

English:

- Upgraded the in-player route panel to card-style route choices that match the detail-page source-switching language, clearly marking current, recommended, failed, online-playable, and BT fallback states.
- Added explicit action labels on each route row: playing, switch to recommended, switch, retry, stream while downloading, or unavailable.
- WebView-only routes are shown as unavailable in the player route panel so users do not switch into routes the current player cannot directly handle.

### v0.2.30

中文：

- 详情页展开“查看线路”后，来源筛选从普通文字芯片升级为横向源卡片，优先显示推荐源、在线源和可播放源，并展示可播/BT 数量。
- 线路列表行改为更接近视频 App 的播放选择项：左侧状态色条，中间展示来源、推荐/在线可播/BT 兜底标签，右侧给出“播放/推荐播放/边下边播”动作。
- 保留自动最佳线路策略，同时让手动换源的信息层级更清楚，减少“线路测试列表”的观感。

English:

- The expanded detail-page route picker now uses horizontal source cards instead of plain text chips, prioritizing the recommended source, online sources, and playable sources while showing playable/BT counts.
- Route rows now feel more like video-app playback choices: a status accent strip, source and recommendation/playable/BT labels, and an explicit play action on the right.
- Kept the automatic-best-route strategy while making manual source switching easier to scan and less like a raw route-testing list.

### v0.2.29

中文：

- 详情页线路卡升级为更清晰的“自动最佳线路”诊断区，直接展示当前推荐源、推荐清晰度/协议、选集、在线优先和 BT 兜底状态。
- 自动推荐逻辑不再把 WebView-only 线路当作可直接播放线路，避免详情页误导用户点进不可播入口。
- 补充线路 UI 状态测试，覆盖推荐线路摘要和 WebView-only 降级状态。

English:

- Upgraded the detail-page route card into a clearer automatic-best-route diagnostic area showing the recommended source, quality/protocol, selected episode, online-first status, and BT fallback status.
- The automatic recommendation model no longer treats WebView-only entries as directly playable routes, avoiding misleading play actions on the detail page.
- Added UI-state coverage for recommendation summaries and WebView-only fallback behavior.

### v0.2.28

中文：

- 竖屏点击播放器后新增紧凑交互行：左侧显示当前时间/总时长，中间是类似 B 站移动端的弹幕输入胶囊，右侧提供弹幕开关。
- 弹幕输入胶囊可直接打开弹幕设置面板，让竖屏用户不用先去底部横向按钮里找“弹幕设置”。
- 保留现有线路摘要和功能条，竖屏控制层更像视频播放器，同时不把画面完全遮住。

English:

- Added a compact portrait interaction row after tapping the player: current/total time on the left, a Bilibili-like danmaku input pill in the center, and a danmaku toggle on the right.
- The danmaku input pill opens the danmaku settings panel directly, reducing the need to hunt through the horizontal action bar.
- Kept the existing route summary and action bar so the portrait overlay feels more like a video player without covering the picture.

### v0.2.27

中文：

- 将 HTML 原型里的“快速换源”思路先落到 Android 横屏播放器：点击画面后，右侧新增快捷功能 dock，可直接进入线路、选集、弹幕、弹幕设置和清晰度。
- 横屏仍保留底部完整控制条，但高频操作不再都挤在底部，线路/选集入口更接近成熟移动播放器的右侧抽屉体验。
- 竖屏播放器不受影响，继续采用 16:9 视频舞台 + 下方追番信息区的结构。

English:

- Ported the quick-source-switch idea from the HTML prototype into the Android landscape player: tapping the video now reveals a right-side quick dock for routes, episodes, danmaku, danmaku settings, and quality.
- The landscape player keeps the full bottom control bar, but frequent actions no longer compete for space at the bottom and now feed naturally into the side drawer.
- Portrait playback remains on the fixed 16:9 video stage plus watch-info layout.

### v0.2.26

中文：

- 播放页“线路”面板新增诊断头，集中显示推荐线路、可用线路数、在线源数、BT 数和失败数，用户打开面板时能直接判断当前源是否健康。
- 线路列表现在会明确标记“当前”“推荐”“已失败”，失败线路继续降级排序，手动切源时不再像单纯的测试列表。
- 新增线路面板 UI 状态测试，覆盖推荐线路选择、失败统计、在线/BT 分类统计，为后续源加载优化保留稳定判断。

English:

- Added a diagnostic header to the player route panel showing the recommended route plus available, online, BT, and failed route counts.
- Route rows now label the current, recommended, and failed entries explicitly, making manual source switching feel closer to a real video app instead of a raw route tester.
- Added UI-state coverage for route-panel recommendation and route-count summaries so future source-loading work has a stable baseline.

### v0.2.25

中文：
- 播放器增加线路失败补救动作：当前线路报错时，竖屏观看信息卡和底部控制条会直接给出“重试当前”和“下一线路”，减少用户卡在错误提示里的挫败感。
- 自动换线逻辑抽成可测试的 UI 模型，失败线路会降级排序，WebView-only 线路不会被自动选为下一条播放兜底。
- 播放线路面板会标记已经失败的线路，用户手动换源时可以更清楚地避开坏源。

English:

- Added explicit recovery actions for failed playback routes: retry the current route or jump to the next route directly from the portrait info card and player controls.
- Moved next-route selection into a tested UI model so failed streams are demoted and WebView-only entries are skipped for automatic fallback.
- The route panel now marks failed routes, making manual source switching clearer when a source breaks during playback.

### v0.2.24

中文：
- 播放页新增竖屏观看布局：上方固定 16:9 视频舞台，下方保留番剧标题、当前集、线路状态、清晰度/倍速入口和快速选集，避免竖屏播放时整页只剩黑色视频层。
- 横屏继续保持沉浸式全屏叠层，线路、清晰度、倍速、选集、弹幕设置仍使用右侧抽屉或底部面板，不打断正在播放的画面。
- 竖屏视频舞台采用更轻的 compact 控制层，减少底部信息、弹幕输入条对画面的遮挡；详细线路信息移动到视频下方，接近成熟移动端长视频播放体验。

English:

- Added a portrait watch layout with a fixed 16:9 video stage on top and episode, route status, quality/speed shortcuts, and quick episode switching below it.
- Kept landscape playback as an immersive full-screen overlay with route, quality, speed, episode, and danmaku panels available as drawers/sheets.
- Made the portrait video controls more compact so playback controls no longer crowd the picture; detailed route context now lives below the video like a mature mobile video app.

### v0.2.23

中文：

- 播放器点击命中层改为 Compose 透明层，放在视频与弹幕之上、控制按钮之下，避免 `TextureView`/`PlayerView` 吃掉触摸导致控制层无法唤出，同时不压住播放器按钮。
- 恢复更接近 B 站移动端的点击显示/隐藏与播放中自动收起逻辑，同时保留顶部标题、中心暂停/快退/快进、底部进度/弹幕/清晰度/倍速/线路入口。
- 弹幕层约束到 16:9 视频画面区域，并下调默认密度、字号和透明度，让真实弹幕不再铺满竖屏黑边。

English:

- Moved player tap handling to a Compose transparent layer above video/danmaku and below controls so `TextureView`/`PlayerView` no longer swallows control toggles while player buttons remain clickable.
- Restored a Bilibili-like tap-to-show/hide and playback auto-hide flow while keeping the top title, center transport controls, and bottom progress/danmaku/quality/speed/route actions.
- Constrained danmaku to the 16:9 video area and reduced default density, size, and opacity so real comments no longer flood the portrait letterbox.

### v0.2.22

中文：
- 参考 B 站竖屏播放器与 Animeko 详情页，把详情页首屏改为“海报沉浸头图 + 立即观看 + 自动最佳线路状态”，弱化原来像线路列表工具的观感。
- 新增线路 UI 状态模型，详情页会展示在线源、BT、来源数量和失败数量；线路详情默认收起，用户需要时再展开换源。
- 播放页控制层改为更轻的移动端视频叠层，播放时会自动收起；选集和线路切换会按 HLS/MP4 在线源优先、BT 兜底的 UI 策略排序。

English:

- Reworked the detail first screen after reviewing Bilibili's portrait player and Animeko's detail flow, using an immersive poster header, a primary watch action, and an automatic best-route status.
- Added a route UI state model so the detail page shows online, BT, source, and failed-route counts while keeping detailed source switching collapsed until needed.
- Made the player overlay lighter and auto-hiding; episode and route switching now use a UI sorting policy that prefers online HLS/MP4 routes before BT fallback routes.

### v0.2.21

中文：
- 调整 Bangumi 条目到在线视频源的别名搜索顺序，优先使用中文名、中文别名，再使用外文原名，避免国漫条目把真正可搜的中文名挤出搜索窗口。
- 优化线路匹配评分：完整片名会优先于“包含片名”的衍生条目，降低解说、预告、粤语等非正片命中的优先级，减少先解析失效线路导致的长时间无结果。
- 给单个线路命中解析增加超时兜底，避免某个慢源或失效页面长期拖住详情页线路加载。

English:

- Reordered Bangumi-to-online-source aliases so Chinese titles and Chinese aliases are searched before foreign original names, which improves Chinese-animation route discovery.
- Improved route scoring so exact titles beat containing variants, while commentary, trailers, and dubbed variants are ranked lower when the catalog title does not request them.
- Added a per-hit timeout guard during route resolution so one slow or broken source cannot stall the detail page route loading for too long.

### v0.2.20

中文：
- 将播放器底部“清晰度 / 倍速 / 线路 / 选集 / 弹幕设置”改为可操作的半屏或侧边面板，避免把所有线路按钮长期堆在底部。
- 倍速切换现在会直接写入播放器；清晰度与线路面板会切换真实 `MediaStream`；选集面板会在播放器内重新解析线路并切到新集。
- 弹幕设置面板新增开关、密度、透明度、字号控制，渲染层会按设置实时更新。

English:

- Replaced the overloaded bottom row with actionable half-sheet/side-drawer panels for quality, speed, routes, episodes, and danmaku settings.
- Speed selection now updates the player directly; quality and route panels switch real `MediaStream` entries; the episode panel resolves routes in-player before switching.
- Added danmaku controls for visibility, density, opacity, and font size, with the rendering layer updated from those settings.

### v0.2.19

中文：
- 加固播放器点按唤出控件：`PlayerView` 后续动态加入的 `TextureView`/子视图也会自动继承点按监听，避免播放起帧后控件难以再次呼出。
- 继续保持弹幕层与播放器控制区分层，隐藏控件时点按画面可回到国内移动端视频 App 常见的沉浸式控制叠层。

English:

- Hardened tap-to-show player controls by binding dynamically inserted `TextureView`/child views inside `PlayerView`, preventing the video surface from swallowing taps after playback starts.
- Kept danmaku and controls layered separately so tapping the hidden-control video surface returns to the immersive mobile video overlay.

### v0.2.18

中文：
- 修正弹幕层按 `sp` 当像素排轨导致的重叠问题，渲染前会用真实文本宽高计算轨道高度。
- 弹幕布局改为滚动、顶部、底部分区调度，同轨滚动弹幕会做安全间距和追尾检测，过载时主动丢弃会碰撞的弹幕。
- 播放器底部控制区改为更接近国内移动端视频 App 的弹幕输入条 + 文本快捷项布局，弱化厚重胶囊按钮。
- 增强视频 Surface 点按事件绑定，避免原生 `PlayerView`/`TextureView` 吃掉触摸导致隐藏控件后不好唤出。

English:

- Fixed danmaku overlap caused by laying out tracks with raw `sp` values instead of measured pixel text metrics.
- Reworked danmaku scheduling into separate scrolling/top/bottom lanes with spacing and catch-up checks; overloaded comments are dropped instead of being drawn on top of each other.
- Adjusted the bottom player controls toward a China-market mobile video layout with a danmaku input strip and lightweight text actions instead of heavy pill buttons.
- Hardened player surface tap handling so the native `PlayerView`/`TextureView` does not swallow taps when controls are hidden.

### v0.2.17

中文：
- 参考 B 站手机端播放器的轻量叠层结构，加入点按显示/隐藏控件和播放中 4 秒自动收起，减少遮挡画面。
- 底部控制栏改为贴边进度 + 横向快捷胶囊，弹幕开关、弹幕密度档位、清晰度、倍速和缓存入口更接近移动端视频播放器。
- 保留真实线路切换能力，多个播放线路仍会在底部以横向小按钮展示。

English:

- Refined the player toward a Bilibili-style mobile overlay with tap-to-show controls and 4-second auto-hide during playback.
- Reworked the bottom controls into an edge progress bar plus compact action chips for danmaku, danmaku density, quality, speed, and offline cache.
- Kept real in-player route switching available as a compact horizontal route row when multiple streams exist.

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
