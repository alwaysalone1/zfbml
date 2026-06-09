# APK Template Analysis

## Scope

Analyzed local APK templates:

- `apk_template/Animeko_5.3.2.apk`
- `apk_template/哔哩哔哩_7.50.0.apk`
- `apk_template/弹弹play 概念版_4.1.3.apk`
- `apk_template/腾讯视频_9.00.51.29549.apk`
- `apk_template/优酷视频_11.1.21.apk`

Tooling: Android SDK `apkanalyzer.bat` for manifest, resource, file, and dex package inspection. The analysis uses package/resource/component structure only; third-party implementation code is not copied.

## Structural Findings

| App | Package | Version | UI/Architecture Signal | Relevant Evidence |
| --- | --- | --- | --- | --- |
| Animeko | `me.him188.ani` | `5.3.2` | Lightweight Compose-heavy app, few activities, modern single-shell architecture. | Manifest has 3 activities; dex is dominated by `androidx.compose`. |
| Bilibili | `tv.danmaku.bili` | `7.50.0` | Large modular video platform with explicit player, danmaku, bangumi, search, and preference surfaces. | Resource names include `player_*`, `danmaku_preferences`, `player_dm_setting_preference`, `player_vertical_mode_setting_preference`, `bangumi_*`. |
| Dandanplay | `com.xyoye.dandanplay` | `4.1.3` | Anime-focused utility layout with clear feature activities for detail, player, advanced search, history, cache, and danmaku source settings. | Manifest includes `AnimeDetailActivity`, `PlayerActivity`, `SearchAdvancedActivity`, `CacheManagerActivity`, `SettingDanmuSourceActivity`. |
| Tencent Video | `com.tencent.qqlive` | `9.00.51.29549` | Large video platform with dynamic splash/icon entry points, detail/player split, casting, predownload/offline services, and player network sniffing. | Manifest includes `SplashHomeActivity`, dynamic icon home activities, `VideoDetailActivity`, `VideoPlayerActivity`, `PreDownloadSettingActivity`, `VBOfflineService`, `TVKPlayService`. |
| Youku | `com.youku.phone` | `11.1.21` | Large video platform with explicit detail preloader, download/cache pages, player debug/service hooks, and audio/background playback services. | Manifest includes `DetailPreLoader`, `DownloadHomeActivity`, `CacheSeriesActivity`, `VideoPreloadReceiver`, `PlayAudioForegroundService`. |

## Product Direction For ZFBML

1. Keep the Animeko-style Compose shell: one coherent app frame, fast route into detail/player, and minimal visible complexity.
2. Borrow Bilibili's player split: portrait player controls stay compact, landscape/fullscreen exposes comprehensive panels and quick controls.
3. Borrow Dandanplay's anime utility clarity: detail, search, history/cache, player setting, and danmaku source concepts should be visible but not overwhelming.
4. Borrow Tencent/Youku's preload mindset: source matching and next-episode route warming should be treated as a first-class playback surface, not hidden diagnostics.
5. Keep source/route decisions explainable: users should see why the app recommends a route, when the next episode is warmed, and when fallback sources are being used.

## App Shell And Navigation Findings

- Animeko's small Compose-heavy structure supports a single coherent app frame where top-level navigation stays lightweight.
- Bilibili, Tencent Video, and Youku all expose major video workflows through stable app-level entry points rather than hiding search, cache, source, and profile readiness deep in settings.
- Dandanplay's explicit cache, danmaku-source, search, detail, and player activities suggest that anime-focused utility features need clear navigation affordances even when the shell remains compact.
- ZFBML implementation direction: the main navigation should summarize recommendation, search-source coverage, source-library readiness, and cache capability from shared state models so the shell feels like a video app control surface, not static page labels.

## Search, Index, And Overlay Findings

- Bilibili exposes search as a platform surface, not a plain text box: its resource map includes search, bangumi, player, and danmaku preference surfaces that point to scoped discovery and visible playback decisions.
- Dandanplay's manifest separates `SearchActivity`, `SearchAdvancedActivity`, anime detail, cache, and danmaku-source settings. For ZFBML this argues for making source scope and result source visible before the user opens detail.
- Tencent Video and Youku both show large-app indexing patterns around search, preload, detail preloading, download/cache, and player services. The matching work should not be hidden as diagnostics; it should be summarized in normal UI.
- ZFBML implementation direction: the search page should show searchable source count, failed sources, result count, and quick source filters. Result filtering should be a first-class index interaction, not a debug message.
- Updated player direction: fullscreen controls, danmaku, route panels, and transient notices need explicit anti-obstruction rules so they do not cover the decisive video area or fight with each other.

## Browse And Schedule Findings

- Animeko's small Compose shell argues for keeping discovery fast and low-friction: the first tab should explain what to watch today without forcing users into source diagnostics.
- Bilibili's bangumi and player resource split suggests a normal video-app hierarchy: recommendation, calendar, category browse, detail, then player.
- Tencent Video and Youku both expose preload/detail infrastructure around browsing, so schedule and category surfaces should summarize readiness and next actions before users open detail.
- ZFBML implementation direction: the home calendar should show today, selected day, weekly coverage, and next update as one digest surface. Weekday chips should be stable and count-bearing, so the schedule feels like a product surface instead of a debug list from the Bangumi API.
- Updated browse direction: category pages should expose index coverage, top rating, heat, source, and fallback state as normal browsing signals, mirroring large video apps where a channel page explains what it can show before the user drills into detail.

## Detail And Playback Decision Findings

- Dandanplay separates anime detail and player activities, which supports a detail surface that explains playback readiness before entering the full player.
- Tencent Video and Youku both expose dedicated detail/player/preload components, suggesting that detail should summarize route readiness, cache/preload state, and fallback policy before the user taps play.
- Bilibili's player resource split reinforces keeping route diagnostics in panels while showing only user-facing readiness and action labels on the normal detail path.
- ZFBML implementation direction: the detail hero should show a compact readiness summary that reuses route recommendation and cache policy data. Users should see online readiness, BT fallback, cache availability, and the recommended action without opening a diagnostic route list.

## Cache And Offline Findings

- Dandanplay has a dedicated `CacheManagerActivity`, which supports treating cache as a normal anime workflow rather than a hidden download implementation detail.
- Tencent Video exposes `PreDownloadSettingActivity` and `VBOfflineService`, pointing to a split between user-visible offline policy and background task execution.
- Youku exposes `DownloadHomeActivity`, `CacheSeriesActivity`, and preload receivers, reinforcing that episode-level caching and preloading need clear labels before full task management exists.
- ZFBML implementation direction: every player cache entry should explain whether the current route can be cached and why. Media3-compatible HLS/DASH/MP4 streams can enter the offline queue, WebView/DRM routes should surface their block reason, and BT routes should be described as handled by the torrent edge-cache path.
- Updated cache direction: the Profile/offline surface should show Media3 cacheability, BT edge-cache, WebView/sniffing blockers, and advanced-download runtime status as normal user-facing cache strategy instead of static placeholder cards.

## Source Library And Route Strategy Findings

- Dandanplay separates cache, danmaku source, advanced search, detail, and player pages, suggesting source management should be understandable as user workflow rather than raw provider debugging.
- Bilibili, Tencent Video, and Youku all expose large player/source ecosystems through normal preference, preload, offline, and player-service surfaces instead of asking users to reason about provider internals.
- ZFBML implementation direction: the source library should summarize online-first playback, BT fallback, cache eligibility, and WebView sniffing as a strategy surface, then list source capabilities and domains for inspection.

## Profile And Settings Findings

- Dandanplay's cache manager and danmaku-source settings show that anime utilities still need a user-facing place for operational features once detail/player flows are clear.
- Bilibili, Tencent Video, and Youku all keep playback, cache, danmaku, and source preferences discoverable from user/profile or settings surfaces rather than scattering them through debug-only pages.
- ZFBML implementation direction: the Profile page should summarize current app readiness, quick actions, cache ability, danmaku coverage, and source strategy from shared state models, so it feels like a video-app user center instead of a static settings list.

## Current Implementation Focus

This pass exposes route prefetching in the detail page. The app already warms nearby episodes through `SourceRegistry.prefetchRouteCandidates`; the UI now surfaces whether adjacent episodes are warming, warmed, queued, or waiting for fallback source coverage.

The next pass after route prefetching adds visible search source coverage and per-source result filtering, based on the search/index findings above.

This pass adds player anti-obstruction support for danmaku. `DanmakuSafeArea` lets the layout engine reserve top, bottom, start, and end zones, and the player now derives those zones from visible controls, fullscreen side dock, option panels, lock state, and route/error notices.

This pass also unifies the player cache action model. Fullscreen controls, the More panel, and the Profile cache card now share source/route cacheability signals so users can see when Media3 offline caching is available, when WebView or DRM blocks caching, and when BT is handled by the torrent engine.

This pass upgrades the home schedule surface. `HomeScheduleUiState` now centralizes today count, weekly coverage, selected-day content, next update, and fallback text, and the expanded calendar shows that digest before weekday chips and anime rows.

This pass adds a detail playback readiness summary. `DetailPlaybackReadinessUiState` now condenses route recommendation, online coverage, BT fallback, and cache capability for the detail hero, keeping the normal watch path clear while preserving diagnostics in route panels.

This pass upgrades the category browse surface. `CategoryBrowseUiState` now centralizes category coverage, top rating, heat, source, list title, and empty/fallback copy, so category pages present a normal video-app channel summary instead of a loose list plus ad hoc metrics.

This pass upgrades the source library surface. `SourceLibraryUiState` now centralizes online, BT fallback, cacheable, WebView sniffing, strategy-card, and source-card state, keeping route/source policy explainable without spreading provider logic through the Compose tree.

This pass upgrades the offline cache surface. `CacheLibraryUiState` now centralizes Media3 cacheable sources, BT edge-cache sources, WebView/sniffing blockers, advanced-download runtime status, and cache capability cards so Profile and cache pages share one user-facing cache strategy model.

This pass upgrades the Profile surface. `ProfileCenterUiState` now centralizes the user-center hero, quick actions, status chips, and playback settings so Profile copy follows real source, danmaku, and cache capability instead of static Compose text.

This pass upgrades the app shell navigation. `AppNavigationUiState` now centralizes top-level tab labels, status copy, selection state, and semantic tone, and the bottom bar / navigation rail surface recommendation, search-source, source-library, and cache-readiness status from shared models.

This pass wires the app shell navigation to the real home schedule digest. `MainScaffold` now owns the weekly schedule load and shares one `HomeScheduleUiState` with both the Discover feed and top-level navigation, so the Discover tab can show the actual "today" update count once Bangumi schedule data is available.

This pass upgrades the search landing surface. `SearchLandingUiState` turns the shared home schedule digest into first-class search suggestions, then fills with popular fallback keywords, so the search tab opens with data-backed anime entries and source-readiness copy before the user types.

This pass upgrades search result cards. `SearchResultCardUiState` classifies catalog, direct-link, BT/RSS, and generic video-source hits, then exposes rating, episode count, category, and popularity chips so users can decide which result to open without relying on raw source names.

This pass upgrades the search-to-detail handoff. `DetailEntryUiState` reuses search result classification to show source type, detail loading state, episode readiness, and next action directly on the detail page before playback route decisions.

This pass upgrades the detail episode selector summary. `DetailEpisodeSummaryUiState` now centralizes current episode, total count, route status, route origin, and source coverage chips so the detail page can explain episode changes and route matching with the same model-driven language as the playback readiness card.

This pass upgrades detail route candidate cards. `RouteCandidateUiState` now centralizes source identity, protocol, primary quality label, file size, playback status, cache capability, and action copy so online, BT, WebView fallback, and failed routes can share one tested display contract before the player route panel reuses it.

This pass reuses `RouteCandidateUiState` in the player route panel. The fullscreen route switcher now shares detail-page route semantics and adds selected-route priority, so "current", "recommended", failed, BT fallback, and WebView fallback states stay consistent across pre-play and in-player switching.

This pass upgrades route source grouping. `RouteSourceGroupUiState` now owns status labels, tone, route-count labels, detailed source stats, and footer copy so detail source filtering and the player route-source strip share one display contract for selected, current, recommended, all-source, and failed-fallback groups.

This pass upgrades the player route summary card. `RoutePanelUiState` now owns compact/detailed titles, recommendation copy, current-route copy, and metric chip states so the in-player route switcher no longer rebuilds source and failure summaries inside Compose.

This pass upgrades the player episode panel. `PlayerEpisodePanelUiState` and `PlayerEpisodeOptionUiState` now own current-episode summary, list title, status chips, row labels, loading state, disabled waiting rows, and action copy so episode switching follows the same model-driven contract as route switching.

This pass upgrades the player danmaku settings panel. `PlayerDanmakuSettingsUiState` now owns toggle copy, density/alpha/font labels, semantic tone, and safe-area summary so danmaku controls and anti-obstruction feedback share the same model-driven language as the player route and episode panels.

This pass upgrades the player quality panel. `PlayerQualityPanelUiState` and `PlayerQualityOptionUiState` now own quality buckets, current-quality selection, source subtitle, action copy, empty state, and normalized auto-quality labels so quality switching follows the same tested model contract as routes, episodes, and danmaku settings.

This pass upgrades the player speed panel. `PlayerSpeedPanelUiState` and `PlayerSpeedOptionUiState` now own speed formatting, sorted option buckets, current-speed selection, helper copy, and action labels so playback speed switching follows the same model-driven panel contract as quality, route, episode, and danmaku controls.

This pass upgrades the player more/settings panel. `PlayerMorePanelUiState`, `PlayerMoreActionUiState`, and `PlayerMoreActionKind` now own the current-setting summary, quick-action order, enabled/selected state, and helper copy for quality, speed, episode, route, danmaku, and cache entry points so the fullscreen control layer follows the same app-store-grade model contract as the specialized player panels.

This pass upgrades the player panel shell. `PlayerPanelSheetUiState`, `PlayerPanelContextUiState`, `PlayerPanelTabUiState`, and `PlayerPanelKind` now own panel titles, playback context metadata, quick-tab order, disabled state, selected state, and danmaku highlighting so the fullscreen player control layer has one tested model contract instead of hard-coded Compose header and tab rules.

This pass upgrades the player status strips. `PlayerTopStatusStripUiState`, `PlayerStatusChipUiState`, and `PlayerFullscreenStatusStripUiState` now own top playback chips, source coverage copy, speed labels, fullscreen status badges, route summary, and compact tags so playback status presentation follows the same model-driven contract as the player panels and avoids duplicated Compose string assembly.

This pass upgrades the player bottom action bar. `PlayerActionBarUiState`, `PlayerActionUiState`, and `PlayerActionKind` now own playback issue priority actions, route fallback availability, quality/speed/episode/cache labels, next-episode state, selected panel state, and disabled-state copy so the bottom control surface follows the same model-driven contract as the fullscreen panel shell and status strips.

This pass upgrades player notices and route status. `PlayerNoticeUiState` and `PlayerRouteStatusUiState` now own playback-error priority, route-switch notice priority, fallback titles, semantic tones, and route-status labels so compact top notices, the top route pill, and fullscreen bottom notices share one tested notification contract instead of duplicating Compose branching.
