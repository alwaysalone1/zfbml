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
6. Keep the product ad-free: do not add ad SDKs, ad identifiers, promoted-content slots, monetization feeds, or advertising interfaces when borrowing large-app patterns.

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

This pass upgrades the portrait watch info panel. `PortraitWatchInfoUiState` and `PortraitWatchActionUiState` now own the visible title, current-episode label, meta chips, playback summary, panel actions, and diagnostic notice so portrait playback uses the same model-driven contract as fullscreen player controls.

This pass upgrades the portrait episode rail. `PortraitEpisodeRailUiState`, `PortraitEpisodeRailItemUiState`, and `PortraitEpisodeMoreActionUiState` now own visible episode windowing, compact episode labels, loading/disabled state, and the all-episodes entry so portrait playback episode switching no longer keeps player-state logic in Compose.

This pass upgrades the fullscreen player top overlay. `PlayerTopOverlayUiState` now owns the top title, subtitle, compact notice, route status, and status chips so full-screen playback metadata follows the same unified model contract as the panel shell, action bar, notices, and portrait player surfaces.

This pass upgrades the portrait playback recovery strip. `PortraitRecoveryActionsUiState` now owns retry and fallback-route action visibility, labels, enabled state, and semantic tones so portrait playback recovery shares action semantics with the fullscreen player action bar instead of hard-coding recovery buttons in Compose.

This pass upgrades the portrait route insight row. `PortraitRouteInsightUiState` and `PortraitRouteInsightChipUiState` now own route coverage, online-route counts, BT fallback counts, current-route labels, and semantic tones so portrait diagnostics no longer compute route insight chips inside Compose.

This pass upgrades the BT playback preparation placeholder. `TorrentPlaybackPreparationUiState` now owns startup buffering progress, metadata/readiness labels, overall/video/buffer percentages, connection speed, selected-file labels, file size, and error copy so torrent edge-cache playback preparation can become a reusable player/cache status surface.

This pass upgrades the search idle guidance. `SearchIdleHintUiState` now owns the pre-search title, summary, action copy, searchable-source chip, schedule-suggestion chip, and index-health chip so the search tab exposes source coverage and schedule-backed entry points before the user types a query.

This pass upgrades the search results section. `SearchResultsSectionUiState` now owns the results header subtitle and empty-result title/subtitle for loading, selected-source filtering, no-hit, and failed-source states, making search/index feedback consistent before the user opens detail playback decisions.

This pass upgrades the detail route resolution panel. `DetailRouteResolutionUiState` now owns loading, failed, empty, ready, and idle route-matching titles, subtitles, details, chips, progress state, and semantic tone so detail playback decisions expose source matching status without hard-coded Compose branches.

This pass upgrades the detail episode rail. `DetailEpisodeOptionUiState` now owns compact episode labels, selected state, route-matching subtitle, action label, and semantic tone so detail-page episode switching uses the same model-driven playback-decision language as route readiness and resolution panels.

This pass upgrades the detail hero actions. `DetailHeroActionUiState` now owns the primary play label plus route-entry title, value, and tone so the first playback decision and the manual route entry share one tested model contract instead of deriving labels inside Compose.

This pass upgrades the detail first-play guidance. `DetailFirstPlayUiState` now owns the first-play title, decision copy, action label, progress/icon state, tone, and detail chips so the detail hero's playback recommendation is model-driven alongside route readiness and hero actions.

This pass upgrades the detail route status card. `DetailRouteStatusUiState` now owns compact versus expanded status, recommendation copy, source-focus chips, loading diagnostics, metrics, progress/icon state, and error tone so detail playback route readiness behaves like a tested product surface instead of a Compose-local diagnostic block.

This pass upgrades the detail route source selector. `DetailRouteSourceSelectorUiState` now owns the source selector header, recommended/current source pills, auto-best card, action label, and source badges so users get a product-level route-source choice surface instead of raw source grouping copy assembled inside Compose.

This pass upgrades the detail route prefetch card. `RoutePrefetchUiState` and `RoutePrefetchItemUiState` now own prefetch card tone, progress visibility, badge copy, item status labels, and item tones so adjacent-episode route warming reads as a first-class cache/product feature rather than a local Compose status mapping.

This pass upgrades the detail playback readiness strip. `DetailPlaybackReadinessUiState` now owns semantic tone, cache tone, and route/online/backup/cache readiness chips so the detail hero can present playback readiness and cache availability without deriving product copy or colors inside Compose.

This pass upgrades route candidate badges. `RouteCandidateUiState` now owns recommended and status badge chips so detail route rows and player route rows present the same source/route state contract for recommended, current, failed, fallback, and online-playable routes.

This pass upgrades route candidate row presentation. `RouteCandidateUiState` now owns player-row enablement, prominent/highlighted state, compact/detailed titles, subtitles, and detail-line copy so route option rows no longer derive visual emphasis or display titles inside Compose.

This pass upgrades player episode row presentation. `PlayerEpisodeOptionUiState` now owns compact episode labels, badges, highlighted/prominent state, action enablement, and text/rail alpha so episode switching rows follow the same model-driven display contract as route rows.

This pass upgrades player quality and speed option presentation. `PlayerQualityOptionUiState` and `PlayerSpeedOptionUiState` now own badges, highlighted/prominent state, action enablement, and icon/text alpha so the player settings panels use the same model-driven row contract as route and episode switching.

This pass upgrades danmaku settings toggle presentation. `PlayerDanmakuSettingsUiState` now owns the toggle action label, status badge, highlighted/prominent state, action enablement, and icon/text alpha so danmaku visibility and safe-area settings share the same model-driven row contract as other player panels.

This pass upgrades danmaku slider presentation. `PlayerDanmakuSliderUiState` now owns density, alpha, and font-size slider titles, formatted values, clamped values, ranges, steps, and semantic tones so danmaku rendering controls are configured by the UI model instead of Compose-local constants.

This pass upgrades player more-action tile presentation. `PlayerMoreActionUiState` now owns highlighted/prominent state, action enablement, tile alpha, container/border alpha, icon alpha, and text alpha so the player settings entry grid follows the same model-driven contract as quality, speed, episode, route, and danmaku controls.

This pass upgrades player panel quick-tab presentation. `PlayerPanelTabUiState` now owns action enablement, prominent state, visual tone, container alpha, content alpha, and value alpha so the panel navigation strip no longer derives selected, highlighted, or disabled visual behavior inside Compose.

This pass upgrades player panel context-bar presentation. `PlayerPanelContextUiState` now owns status tone, icon tone, container/border alpha, icon-container alpha, title alpha, and metadata alpha so the current-playback context strip is configured by the UI model instead of Compose-local color constants.

This pass upgrades player panel header presentation. `PlayerPanelSheetUiState` now owns the dismiss label plus title, subtitle, and dismiss-action alpha so the panel header is rendered from the sheet model instead of Compose-local copy and opacity constants.

This pass upgrades player option-panel shell presentation. `PlayerPanelShellUiState` now owns landscape/portrait sizing, scrim alpha, panel surface color, border alpha, content padding/spacing, and corner radii so the fullscreen player panel container follows the same model-driven contract as its header, context bar, tabs, and action rows.

This pass upgrades player route-source filter presentation. `PlayerRouteSourceStripUiState` and `PlayerRouteSourceChipUiState` now own source-strip visibility, source titles, filtered route-list titles, visible route sets, chip sizing, emphasis alpha, border alpha, detail visibility, and failed-source footer state so manual route switching follows a tested model contract instead of deriving source grouping and chip presentation inside Compose.

This pass upgrades player route-row presentation. `RouteCandidateUiState` now owns row container alpha, disabled alpha, border tone/alpha, row padding/spacing, rail dimensions, subtitle tones, and action-label dimensions so the manual route switching list renders candidate rows from model state instead of keeping row shell and action-chip constants in Compose.

This pass upgrades player route-summary presentation. `RoutePanelUiState` and `RoutePanelMetricUiState` now own route summary card alpha, border alpha, corner radius, content spacing, icon tone/size, summary tone, selected-route summary alpha, detailed-toggle labels/dimensions, metric spacing, notice presentation, and metric-chip shell values so the route switcher summary follows the same tested model contract as source filters and candidate rows.

This pass upgrades player episode-summary presentation. `PlayerEpisodePanelUiState` now owns list spacing, list-title alpha, summary-card alpha/border/radius, icon tone/size/container alpha, text spacing, chip spacing, helper copy, and helper alpha so the episode switching panel header follows the same model-driven contract as route summaries and route rows.

This pass upgrades player episode-row presentation. `PlayerEpisodeOptionUiState` now owns row container alpha, disabled alpha, border alpha, row padding/spacing, rail dimensions, index-box dimensions, loading indicator size, text spacing, subtitle tone, and action-label dimensions so episode switching rows render from model state instead of keeping row shell and action-chip constants in Compose.

This pass upgrades shared player selectable-row presentation. `PlayerSelectableRowUiState` now owns shared row min-height, radius, container/border tones and alpha, padding, spacing, icon sizing, subtitle tone, trailing alpha, and selected-check sizing so danmaku toggle rows, quality rows, and speed rows render from their UI models instead of deriving row chrome inside Compose.

This pass upgrades danmaku slider-row presentation. `PlayerDanmakuSliderUiState` now owns shared vertical spacing, slider height, title/value alpha, thumb and active-track tones, inactive-track tone/alpha, and per-slider semantic tones so density, opacity, and font-size controls render from model state instead of keeping slider chrome constants inside Compose.

This pass upgrades player seek-bar presentation. `PlayerSeekBarUiState` now owns current and duration labels, pending seek clamping, seekable versus loading states, compact progress fraction, time-label sizing and alpha, slider sizing, track tones, and loading-track presentation so fullscreen and compact bottom controls use one tested playback-progress contract instead of deriving seek chrome inside Compose.

This pass upgrades compact player interaction presentation. `PlayerCompactInteractionUiState`, `PlayerCompactProgressUiState`, and `PlayerCompactDanmakuUiState` now own compact progress-line sizing and tones, danmaku entry copy, enabled/disabled toggle labels, alpha, spacing, and button dimensions so narrow-screen playback controls follow the same tested state contract as fullscreen seek bars.

This pass upgrades compact playback recovery presentation. `PlayerCompactRecoveryUiState` and `PlayerCompactRecoveryActionUiState` now own issue visibility, retry and next-route action semantics, row height, spacing, button dimensions, selected/enabled state, and disabled alpha so narrow-screen playback failures expose recovery controls from the UI model instead of Compose-local buttons.

This pass upgrades fullscreen side-dock presentation. `PlayerFullscreenSideDockUiState` and `PlayerFullscreenDockActionUiState` now own dock shell dimensions, action order, shortcut labels, enabled/selected state, tones, button sizing, and disabled alpha so fullscreen danmaku, quality, speed, episode, route, and more shortcuts are driven by a tested UI model instead of Compose-local button assembly.

This pass upgrades player panel tab-strip presentation. `PlayerPanelTabStripUiState` and `PlayerPanelTabUiState` now own strip height, item spacing, content padding, tab width/height, corner radius, icon size, and internal spacing so the fullscreen player option-panel navigation strip follows a tested model contract instead of Compose-local layout constants.

This pass upgrades player panel context-bar layout presentation. `PlayerPanelContextUiState` now owns the context card radius, row padding, row spacing, icon-box size, icon size, icon corner radius, and text spacing so the current-playback context strip can be tuned from the same tested UI model as the panel header, tabs, and shell.

This pass upgrades player panel header layout presentation. `PlayerPanelSheetUiState` now owns header spacing, title/subtitle spacing, and dismiss-button height so the player option-panel header follows the same tested model contract as the panel shell, context strip, and tab navigation.

This pass upgrades danmaku episode mapping. `DanmakuManualMapping` now represents user-calibrated anime/episode to provider-token matches, and `DanmakuRegistry` tries those calibrated mappings before automatic provider search while falling back to automatic matching when the calibrated timeline is empty. Replacing manual mappings also clears cached timelines so user corrections take effect immediately.

This pass upgrades player danmaku mapping presentation. `PlayerDanmakuMappingUiState` now summarizes pending, automatic, manual, and loading mapping states, and the player tracks danmaku candidates separately from loaded timeline items so the danmaku panel can show candidate counts, loaded comment counts, calibrated status, and a manual search/calibration entry.

This pass upgrades player danmaku candidate calibration. The danmaku settings panel now renders automatic match candidates with provider/platform badges and lets the user set any candidate as the current episode's manual mapping, then refreshes candidates and reloads the selected timeline immediately.

This pass upgrades persisted danmaku calibration. Manual episode mappings are now serialized to app storage, loaded before episode matching, and written through a synchronized AppGraph path so user-selected danmaku corrections survive app restarts.

This pass upgrades manual danmaku search. The player danmaku panel now exposes an editable anime-title query for Chinese names, original names, and aliases, and `DanmakuRegistry.searchCandidates()` searches providers with the entered title while keeping candidate selection wired to the persisted calibration flow.

This pass upgrades automatic danmaku title matching. `DanmakuRegistry` now expands automatic provider searches across Bangumi subject Chinese names, aliases, original names, and detail titles with CJK names prioritized and a small candidate cap, while explicit manual searches remain isolated to the user's entered query.

This pass upgrades automatic danmaku candidate ranking. Platform matches now combine provider base weight with title exactness, known anime-title alias normalization, episode-title similarity, and episode-order agreement so automatic loading is less likely to pick a loose high-platform result when a lower-base provider has the precise anime and episode mapping.

This pass upgrades danmaku episode-number parsing. The shared matcher now recognizes Arabic digits, `EP.07` style labels, and Chinese-number episode names such as `第十二话` or `第三集`, and the platform matcher uses that same parser for target episode detection, candidate scoring, and episode list ordering.

This pass upgrades danmaku matching cache behavior. `DanmakuRegistry` now keeps a bounded LRU cache for automatic candidate matches and coalesces concurrent provider searches, while timeline fetching still retries empty timelines so a stale missing danmaku body does not permanently block later playback.

This pass upgrades danmaku anti-occlusion. `DanmakuSafeArea` now carries a center exclusion band for play buttons and seek feedback, `DanmakuLayoutEngine` skips lanes that would cross that band, and the player shares the same computed safe-area state with both rendering and the danmaku settings summary.

This pass upgrades search result presentation. The all-sources search view now collapses normalized same-title hits to the strongest metadata-rich result while source filters continue to expose each provider's raw results for inspection.

This pass upgrades brand splash presentation. `BrandSplashUiState` now owns startup duration, headline/tagline/progress copy, readiness pills, poster-ribbon tiles, danmaku streaks, and signal rails so the launch animation follows the same tested model-driven contract as navigation and player panels.

This pass upgrades main app chrome navigation. `AppNavigationChromeUiState` now owns shared brand copy, selected-page summary, semantic tone, rail dimensions, bottom-bar dimensions, spacing, and corner radius so wide and compact top-level navigation follow the same tested shell contract.

This pass upgrades Discover content chrome. `HomeBrowseChromeUiState` now owns the home header copy, search entry copy, calendar action state, ordered category tabs, semantic tones, and tab/header dimensions so recommendation, schedule, and category browsing share one tested content-layout contract.

This pass upgrades the Discover watch hub. `HomeWatchHubUiState` now owns the Continue, Calendar, and Recommendation action-card copy, enabled state, semantic tone, weights, sizing, padding, and spacing so the home decision row is model-driven instead of Compose-local.

This pass upgrades the Discover spotlight carousel. `HomeSpotlightCarouselUiState` now owns visible item selection, stable-key deduplication, helper copy, focus labels, metadata chips, companion-card links, and carousel dimensions so the home hero rail is a tested content-layout model.

This pass upgrades Discover schedule presentation. `HomeScheduleDigestUiState`, `HomeScheduleHeroUiState`, and `HomeScheduleAnimeRowUiState` now own timetable digest copy, loading/error tone, metric chips, hero metadata, weekday-chip chrome, anime-row metadata, and layout dimensions so the anime schedule behaves like a product browsing surface instead of Compose-local cards.

This pass upgrades Discover home content modules. `HomeSectionHeaderUiState`, `HomeContinueWatchingUiState`, and `HomePosterRailUiState` now own section actions, continue-watching progress, source fallback subtitles, poster-rail deduplication, fallback content, source tones, and poster/card dimensions so the home feed reads as one tested product surface rather than separate Compose-local rows.

This pass upgrades category browse insight presentation. `CategoryBrowseUiState` now emits ordered metric tiles with semantic tones, sizing, padding, and spacing for coverage, rating, heat, and source summaries, so category channels behave like a tested browsing surface rather than four hand-built Compose cards.

This pass upgrades category browse list rows. `CategoryBrowseItemUiState` now owns source-aware tone, rating and heat chips, provider fallback subtitles, action copy, poster dimensions, row spacing, and card chrome so category channels no longer reuse timetable rows for ordinary browsing results.

This pass upgrades category browse list selection. `CategoryBrowseListUiState` now owns row selection, hero de-duplication, fallback recommendation rows, list headers, fallback actions, empty-state copy, and row spacing so category pages still show useful browse content when a direct category feed is empty.

This pass upgrades detail hero chrome. `DetailHeroChromeUiState` now owns title and summary fallback copy, poster metadata, selected-episode chips, episode-count chips, shell dimensions, poster sizing, overlay alpha, and action sizing so the detail landing area follows the same tested model contract as route readiness and first-play guidance.

This pass upgrades detail entry-card chrome. `DetailEntryUiState` now owns status tone, radius, padding, row spacing, icon-box sizing, chip spacing, and side-column dimensions so the search-to-detail handoff card follows the same model-driven presentation contract as the detail hero and playback readiness surfaces.

This pass upgrades the player first-frame loading overlay. `PlayerStartupOverlayUiState` now owns loading title, protocol/status metadata, progress tone, width, corner radius, border width, alpha, padding, spacing, and progress sizing so ordinary-stream startup presentation follows the same tested player-state contract as BT preparation, seek bars, and route diagnostics.

This pass upgrades BT playback preparation chrome. `TorrentPlaybackPreparationUiState` now owns content width, padding, spacing, progress tone, progress-track tone and alpha, title/description/line alpha, and error tone so edge-cache startup presentation follows the same model-driven player contract as ordinary-stream loading overlays and cache diagnostics.

This pass upgrades player bottom text-action chrome. `PlayerTextActionChromeUiState` now owns quick-action pill height, radius, padding, spacing, icon size, container tone and alpha, content tone and alpha, value alpha, and disabled presentation so fullscreen recovery, route, episode, cache, and settings shortcuts share a tested model-driven control contract.

This pass upgrades player circular-control chrome. `PlayerCircleButtonChromeUiState` now owns normal versus prominent button sizing, selected state, disabled state, container tone/base color and alpha, plus icon tone/base color and alpha so top-bar, center-play, seek, and fullscreen controls follow the same tested model-driven player-control contract.

This pass upgrades player edge-progress chrome. `PlayerEdgeProgressUiState` now owns clamped progress, unknown-duration fallback, edge-bar height, progress tone, track tone, and track alpha so the always-visible video progress line stays aligned with seek bar and compact progress models while minimizing overlay obstruction.

This pass upgrades player seek-feedback chrome. `PlayerSeekFeedbackUiState` now owns feedback text, placement, anti-obstruction margins, compact/fullscreen vertical offsets, radius, border, padding, icon visibility/size/tone, container alpha, and text alpha so double-tap seeking feedback stays predictable without covering the main video area.

This pass upgrades player top-status strip chrome. `PlayerTopStatusStripUiState` and `PlayerStatusChipUiState` now own strip height, chip spacing, end padding, chip width/height, radius, container alpha, border alpha, padding, label alpha, and value alpha so fullscreen playback metadata remains compact, scannable, and model-driven.

This pass upgrades player fullscreen-status strip chrome. `PlayerFullscreenStatusStripUiState` and `PlayerFullscreenStatusTagUiState` now own strip height, radius, container base color, alpha, border width/alpha, padding, spacing, status tone, route text alpha, and tag text alpha so bottom playback metadata follows the same tested model-driven contract as the top status chips.

This pass upgrades fullscreen danmaku input chrome. `PlayerFullscreenDanmakuInputUiState` now owns entry copy, toggle labels, enabled state, height, radius, container alpha, padding, spacing, icon/text/toggle tones, and alpha values so the fullscreen danmaku entry keeps the same model-driven control contract as compact playback, status strips, and player panels.

This pass upgrades fullscreen seek shortcut chrome. `PlayerFullscreenSeekClusterUiState` and `PlayerFullscreenSeekButtonUiState` now own 10-second backward/forward semantics, content descriptions, deltas, cluster sizing, border, divider, button sizing, icon sizing, and alpha values so fullscreen seek controls share the same tested model contract as double-tap seek feedback and bottom playback controls.

This pass upgrades compact fullscreen-entry chrome. `PlayerCompactFullscreenActionUiState` now owns the compact fullscreen button's content description, width, height, radius, icon size, selected/enabled state, container/content colors, and disabled alpha values so portrait playback keeps the compact control row model-driven instead of leaving the fullscreen action as a local Compose button.

This pass upgrades shared route/status badge chrome. `RouteStatusBadgeChromeUiState` now owns badge height, radius, container alpha, horizontal padding, and text alpha so detail cards, source chips, search results, and player panels share the same tested small-badge presentation contract instead of each depending on local Compose constants.

This pass upgrades route play-action pill chrome. `RoutePlayActionChromeUiState` now owns action-pill height, radius, container alpha, padding, spacing, icon size, icon alpha, and text alpha so route cards keep the primary play action aligned with the shared badge model and avoid local Compose constants.

This pass upgrades fullscreen lock-button chrome. `PlayerFullscreenLockButtonUiState` now owns locked/unlocked content descriptions, sizing, radius, icon size, container tone/base color, content tone/base color, and alpha values so the fullscreen anti-mistouch control stays model-driven alongside side-dock, seek, status, and danmaku controls.

This pass upgrades fullscreen route-status pill chrome. `PlayerRouteStatusUiState` now owns width bounds, height, radius, container base color, alpha, border width/alpha, spacing, indicator size/alpha, and route/status text alpha values so the top overlay's current-line feedback is model-driven alongside status chips, notices, and lock controls.

This pass upgrades player notice chrome. `PlayerNoticeUiState` now owns compact status-pill and fullscreen notice-strip sizing, radius, container base color, alpha, border width/alpha, padding, indicator size/alpha, text alpha, and layout weights so route notices and playback errors stay model-driven across compact and fullscreen overlays.

This pass upgrades fullscreen control-row layout chrome. `PlayerFullscreenControlRowUiState` now owns the fullscreen bottom overlay's vertical spacing, action-row spacing, danmaku input weight, and action-bar weight so status, danmaku, seek, and quick actions are composed by a tested layout contract instead of Compose-local constants.

This pass upgrades detail route-prefetch chip chrome. `RoutePrefetchItemUiState` now owns chip height, radius, container alpha, border width/alpha, padding, spacing, indicator size/alpha, title base color, title alpha, and status alpha so detail-page route warming and cache-hit feedback follows the same tested model contract as player route/status controls.

This pass upgrades detail route-metric chip chrome. `DetailRouteMetricUiState` now owns metric-chip height, radius, container base color, alpha, padding, spacing, label tone, label alpha, and value alpha so detail-page route diagnostics render from a tested model contract instead of Compose-local dimensions and opacity constants.

This pass upgrades detail route-prefetch card shell chrome. `RoutePrefetchUiState` now owns the route-warming card radius, container base color, alpha, border width/alpha, content padding/spacing, header spacing, icon-box sizing/radius/alpha, title and summary tones, progress track, and item spacing so adjacent-episode prefetch feedback is model-driven as a complete detail-page module.

This pass upgrades portrait playback action chrome. `PortraitWatchActionUiState` now owns action button height, radius, container alpha, padding, spacing, icon size/alpha, title base color, title alpha, and subtitle alpha so portrait detail-playback controls for episode and route entry points follow the same tested model contract as fullscreen player actions.

This pass upgrades portrait recovery action chrome. `PlayerActionUiState` now owns portrait recovery button height, radius, container base color, alpha, icon size, spacing, and content alpha so retry and next-route controls in portrait playback errors follow the same tested model contract as fullscreen and compact recovery actions.

This pass upgrades player route-source chip chrome. `PlayerRouteSourceChipUiState` now owns chip radius, border width, header spacing, title alpha, footer alpha, and the existing size/padding/container/detail controls so fullscreen route-source filtering keeps a tested model contract instead of relying on Compose-local chip constants.

This pass upgrades detail route-status card chrome. `DetailRouteStatusUiState` now owns card radius, border width, padding, compact/expanded spacing, header spacing, icon-box sizing, progress sizing, action height, and diagnostics spacing so route matching on the detail playback page follows the same tested model contract as player route-source controls.

This pass upgrades detail route-focus chip chrome. `DetailRouteFocusChipUiState` now owns minimum height, radius, container alpha, padding, spacing, label alpha, and value alpha so detail playback route diagnostics use the same tested model-driven presentation as the route-status card and fullscreen route-source filters.
