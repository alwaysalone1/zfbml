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

### v0.5.111

English:

- Manual danmaku episode mappings are now persisted to app storage through `DanmakuManualMappingStore` so user calibration survives app restarts.
- `AppGraph` now loads saved danmaku mappings before episode matching and writes candidate corrections through one synchronized mapping path before refreshing `DanmakuRegistry`.
- Added unit coverage for persisted mapping reloads, same-episode replacement, and missing/corrupt mapping files.
- App version labels, request user agents, and README notes are now updated to `0.5.111`.

### v0.5.110

English:

- Player danmaku settings now show the automatic match candidate list directly in the panel, including provider labels, platform badges, score copy, and calibrated/manual row state.
- Selecting a danmaku candidate now writes that match as the current episode's manual mapping, refreshes candidate state, and reloads the best timeline immediately.
- Added unit coverage for danmaku candidate row presentation, candidate ordering, platform badges, and calibrated candidate disablement.
- App version labels, request user agents, and README notes are now updated to `0.5.110`.

### v0.5.109

English:

- Player danmaku settings now expose automatic/manual mapping state through `PlayerDanmakuMappingUiState`, including candidate count, loaded timeline count, calibrated status, action labels, row tone, and badges.
- `PlayerScreen` now tracks danmaku candidate matches separately from loaded timeline items, refreshes candidates during episode load, and exposes a search/calibration entry from the danmaku panel.
- Added unit coverage for pending, automatic, manual, and loading danmaku mapping presentation states.
- App version labels, request user agents, and README notes are now updated to `0.5.109`.

### v0.5.108

English:

- Danmaku matching now has an explicit `DanmakuManualMapping` model for user-calibrated anime/episode to provider-token mappings.
- `DanmakuRegistry` now tries calibrated mappings before automatic provider search, falls back to automatic matching when a calibrated timeline is empty, and clears cached timelines when manual mappings are replaced.
- Added unit coverage for calibrated timeline priority, automatic fallback, and cache invalidation after manual correction.
- App version labels, request user agents, and README notes are now updated to `0.5.108`.

### v0.5.107

English:

- Player option-panel headers now carry layout metrics in `PlayerPanelSheetUiState`, including header spacing, title/subtitle spacing, and dismiss-button height.
- `PlayerPanelHeader` now renders header spacing and dismiss sizing from sheet state instead of Compose-local constants.
- Added unit coverage for header spacing and dismiss-button dimensions.
- App version labels, request user agents, and README notes are now updated to `0.5.107`.

### v0.5.106

English:

- Player option-panel context bars now carry layout metrics in `PlayerPanelContextUiState`, including corner radius, row padding, row spacing, icon-box size, icon size, icon corner radius, and text spacing.
- `PlayerPanelContextBar` now renders current playback context from state-owned layout values instead of Compose-local constants.
- Added unit coverage for context-bar dimensions, padding, and spacing.
- App version labels, request user agents, and README notes are now updated to `0.5.106`.

### v0.5.105

English:

- Player option-panel quick tabs now use `PlayerPanelTabStripUiState` plus per-tab layout fields for strip height, spacing, content padding, tab sizing, corner radius, icon size, and internal content spacing.
- `PlayerPanelQuickTabs` and `PlayerPanelQuickTab` now render the panel navigation strip from `PlayerPanelSheetUiState` instead of keeping navigation chrome constants in Compose.
- Added unit coverage for tab strip sizing and route-tab button presentation fields.
- App version labels, request user agents, and README notes are now updated to `0.5.105`.

### v0.5.104

English:

- Fullscreen side dock controls now use `PlayerFullscreenSideDockUiState` and `PlayerFullscreenDockActionUiState` for dock shell dimensions, action order, labels, enabled/selected state, tones, button sizing, and disabled alpha.
- `PlayerFullscreenSideDock` and `PlayerFullscreenDockButton` now render danmaku, quality, speed, episode, route, and more shortcuts from dock action state instead of building the dock button list locally in Compose.
- Added unit coverage for ordered dock actions, selected danmaku state, route/episode availability, and disabled dock action presentation.
- App version labels, request user agents, and README notes are now updated to `0.5.104`.

### v0.5.103

English:

- Compact playback recovery now uses `PlayerCompactRecoveryUiState` and `PlayerCompactRecoveryActionUiState` for issue visibility, retry/next-route actions, row height, spacing, button sizing, enabled state, and selected/disabled color alpha.
- `PlayerCompactRecoveryRow` and `PlayerTinyToggle` now render retry and fallback-route controls from recovery action state instead of hard-coding compact recovery chrome locally in Compose.
- Added unit coverage for hidden recovery, retry action presentation, fallback availability, and disabled fallback-route presentation.
- App version labels, request user agents, and README notes are now updated to `0.5.103`.

### v0.5.102

English:

- Compact player controls now use `PlayerCompactInteractionUiState`, `PlayerCompactProgressUiState`, and `PlayerCompactDanmakuUiState` for compact progress, danmaku entry copy, toggle labels, alpha, sizing, and tone semantics.
- `PlayerCompactInteractionRow`, `PlayerCompactDanmakuInputBar`, and `PlayerCompactProgressLine` now render from compact interaction state instead of deriving progress-line and danmaku-entry chrome locally in Compose.
- Added unit coverage for compact progress clamping plus enabled/disabled danmaku entry presentation fields.
- App version labels, request user agents, and README notes are now updated to `0.5.102`.

### v0.5.101

English:

- Player bottom seek bars now use `PlayerSeekBarUiState` for current/duration labels, seekability, pending seek clamping, compact progress fraction, time-label sizing/alpha, slider sizing, and track color semantics.
- `PlayerBottomControls` now renders known-duration sliders and unknown-duration loading tracks from that model instead of deriving progress labels and seek-bar chrome locally in Compose.
- Added unit coverage for seekable playback progress, pending seek clamping, unknown-duration fallback, and shared playback time formatting.
- App version labels, request user agents, and README notes are now updated to `0.5.101`.

### v0.5.100

English:

- Danmaku settings sliders now expose model-provided vertical spacing, slider height, title/value alpha, thumb/active-track tones, and inactive-track alpha through `PlayerDanmakuSliderUiState`.
- `PlayerSliderSetting` now renders density, opacity, and font-size controls from those slider presentation fields instead of keeping slider chrome constants locally in Compose.
- Added unit coverage for shared slider presentation fields and each danmaku slider's semantic color contract.
- App version labels, request user agents, and README notes are now updated to `0.5.100`.

### v0.5.99

English:

- Shared player selectable rows now use `PlayerSelectableRowUiState` for min height, radius, container/border tones and alpha, padding, spacing, icon sizing, subtitle tone, trailing alpha, and selected-check sizing.
- Danmaku toggle rows, quality rows, and speed rows now carry that shared row presentation state from their UI models instead of letting `PlayerSelectableRow` derive row chrome locally in Compose.
- Added unit coverage for selected and unselected shared selectable-row presentation across danmaku, quality, and speed panels.
- App version labels, request user agents, and README notes are now updated to `0.5.99`.

### v0.5.98

English:

- Player episode rows now expose model-provided container alpha, disabled alpha, border alpha, row spacing, rail dimensions, index-box dimensions, loading indicator size, subtitle tone, and action-label dimensions through `PlayerEpisodeOptionUiState`.
- `PlayerEpisodeOptionRow` and `EpisodeActionLabel` now render those fields instead of hard-coding episode-row shell, index box, loading indicator, subtitle, and action-chip presentation locally in Compose.
- Added unit coverage for current, playable, loading, and disabled episode-row presentation fields.
- App version labels, request user agents, and README notes are now updated to `0.5.98`.

### v0.5.97

English:

- Player episode panels now expose model-provided list spacing, list-title alpha, summary-card alpha/border/radius, summary icon tone/size/container alpha, summary text spacing, chip spacing, helper copy, and helper alpha through `PlayerEpisodePanelUiState`.
- `PlayerEpisodePanel` and `PlayerEpisodeSummaryCard` now render those fields instead of hard-coding episode summary card and list-title presentation locally in Compose.
- Added unit coverage for episode-panel summary shell, list title, icon, helper text, and spacing presentation fields.
- App version labels, request user agents, and README notes are now updated to `0.5.97`.

### v0.5.96

English:

- Player route summary cards now expose model-provided card alpha, border alpha, corner radius, content spacing, icon tone/size, summary tone, selected-route summary alpha, detailed-toggle labels, toggle dimensions, metric spacing, and notice presentation through `RoutePanelUiState`.
- Route-panel metric chips now expose height, radius, container alpha, padding, spacing, and label alpha through `RoutePanelMetricUiState`.
- `RoutePanelSummaryCard` and `RoutePanelMetricChip` now render those model fields instead of hard-coding route summary card and metric chip presentation locally in Compose.
- Added unit coverage for route summary card shell, toggle, notice, and metric-chip presentation fields.
- App version labels, request user agents, and README notes are now updated to `0.5.96`.

### v0.5.95

English:

- Player route rows now expose model-provided container alpha, disabled alpha, border tone/alpha, row spacing, rail dimensions, subtitle tones, and action-label dimensions through `RouteCandidateUiState`.
- `PlayerRouteOptionRow` and `PlayerRouteActionLabel` now render those route-row fields instead of hard-coding row shell, rail, subtitle, and action-chip presentation locally in Compose.
- Added unit coverage for route-row presentation fields across recommended, current, web-only, and failed route states.
- App version labels, request user agents, and README notes are now updated to `0.5.95`.

### v0.5.94

English:

- Player route-source filtering now uses `PlayerRouteSourceStripUiState` and `PlayerRouteSourceChipUiState` for source strip visibility, titles, visible routes, chip sizing, emphasis, and detailed failure presentation.
- `PlayerRoutePanel` now renders the route-source strip and filtered route-list title from model state instead of deriving source grouping, chip dimensions, and title copy locally in Compose.
- Added unit coverage for hidden single-source strips, compact multi-source strips, detailed source filters, and failed-source chip presentation.
- App version labels, request user agents, and README notes are now updated to `0.5.94`.

### v0.5.93

English:

- Player option-panel shell layout now uses `PlayerPanelShellUiState` for landscape/portrait sizing, scrim alpha, panel surface color, border alpha, content spacing, and corner radii.
- `PlayerOptionPanel` now renders those shell fields instead of deriving panel width, height, shape, scrim, padding, and spacing locally in Compose.
- Added unit coverage for landscape, compact landscape, portrait, and compact portrait shell layout parameters.
- App version labels, request user agents, and README notes are now updated to `0.5.93`.

### v0.5.92

English:

- Player panel headers now expose model-provided dismiss labels plus title, subtitle, and dismiss-action alpha through `PlayerPanelSheetUiState`.
- `PlayerPanelHeader` now renders the sheet model directly instead of hard-coding header copy and opacity inside Compose.
- Added unit coverage for player-panel header copy and alpha payloads.
- App version labels, request user agents, and README notes are now updated to `0.5.92`.

### v0.5.91

English:

- Player panel context bars now expose model-provided status tone, icon tone, container/border alpha, icon-container alpha, title alpha, and metadata alpha through `PlayerPanelContextUiState`.
- `PlayerPanelContextBar` now renders those model fields instead of hard-coding playback context colors inside Compose.
- Added unit coverage for playback context tone and alpha payloads.
- App version labels, request user agents, and README notes are now updated to `0.5.91`.

### v0.5.90

English:

- Player panel quick tabs now expose model-provided action enablement, prominent state, visual tone, container alpha, content alpha, and value alpha through `PlayerPanelTabUiState`.
- `PlayerPanelQuickTab` now renders those tab fields instead of deriving selected, highlighted, and disabled presentation locally in Compose.
- Added unit coverage for selected, highlighted, enabled, and disabled player-panel tab presentation state.
- App version labels, request user agents, and README notes are now updated to `0.5.90`.

### v0.5.89

English:

- Player more-panel actions now expose model-provided highlighted/prominent state, action enablement, tile alpha, container/border alpha, icon alpha, and text alpha through `PlayerMoreActionUiState`.
- `PlayerMoreActionTile` now renders those model fields instead of deriving selected and disabled presentation locally in Compose.
- Added unit coverage for selected, enabled, and disabled more-action tile presentation state.
- App version labels, request user agents, and README notes are now updated to `0.5.89`.

### v0.5.88

English:

- Danmaku density, alpha, and font-size controls now use `PlayerDanmakuSliderUiState` for title, formatted value, clamped value, range, steps, and semantic tone.
- `PlayerSliderSetting` now renders those slider models instead of receiving hard-coded danmaku slider configuration from Compose.
- Added unit coverage for danmaku slider titles, ranges, steps, clamped values, and tones.
- App version labels, request user agents, and README notes are now updated to `0.5.88`.

### v0.5.87

English:

- Danmaku settings now expose model-provided toggle action labels, status badges, highlighted/prominent row state, and icon/text alpha through `PlayerDanmakuSettingsUiState`.
- The player danmaku toggle row now renders those model fields instead of relying on default selected-state styling in `PlayerSelectableRow`.
- Added unit coverage for enabled and hidden danmaku toggle presentation state.
- App version labels, request user agents, and README notes are now updated to `0.5.87`.

### v0.5.86

English:

- Player quality and speed options now expose model-provided badges, highlighted/prominent row state, action enablement, and icon/text alpha through their option UI states.
- `PlayerSelectableRow` can now render option badges, model-provided emphasis, and tone-aware action labels for quality and speed panels.
- Added unit coverage for quality and speed option row presentation state.
- App version labels, request user agents, and README notes are now updated to `0.5.86`.

### v0.5.85

English:

- Player episode rows now expose model-provided compact episode labels, badges, highlighted/prominent state, action enablement, and text/rail alpha through `PlayerEpisodeOptionUiState`.
- `PlayerEpisodeOptionRow` now renders those model fields instead of deriving selected/loading/enabled row presentation inside Compose.
- Added unit coverage for current, loading, playable, and waiting episode-row presentation state.
- App version labels, request user agents, and README notes are now updated to `0.5.85`.

### v0.5.84

English:

- `RouteCandidateUiState` now owns player-row enablement, prominent/highlighted row states, compact/detailed titles, subtitles, and detail line copy for route options.
- `PlayerRouteOptionRow` now renders those model fields instead of deriving enabled state, highlighted borders, and compact/detailed titles in Compose.
- Added unit coverage for route candidate enablement, row emphasis, highlight state, and compact/detailed title payloads.
- App version labels, request user agents, and README notes are now updated to `0.5.84`.

### v0.5.83

English:

- Route candidate cards now expose model-provided badge chips through `RouteCandidateUiState`, covering recommended, current, failed, fallback, and online-playable route states.
- Detail route candidate rows and player route option rows now render the same badge list instead of deriving recommended/status badges inside Compose.
- Added unit coverage for recommended online, current playback, and failed route candidate badges.
- App version labels, request user agents, and README notes are now updated to `0.5.83`.

### v0.5.82

English:

- Detail playback readiness now exposes model-provided semantic tone, cache tone, and readiness chips for route, online coverage, backup, and cache status.
- `DetailPlaybackReadinessStrip` now renders readiness chips from `DetailPlaybackReadinessUiState` instead of deriving accent color and cache-chip color in Compose.
- Added unit coverage for readiness tones, cache tones, and chip payloads across playable and loading states.
- App version labels, request user agents, and README notes are now updated to `0.5.82`.

### v0.5.81

English:

- Detail route prefetch cards now expose model-provided tone, progress visibility, badge label, item status labels, and item tones through `RoutePrefetchUiState` and `RoutePrefetchItemUiState`.
- `DetailRoutePrefetchCard` and `DetailRoutePrefetchChip` no longer derive Warming/Ready/Empty/Queued labels or accent colors inside Compose.
- Added unit coverage for prefetch card tones, progress state, badge labels, and per-episode prefetch chip labels.
- App version labels, request user agents, and README notes are now updated to `0.5.81`.

### v0.5.80

English:

- Detail route source selection now has `DetailRouteSourceSelectorUiState`, `DetailRouteSourceAutoChoiceUiState`, and `DetailRouteSourceStatusPillUiState`, centralizing the route-source selector header, recommended/current source pills, auto-best card, action label, and source badges.
- `RouteSourceSelector`, `RouteSourceSelectorHeader`, `RouteSourceAutoChoiceCard`, and `RouteSourceStatusPill` now render model-provided state instead of assembling source-count and auto-best copy in Compose.
- Added unit coverage for auto-best source selection and manual source-selection states.
- App version labels, request user agents, and README notes are now updated to `0.5.80`.

### v0.5.79

English:

- Detail route status cards now have `DetailRouteStatusUiState`, `DetailRouteRecommendationUiState`, `DetailRouteFocusChipUiState`, and `DetailRouteMetricUiState`, centralizing compact/expanded route status, recommendation copy, diagnostics, and metrics.
- `DetailRouteStatusCard`, `RouteRecommendationBand`, and route diagnostic rows now render model-provided state instead of deriving titles, subtitles, action labels, source chips, and route metrics in Compose.
- Added unit coverage for ready compact route cards, loading diagnostics, and failed-route error states.
- App version labels, request user agents, and README notes are now updated to `0.5.79`.

### v0.5.78

English:

- Detail first-play guidance now has `DetailFirstPlayUiState` and `DetailFirstPlayChipUiState`, centralizing the title, decision copy, action label, progress/icon state, tone, and decision chips.
- `DetailFirstPlayStrip` now renders model-provided state instead of deriving ready/loading/failed/empty labels and quality chips in Compose.
- Added unit coverage for ready recommendations, loading route matching, and empty-route first-play guidance.
- App version labels, request user agents, and README notes are now updated to `0.5.78`.

### v0.5.77

English:

- Detail hero actions now have `DetailHeroActionUiState`, centralizing the primary play button label and route-entry title/value/tone.
- `DetailHero` and `DetailRouteEntryButton` no longer derive play and route-entry labels directly from `RouteUiState` inside Compose.
- Added unit coverage for playable multi-source routes, loading route matching, and failed route matching.
- App version labels, request user agents, and README notes are now updated to `0.5.77`.

### v0.5.76

English:

- Detail episode cards now have `DetailEpisodeOptionUiState`, centralizing compact episode labels, selected state, route-matching copy, action text, and semantic tone.
- The detail episode rail now renders model-provided episode card state instead of formatting episode numbers and route-matching prompts inside Compose.
- Added unit coverage for selected, pending, and special-episode fallback states.
- App version labels, request user agents, and README notes are now updated to `0.5.76`.

### v0.5.75

English:

- Detail route resolution now has `DetailRouteResolutionUiState`, centralizing loading, failed, empty, ready, and idle route-matching panel copy.
- The expanded detail route area now renders one shared resolution panel instead of separate hard-coded loading, error, and empty-route Compose branches.
- Added unit coverage for loading preparation, failed route matching, and empty route states.
- App version labels, request user agents, and README notes are now updated to `0.5.75`.

### v0.5.74

English:

- Search results now have `SearchResultsSectionUiState`, centralizing the results header and empty-state copy for idle, loading, filtered, no-hit, and partial-failure cases.
- `ResultsHeader` and `EmptySearchState` no longer assemble search-result explanations in Compose; they render model-provided title and subtitle fields.
- Added unit coverage for loading, selected-source filtering, filtered empty results, and no-hit searches with failed sources.
- App version labels, request user agents, and README notes are now updated to `0.5.74`.

### v0.5.73

English:

- Search idle guidance now has `SearchIdleHintUiState`, summarizing searchable source coverage, schedule suggestions, and index health before the user types.
- `SearchHintPanel` no longer hard-codes the pre-search explanation inside Compose; it renders model-provided title, subtitle, action copy, and status chips.
- Added unit coverage for schedule-backed idle hints and the no-search-source fallback.
- App version labels, request user agents, and README notes are now updated to `0.5.73`.

### v0.5.72

中文：
- BT 播放准备占位页新增 `TorrentPlaybackPreparationUiState`，统一生成准备标题、说明、起播进度、状态、元数据/播放通道、整体进度、连接、文件和错误信息。
- `TorrentPlaceholderSurface` 不再在 Compose 内拼接 BT 起播缓冲、文件大小、下载速度、连接节点和错误文案，UI 只负责进度条和文本排版。
- 起播百分比、字节大小、速度和无 plan 兜底现在由模型层统一格式化，便于后续把 BT 边下边播升级成可复用的缓存/播放状态面板。
- 新增单元测试覆盖完整 BT plan、起播缓存、连接速度、文件信息，以及无 plan/error 兜底。
- App 内版本号、请求 UA 和 README 同步到 `0.5.72`。

English:

- The BT playback preparation placeholder now has `TorrentPlaybackPreparationUiState` for preparation title, description, startup progress, status, metadata/playback-channel readiness, overall progress, connection, file, and error information.
- `TorrentPlaceholderSurface` no longer assembles BT startup buffering, file size, download speed, peer counts, or error copy inside Compose; UI only handles the progress bar and text layout.
- Startup percentages, byte sizes, speed, and no-plan fallback are now formatted by the model layer, preparing the BT edge-cache path for a reusable cache/playback status panel.
- Added unit coverage for a complete BT plan, startup buffer, connection speed, file information, and no-plan/error fallback.
- App version labels, request user agents, and README notes are now updated to `0.5.72`.

### v0.5.71

中文：
- 竖屏线路洞察新增 `PortraitRouteInsightUiState` 和 `PortraitRouteInsightChipUiState`，统一生成覆盖、在线、备用和当前线路 chips。
- `PortraitRouteInsightRow` 不再在 Compose 内统计在线源、BT 备用、线路覆盖和当前协议/清晰度，UI 只负责横向排版和 tone 颜色映射。
- 空线路、待匹配、自动备用和当前协议兜底现在由模型层统一处理，竖屏诊断区域与全屏线路状态继续共享同一线路语义。
- 新增单元测试覆盖多源多线路统计、BT 备用、当前清晰度、空线路和 DASH 协议兜底。
- App 内版本号、请求 UA 和 README 同步到 `0.5.71`。

English:

- The portrait route insight row now has `PortraitRouteInsightUiState` and `PortraitRouteInsightChipUiState` for coverage, online, backup, and current-route chips.
- `PortraitRouteInsightRow` no longer counts online sources, BT fallback routes, route coverage, or current protocol/quality inside Compose; UI only handles horizontal layout and tone-to-color mapping.
- Empty routes, pending match, automatic backup fallback, and current protocol fallback now come from the model layer, keeping portrait diagnostics aligned with fullscreen route semantics.
- Added unit coverage for multi-source/multi-route counts, BT fallback, current quality, empty routes, and DASH protocol fallback.
- App version labels, request user agents, and README notes are now updated to `0.5.71`.

### v0.5.70

中文：
- 竖屏播放异常恢复条新增 `PortraitRecoveryActionsUiState`，复用 `PlayerActionKind.Retry` 和 `PlayerActionKind.NextRoute` 生成重试与换源动作。
- `PortraitWatchInfoPanel` 不再在 Compose 内硬编码 `重试当前`、`换个源`、可用态和颜色判断，UI 只负责图标、颜色映射和回调分发。
- 竖屏恢复动作与全屏底部操作栏共享同一动作语义，播放异常时的重试优先、换源可用态和无更多源兜底更一致。
- 新增单元测试覆盖无异常隐藏、异常时动作顺序、可换源状态、无更多线路禁用和 muted tone。
- App 内版本号、请求 UA 和 README 同步到 `0.5.70`。

English:

- The portrait playback recovery strip now has `PortraitRecoveryActionsUiState`, reusing `PlayerActionKind.Retry` and `PlayerActionKind.NextRoute` for retry and fallback-route actions.
- `PortraitWatchInfoPanel` no longer hard-codes `重试当前`, `换个源`, availability, or color branching inside Compose; UI only maps icons, colors, and callbacks.
- Portrait recovery actions now share action semantics with the fullscreen bottom action bar, keeping retry priority, fallback availability, and no-more-route fallback consistent.
- Added unit coverage for hidden non-issue state, playback-issue action order, available fallback route, disabled no-more-route state, and muted tone.
- App version labels, request user agents, and README notes are now updated to `0.5.70`.

### v0.5.69

中文：
- 全屏播放器顶部信息条新增 `PlayerTopOverlayUiState`，统一生成标题、副标题、紧凑通知、线路状态和顶部状态 chips。
- `PlayerTopOverlay` 不再在 Compose 内拼接 `episodeTitle · playbackState`，调用处也不再拼接 `3/12`、`第 3 集` 或 `当前集` 等集数值。
- 顶部栏的紧凑提示、线路 pill 和状态条现在共享同一个模型入口，继续减少播放器控制层的重复状态组装。
- 新增单元测试覆盖顶部栏标题/副标题、线路状态、`3/12` 集数 chip、倍速 chip，以及空标题和切源提示兜底。
- App 内版本号、请求 UA 和 README 同步到 `0.5.69`。

English:

- The fullscreen player top overlay now has `PlayerTopOverlayUiState` for title, subtitle, compact notice, route status, and top status chips.
- `PlayerTopOverlay` no longer assembles `episodeTitle · playbackState` inside Compose, and the call site no longer assembles `3/12`, `第 3 集`, or `当前集` episode values.
- The compact notice, route pill, and top status strip now share one model entry point, further reducing duplicated player-control state assembly.
- Added unit coverage for top-overlay title/subtitle, route state, the `3/12` episode chip, speed chip, and blank title plus route-notice fallbacks.
- App version labels, request user agents, and README notes are now updated to `0.5.69`.

### v0.5.68

中文：
- 竖屏选集横栏新增 `PortraitEpisodeRailUiState`、`PortraitEpisodeRailItemUiState` 和 `PortraitEpisodeMoreActionUiState`，统一生成横栏标题、全部入口、可见集数窗口、集数卡片标签和更多入口。
- `PortraitWatchInfoPanel` 不再在 Compose 内计算可见选集窗口、加载中文案、禁用态、选中态和 `02` / `SP` 集数标签，竖屏播放页继续向模型驱动播放器靠拢。
- 选集切换中时，横栏卡片的加载、禁用和 tone 状态由模型层生成，UI 只负责颜色映射和点击路由。
- 新增单元测试覆盖长列表窗口裁剪、加载中禁用态、更多入口，以及单集内容隐藏横栏的兜底。
- App 内版本号、请求 UA 和 README 同步到 `0.5.68`。

English:

- The portrait episode rail now has `PortraitEpisodeRailUiState`, `PortraitEpisodeRailItemUiState`, and `PortraitEpisodeMoreActionUiState` for the rail title, all-episodes entry, visible episode window, card labels, and more entry.
- `PortraitWatchInfoPanel` no longer calculates visible episode windows, loading copy, disabled state, selection state, or `02` / `SP` labels inside Compose, moving portrait playback further toward model-driven player UI.
- During episode switching, card loading, disabled, and tone state now come from the model layer, while UI only maps color and click routing.
- Added unit coverage for long-list windowing, loading disabled state, the more entry, and the single-episode hidden rail fallback.
- App version labels, request user agents, and README notes are now updated to `0.5.68`.

### v0.5.67

中文：
- 竖屏播放信息面板新增 `PortraitWatchInfoUiState` 和 `PortraitWatchActionUiState`，统一生成标题、当前集标签、meta chips、播放摘要、面板入口和诊断提示。
- `PortraitWatchInfoPanel` 不再在 Compose 内拼接清晰度、播放状态、来源、选集/换源按钮值和错误提示，UI 只负责图标、颜色和点击路由。
- 播放异常、切源提示和空标题/空集标题兜底现在复用播放器通知状态，竖屏信息面板与全屏通知条保持同一诊断语义。
- 新增单元测试覆盖多集多源摘要、面板动作、线路覆盖标签，以及空标题和错误状态兜底。
- App 内版本号、请求 UA 和 README 同步到 `0.5.67`。

English:

- The portrait watch info panel now has `PortraitWatchInfoUiState` and `PortraitWatchActionUiState` for title, current-episode labels, meta chips, playback summary, panel actions, and diagnostics.
- `PortraitWatchInfoPanel` no longer assembles quality, playback state, source, episode/route action values, or error copy inside Compose; UI only maps icons, colors, and click routing.
- Playback issues, route notices, and blank title/episode fallbacks now reuse the player notice state, keeping portrait diagnostics aligned with fullscreen notices.
- Added unit coverage for multi-episode/multi-route summaries, panel actions, route coverage labels, and blank/error fallbacks.
- App version labels, request user agents, and README notes are now updated to `0.5.67`.

### v0.5.66

中文：
- 播放器通知与线路状态新增 `PlayerNoticeUiState` 和 `PlayerRouteStatusUiState`，统一生成顶部紧凑提示、顶部线路状态和全屏底部通知条的语义。
- 播放错误、切源提示和普通线路状态的优先级、错误态、标题、提示内容和 tone 改由模型层生成，UI 只负责颜色映射与排版。
- 顶部线路状态不再在 Compose 内判断错误/切源/正常状态；底部通知条也不再直接拼接 `routeNotice` 和 `errorMessage`。
- 新增单元测试覆盖错误优先级、切源提示优先于错误消息、空线路标题兜底，以及线路状态 tone 映射。
- App 内版本号、请求 UA 和 README 同步到 `0.5.66`。

English:

- Player notices and route status now have `PlayerNoticeUiState` and `PlayerRouteStatusUiState` for compact top notices, top route status, and fullscreen bottom notices.
- Playback errors, route-switch notices, and normal route states now get priority, error state, title, message, and tone from the model layer, while Compose only maps color and layout.
- The top route status no longer branches on error/notice/normal state in Compose, and the bottom notice strip no longer assembles `routeNotice` and `errorMessage` directly.
- Added unit coverage for error priority, route notices winning over error messages, empty route-title fallback, and route-status tone mapping.
- App version labels, request user agents, and README notes are now updated to `0.5.66`.

### v0.5.65

中文：
- 播放器底部操作栏新增 `PlayerActionBarUiState`、`PlayerActionUiState` 和 `PlayerActionKind`，统一生成重试、换源、清晰度、倍速、选集、下一集、缓存和更多入口。
- 操作栏的播放异常优先动作、下一线路可用态、当前面板选中态、单集/单线路禁用态、缓存可用态和倍速文案改由模型层生成。
- `PlayerActionBar` 现在只负责图标、点击路由和排版，和播放器面板壳层、状态条、更多设置面板保持同一模型驱动控制体系。
- 新增单元测试覆盖常规播放动作顺序、倍速/线路/下一集/缓存状态，以及播放异常下的重试和换源兜底。
- App 内版本号、请求 UA 和 README 同步到 `0.5.65`。

English:

- The player bottom action bar now has `PlayerActionBarUiState`, `PlayerActionUiState`, and `PlayerActionKind` for retry, route fallback, quality, speed, episode, next-episode, cache, and more actions.
- Playback issue priority actions, next-route availability, active-panel selection, single-episode/single-route disabled state, cache availability, and speed copy now come from the model layer.
- `PlayerActionBar` now only handles icons, click routing, and layout, aligning it with the model-driven panel shell, status strips, and more/settings panel.
- Added unit coverage for default action order, speed/route/next-episode/cache state, and retry/fallback actions during playback issues.
- App version labels, request user agents, and README notes are now updated to `0.5.65`.

### v0.5.64

中文：
- 播放器状态条新增 `PlayerTopStatusStripUiState`、`PlayerStatusChipUiState` 和 `PlayerFullscreenStatusStripUiState`，统一生成顶部状态 chip 与全屏播放状态条。
- 顶部状态条的本集、来源、清晰度和倍速文案改由模型层生成，线路覆盖和空来源/空清晰度归一化不再散在 Compose 中。
- 全屏状态条的播放/异常状态、线路摘要、清晰度、倍速、线路覆盖和集数标签改由模型层生成，异常态只由 UI 映射颜色。
- 新增单元测试覆盖多线路状态 chip、倍速格式、空值兜底和播放异常状态条。
- App 内版本号、请求 UA 和 README 同步到 `0.5.64`。

English:

- The player status strips now have `PlayerTopStatusStripUiState`, `PlayerStatusChipUiState`, and `PlayerFullscreenStatusStripUiState` for top chips and fullscreen playback status.
- Top status chips for episode, source, quality, and speed now come from the model layer, including route coverage and blank source/quality normalization.
- The fullscreen status strip now receives playback/error state, route summary, quality, speed, route coverage, and episode-count tags from the model layer, while Compose only maps presentation color.
- Added unit coverage for multi-route chips, speed formatting, fallback values, and playback issue status.
- App version labels, request user agents, and README notes are now updated to `0.5.64`.

### v0.5.63

中文：
- 播放器面板壳层新增 `PlayerPanelSheetUiState`、`PlayerPanelContextUiState`、`PlayerPanelTabUiState` 和 `PlayerPanelKind`，统一生成标题、副标题、当前播放上下文和快捷 tab。
- 面板顶部上下文条改为消费模型字段，当前集、当前源、清晰度、倍速和播放状态不再在 Compose 中拼接。
- 快捷 tab 的顺序、值、禁用态、当前选中态和弹幕高亮态由模型层统一管理，和更多设置、清晰度、倍速、换源、选集面板保持同一状态合同。
- 新增单元测试覆盖播放源面板、多线路多集、弹幕开启、单线路、单集和空标题/空质量归一化。
- App 内版本号、请求 UA 和 README 同步到 `0.5.63`。

English:

- The player panel shell now has `PlayerPanelSheetUiState`, `PlayerPanelContextUiState`, `PlayerPanelTabUiState`, and `PlayerPanelKind` for headers, playback context, and quick tabs.
- The panel context bar now renders model fields for episode, source, quality, speed, and playback status instead of composing them in Compose.
- Quick-tab order, values, disabled state, selected state, and danmaku highlighting now come from the model layer, matching the specialized player panels.
- Added unit coverage for route panels, multi-route/multi-episode playback, danmaku-on state, single-route/single-episode edges, and blank title/quality normalization.
- App version labels, request user agents, and README notes are now updated to `0.5.63`.

### v0.5.62

中文：
- 播放器更多设置新增 `PlayerMorePanelUiState`、`PlayerMoreActionUiState` 和 `PlayerMoreActionKind`，统一生成当前设置摘要和 6 个快捷入口。
- 更多设置面板改为消费模型字段，清晰度、倍速、选集、换源、弹幕和缓存入口的启用态、选中态、说明文案不再散在 Compose 里。
- 摘要卡统一显示当前源、线路覆盖、集数、清晰度和倍速，空质量、单集、单线路等边界状态由模型层归一化。
- 新增单元测试覆盖多线路多集、弹幕开启、可缓存，以及单线路、无选集、弹幕关闭、不可缓存状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.62`。

English:

- The player more/settings panel now has `PlayerMorePanelUiState`, `PlayerMoreActionUiState`, and `PlayerMoreActionKind` for its summary and six quick actions.
- The panel now renders model fields for quality, speed, episode, route, danmaku, and cache actions, keeping enabled, selected, and helper copy out of Compose.
- The summary card now normalizes current source, route coverage, episode count, quality, and speed, including blank quality, single-episode, and single-route edge cases.
- Added unit coverage for multi-route/multi-episode/danmaku/cacheable states and unavailable single-route/cache-blocked states.
- App version labels, request user agents, and README notes are now updated to `0.5.62`.

### v0.5.61

中文：
- 播放器倍速面板新增 `PlayerSpeedPanelUiState` 和 `PlayerSpeedOptionUiState`，统一生成当前倍速摘要、可选档位、说明和动作文案。
- 倍速面板改为消费模型字段，`标准速度`、`慢速回看`、`快速播放`、`使用中/切换` 不再散在 Compose 里。
- 倍速格式化新增 `formatPlaybackSpeedForUi`，顶部状态、面板和更多菜单继续共享同一显示格式。
- 新增单元测试覆盖倍速列表去重排序、当前倍速选中、说明文案和动作语义。
- App 内版本号、请求 UA 和 README 同步到 `0.5.61`。

English:

- The player speed panel now has `PlayerSpeedPanelUiState` and `PlayerSpeedOptionUiState` for current-speed summary, available options, helper copy, and action labels.
- The speed panel now renders model fields, keeping standard/slow/fast labels and use/switch copy out of Compose.
- `formatPlaybackSpeedForUi` centralizes speed formatting for top status, panels, and more-menu copy.
- Added unit coverage for de-duplicated sorted speed options, selected speed, helper copy, and action semantics.
- App version labels, request user agents, and README notes are now updated to `0.5.61`.

### v0.5.60

中文：
- 播放器清晰度面板新增 `PlayerQualityPanelUiState` 和 `PlayerQualityOptionUiState`，统一生成当前画质、可选档位、来源说明和动作文案。
- 清晰度面板保留同画质取最高分线路的策略，但选中态、空状态和 `使用中/切换` 动作改由模型生成。
- 画质标签新增 `routeQualityLabelForUi`，自动画质统一显示为 `自动`，减少播放器内多套画质标签逻辑。
- 新增单元测试覆盖同画质去重、当前画质选中、BT 画质语义和空清晰度面板。
- App 内版本号、请求 UA 和 README 同步到 `0.5.60`。

English:

- The player quality panel now has `PlayerQualityPanelUiState` and `PlayerQualityOptionUiState` for current quality, available options, source copy, and action labels.
- The panel keeps the best-scored route per quality bucket while moving selected state, empty state, and use/switch copy into the model.
- `routeQualityLabelForUi` now normalizes quality labels, including `auto` to `自动`, reducing duplicated player quality logic.
- Added unit coverage for quality de-duplication, current-quality selection, BT quality semantics, and empty quality panels.
- App version labels, request user agents, and README notes are now updated to `0.5.60`.

### v0.5.59

中文：
- 播放器弹幕设置新增 `PlayerDanmakuSettingsUiState`，统一生成开关标题、开关说明、密度、透明度、字号和防遮挡摘要。
- 弹幕设置面板改为消费模型字段，`弹幕已开启/已关闭`、密度百分比、透明度和字号格式不再散在 Compose 里。
- 弹幕设置新增安全区摘要，显示当前自动避让播放器控制区的策略。
- 新增单元测试覆盖开关状态、滑杆标签格式和安全区摘要。
- App 内版本号、请求 UA 和 README 同步到 `0.5.59`。

English:

- The player danmaku settings panel now has `PlayerDanmakuSettingsUiState` for toggle title, toggle copy, density, alpha, font scale, and anti-obstruction summary.
- The settings panel now renders model fields, keeping enabled/disabled copy and percent/scale formatting out of Compose.
- Danmaku settings now show a safe-area summary for automatic control avoidance.
- Added unit coverage for toggle state, slider labels, and safe-area summary.
- App version labels, request user agents, and README notes are now updated to `0.5.59`.

### v0.5.58

中文：
- 播放器选集面板新增 `PlayerEpisodePanelUiState` 和 `PlayerEpisodeOptionUiState`，统一生成当前集、总集数、加载中目标、选集行状态和动作文案。
- 播放器选集摘要卡和选集行改为消费模型字段，正在看、自动匹配、切换中、播放中、等待等状态不再散在 Compose 里。
- 切换选集时，非目标剧集会进入等待状态，加载目标显示 `加载中`，当前集显示 `播放中`。
- 新增单元测试覆盖当前集、加载中目标、等待项和空选集面板。
- App 内版本号、请求 UA 和 README 同步到 `0.5.58`。

English:

- The player episode panel now has `PlayerEpisodePanelUiState` and `PlayerEpisodeOptionUiState` for current episode, total count, loading target, row status, and action copy.
- The episode summary card and rows now render model fields, keeping watching, auto-match, switching, playing, and waiting states out of Compose.
- During episode switching, non-target rows wait, the target row shows loading, and the current row shows playing.
- Added unit coverage for current episode, loading target, waiting rows, and empty episode panels.
- App version labels, request user agents, and README notes are now updated to `0.5.58`.

### v0.5.57

中文：
- `RoutePanelUiState` 新增播放器换源摘要标题、简单/详细摘要、当前线路说明和简单/详细指标列表。
- 播放器换源摘要卡改为消费模型字段，推荐源、当前源、可用/在线/BT/失败统计不再由 Compose 本地拼接。
- 指标 chip 新增 `RoutePanelMetricUiState`，统一用 `SourceLibraryTone` 映射颜色，和来源组/候选线路模型保持一致。
- 新增单元测试覆盖线路摘要标题、推荐摘要、当前线路说明和指标语义色。
- App 内版本号、请求 UA 和 README 同步到 `0.5.57`。

English:

- `RoutePanelUiState` now exposes player route-panel titles, compact/detailed summaries, current-route copy, and compact/detailed metric lists.
- The player route summary card now renders model fields instead of composing recommendation, current-source, available, online, BT, and failure stats locally.
- Metric chips now use `RoutePanelMetricUiState` with `SourceLibraryTone`, keeping color semantics aligned with source-group and route-candidate models.
- Added unit coverage for route-summary titles, recommendation copy, current-route copy, and metric tones.
- App version labels, request user agents, and README notes are now updated to `0.5.57`.

### v0.5.56

中文：
- `RouteSourceGroupUiState` 新增来源组状态标签、语义色、线路数标签、详细统计和底部动作文案。
- 详情页播放方案筛选和播放器换源来源条改为消费同一来源组模型，已选、当前、推荐、全部来源和失败降级语义保持一致。
- 来源筛选 UI 移除本地颜色/文案推导，后续详情页和播放器不会再各自维护一套来源状态分支。
- 新增单元测试覆盖已选来源、推荐来源、全部来源、当前播放来源和详细统计文案。
- App 内版本号、请求 UA 和 README 同步到 `0.5.56`。

English:

- `RouteSourceGroupUiState` now exposes source-group status labels, semantic tone, route-count labels, detailed stats, and footer action copy.
- Detail source filtering and the player route-source strip now consume the same source-group model, keeping selected, current, recommended, all-source, and failed-fallback semantics consistent.
- Source-filter UI no longer derives local color/copy branches, avoiding separate source-state logic between detail and player surfaces.
- Added unit coverage for selected sources, recommended sources, all sources, current playback sources, and detailed stats.
- App version labels, request user agents, and README notes are now updated to `0.5.56`.

### v0.5.55

中文：
- 播放器换源面板复用 `RouteCandidateUiState`，和详情页候选线路卡共享来源、协议、质量、大小、状态和动作语义。
- `RouteCandidateUiState` 新增当前播放状态，播放器线路行可直接显示“当前 / 播放中”，避免和推荐、失败、BT、WebView 状态分叉。
- 播放器线路行移除本地状态拼接，改为按模型字段渲染筛选标题、状态徽标、协议和大小。
- 新增单元测试覆盖当前播放线路与推荐线路重叠时的语义优先级。
- App 内版本号、请求 UA 和 README 同步到 `0.5.55`。

English:

- The player route panel now reuses `RouteCandidateUiState`, sharing source, protocol, quality, size, status, and action semantics with detail route cards.
- `RouteCandidateUiState` now models the current playback route, allowing player rows to show "current / playing" without diverging from recommended, failed, BT, and WebView states.
- Player route rows no longer assemble local status copy; they render filter titles, badges, protocol, and size from model fields.
- Added unit coverage for current-playback semantics when the selected route is also recommended.
- App version labels, request user agents, and README notes are now updated to `0.5.55`.

### v0.5.54

中文：
- 详情页新增 `RouteCandidateUiState`，统一生成线路来源、协议、质量、大小、状态、缓存能力和播放动作标签。
- 详情页候选线路卡改为消费模型状态，在线推荐、BT 备用、WebView 兜底和失败线路的显示语义更一致。
- 协议名称、线路主标签和大小格式化开始沉到 UI 模型层，为后续复用到播放器换源面板做准备。
- 新增单元测试覆盖推荐在线源、BT 备用源、WebView 兜底源和失败线路候选卡。
- App 内版本号、请求 UA 和 README 同步到 `0.5.54`。

English:

- Detail pages now have `RouteCandidateUiState` to centralize route source, protocol, quality, size, status, cache capability, and playback action labels.
- Detail route cards now render from model state, keeping online recommendations, BT fallbacks, WebView fallbacks, and failed routes consistent.
- Protocol labels, primary route labels, and size formatting are now available from the UI model layer for future player route-panel reuse.
- Added unit coverage for recommended online routes, BT fallbacks, WebView fallbacks, and failed route candidates.
- App version labels, request user agents, and README notes are now updated to `0.5.54`.

### v0.5.53

中文：
- 详情页新增 `DetailEpisodeSummaryUiState`，统一生成当前选集、总集数、线路状态、动作标签和摘要文案。
- 选集区 Header 改为消费模型状态，已匹配、匹配中、异常、待补源和待选集语义不再散在 Compose 里。
- 选集区新增状态标签行，展示当前集、集数、来源覆盖和加载方式，和详情播放准备卡保持一致。
- 新增单元测试覆盖缓存命中、加载中、空线路、异常和待选集状态下的选集摘要。
- App 内版本号、请求 UA 和 README 同步到 `0.5.53`。

English:

- Detail pages now have `DetailEpisodeSummaryUiState` to centralize the current episode, episode count, route status, action label, and summary copy.
- The episode header now renders from model state, keeping ready, loading, failed, empty, and idle semantics out of Compose.
- The episode area now shows compact status chips for current episode, count, source coverage, and route origin, matching the playback readiness model.
- Added unit coverage for cached-ready, loading, empty, failed, and idle episode-summary states.
- App version labels, request user agents, and README notes are now updated to `0.5.53`.

### v0.5.52

中文：
- 详情页新增 `DetailEntryUiState`，统一生成进入详情后的条目来源、结果类型、详情加载状态、集数和下一步动作。
- 详情页标题区下方新增入口状态卡，加载中、加载失败、详情已就绪都会显示一致的来源/选集/播放准备摘要。
- 详情入口摘要复用搜索结果卡的类型判断，搜索结果到详情页的用户路径更连贯。
- 新增单元测试覆盖详情已载入、加载中和异常状态下的入口摘要。
- App 内版本号、请求 UA 和 README 同步到 `0.5.52`。

English:

- Detail pages now have `DetailEntryUiState` to centralize source, result type, detail loading state, episode count, and next action.
- A detail-entry status card now appears below the detail title area, keeping loading, failure, and ready states consistent.
- Detail entry summaries reuse the search-result classification model, making the search-to-detail path more coherent.
- Added unit coverage for loaded, loading, and failed detail-entry summaries.
- App version labels, request user agents, and README notes are now updated to `0.5.52`.

### v0.5.51

中文：
- 搜索结果新增 `SearchResultCardUiState`，统一生成标题、副标题、来源标签、结果类型、动作文案和元数据标签。
- 搜索结果卡现在会区分 Bangumi 资料库、直链、BT/RSS 和普通视频源，用户进入详情前即可判断结果类型。
- 结果卡新增评分、集数、分类和热度标签，减少只看标题/来源名时的信息不足。
- 新增单元测试覆盖 Bangumi 元数据摘要、直链结果和 BT/RSS 结果的卡片状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.51`。

English:

- Search results now use `SearchResultCardUiState` to centralize title, subtitle, source label, result type, action copy, and metadata chips.
- Result cards distinguish Bangumi catalog hits, direct links, BT/RSS entries, and generic video sources before the user opens detail.
- Result cards now show rating, episode count, category, and popularity chips when available.
- Added unit coverage for Bangumi metadata, direct-link results, and BT/RSS result-card state.
- App version labels, request user agents, and README notes are now updated to `0.5.51`.

### v0.5.50

中文：
- 搜索页新增 `SearchLandingUiState` 和 `SearchSuggestionUiState`，把找番入口标题、说明、输入台文案和搜索建议统一模型化。
- 搜索建议优先来自当前日程摘要里的番剧条目，再用热门关键词补足，避免“大家在找”只停留在静态 Compose 文案。
- 搜索输入台现在会显示真实可搜索来源数量和“先进详情再匹配线路”的观看流程提示。
- 新增单元测试覆盖日程条目优先、热门词去重兜底、无搜索源提示和建议语义色。
- App 内版本号、请求 UA 和 README 同步到 `0.5.50`。

English:

- Search now has `SearchLandingUiState` and `SearchSuggestionUiState`, centralizing the Find Anime headline, summary, input copy, and suggestion strip.
- Search suggestions prefer anime titles from the current schedule digest, then fill from popular fallback keywords instead of being hard-coded Compose text only.
- The search station now shows the real searchable-source count and the detail-first route-matching flow.
- Added unit coverage for schedule-first suggestions, fallback de-duplication, missing-source copy, and suggestion tones.
- App version labels, request user agents, and README notes are now updated to `0.5.50`.

### v0.5.49

中文：
- 首页新番时间表状态提升到主 `MainScaffold`，导航栏和首页内容共用同一份 `HomeScheduleUiState`。
- 主导航的首页状态现在会在日程加载完成后显示真实“今日 N”更新数量，不再停留在静态推荐占位。
- 首页日程网络请求从 `DiscoverScreen` 内部移到 app shell，避免切换页面时导航、首页摘要和时间表状态分叉。
- 新增单元测试覆盖 `HomeScheduleUiState.todayCount` 驱动 `AppNavigationUiState` 首页状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.49`。

English:

- Home schedule state is now hoisted into `MainScaffold`, so navigation and the home feed share the same `HomeScheduleUiState`.
- The Discover tab in main navigation now updates to the real `今日 N` count once schedule data is loaded instead of staying on a static recommendation placeholder.
- Schedule loading moved out of `DiscoverScreen` into the app shell, preventing navigation, home digest, and calendar state from diverging during tab switches.
- Added unit coverage for `HomeScheduleUiState.todayCount` driving `AppNavigationUiState`.
- App version labels, request user agents, and README notes are now updated to `0.5.49`.

### v0.5.48

中文：
- 主导航新增 `AppNavigationUiState`，统一生成首页、搜索、频道、我的四个 Tab 的状态文案、选中态和语义色。
- 手机底栏会在当前 Tab 下显示能力摘要，宽屏侧栏会持续展示推荐、搜索源、来源接入和缓存能力状态。
- 导航状态复用来源清单和缓存能力模型，搜索、频道、个人中心入口不再只显示静态 Tab 文案。
- 新增单元测试覆盖有来源/可缓存状态和空来源状态下的导航摘要。
- App 内版本号、请求 UA 和 README 同步到 `0.5.48`。

English:

- Main navigation now has `AppNavigationUiState` to generate tab labels, status text, selection state, and semantic tone for Discover, Search, Sources, and Profile.
- The mobile bottom bar shows a capability summary for the active tab, while the wide navigation rail keeps recommendation, search-source, source-library, and cache-readiness status visible.
- Navigation state now reuses source inventory and cache capability models instead of leaving Search, Sources, and Profile as static tab labels.
- Added unit coverage for populated/cacheable and empty-source navigation summaries.
- App version labels, request user agents, and README notes are now updated to `0.5.48`.

### v0.5.47

中文：
- “我的”页新增 `ProfileCenterUiState`，统一生成个人中心 Hero、版本/来源/弹幕/缓存标签、快捷入口和播放体验设置行。
- 个人中心快捷卡和设置行改为模型驱动，继续看、离线缓存、弹幕设置、线路管理与缓存/来源状态保持同一套文案。
- 个人中心摘要会根据来源数量和可缓存来源数量调整，避免无来源或无缓存能力时仍显示静态成功文案。
- 新增单元测试覆盖有来源/可缓存状态和无来源状态下的个人中心摘要、快捷入口和设置行。
- App 内版本号、请求 UA 和 README 同步到 `0.5.47`。

English:

- The Profile page now has `ProfileCenterUiState` to generate the hero, version/source/danmaku/cache chips, quick actions, and playback setting rows.
- Profile quick cards and setting rows are model-driven, keeping Continue, Offline Cache, Danmaku, and Source Management copy aligned with cache/source state.
- Profile summary now adapts to source count and cacheable source count instead of showing static success copy when sources or cache capability are missing.
- Added unit coverage for populated/cacheable and empty-source profile summaries, quick actions, and setting rows.
- App version labels, request user agents, and README notes are now updated to `0.5.47`.

### v0.5.46

中文：
- 离线缓存新增 `CacheLibraryUiState`，统一计算 Media3 可缓存源、BT 边下边播源、WebView/嗅探阻断源和高级下载运行时状态。
- “我的”页新增离线片库摘要和缓存能力卡，离线缓存入口现在展示真实可缓存来源数量、Media3 队列、BT 缓存和不可缓存原因。
- 旧 `CacheScreen` 改为复用同一份缓存状态模型，避免独立缓存页和个人页文案分叉。
- 新增单元测试覆盖 Media3、BT、WebView 阻断、高级运行时和空缓存来源状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.46`。

English:

- Offline cache now has `CacheLibraryUiState`, centralizing Media3-cacheable sources, BT edge-cache sources, WebView/sniffing blockers, and advanced-download runtime state.
- The Profile page now shows an offline-library summary and cache capability cards with real cacheable source count, Media3 queue, BT cache path, and non-cacheable reasons.
- The legacy `CacheScreen` reuses the same cache state model, keeping standalone cache and profile copy aligned.
- Added unit coverage for Media3, BT, WebView blockers, advanced runtime, and empty cache-source states.
- App version labels, request user agents, and README notes are now updated to `0.5.46`.

### v0.5.45

中文：
- 来源/线路页新增 `SourceLibraryUiState`，统一计算在线源、BT 备用、可缓存源、网页嗅探源和来源卡片状态。
- 片库频道 Hero、策略卡和来源列表改为模型驱动，页面先说明自动选源策略，再展示每个来源的能力、域名和类型。
- 空来源状态新增明确文案，后续接入规则源或来源健康度时不需要继续把业务判断散在 Compose 层。
- 新增单元测试覆盖来源策略摘要、在线/BT/WebView/缓存能力卡片和空来源状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.45`。

English:

- The sources/routes page now uses `SourceLibraryUiState` to centralize online, BT fallback, cacheable, WebView sniffing, and source-card state.
- The source library hero, strategy cards, and source list are model-driven, explaining automatic route strategy before showing each source's capabilities, domains, and type.
- Empty source states now have explicit copy, keeping future rule-source or route-health work out of scattered Compose conditionals.
- Added unit coverage for source strategy summaries, online/BT/WebView/cache cards, and empty provider state.
- App version labels, request user agents, and README notes are now updated to `0.5.45`.

### v0.5.44

中文：
- 浏览/分类页新增 `CategoryBrowseUiState`，统一计算分类覆盖量、最高评分、最高热度、数据来源、列表标题和空状态文案。
- 分类页信息条改为模型驱动，先展示分类索引摘要，再用稳定指标卡呈现条目数、评分、热度和来源。
- 分类接口无结果时会明确展示首页推荐兜底状态，避免用户看到空列表却不知道是否还可继续浏览。
- 新增单元测试覆盖分类命中、兜底推荐、空态和错误摘要。
- App 内版本号、请求 UA 和 README 同步到 `0.5.44`。

English:

- Browse/category pages now use `CategoryBrowseUiState` to centralize category coverage, top rating, heat, source, list title, and empty-state copy.
- The category insight strip is model-driven, showing an index summary plus stable metric tiles for count, rating, heat, and source.
- Empty category responses now explain when home recommendations are being used as a fallback instead of presenting an unexplained blank list.
- Added unit coverage for category hits, fallback recommendations, empty states, and error summaries.
- App version labels, request user agents, and README notes are now updated to `0.5.44`.

### v0.5.43

中文：
- 详情页新增 `DetailPlaybackReadinessUiState`，把推荐线路、在线源、BT 备用和缓存能力汇总成播放就绪状态。
- 详情 Hero 的首播决策下方新增播放就绪摘要条，用户打开详情即可看到是否能播、推荐动作和缓存状态。
- 播放就绪摘要复用已有线路推荐与缓存动作模型，避免详情页、播放器和缓存入口各说各话。
- 新增单元测试覆盖 HLS 可缓存、BT 边下边播、WebView-only 不可播和线路匹配中等详情页状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.43`。

English:

- The detail page now has a `DetailPlaybackReadinessUiState` that summarizes recommended route, online source, BT fallback, and cache capability.
- The detail hero shows a playback-readiness strip below the first-play decision, so users can see readiness, action, and cache status before entering the player.
- The readiness strip reuses existing route recommendation and cache-action models to keep detail, player, and cache wording aligned.
- Added unit coverage for HLS cacheable, BT edge-cache, WebView-only blocked, and route-matching detail states.
- App version labels, request user agents, and README notes are now updated to `0.5.43`.

### v0.5.42

中文：
- 首页追番时间表新增 `HomeScheduleUiState`，统一计算今日更新、本周放送、选中日、下一批更新和空状态文案。
- 日历展开态新增周放送摘要卡，先展示今日数量、本周覆盖和下一批更新，再进入星期切换和条目列表。
- 星期切换按钮改为模型驱动，支持今日标记、选中态和每日电视动画数量，接口未返回时保持稳定 fallback。
- 新增单元测试覆盖选中日摘要、今日计数、本周总量、下一批更新和空日历状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.42`。

English:

- The home schedule now has a `HomeScheduleUiState` that centralizes today count, weekly count, selected day, next update, and empty-state text.
- The expanded calendar adds a weekly schedule digest before the weekday selector and anime list.
- Weekday chips are now model-driven, with today markers, selected state, and per-day counts while preserving a stable fallback before network data arrives.
- Added unit coverage for selected-day summaries, today count, weekly totals, next update, and empty schedule state.
- App version labels, request user agents, and README notes are now updated to `0.5.42`.

### v0.5.41

中文：
- 播放器新增缓存动作状态模型，按 `DownloadPolicy`、协议、WebView 嗅探和 DRM 情况解释当前线路是否能离线缓存。
- 全屏底栏和“更多”面板的缓存入口统一使用同一份状态；可缓存线路加入 Media3 离线队列，不可缓存线路会显示明确原因。
- “我的”页离线缓存卡片改为展示真实可缓存来源数量，并补充 Media3/BT 缓存职责说明。
- 新增单元测试覆盖 HLS、WebView、DRM、BT 和 RTSP 等缓存可用性分支。
- App 内版本号、请求 UA 和 README 同步到 `0.5.41`。

English:

- The player now has a cache-action UI model that explains offline availability from `DownloadPolicy`, protocol, WebView sniffing, and DRM state.
- Fullscreen controls and the More panel share that model; cacheable streams enqueue Media3 offline tasks, while blocked streams surface the exact reason.
- The Profile cache card now shows the real count of cache-capable sources and clarifies Media3 versus BT cache responsibilities.
- Added unit coverage for cache availability across HLS, WebView, DRM, BT, and RTSP streams.
- App version labels, request user agents, and README notes are now updated to `0.5.41`.

### v0.5.40

中文：

- 弹幕渲染新增 `DanmakuSafeArea`，布局引擎会按顶部、底部、左侧和右侧安全区重新分配轨道，避免弹幕压到播放器浮层。
- 播放器根据横屏/竖屏、控制条显隐、锁定状态、右侧 Dock、播放面板和错误/切源通知动态计算弹幕安全区。
- `DanmakuSurface` 的布局缓存纳入安全区变化，控制条或面板状态切换时会重新 prepare 弹幕布局。
- 新增单元测试覆盖弹幕安全区坐标、Surface 缓存失效和播放器防遮挡策略。
- App 内版本号、请求 UA 和 README 同步到 `0.5.40`。

English:

- Danmaku rendering now supports `DanmakuSafeArea`, so the layout engine allocates tracks inside top, bottom, start, and end safe zones instead of the full video rectangle.
- The player derives danmaku safe areas from portrait/landscape mode, visible controls, lock state, the side dock, option panels, and route/error notices.
- `DanmakuSurface` now includes safe-area changes in its layout cache key, forcing a new prepared layout when overlays change.
- Added unit coverage for safe-area coordinates, Surface cache invalidation, and the player anti-obstruction policy.
- App version labels, request user agents, and README notes are now updated to `0.5.40`.

### v0.5.39

中文：

- 搜索页新增“搜索索引”概览卡，展示可搜索来源数、异常来源数、结果数和当前筛选范围。
- 搜索结果现在可按来源快速筛选，用户能直接看到 Bangumi、在线源、BT/RSS 或直连源各自命中情况。
- APK 分析文档补充搜索/索引与播放器画面防遮挡方向，后续播放器浮层会围绕不遮挡关键画面继续优化。
- 新增单元测试覆盖搜索索引 UI 状态、异常源摘要和按源过滤。
- App 内版本号、请求 UA 和 README 同步到 `0.5.39`。

English:

- The search page now includes a search-index overview with searchable source count, failed source count, result count, and current filter scope.
- Search results can be filtered by source, making Bangumi, online, BT/RSS, and direct-link hits visible before opening detail.
- APK analysis notes now include search/index guidance and a player anti-obstruction direction for future overlay work.
- Added unit coverage for search-index UI state, failed-source summaries, and provider filtering.
- App version labels, request user agents, and README notes are now updated to `0.5.39`.

### v0.5.38

中文：
- 新增 `docs/apk-template-analysis.md`，记录 Animeko、B站、弹弹play、腾讯视频和优酷 APK 的 Manifest、资源命名和结构性启发。
- 详情页新增“邻集预热”卡片，把后台线路预取从隐藏逻辑变成可见状态，显示排队、预热中、已命中和待补源。
- 预取状态复用已有 `SourceRegistry.prefetchRouteCandidates` 缓存，不改变线路解析契约，切集时更接近大厂视频 App 的预加载心智。
- 新增单元测试覆盖邻集预热 UI 状态模型。
- App 内版本号、请求 UA 和 README 同步到 `0.5.38`。

English:

- Added `docs/apk-template-analysis.md` with structural findings from Animeko, Bilibili, Dandanplay, Tencent Video, and Youku APK manifests/resources.
- The detail page now shows a nearby-episode preheat card, turning hidden route prefetching into visible queued, warming, ready, and empty states.
- Prefetch visibility reuses the existing `SourceRegistry.prefetchRouteCandidates` cache without changing route-resolution contracts, making episode switching feel closer to large video apps.
- Added unit coverage for the route-prefetch UI state model.
- App version labels, request user agents, and README notes are now updated to `0.5.38`.

### v0.5.37

中文：
- 线路推荐模型新增“推荐理由”，把自动最佳背后的协议、清晰度和综合评分依据暴露给详情页与播放页换源面板。
- 详情页首播条现在会直接说明为什么推荐当前源，用户不用展开调试信息也能理解“自动最佳”的判断。
- 播放页换源摘要改为显示推荐理由，手动换源时能更快区分在线播放、BT 备用和网页兜底线路。
- 新增单元测试覆盖推荐理由、换源面板摘要和不同线路类型的解释。
- App 内版本号、请求 UA 和 README 同步到 `0.5.37`。

English:

- Route recommendation models now expose a reason string that explains the protocol, quality, and score signals behind the automatic best route.
- The detail first-play strip now states why the current source is recommended, so users can trust the automatic choice without opening diagnostics.
- The player route panel summary now surfaces the same recommendation reason, making online, BT fallback, and WebView fallback choices easier to scan.
- Added unit coverage for recommendation reasons, route-panel summaries, and different route-type explanations.
- App version labels, request user agents, and README notes are now updated to `0.5.37`.

### v0.5.36

中文：
- 启动页标题区新增三枚状态胶囊，突出今日片单、源站在线和弹幕同步状态，强化打开 App 时的追番场景感。
- 状态胶囊复用粉、青、琥珀三色体系，并随启动进度渐入，和现有海报 ribbon、弹幕轨道保持一致。
- 布局放在现有标题组内，避免增加新的大块区域，小屏启动页仍保持紧凑。
- App 内版本号、请求 UA 和 README 同步到 `0.5.36`。

English:

- The splash title area now adds three compact status pills for today's queue, source availability, and danmaku sync, making the first launch frame feel more like an anime watchlist.
- The pills reuse the pink, cyan, and amber palette and fade in with the splash progress, matching the existing poster ribbon and danmaku rails.
- The layout stays inside the existing title group, keeping the splash compact on smaller screens.
- App version labels, request user agents, and README notes are now updated to `0.5.36`.

### v0.5.35

中文：

- 弹幕播放时钟新增前向采样尖峰平滑策略，避免播放器短时位置采样跳变直接造成弹幕坐标瞬移。
- 明确的大跨度快进 / seek 仍会立即硬同步，保证用户主动跳转后弹幕位置准确。
- 大幅但未达 seek 阈值的前向漂移会以更快的软修正追赶，减少卡顿感同时避免长时间滞后。
- 新增单元测试覆盖前向采样尖峰平滑和明确 seek 硬同步边界。
- App 内版本号、请求 UA 和 README 同步到 `0.5.35`。

English:

- The danmaku playback clock now smooths forward sample spikes so short player-position jumps do not directly teleport rendered danmaku.
- Clear large seek/fast-forward jumps still hard-sync immediately, keeping danmaku aligned after intentional user jumps.
- Large forward drift below the seek threshold now catches up with a faster soft correction, reducing stutter without lingering far behind.
- Added unit coverage for forward sample-spike smoothing and explicit seek hard-sync boundaries.
- App version labels, request user agents, and README notes are now updated to `0.5.35`.

### v0.5.34

中文：

- 详情页线路状态新增“加载”标签，区分预取缓存命中、实时匹配、可重试和待补源。
- 选集命中邻近集预取缓存时会显示“预取命中”，用户能直接感知线路是否已经提前加载好。
- 线路诊断区第三格改为“加载方式”，让来源覆盖和加载路径一起可见。
- 新增单元测试覆盖缓存命中与实时匹配标签。
- App 内版本号、请求 UA 和 README 同步到 `0.5.34`。

English:

- The detail route state now exposes a loading-origin label that distinguishes prefetched cache hits, live matching, retryable failures, and missing routes.
- Episode selections that hit the nearby-episode prefetch cache now show `预取命中`, making warmed source loading visible to users.
- The route diagnostics row now shows loading origin alongside source coverage.
- Added unit coverage for cached and live route-origin labels.
- App version labels, request user agents, and README notes are now updated to `0.5.34`.

### v0.5.33

中文：

- 横屏全屏播放器底部新增 10 秒快退 / 快进快捷簇，补齐全屏常用播放控制入口。
- 快捷跳转复用现有 seekBy、边界裁剪和反馈浮层，点击后直接显示时间跳转反馈。
- 竖屏未全屏控制保持精简，只保留进度线、弹幕入口和全屏按钮。
- App 内版本号、请求 UA 和 README 同步到 `0.5.33`。

English:

- The landscape fullscreen player now adds a bottom 10-second rewind/forward quick-control cluster for a more complete fullscreen control surface.
- Quick seeks reuse the existing seekBy flow, boundary clamping, and feedback overlay, so taps immediately show seek feedback.
- Portrait non-fullscreen controls stay compact with only the progress line, danmaku entry, and fullscreen button.
- App version labels, request user agents, and README notes are now updated to `0.5.33`.

### v0.5.32

中文：

- 播放器新增统一的线路覆盖标签，区分“来源数”和“线路数”，避免把多线路误显示为多来源。
- 横屏全屏顶部状态条、底部状态条、换源按钮、面板快捷入口和更多菜单统一使用覆盖标签。
- 详情页决策 chip、压缩线路状态和竖屏线路洞察同步使用“几源 / 几线”语义。
- 新增单元测试覆盖播放器线路覆盖标签。
- App 内版本号、请求 UA 和 README 同步到 `0.5.32`。

English:

- The player now uses a unified route-coverage label that distinguishes source count from route count, avoiding multi-route results being shown as multi-source.
- Landscape fullscreen top status, bottom status, route action, panel quick tabs, and the More panel now share the same coverage label.
- Detail decision chips, compact route status, and portrait route insights now use the same source/route wording.
- Added unit coverage for the player route-coverage label.
- App version labels, request user agents, and README notes are now updated to `0.5.32`.

### v0.5.31

中文：

- 弹幕 Surface 帧循环收敛为单一 `frameTick` 驱动，播放器采样位置和帧时间改为同一个非 Compose state 快照更新。
- 播放中每帧只触发一次弹幕绘制失效，减少双 state 写入带来的调度压力和潜在微顿。
- reset 时会同步清空帧就绪状态，避免换集、换设置后复用上一帧时间戳。
- 新增单元测试覆盖帧快照 capture/reset 行为。
- App 内版本号、请求 UA 和 README 同步到 `0.5.31`。

English:

- The danmaku Surface frame loop now uses a single `frameTick`, while playback samples and frame time are updated through one non-Compose-state snapshot.
- Playback now invalidates danmaku drawing once per frame, reducing scheduling pressure and possible micro-stutter from double state writes.
- Reset clears frame readiness so episode or setting changes cannot reuse a stale frame timestamp.
- Added unit coverage for frame snapshot capture/reset behavior.
- App version labels, request user agents, and README notes are now updated to `0.5.31`.

### v0.5.30

中文：

- 弹幕 Canvas 绘制路径新增 Paint 状态缓存，避免每帧重复设置相同描边、阴影、字号和颜色。
- 高弹幕密度下每帧绘制开销更低，有助于减少弹幕滚动的细微顿挫。
- 保持现有弹幕时钟、布局和视觉表现不变，只优化帧内绘制稳定性。
- App 内版本号、请求 UA 和 README 同步到 `0.5.30`。

English:

- The danmaku Canvas draw path now caches Paint state, avoiding repeated per-frame writes of identical stroke, shadow, text-size, and color values.
- Frame draw overhead is lower under dense danmaku, helping reduce subtle scrolling hitches.
- Existing danmaku timing, layout, and visual output are preserved while improving per-frame stability.
- App version labels, request user agents, and README notes are now updated to `0.5.30`.

### v0.5.29

中文：

- 详情页播放源状态新增“来源覆盖”摘要，优先展示多来源/多线路覆盖情况。
- 播放源加载步骤从单纯线路数升级为“来源数 · 线路数”，用户能更快判断是否已有可切换备选。
- 详情页线路入口在多来源时优先显示来源数量，减少只看到线路总数但不知道是否跨源的歧义。
- 新增单元测试覆盖来源覆盖摘要和加载步骤统计。
- App 内版本号、请求 UA 和 README 同步到 `0.5.29`。

English:

- The detail route status now exposes a source-coverage summary, prioritizing multi-source/multi-route visibility.
- Route loading steps now report source and route counts instead of only route counts, making fallback availability clearer.
- The detail route entry prioritizes source count when multiple sources are available, reducing ambiguity in source switching.
- Added unit coverage for source-coverage summaries and loading-step counts.
- App version labels, request user agents, and README notes are now updated to `0.5.29`.

### v0.5.28

中文：
- 路由解析器选择在线命中时先覆盖更多在线来源，再按分数补足，避免同一 Animeko 在线源的多个相似结果挤掉其它来源。
- 详情页和播放器线路面板因此更容易拿到多来源候选，单源失败时也更容易切换到其它在线源。
- 新增单元测试覆盖同一 provider 下多在线源命中的解析多样性。
- App 内版本号、请求 UA 和 README 同步到 `0.5.28`。

English:

- Route resolution now diversifies online hits by source before filling the remaining slots by score, so repeated high-score results from one Animeko online source do not crowd out other sources.
- Detail and player route panels are more likely to receive multi-source candidates, making source switching more resilient when one source fails.
- Added unit coverage for diversified online-hit resolution within a single provider.
- App version labels, request user agents, and README notes are now updated to `0.5.28`.

### v0.5.27

中文：
- 弹幕 Surface 将播放位置采样并入每帧 `withFrameNanos` 渲染循环，减少采样定时器和绘制帧错位带来的细小校正。
- 移除播放中独立 32ms 采样循环，播放时每个可绘制帧都会使用最新播放器位置，暂停时仍保持低频刷新。
- 新增单元测试覆盖帧对齐采样下的弹幕时钟单调推进和稳定步进。
- App 内版本号、请求 UA 和 README 同步到 `0.5.27`。

English:

- Danmaku playback-position sampling now runs inside the per-frame `withFrameNanos` render loop, reducing small corrections caused by timer/render-frame misalignment.
- The separate 32ms playing sampler was removed; playback uses the latest player position on each renderable frame while paused playback remains low-rate.
- Added unit coverage for monotonic, stable danmaku clock steps with frame-aligned samples.
- App version labels, request user agents, and README notes are now updated to `0.5.27`.

### v0.5.26

中文：

- 全屏横屏控制条新增“下一集”快捷按钮，用户不用先打开选集面板即可连续追番。
- 下一集选择规则抽到 `PlaybackUiModels`，按当前播放列表顺序取下一项；最后一集或当前集不在列表时按钮置灰。
- 竖屏未全屏播放器保持精简布局，不增加额外功能按钮。
- 新增单元测试覆盖下一集选择规则。
- App 内版本号、请求 UA 和 README 同步到 `0.5.26`。

English:

- The landscape fullscreen control bar now has a direct "next episode" action so users can continue watching without opening the episode panel first.
- Next-episode selection is modeled in `PlaybackUiModels` and follows the current playback list order; the action is disabled on the last episode or when the current episode is not in the list.
- Portrait non-fullscreen playback remains compact with no extra action button added.
- Added unit coverage for next-episode selection.
- App version labels, request user agents, and README notes are now updated to `0.5.26`.

### v0.5.25

中文：

- 弹幕 Surface 在拿到真实 `withFrameNanos` 时间戳之前不再启动预测播放时钟，避免首帧误用 `0` 作为帧锚点后产生大幅跳动。
- 播放中的弹幕播放位置采样从 48ms 收紧到 32ms，降低预测时钟需要软校正的幅度。
- 采样循环现在只在播放位置实际变化时写入状态，减少弹幕层无意义的重组触发。
- 新增单元测试覆盖未就绪帧时间判定和新的 32ms 采样节奏。
- App 内版本号、请求 UA 和 README 同步到 `0.5.25`。

English:

- The danmaku surface no longer starts the predictive playback clock until a real `withFrameNanos` timestamp is available, preventing a first-frame `0` anchor from causing a large jump.
- Playing playback-position sampling now runs every 32ms instead of 48ms, reducing the correction span needed by the predictive clock.
- The sampling loop now writes state only when the sampled playback position actually changes, reducing unnecessary danmaku-layer recomposition triggers.
- Added unit coverage for frame-time readiness and the new 32ms sampling cadence.
- App version labels, request user agents, and README notes are now updated to `0.5.25`.

### v0.5.24

中文：

- 播放页“换源”面板的线路列表现在复用面板专用排序：可直接播放线路优先，失败线路和 WebView-only 网页兜底自动后置。
- 线路面板列表会按 `stream.id` 去重，并在来源筛选时先过滤再去重，避免跨来源共享流导致用户选中的来源被隐藏。
- 新增单元测试覆盖换源面板排序、失败/网页兜底后置和来源筛选去重规则。
- App 内版本号、请求 UA 和 README 同步到 `0.5.24`。

English:

- The player route-switching panel now uses panel-specific ordering: directly playable routes come first, while failed routes and WebView-only fallbacks are pushed behind.
- Route rows are deduplicated by `stream.id`, with source filtering applied before deduplication so a selected provider remains visible when providers share a stream.
- Added unit coverage for route-panel ordering, failed/web fallback demotion, and source-filtered deduplication.
- App version labels, request user agents, and README notes are now updated to `0.5.24`.

### v0.5.23

中文：

- 详情页“立即观看/匹配播放源”的自动播放入口现在统一使用可直接播放线路选择，避免缓存或异步解析命中 WebView-only 网页兜底时误进入播放页。
- `RouteUiState`、推荐源和播放器线路面板复用同一个自动可播规则，让“自动最佳”的展示和实际开播线路保持一致。
- 新增单元测试覆盖高分 WebView-only 线路不会被自动播放，以及仅有网页兜底时自动播放返回空。
- App 内版本号、请求 UA 和 README 同步到 `0.5.23`。

English:

- Detail-page autoplay for "watch now / match source" now uses a shared directly-playable route selector, avoiding accidental playback entry when cached or resolved routes only provide WebView-only fallbacks.
- `RouteUiState`, recommended sources, and the player route panel now share the same autoplay-eligible route rule so the displayed "auto best" source matches the route that actually starts.
- Added unit coverage for skipping high-scoring WebView-only routes during autoplay and returning no autoplay route when only web fallbacks exist.
- App version labels, request user agents, and README notes are now updated to `0.5.23`.

### v0.5.22

中文：

- 弹幕预测时钟的单帧软校正上限从 12ms 收窄到 6ms，降低播放器采样漂移被修正时产生的可见像素跳动。
- 新增单测覆盖大漂移样本下的单帧校正范围，避免后续改动把弹幕滚动重新变成突兀跳动。
- App 内版本号、请求 UA 和 README 同步到 `0.5.22`。

English:

- The danmaku predictive clock now caps one-frame soft correction at 6ms instead of 12ms, reducing visible pixel jumps when sampled playback drift is corrected.
- Added unit coverage for the one-frame correction cap under large sample drift so future changes do not reintroduce abrupt danmaku jumps.
- App version labels, request user agents, and README notes are now updated to `0.5.22`.

### v0.5.21

中文：

- 弹幕播放位置采样从 96ms 缩短到 48ms，继续保留逐帧 vsync 渲染与预测时钟，减少播放器采样漂移带来的微校正顿挫。
- 单元测试同步覆盖新的播放中采样节奏，锁定弹幕 Surface 的低频采样与逐帧渲染分离策略。
- App 内版本号、请求 UA 和 README 同步到 `0.5.21`。

English:

- Danmaku playback-position sampling now runs every 48ms instead of 96ms while preserving per-vsync rendering and the predictive clock, reducing micro-corrections caused by coarse player samples.
- Unit coverage now locks the updated playing sample cadence and the separation between low-frequency player sampling and frame-rate rendering.
- App version labels, request user agents, and README notes are now updated to `0.5.21`.

### v0.5.20

中文：

- 全屏播放器左侧锁定入口改为状态化图标按钮，锁定与解锁状态更直观，并减少横屏画面里的文字占位。
- 锁定后仍保留常驻解锁入口，避免控制层被隐藏后需要额外手势才能恢复。
- App 内版本号、请求 UA 和 README 同步到 `0.5.20`。

English:

- The fullscreen player's left-side lock control is now a stateful icon button, making locked/unlocked states clearer while reducing text footprint over the landscape video.
- A persistent unlock affordance remains visible after locking so controls can be restored without relying on extra gestures.
- App version labels, request user agents, and README notes are now updated to `0.5.20`.

### v0.5.19

中文：

- 应用图标补上 Android 13 themed/monochrome 图层，保留番剧书册、播放按钮、弹幕轨迹和收藏星标的核心品牌符号。
- Android 12+ 系统启动页改用前景图层作为 splash icon，避免启动页把完整图标背景重复叠在系统图标底色上，视觉更干净。
- Compose 开屏动画在 BrandMark 周围新增弹幕扫描轨道，和已有海报带、加载轨道一起形成更明确的二次元追番入口。
- App 内版本号、请求 UA 和 README 同步到 `0.5.19`。

English:

- Added an Android 13 themed/monochrome launcher icon layer while keeping the anime booklet, play button, danmaku trail, and favorite star brand marks.
- Android 12+ system splash now uses the foreground icon layer, avoiding a duplicated full-icon background inside the splash icon container.
- The Compose splash animation now adds danmaku sweep rails around the BrandMark, reinforcing the anime-tracking identity alongside the poster ribbon and loading rail.
- App version labels, request user agents, and README notes are now updated to `0.5.19`.

### v0.5.18

中文：

- Bangumi/Animeko 详情页的线路解析现在会在在线线路选择不足时补充少量备用命中，避免只有单一路线时线路面板缺少可切换兜底。
- 补源策略保持有限：已有多个在线来源时不再解析备用源，在线不足时只取少量备用命中，并使用更短超时，兼顾开播速度和可切换空间。
- 新增单元测试覆盖“单在线源会补备用线路”和“多在线源不触发备用补源”，锁定自动推荐优先、备用源补位的行为。
- App 内版本号、请求 UA 和 README 同步到 `0.5.18`。

English:

- Bangumi/Animeko detail route resolution now supplements sparse online results with a small number of fallback hits, giving the route panel a real backup choice instead of a single locked path.
- The fallback pass is intentionally limited: diverse online sources skip fallback work, while sparse online results use only a few fallback hits with a shorter timeout.
- Added unit coverage for sparse-online fallback supplementation and diverse-online fast return behavior.
- App version labels, request user agents, and README notes are now updated to `0.5.18`.

### v0.5.17

中文：

- 线路解析缓存现在只写入非空结果，临时没有解析到播放源时不会把“空线路”固定进缓存。
- 详情页和预取流程在遇到空线路结果后可以重新尝试解析同一集，避免网络波动、资源站临时无结果或在线源短暂失败后长期显示“暂无播放源”。
- 预取线路只有拿到至少一条可用线路才会标记为预热成功，后续进入剧集时不会被空预取结果误导。
- 新增单元测试覆盖空线路不缓存、空预取不暖缓存，以及空结果后恢复可播线路的路径。
- App 内版本号、请求 UA 和 README 同步到 `0.5.17`。

English:

- Route resolution caching now stores only non-empty results, so temporary no-route responses no longer poison the episode cache.
- Detail and prefetch flows can retry the same episode after an empty route result, improving recovery from transient source or network misses.
- Route prefetch reports success only when at least one playable route is resolved, preventing empty prefetch results from misleading later playback.
- Added unit coverage for non-cached empty route results, empty prefetch behavior, and recovery from an empty first result.
- App version labels, request user agents, and README notes are now updated to `0.5.17`.

### v0.5.16

中文：

- 弹幕 Canvas 绘制现在让文字描边跟随同一条弹幕的透明度、固定弹幕淡入淡出和密度溢出淡化一起变化，避免正文已经变淡但黑边仍然过重的视觉噪点。
- 描边保留 80% 的最大黑边强度，同时按条目 alpha 缩放，降低密集弹幕时的过强轮廓和闪烁感，让弹幕观感更接近主流视频 App 的稳定层次。
- 新增单元测试覆盖填充色和描边色的 alpha 计算，防止后续回退到描边不随弹幕透明度变化。
- App 内版本号、请求 UA 和 README 同步到 `0.5.16`。

English:

- Danmaku Canvas rendering now applies each entry's alpha to the text stroke as well as the fill, including fixed danmaku fade and overflow density fade.
- The stroke keeps an 80% maximum outline strength but scales with entry alpha, reducing heavy outlines and flicker in dense danmaku scenes.
- Added unit coverage for fill and stroke alpha color calculation to prevent regressions where outlines stay opaque while danmaku text fades.
- App version labels, request user agents, and README notes are now updated to `0.5.16`.

### v0.5.15

中文：
- 横屏全屏播放器右侧 Dock 新增“清晰度”和“倍速”直达入口，和原有弹幕、选集、线路、更多一起组成更完整的全屏快捷操作区。
- 竖屏未全屏控制层继续保持轻量：弹幕输入/开关和全屏入口为主，异常时才显示重试和换源，符合“竖屏精简、全屏全面”的层级。
- 全屏用户现在不必移动到下方控制栏即可打开清晰度和倍速面板，横屏拇指操作更接近主流视频 App。
- App 内版本号、请求 UA 和 README 同步到 `0.5.15`。

English:

- The landscape fullscreen right-side dock now adds direct Quality and Speed entries, joining danmaku, episodes, routes, and more as a fuller fullscreen quick-action area.
- Portrait non-fullscreen controls remain lightweight: danmaku input/toggle and fullscreen stay primary, with retry/route recovery shown only when playback has issues.
- Fullscreen users can now open quality and speed panels without reaching for the bottom control bar, making landscape thumb operation closer to mainstream video apps.
- App version labels, request user agents, and README notes are now updated to `0.5.15`.

### v0.5.14

中文：
- 弹幕平台匹配现在并发执行，B站、腾讯、爱奇艺、优酷等 provider 不再按顺序逐个等待，进入播放器时弹幕匹配等待更接近最慢平台而不是总和。
- 最佳弹幕候选如果返回空时间轴，会继续尝试下一候选平台，避免高分但临时无弹幕的结果直接导致整集无弹幕。
- 新增单元测试覆盖多 provider 并发匹配和空时间轴兜底，继续增强弹幕加载稳定性。
- App 内版本号、请求 UA 和 README 同步到 `0.5.14`。

English:

- Danmaku provider matching now runs concurrently, so Bilibili, Tencent, iQiyi, Youku, and other providers no longer wait one by one when entering playback.
- If the best danmaku match returns an empty timeline, the registry now tries the next candidate provider instead of leaving the whole episode without danmaku.
- Added unit coverage for concurrent provider matching and empty-timeline fallback to keep danmaku loading stable.
- App version labels, request user agents, and README notes are now updated to `0.5.14`.

### v0.5.13

中文：
- 播放器弹幕加载现在只跟当前剧集绑定，切换同一集的清晰度/线路不会重新匹配并拉取同一份弹幕时间轴。
- `DanmakuRegistry` 新增最佳弹幕时间轴缓存和并发请求合并，同一集重复进入或并发请求会复用已拉取的非空时间轴。
- 空时间轴不写入缓存，避免临时无结果把后续可恢复的弹幕请求固定成空结果。
- App 内版本号、请求 UA 和 README 同步到 `0.5.13`。

English:

- Player danmaku loading is now tied only to the current episode, so switching quality/routes within the same episode no longer rematches and refetches the same timeline.
- `DanmakuRegistry` now caches the best non-empty timeline and coalesces concurrent requests, allowing repeated or concurrent loads for the same episode to reuse the fetched timeline.
- Empty timelines are not cached, preventing transient no-result responses from locking future recoverable danmaku requests to an empty state.
- App version labels, request user agents, and README notes are now updated to `0.5.13`.

### v0.5.12

中文：
- 弹幕层现在把播放器进度读取降为低频采样，播放中约每 96ms 采样一次，帧间位置继续用弹幕时钟按 `withFrameNanos` 平滑外推。
- 弹幕绘制仍保持播放时满帧刷新，但不再每帧触碰播放器 position，减少 UI 线程固定开销并降低粗粒度播放器采样带来的抖动。
- 关闭弹幕或没有弹幕内容时停止采样；暂停时降为 250ms 慢采样，继续支持暂停拖动后的状态更新。
- App 内版本号、请求 UA 和 README 同步到 `0.5.12`。

English:

- The danmaku layer now reads player position at a lower sampling rate: about every 96 ms while playing, with frame-to-frame motion still smoothly extrapolated by the danmaku clock using `withFrameNanos`.
- Danmaku rendering still refreshes every frame during playback, but it no longer touches player position every frame, reducing fixed UI-thread work and jitter from coarse player samples.
- Sampling stops when danmaku is disabled or empty; paused playback uses a slower 250 ms sample interval while still reflecting paused seek changes.
- App version labels, request user agents, and README notes are now updated to `0.5.12`.

### v0.5.11

中文：
- 弹幕播放时钟现在把播放中的中等幅度 position 漂移改为小步软校正，避免播放器采样突然超前时弹幕一次性跳位。
- 只有明显的前向 seek 才会硬同步到新进度，普通播放过程中的采样抖动会保持连续、单调的弹幕运动。
- 新增单元测试覆盖中等前向漂移软校正和真实快进硬同步，防止弹幕时钟退回可见顿挫。
- App 内版本号、请求 UA 和 README 同步到 `0.5.11`。

English:

- The danmaku playback clock now soft-corrects moderate in-play position drift in small steps, preventing visible jumps when player samples suddenly run ahead.
- Only obvious forward seeks hard-sync to the new playback position; normal playback jitter keeps continuous, monotonic danmaku motion.
- Added unit coverage for moderate forward drift smoothing and real forward seek hard sync to prevent regressions back to visible stutter.
- App version labels, request user agents, and README notes are now updated to `0.5.11`.

### v0.5.10

中文：
- 详情页和播放器的相邻剧集线路预取现在优先预热后续剧集，连续追番时会先缓存下一集、下下一集，再补充上一集。
- 中间剧集的预取窗口从“下一集、上一集交替”调整为“未来剧集优先”，让自动连播和手动下一集更容易直接命中已解析线路。
- 更新单元测试锁定新的预取顺序，避免后续退回不利于连续观看的策略。
- App 内版本号、请求 UA 和 README 同步到 `0.5.10`。

English:

- Detail and player route prefetch now warms upcoming episodes first, so binge watching caches the next and following episodes before backfilling the previous one.
- Middle-episode prefetch changed from alternating next/previous to future-first, making autoplay and manual next-episode switches more likely to hit already resolved routes.
- Updated unit coverage to lock the new prefetch order and prevent regressions to a less continuous-watch-friendly strategy.
- App version labels, request user agents, and README notes are now updated to `0.5.10`.

### v0.5.9

中文：

- 同一个播放源内部的多别名搜索现在会并发执行，中文名、别名、原名不再按顺序逐个等待。
- 线路匹配在多别名场景下的首轮搜索耗时更接近最慢别名，而不是所有别名请求耗时累加。
- 新增单元测试验证同一 provider 内别名搜索会并发启动，继续降低详情页和播放器等待推荐线路的时间。
- App 内版本号、请求 UA 和 README 同步到 `0.5.9`。

English:

- Alias searches within the same playback source now run concurrently, so Chinese titles, aliases, and original names no longer wait one after another.
- In multi-alias route matching, first-pass search time is closer to the slowest alias request instead of the sum of all alias requests.
- Added unit coverage proving alias searches inside one provider start concurrently, further reducing detail/player wait time for recommended routes.
- App version labels, request user agents, and README notes are now updated to `0.5.9`.

### v0.5.8

中文：

- 线路解析现在会并发处理同一批搜索命中的候选源，多个 `loadDetail + resolveStreams` 不再逐个串行等待。
- 在线源命中较多时，详情页和播放器等待推荐线路的时间会更接近最慢候选源，而不是所有候选源耗时累加。
- 新增单元测试验证候选线路详情解析会并发执行，防止后续退回串行解析。
- App 内版本号、请求 UA 和 README 同步到 `0.5.8`。

English:

- Route resolution now processes matched candidate sources concurrently, so multiple `loadDetail + resolveStreams` chains no longer wait one by one.
- When many online candidates are found, detail/player route wait time is closer to the slowest candidate instead of the sum of all candidates.
- Added unit coverage proving candidate route detail loading runs concurrently to prevent regressions back to serial resolution.
- App version labels, request user agents, and README notes are now updated to `0.5.8`.

### v0.5.7

中文：

- 详情页线路状态现在暴露“选集 / 在线源 / 备用源”三段加载步骤，匹配播放源时能看到当前策略和兜底路径。
- 线路诊断卡在加载中也会展开阶段信息，减少用户等待时的黑盒感，源选择体验更接近 Animeko 的可理解加载流程。
- 详情页和播放器的相邻集线路预取改为并发启动，下一集解析较慢时不会阻塞其他邻近剧集预热。
- App 内版本号、请求 UA 和 README 同步到 `0.5.7`。

English:

- The detail route state now exposes three loading steps: episode selection, online sources, and fallback sources, making route matching strategy visible while waiting.
- The route diagnostics card shows step progress during loading, reducing black-box waits and moving source selection closer to Animeko-style readable loading.
- Adjacent-episode route prefetch now starts concurrently from both the detail page and player, so a slow next episode does not block warming other nearby episodes.
- App version labels, request user agents, and README notes are now updated to `0.5.7`.

### v0.5.6

中文：

- 弹幕渲染的 `maxItemsPerMinute` 现在会按当前可见窗口折算活动弹幕上限，避免把一分钟级别的密度直接当成每帧绘制上限。
- 高密度时间段会优先保留较新的可见弹幕，并对超出活动上限的边缘弹幕做淡出，降低 Canvas 每帧绘制压力和掉帧风险。
- 新增单元测试覆盖活动弹幕上限折算、高密度排期裁剪和溢出淡出行为。
- App 内版本号、请求 UA 和 README 同步到 `0.5.6`。

English:

- Danmaku rendering now converts `maxItemsPerMinute` into an active-item limit for the current visible window instead of treating the per-minute density as a per-frame draw cap.
- Dense bursts keep the newest visible danmaku first and fade overflow items near the cap, reducing Canvas draw pressure and frame-drop risk.
- Added unit coverage for active-item limit scaling, dense schedule clipping, and overflow fading.
- App version labels, request user agents, and README notes are now updated to `0.5.6`.

### v0.5.5

中文：

- 详情页加载现在通过 `SourceRegistry` 复用成功的 `MediaDetail` 结果，返回同一个番剧详情时不再重复触发源解析。
- 同一个条目的并发详情请求会合并为一次 provider 调用，降低快速进入/返回详情页时的等待和闪烁。
- 详情加载失败不会写入缓存，下一次进入仍会重新尝试，避免临时网络失败污染后续状态。
- App 内版本号、请求 UA 和 README 同步到 `0.5.5`。

English:

- Detail loading now reuses successful `MediaDetail` results through `SourceRegistry`, avoiding repeated source parsing when returning to the same title.
- Concurrent detail requests for the same item are coalesced into one provider call to reduce waits and flicker during quick detail navigation.
- Failed detail loads are not cached, so later attempts can retry instead of preserving transient network failures.
- App version labels, request user agents, and README notes are now updated to `0.5.5`.

### v0.5.4

中文：
- 弹幕播放时钟在倍速变化、播放/暂停切换时改用连续输出位置作为新锚点，避免直接回落到滞后播放器采样值。
- 暂停瞬间如果播放器采样尚未刷新，弹幕会停在最后预测位置，不再先后退再停住。
- 新增单测覆盖倍速切换后持续前进、暂停时保持最后预测位置，继续压低滚动弹幕的顿挫风险。
- App 内版本号、请求 UA 和 README 同步到 `0.5.4`。

English:

- The danmaku playback clock now re-anchors speed and play/pause transitions from the continuous output position instead of snapping to a lagging player sample.
- When pausing before the player sample refreshes, danmaku now freezes at the last predicted position instead of stepping backward first.
- Added unit coverage for continued motion after speed changes and stable pause anchoring to further reduce visible scrolling stutter.
- App version labels, request user agents, and README notes are now updated to `0.5.4`.

### v0.5.3

中文：
- 弹幕 Surface 布局缓存现在会复用内容相同的新列表，避免父层刷新同一条弹幕时间线时重复测量和排布。
- 这能减少播放中因弹幕列表实例替换导致的大量布局重建，降低滚动弹幕的偶发卡顿风险。
- 新增单测覆盖相同内容列表复用缓存、内容变化仍重建缓存。
- App 内版本号、请求 UA 和 README 同步到 `0.5.3`。

English:

- The danmaku surface layout cache now reuses refreshed lists with equal content, avoiding repeated measurement and scheduling for the same timeline.
- This reduces the chance of playback stutter caused by full danmaku layout rebuilds when parent state replaces the list instance.
- Added unit coverage proving equal-content lists reuse the cache while real content changes still rebuild it.
- App version labels, request user agents, and README notes are now updated to `0.5.3`.

### v0.5.2

中文：
- 竖屏未全屏播放器进一步精简：视频层底部只保留弹幕输入/开关、轻量进度线和全屏入口。
- 选集、换源等次级操作保留在视频下方信息区和弹层中，避免播放画面上重复出现同类按钮。
- 移除竖屏视频层里常驻的重复全屏浮动按钮，减少控件重叠和画面遮挡。
- App 内版本号、请求 UA 和 README 同步到 `0.5.2`。

English:

- Portrait non-fullscreen playback is leaner: the video layer now keeps only danmaku input/toggle, a lightweight progress line, and the fullscreen entry.
- Episode and route switching remain available in the watch-info area and option panels instead of duplicating controls over the video.
- Removed the always-on duplicate fullscreen floating button from the portrait video surface to reduce overlap and visual obstruction.
- App version labels, request user agents, and README notes are now updated to `0.5.2`.

### v0.5.1

中文：
- 开屏页改为片单、弹幕轨迹和播放标记组合，弱化纯装饰光晕，让启动体验更贴近二次元追番产品。
- App 品牌标记去掉文字符号依赖，改用可控几何形状绘制高光和弹幕线，避免字体/编码差异造成显示不一致。
- 启动图标、adaptive icon 前景和背景同步为同一套追番片单视觉，系统图标与 Compose 开屏保持一致。
- App 内版本号、请求 UA 和 README 同步到 `0.5.1`。

English:

- The splash screen now combines watch-list tiles, danmaku signal rails, and a play mark, reducing decorative glow and making launch feel more anime-tracking focused.
- The brand mark no longer depends on a special text glyph; highlights and danmaku lines are drawn with stable geometry for consistent rendering.
- Launcher icon, adaptive-icon foreground, and adaptive-icon background now share the same watch-list visual language as the Compose splash screen.
- App version labels, request user agents, and README notes are now updated to `0.5.1`.

### v0.5.0

中文：
- 线路预取缓存现在可以被详情页和播放器选集即时复用，命中缓存时不再先清空线路列表或显示加载态。
- 详情页切换剧集时会忽略已过期的慢解析结果，避免旧剧集线路覆盖用户后续选择。
- 播放器选集沿用原有推荐源/同源优先逻辑，但相邻集已预热时可直接切换到可播线路。
- App 内版本号、请求 UA 和 README 同步到 `0.5.0`。

English:

- Route prefetch cache can now be reused immediately by the detail page and in-player episode picker, avoiding unnecessary empty/loading flashes on cache hits.
- Detail episode switching now ignores stale slow route-resolution results so old episode routes cannot overwrite a later user selection.
- In-player episode switching keeps the existing preferred-source behavior while moving directly to a playable route when adjacent episodes are already warmed.
- App version labels, request user agents, and README notes are now updated to `0.5.0`.

### v0.4.9

中文：

- 顶部、底部和高级定位弹幕新增短淡入/淡出，减少固定弹幕到点直接出现或消失造成的视觉突兀。
- 滚动弹幕仍保持原有线性位移和透明度，不影响滚动速度、轨道避让和高密度淡出策略。
- 新增布局引擎单测覆盖固定弹幕淡入淡出，并确认滚动弹幕透明度不受影响。
- App 内版本号、请求 UA 和 README 同步到 `0.4.9`。

English:

- Top, bottom, and advanced positioned danmaku now use a short fade-in/out to avoid abrupt fixed-comment appearance and disappearance.
- Scrolling danmaku keeps its original linear motion and alpha behavior, preserving speed, lane collision handling, and dense-overflow fading.
- Added layout-engine coverage for fixed-comment fading while confirming scrolling comment alpha remains unchanged.
- App version labels, request user agents, and README notes are now updated to `0.4.9`.

### v0.4.8

中文：

- 弹幕播放时钟增强抗采样抖动能力：播放器偶发的非 seek 向后采样不再触发硬同步。
- 播放中弹幕时钟输出保持单调前进，避免弹幕因播放器位置小幅回跳而出现肉眼可见的倒退或顿挫。
- 向前大漂移仍会快速同步，真实后退 seek 仍会立即重锚，兼顾稳定性和响应性。
- App 内版本号、请求 UA 和 README 同步到 `0.4.8`。

English:

- The danmaku playback clock now resists transient backward player-sample jitter without hard-syncing to those noisy samples.
- While playing, the danmaku clock output remains monotonic, preventing visible backward jumps or stutter from small player-position rollbacks.
- Large forward drift still syncs quickly, and real backward seeks still re-anchor immediately.
- App version labels, request user agents, and README notes are now updated to `0.4.8`.

### v0.4.7

中文：

- 弹幕高密度渲染不再对预算外旧弹幕直接硬切，而是保留少量溢出弹幕并逐级降低透明度。
- 这样新弹幕挤入时旧弹幕会先淡出再离场，减少同屏密度变化带来的突兀消失和视觉顿挫。
- 新增布局引擎单测覆盖溢出弹幕淡出策略，保证最新弹幕保持完整可读性。
- App 内版本号、请求 UA 和 README 同步到 `0.4.7`。

English:

- Dense danmaku rendering no longer hard-drops older over-budget entries; a small overflow window now remains visible with stepped-down alpha.
- Older comments fade before leaving when newer comments enter, reducing abrupt disappearances and visual stutter during dense timelines.
- Added layout-engine coverage for the overflow fade policy while keeping the newest comments fully readable.
- App version labels, request user agents, and README notes are now updated to `0.4.7`.

### v0.4.6

中文：

- 双击播放区域 seek 改为轻量手势反馈，不再强制展开完整控制层，隐藏控制层时仍保持沉浸观看。
- 横屏中心按钮快进/快退继续展开或续命控制层，和用户明确点击按钮的操作语义保持一致。
- 新增 seek 控制层展开策略单测，避免后续手势改动破坏竖屏精简体验。
- App 内版本号、请求 UA 和 README 同步到 `0.4.6`。

English:

- Double-tap seek now stays lightweight: it shows gesture feedback without forcing the full control layer open, preserving immersive playback when controls are hidden.
- Fullscreen center skip buttons still reveal or keep controls alive, matching explicit button interaction semantics.
- Added unit coverage for the seek reveal policy so future gesture changes do not regress the compact portrait experience.
- App version labels, request user agents, and README notes are now updated to `0.4.6`.

### v0.4.5

中文：

- 双击播放区域 seek 的反馈改为分侧显示：左半区双击在左侧展示后退提示，右半区双击在右侧展示快进提示。
- 横屏按钮快进/快退仍保持居中反馈，避免按钮操作和手势操作的视觉语义混在一起。
- 反馈 pill 新增方向图标，继续复用统一 seek 目标计算和边界策略。
- App 内版本号、请求 UA 和 README 同步到 `0.4.5`。

English:

- Double-tap seek feedback now appears on the tapped side: left-half double taps show rewind feedback on the left, and right-half double taps show forward feedback on the right.
- Fullscreen button seek feedback remains centered so button actions and gesture actions keep distinct visual meaning.
- The feedback pill now includes direction icons while continuing to reuse the shared seek target and clamping policy.
- App version labels, request user agents, and README notes are now updated to `0.4.5`.

### v0.4.4

中文：

- 播放区域新增双击左右半区快退/快进 10 秒，单击仍保持显示或隐藏控制层，竖屏不增加常驻复杂按钮。
- 双击 seek 复用上一版的统一目标计算和操作反馈，左半区后退、右半区快进，并继续遵守片头/片尾边界。
- 新增单测覆盖双击区域到 seek 方向的映射，后续接手势动画时可保持行为稳定。
- App 内版本号、请求 UA 和 README 同步到 `0.4.4`。

English:

- The video surface now supports double-tapping the left or right half to seek backward or forward by 10 seconds, while single tap still toggles controls.
- Double-tap seeking reuses the shared seek target policy and feedback pill from the previous release, including start/end clamping.
- Added unit coverage for mapping double-tap regions to seek direction so future gesture animation work keeps the behavior stable.
- App version labels, request user agents, and README notes are now updated to `0.4.4`.

### v0.4.3

中文：

- 横屏播放器的快进/快退改为复用统一的 seek 目标计算，后退会停在片头，有可靠总时长时快进不会越过片尾。
- 点击左右 10 秒按钮后新增短暂操作反馈，显示快进/后退方向和目标时间，减少全屏操作的不确定感。
- 竖屏仍保持精简控制布局，不新增常驻快进/快退按钮，为后续双击手势复用同一套 seek 行为打基础。
- App 内版本号、请求 UA 和 README 同步到 `0.4.3`。

English:

- Fullscreen skip forward/backward now reuses a single seek-target policy: backward seeks clamp to the start, and forward seeks clamp to the known duration.
- Tapping the 10-second skip buttons now shows a brief operation feedback pill with the direction and target timestamp.
- Portrait playback keeps the compact control layout without adding persistent skip buttons, while the shared seek behavior is ready for future double-tap gestures.
- App version labels, request user agents, and README notes are now updated to `0.4.3`.

### v0.4.2

中文：

- 播放器进度轮询改为按 UI 状态动态调度：播放中且下方控制条可见时提高刷新频率，让进度条和时间显示更顺滑。
- 面板打开、控制层隐藏或暂停时自动降频，减少不可见场景的主线程刷新压力。
- 新增单测覆盖播放器进度轮询策略，保持播放中的可见进度优先级。
- App 内版本号、请求 UA 和 README 同步到 `0.4.2`。

English:

- Player progress polling now adapts to UI state: visible controls refresh more often while playback is active, making the progress bar and time labels feel smoother.
- Polling backs off when panels are open, controls are hidden, or playback is paused, reducing main-thread refresh pressure in less visible states.
- Added unit coverage for the player progress polling policy so visible playback remains prioritized.
- App version labels, request user agents, and README notes are now updated to `0.4.2`.

### v0.4.1

中文：

- 弹幕帧调度改为按状态运行：播放中保持逐帧刷新，暂停/缓冲时降为低频刷新，无弹幕或关闭弹幕时不再持续跑帧循环。
- 降低无弹幕、暂停和缓冲场景下的主线程无效刷新，为播放器控制层和系统动画留下更多余量。
- 新增单测覆盖弹幕帧调度策略，避免后续改动重新引入空弹幕满帧刷新。
- App 内版本号、请求 UA 和 README 同步到 `0.4.1`。

English:

- Danmaku frame scheduling now follows playback state: full-rate while playing, low-rate while paused/buffering, and stopped when danmaku is disabled or empty.
- This reduces unnecessary main-thread redraw pressure in empty, paused, and buffering states, leaving more room for player controls and system animations.
- Added unit coverage for the danmaku frame scheduling policy so empty timelines do not regress to full-rate refresh.
- App version labels, request user agents, and README notes are now updated to `0.4.1`.

### v0.4.0

中文：

- 更新应用图标视觉，强化“追番书签 + 播放入口 + 星轨导航”的二次元追番识别度。
- 新增 Android 26+ adaptive icon 资源，保留旧版 vector fallback，并给启动器圆形图标配置同一品牌资源。
- Compose 开屏品牌标同步为新版图标构图，启动动画中的 Logo、片单色带和进度轨视觉更加统一。
- App 内版本号、请求 UA 和 README 同步到 `0.4.0`。

English:

- Refreshed the app icon around an anime-watchlist bookmark, play entry, and star-route navigation motif.
- Added Android 26+ adaptive icon resources while keeping a vector fallback, and wired round launcher icons to the same brand asset.
- The Compose splash brand mark now matches the new icon structure, aligning the logo, poster ribbon, and progress rail.
- App version labels, request user agents, and README notes are now updated to `0.4.0`.

### v0.3.9

中文：

- 详情页在当前剧集线路匹配成功后，会后台预取相邻剧集线路，优先下一集，再回补上一集。
- 播放页切入当前集后同样会预取相邻剧集，用户在选集面板切到下一集时更容易命中上一版的线路缓存。
- `SourceRegistry` 新增安全预取接口，预取成功会暖缓存，预取失败只返回 `false`，不影响当前播放和 UI 状态。
- 新增单测覆盖预取缓存、预取失败语义，以及相邻剧集预取顺序。

English:

- The detail page now prefetches adjacent episode routes after the current episode resolves successfully, prioritizing the next episode before the previous one.
- The player also prefetches adjacent episode routes for the current episode, making episode-panel switches more likely to hit the route cache.
- `SourceRegistry` now exposes a safe prefetch API: successful prefetches warm the cache, while failures return `false` without affecting current playback or UI state.
- Added tests for prefetch cache warming, prefetch failure semantics, and adjacent episode ordering.

### v0.3.8

中文：

- `SourceRegistry` 新增按剧集的播放线路缓存，详情页、播放器切集和重复进入同一剧集时会复用已解析的线路结果，减少等待和重复请求。
- 同一剧集的并发线路解析会合并为一次请求，避免用户连续点按、自动播放和面板刷新同时触发多路源加载。
- 失败的线路解析不会写入缓存，临时网络失败后重试仍会重新请求来源。
- 新增单测覆盖线路缓存命中、并发合并和不同剧集缓存隔离。

English:

- `SourceRegistry` now caches route candidates per episode, so the detail page, player episode switching, and repeated entry into the same episode can reuse resolved routes.
- Concurrent route requests for the same episode are coalesced into one provider call, reducing duplicate source loading from rapid taps, autoplay, or panel refreshes.
- Failed route resolutions are not cached, so transient network failures can still retry against providers.
- Added tests for cache hits, concurrent coalescing, and cache separation across episodes.

### v0.3.7

中文：

- 详情页和播放器线路面板共用同一套播放源分组模型，推荐源、当前源、在线源、BT 备用源和失败源的统计不再各算各的。
- 播放方案卡继续保留“自动最佳”入口，同时来源卡会按当前筛选、当前播放、推荐、在线可播数量排序，换源路径更接近追番场景。
- 播放器全屏线路面板的来源筛选也改用统一模型，失败线路会从可播统计中降级，避免用户误以为不可用源仍是可选主线路。
- 新增单测覆盖播放源分组排序、“全部播放源”聚合和失败线路统计。

English:

- Detail and player route panels now share one source-grouping model, keeping recommended, current, online, BT fallback, and failed source counts consistent.
- The route plan card still keeps the automatic best-entry path, while source cards now sort by active filter, currently playing source, recommendation, and playable online count.
- The fullscreen player route panel now uses the same grouping model, so failed routes are downgraded from playable counts instead of looking like primary choices.
- Added unit coverage for source-group ordering, all-source aggregation, and failed-route accounting.

### v0.3.6

中文：

- 弹幕播放时钟改为基于播放器采样点和每帧时间外推，播放器进度粗粒度刷新时弹幕仍按帧连续移动，seek、暂停和倍速变化会重新同步。
- 弹幕布局新增无分配的可见弹幕遍历路径，Canvas 绘制不再每帧创建 RenderedDanmaku 列表，减少 GC 抖动造成的顿挫。
- 弹幕渲染把描边、阴影等帧内固定画笔参数移出单条弹幕循环，进一步降低每帧主线程开销。
- 播放器向弹幕层传入播放状态和倍速，让弹幕在暂停、缓冲、倍速播放时和真实视频时钟更一致。

English:

- Danmaku now uses a smooth playback clock anchored to player samples and frame time, so comments keep moving continuously between coarse player progress updates while still resyncing on seek, pause, and speed changes.
- The prepared danmaku layout now exposes an allocation-light visible traversal path, letting Canvas draw active comments without creating a RenderedDanmaku list every frame.
- Stroke and shadow paint parameters that are stable within a frame are moved out of the per-comment loop to reduce main-thread draw overhead.
- The player now passes playback state and speed into the danmaku layer, keeping paused, buffered, and speed-adjusted playback closer to the actual video clock.

### v0.3.5

中文：

- 横屏全屏播放器顶部新增状态胶囊，直接显示当前集、来源/线路数、清晰度和倍速，进入全屏后不用先扫底部按钮才能确认播放状态。
- 竖屏未全屏播放器仍保持精简，只保留返回、必要提示、弹幕输入、选集、换源和全屏入口，避免遮挡画面。
- 播放器顶部状态模型补充来源和清晰度字段，为后续全屏布局和面板联动继续提供稳定数据。
- 弹幕渲染改为预编排轨道和测量结果，播放时每帧只计算当前位置，减少主线程测量和重排造成的顿挫感。
- 视频 Surface 自身也接入点击控制层切换，横屏和竖屏点击画面都能稳定呼出播放器控制。

English:

- The landscape fullscreen player now adds top status chips for the current episode, source/route count, quality, and playback speed, so users can read playback context without scanning the bottom controls first.
- The portrait non-fullscreen player remains intentionally minimal, keeping only back/status hints, danmaku input, episode/source shortcuts, and fullscreen entry to avoid covering the video.
- The player overlay model now exposes source and quality labels, giving later fullscreen layout and panel interactions stable UI data.
- Danmaku rendering now precomputes lanes and text metrics, so playback frames only calculate current positions and avoid repeated main-thread measuring/re-layout.
- The video surface now forwards taps to the custom player overlay, making portrait and landscape controls reliably reveal from the video area.

### v0.3.4

中文：

- 详情页展开线路时从“播放源分组”改为“播放方案”，先展示“自动最佳”方案，再展示各来源方案卡。
- 自动最佳方案卡会显示推荐来源、可播数量、在线源和备用源数量，让用户先理解默认会怎么播，再决定是否手动切源。
- 来源方案卡改为更高的信息卡片，展示线路数、可播结构、自动推荐和当前方案状态，减少筛选器/调试器观感。

English:

- The detail route expansion now presents playback plans instead of a source filter strip, with the automatic best plan shown first.
- The automatic plan card shows the recommended source, playable count, online routes, and backup routes before users decide whether to switch manually.
- Source plan cards now show route count, playable structure, recommendation state, and current selection, reducing the previous diagnostic feel.

### v0.3.3

中文：

- 片库频道从能力统计页调整为“先播在线、备用补源、离线缓存、网页兜底”的观看策略入口，更接近国内长视频 App 的线路表达。
- “已接入线路”区域弱化调试感，强调详情页自动选择最佳线路，手动切换只在卡顿、失效或换清晰度时使用。
- “我的”页面升级为“我的追番中心”，把追番记录、缓存、弹幕设置和线路管理收拢成用户入口，同步版本和数据源 UA 到 `0.3.3`。

English:

- The source channel now presents watch-oriented strategy entries: online first, backup sources, offline cache, and web fallback.
- The connected routes section now emphasizes automatic best-source selection on detail pages, leaving manual switching for buffering, failures, or quality changes.
- The profile page is upgraded into a watch-center style hub with continue watching, cache, danmaku settings, and route management; version and source user agents are synchronized to `0.3.3`.

### v0.3.2

中文：
- 主框架底部导航改为自定义追番风格胶囊按钮，选中态更明显，减少默认 Material 工具感。
- 宽屏/TV 侧边导航同步升级，顶部使用应用品牌标识，频道入口更像正式视频 App。
- 应用内版本和数据源请求 UA 同步到 `0.3.2`。

English:

- The main bottom navigation now uses custom anime-app capsule buttons with clearer selected states instead of the default Material look.
- Wide-screen/TV navigation is upgraded with the app brand mark and more video-app-like channel entries.
- In-app version display and source request user agents are now synchronized to `0.3.2`.

### v0.3.1

中文：
- Compose 开屏升级为更完整的追番品牌场景，加入片单色块、品牌光晕和启动进度轨。
- 开屏保留“追番不迷路 / ZFBML / 今晚继续追”的核心识别，同时把进入首页的等待控制在短时长内。
- 应用内版本和数据源请求 UA 同步到 `0.3.1`，便于后续排查真实源加载问题。

English:

- The Compose splash screen now presents a fuller anime-tracking brand scene with watchlist tiles, brand glow, and a startup progress rail.
- The splash keeps the core identity of “追番不迷路 / ZFBML / 今晚继续追” while staying short before entering the home screen.
- In-app version display and source request user agents are now synchronized to `0.3.1`.

### v0.3.0

中文：
- 全屏/横屏播放器新增右侧快捷 Dock，提供弹幕、选集、换源和更多设置入口。
- 右侧 Dock 只在控制层展开且未锁定时显示，避免常驻遮挡画面，同时让全屏操作更接近成熟视频 App。
- 播放器版本进入 `0.3.0`，本阶段继续围绕“竖屏精简、全屏完整”的交互方向推进。

English:

- Fullscreen/landscape playback now includes a right-side quick dock for danmaku, episodes, source switching, and more settings.
- The dock only appears while controls are visible and unlocked, keeping the video clean while making fullscreen actions easier to reach.
- The app moves to `0.3.0`, continuing the player direction of compact portrait controls and comprehensive fullscreen controls.

### v0.2.99

中文：
- 竖屏未全屏播放器底部控制重构为“细进度条 + 弹幕输入胶囊 + 必要快捷键”，减少按钮拥挤感。
- 弹幕开关收进输入胶囊右侧，保留发弹幕入口，同时让选集、换源、全屏的层级更清楚。
- 横屏/全屏播放器继续保留完整控制区，清晰度、倍速、换源、选集、缓存和更多设置不受影响。

English:

- The portrait inline player bottom controls now use a slim progress line, danmaku input capsule, and only essential shortcuts.
- The danmaku toggle now lives inside the input capsule, keeping the send entry visible while making episode, source, and fullscreen actions clearer.
- Landscape/fullscreen playback still keeps the full control bar with quality, speed, source switching, episodes, cache, and more settings.

### v0.2.98

中文：
- 详情页首屏的“即将播放”提示升级为观看决策卡，展示当前集、推荐源、清晰度和可切换播放源数量。
- 主播放按钮上方现在能直接判断“点播放会看哪一集、从哪个源播、是否还有备用源”。
- 底层播放源状态文案统一从“线路”收敛为“播放源”，详情页和播放器面板口径更一致。

English:

- The detail hero playback hint is now a watch decision card showing episode, recommended source, quality, and switchable source count.
- Users can now tell which episode and source will play before pressing the main watch button.
- Underlying route status wording now consistently uses playback source terminology across detail and player panels.

### v0.2.97

中文：
- 播放器设置抽屉新增统一“播放上下文条”，打开清晰度、倍速、换源、选集、弹幕等面板时先显示当前番名和集数。
- 上下文条同步展示当前播放源、清晰度和倍速，让横屏/竖屏面板操作时不容易迷路。
- 面板顶部层级更接近国内视频播放器：标题、当前播放信息、快捷功能 Tab、具体设置内容。

English:

- Player option drawers now include a playback context bar for quality, speed, source, episode, danmaku, and more panels.
- The context bar shows the current anime, episode, source, quality, and speed so users stay oriented while changing settings.
- The panel hierarchy now better matches modern Chinese video players: title, current playback info, quick tabs, then detailed settings.

### v0.2.96

中文：
- “我的”页从设置状态面板重构为“我的追番”用户中心，顶部展示品牌、版本、播放源和弹幕平台概况。
- 新增继续观看、离线缓存、弹幕偏好、播放源四个快捷能力卡，让个人页更像视频 App。
- 观看设置区改为带图标的偏好列表，展示播放内核、弹幕样式和播放源策略，减少工程状态感。

English:

- The Mine tab is now a My Anime profile center with brand identity, version, source count, and danmaku platform overview.
- Added quick cards for Continue Watching, Offline Cache, Danmaku Preferences, and Playback Sources.
- The watch settings area now uses icon-led preference rows for player engine, danmaku style, and source strategy instead of raw status panels.

### v0.2.95

中文：
- 搜索页升级为“找番”入口，顶部说明改为“先进详情页，再自动匹配播放源”的观看流程。
- 搜索框改成卡片式“全站找番”搜索台，减少表单工具感，按钮和图标更接近视频 App。
- 新增“大家在找”快捷词横滑入口，用户可一键搜索热门番名；空结果提示也改为换番名/别名/关键词。

English:

- The Search tab is now a Find Anime entry, explaining the watch flow from detail page to automatic source matching.
- The search field now uses a card-style global search station with clearer video-app controls.
- Added a horizontal Trending Searches strip for one-tap anime searches, and the empty state now suggests alternate titles or keywords.

### v0.2.94

中文：
- “频道”页重构为“片库频道”，顶部展示播放源总览，不再像 Provider 调试列表。
- 新增在线优先、BT 备用、可缓存、网页嗅探四个能力卡，让用户先理解观看策略，再查看来源明细。
- 播放源卡片改为视频 App 风格，展示来源类型、可用能力、域名和版本，弱化底层技术字段。

English:

- The Channels tab is now a Library Channels page with a source overview instead of a provider-debug list.
- Added capability cards for Online First, BT Backup, Cacheable, and Web Sniffing so users understand the watch strategy before source details.
- Source cards now read like video-app entries, showing source type, user-facing abilities, domains, and version while hiding raw technical noise.

### v0.2.93

中文：
- 竖屏播放器把“线路”入口收敛为“换源”，只保留弹幕、选集、换源和全屏等轻量入口，普通观看时不再像调试面板。
- 竖屏播放信息卡改为“继续看”语义，并显示当前源、清晰度和自动推荐状态，让用户更像在看视频而不是在看解析结果。
- 横屏全屏按钮组同步改为“换源 / 播放源”表达，保留清晰度、倍速、选集、缓存和更多设置，完整功能集中在全屏场景。

English:

- The portrait player now uses Switch Source instead of Routes and keeps only lightweight entries for danmaku, episodes, source switching, and fullscreen.
- The portrait watch card now reads as Continue Watching and surfaces the current source, quality, and auto recommendation state.
- The landscape fullscreen controls now use source-oriented wording while keeping the full set of quality, speed, episodes, cache, and more actions.

### v0.2.92

中文：

- 详情页剧集区标题升级为“选集”状态头，显示当前集、总集数和线路匹配状态。
- 选集区说明明确“切换后自动匹配最佳线路”，让用户理解选集和线路解析是一体流程。
- 已匹配线路卡的默认入口从“线路”改为“切换”，让手动换源意图更明确，默认状态更收敛。

English:

- The detail episode section now uses a status header showing the current episode, total episode count, and route matching state.
- The episode section explains that switching episodes automatically matches the best route, connecting episode selection with route resolution.
- The matched route card now uses “Switch” instead of a generic “Routes” action, making manual source switching clearer while keeping the default state compact.

### v0.2.91

中文：

- 番剧详情页 Hero 的“线路”入口升级为状态按钮，直接显示自动最佳、匹配中、线路异常或手动线路状态。
- 线路入口会展示可切线路数、来源数或推荐源名称，让用户不用理解底层解析也能判断能否播放。
- 保持详情页首屏以“立即观看”为主操作，线路切换作为清晰的辅助入口，更接近 Animeko 式多源体验。

English:

- The anime detail Hero route entry is now a status button showing Auto Best, Matching, Route Issue, or Manual Routes.
- The route entry surfaces route count, source count, or the recommended source name so users can judge playability without reading resolver details.
- The detail first screen keeps Watch Now as the primary action while making route switching a clear secondary entry, closer to an Animeko-style multi-source flow.

### v0.2.90

中文：

- 首页首屏新增“继续看 / 今日更新 / 热门推荐”观影入口区，让打开 App 后先看到内容消费入口，而不是零散组件。
- 首页“正在播放”区改名为“继续观看”，避免和真实播放状态混淆，更贴近日常追番表达。
- 应用图标和 Compose 开屏 BrandMark 更新为书签、播放圆心和星光组合，品牌文案改为“今晚继续追”。

English:

- The home first screen now adds a Watch Hub for Continue, Today Updates, and Hot Picks, making the app feel more like a content-first video product.
- The home “Now Playing” section is renamed to Continue Watching to avoid confusion with actual playback state.
- The launcher icon and Compose splash BrandMark now use a bookmark, play center, and star motif with the new “Continue tonight” brand line.

### v0.2.89

中文：

- 横屏全屏播放器底部改为“播放状态摘要 + 功能按钮”两层结构，先显示当前线路、清晰度、倍速、线路数和集数，再提供操作入口。
- 全屏状态继续保留清晰度、倍速、线路、选集、缓存、更多和弹幕设置，让完整功能集中在横屏场景。
- 竖屏非全屏的轻量控制逻辑不变，继续只放即时观看所需入口，避免普通观看时像调试工具。

English:

- The landscape fullscreen player bottom area now uses a two-layer structure: playback status first, then action controls.
- Fullscreen mode keeps the complete control set for quality, speed, routes, episodes, cache, more options, and danmaku settings.
- Portrait non-fullscreen controls stay lightweight, keeping everyday watching focused instead of feeling like a route/debug tool.

### v0.2.88

中文：

- 竖屏播放器底栏不再在“选集”和“线路”之间二选一；有多集和多线路时两个入口会同时显示。
- 保持竖屏非全屏控制层轻量，只增加必要的即时入口：弹幕、选集、线路、全屏。
- 多线路场景下可以更快打开线路弹层，减少从播放信息区再进入的路径。

English:

- The portrait player bottom bar no longer chooses between Episodes and Routes; when both are available, both actions are shown.
- The portrait non-fullscreen control layer stays lightweight while keeping the essential instant actions: danmaku, episodes, routes, and fullscreen.
- Multi-route playback now gives quicker access to the route panel without forcing the user through the watch-info area.

### v0.2.87

中文：

- 播放器弹层标题下新增快捷标签，可在清晰度、倍速、线路、选集、弹幕、设置之间直接切换。
- 横屏右侧抽屉和竖屏底部弹层复用同一套标签，减少反复关闭面板再从底部按钮进入的操作。
- 标签会根据线路数、选集数和弹幕状态显示可用性与当前状态，让全屏播放器的功能入口更完整、更清楚。

English:

- Player option panels now include quick tabs for Quality, Speed, Routes, Episodes, Danmaku, and Settings directly under the header.
- The same tab row is shared by the landscape side drawer and portrait bottom sheet, reducing repeated close-and-reopen panel navigation.
- Tabs reflect route count, episode count, and danmaku state so fullscreen controls feel more complete and easier to scan.

### v0.2.86

中文：

- 竖屏播放页的“选集/线路”快捷入口改为单行胶囊按钮，降低设置面板感，让非全屏观看区更轻。
- 竖屏选集横排不再固定展示前 18 集，会围绕当前播放集显示，并在末尾保留“全部”入口。
- 继续弱化竖屏普通观看路径里的诊断信息，只在线路异常或提示存在时展示线路状态细节。

English:

- The portrait watch page now uses lighter single-line pill actions for Episodes and Routes, reducing the settings-panel feel in non-fullscreen viewing.
- The portrait episode rail now follows the current episode instead of always showing the first 18 episodes, with an All entry kept at the end.
- Normal portrait viewing continues to hide route diagnostics unless a route notice or playback issue needs attention.

### v0.2.85

中文：

- 横屏全屏播放器底部功能区从横向滑动列表改为等宽铺开的固定入口，清晰度、倍速、线路、选集、缓存、更多可以一眼看到。
- 横屏弹幕输入条给右侧功能入口让出更多空间，窄屏时按钮自动隐藏副值，保持功能完整且不挤压文字。
- 自动切线路、手动下一线路和选集沿用提示不再直接暴露来源名，改为展示线路名/清晰度，让普通播放提示更像正式视频 App。

English:

- The landscape fullscreen action area now uses fixed equal-width actions instead of a horizontally scrolling list, keeping quality, speed, routes, episodes, cache, and more visible at once.
- The fullscreen danmaku input now gives more space to the action area; on narrow screens action buttons hide secondary values to keep the full control set readable.
- Auto route fallback, manual next-route, and next-episode notices now use route/quality labels instead of source names, making normal playback messages feel less diagnostic.

### v0.2.84

中文：

- 播放器线路弹层的默认视图改为先展示清晰度/线路名、当前状态和推荐操作，来源名降为副标题，详细模式才展开来源和协议细节。
- 单来源线路不再默认显示来源筛选条，多来源时入口改为“线路分组/全部线路”，减少工具面板感。
- 竖屏播放器下方信息条改为线路数、可播数、备用线路和当前清晰度/协议，更接近国内视频 App 的观看状态表达。

English:

- The player route panel now prioritizes quality/route labels, current state, and recommended actions in the default view, moving source names to secondary text and keeping source/protocol details in Detailed mode.
- Single-source route lists no longer show the source filter strip by default; multi-source lists now use viewer-facing route group wording.
- The portrait player insight strip now shows route count, playable count, fallback routes, and current quality/protocol instead of source diagnostics.

### v0.2.83

中文：

- 详情页线路状态卡进一步收敛：普通加载和可播放状态不再自动展开来源、在线、备用、异常等诊断格子。
- 推荐线路的折叠摘要改为“当前集 · 自动最佳 · 清晰度 · 线路数”，不直接暴露来源名。
- 只有用户展开线路卡或线路失败时才展示详细线路信息，让详情页更像正式追番 App。

English:

- The detail-page route status card is further simplified: normal loading and playable states no longer auto-expand source, online, fallback, or failure diagnostics.
- The collapsed recommendation summary now reads as current episode, Auto Best, quality, and route count instead of exposing source names directly.
- Detailed route information is now shown only when the user expands the card or when a route fails, making the detail page feel more like a polished anime app.

### v0.2.82

中文：

- 详情页、竖屏观看信息、全屏设置和线路弹层统一使用“线路”语言，去掉普通观看路径里的“播放源/换源/诊断”工具感文案。
- 播放设置摘要不再直接暴露来源名，改为展示当前清晰度、倍速、当前线路、线路数量和选集数量。
- 线路状态模型同步改为“推荐线路/匹配线路/可用线路”，让 Animeko 式多线路体验更像正式视频 App。

English:

- Detail, portrait watch info, fullscreen settings, and route panels now consistently use viewer-facing route language, removing tool-like source/switch/diagnostic wording from normal playback paths.
- The playback settings summary no longer exposes source names directly, showing quality, speed, current route, route count, and episode count instead.
- Route UI state labels now use recommended, matching, and available route wording to make the Animeko-style multi-route flow feel more like a polished video app.

### v0.2.81

中文：

- 横屏全屏播放器底部功能栏从固定宽度工具卡片改为一行式胶囊按钮，保留清晰度、倍速、线路、选集、缓存和更多入口。
- 横屏弹幕输入条改为圆形胶囊，并统一使用“点我发弹幕”的移动端观看文案。
- “换源”入口在横屏控制层改为“线路”，表达更贴近国内长视频 App 的用户语言。

English:

- The landscape fullscreen bottom action bar now uses compact one-line pill buttons instead of fixed-width tool cards, while keeping quality, speed, routes, episodes, cache, and more.
- The landscape danmaku input now uses a rounded pill and the same mobile-viewing copy, "Tap to send danmaku".
- The route-switching entry is labeled as Routes in the fullscreen control layer to better match Chinese long-form video app language.

### v0.2.80

中文：

- 竖屏播放器顶部不再展示普通播放状态胶囊，只保留异常和切源提示，减少对画面的干扰。
- 竖屏底部控制改为细进度线 + 弹幕输入胶囊 + 精简操作，更接近国内移动端视频播放器的轻交互层级。
- 弹幕入口文案改为“点我发弹幕”，弹幕开关改为图标按钮，降低工具按钮感。

English:

- The portrait player top overlay no longer shows normal playback-state pills, keeping only error and route-switching notices to reduce visual noise.
- The portrait bottom controls now use a slim progress line, a danmaku input pill, and compact actions, closer to Chinese mobile video player interaction patterns.
- The danmaku entry now says "Tap to send danmaku", and the danmaku toggle is an icon button to reduce the tool-like feel.

### v0.2.79

中文：

- 播放器顶部覆盖层状态从 `READY/BUFFERING` 等原始状态改为中文观看态，如“播放就绪”“缓冲中”“等待播放”。
- 全屏顶部线路胶囊不再默认显示来源名和协议，改为展示清晰度、线路名或“自动最佳”。
- 普通观看路径进一步减少源诊断信息，把来源和协议细节留给“换源”的详细模式。

English:

- Player top-overlay states now use localized viewing labels such as Ready, Buffering, and Waiting instead of raw `READY/BUFFERING` text.
- The fullscreen top route pill no longer exposes source names and protocols by default, showing quality, route name, or Auto Best instead.
- The normal viewing path now hides more source diagnostics, leaving source and protocol details to the source panel's Detailed mode.

### v0.2.78

中文：

- 选集面板摘要去掉当前源、协议和播放源数量等工程信息，改为“当前第几集 / 共几集”的合集进度。
- 选集面板新增“正在看”“自动匹配”提示，表达切换选集后会自动选择最佳播放线路。
- 播放器弹层职责进一步分离：选集只负责追番进度，换源面板才展示播放源细节。

English:

- The episode panel summary no longer exposes current source, protocol, or route-count diagnostics, focusing on current episode progress instead.
- The episode panel now shows Watching and Auto Match hints to explain that episode changes will pick the best playable route automatically.
- Player panels are more clearly separated: Episodes focus on watch progress, while source details stay in the source-switching panel.

### v0.2.77

中文：

- 播放源面板新增默认“简单模式”，普通用户打开换源时只看到推荐线路、来源筛选和可切换项。
- “详细”模式才展示来源可播数、在线/BT 分布、失败降级数、当前源和推荐源等诊断信息。
- 线路行在简单模式下隐藏协议和文件大小，降低工具感；失败和不可选状态仍保留明确提示。

English:

- The playback source panel now defaults to a Simple mode, showing recommended routes, source filters, and switchable options for regular viewing.
- Detailed mode exposes diagnostics such as playable counts, online/BT distribution, failed routes, current source, and recommended source.
- Route rows hide protocol and file-size details in Simple mode to reduce the tool-like feel while preserving failure and unavailable states.

### v0.2.76

中文：

- 竖屏播放页下方“正在观看”卡片弱化播放源技术信息，默认展示自动清晰度、播放状态和自动最佳线路。
- “选集”入口改为显示当前观看进度与合集总集数，“换源”入口改为显示自动最佳和可切换线路数量。
- 竖屏播放状态从 `READY/BUFFERING` 等英文标签转为中文展示，源细节只在需要换源或出现异常时展开。

English:

- The portrait watch card now hides technical source details by default, showing automatic quality, playback state, and best-route status instead.
- The episode entry now summarizes current progress and total episode count, while source switching shows best-route status and switchable route count.
- Portrait playback states now display localized Chinese labels instead of raw `READY/BUFFERING` text, with source details exposed only for switching or recovery.

### v0.2.75

中文：

- 全屏播放器底部删除重复的“弹幕/弹幕设置”功能按钮，把弹幕开关收进弹幕输入条右侧小开关。
- 点击弹幕输入条可直接打开弹幕设置，保留密度、透明度、字号和显示区域调整。
- 横屏功能栏现在优先呈现清晰度、倍速、换源、选集、缓存、更多，更贴近国内视频 App 的观看层级。

English:

- Removed duplicate Danmaku and Danmaku Settings buttons from the fullscreen bottom action row, moving the danmaku toggle into the input bar.
- Tapping the danmaku input bar now opens danmaku settings while preserving density, opacity, font scale, and display-area controls.
- The landscape action row now prioritizes quality, speed, source switching, episodes, cache, and More for a hierarchy closer to Chinese long-video apps.

### v0.2.74

中文：

- 全屏播放器“更多”面板从三列大方块改为两列紧凑设置项，减少工具面板感。
- 清晰度、倍速、选集、换源、弹幕、缓存等入口完整保留，但在横屏右侧抽屉中更易扫读。
- 移除不再使用的网格布局依赖，让播放器设置弹层更轻、更贴近国内视频 App 的设置列表。

English:

- The fullscreen player More panel now uses compact two-column setting rows instead of large three-column tiles.
- Quality, speed, episodes, source switching, danmaku, and cache actions remain available while becoming easier to scan in the landscape drawer.
- Removed the unused grid layout dependency from this panel, making the player settings overlay lighter and closer to a video-app settings list.

### v0.2.73

中文：

- 详情页播放源卡片在“已找到推荐播放源”时改为紧凑摘要，只展示当前集、推荐来源和清晰度。
- 推荐播放源详情、来源信息和诊断数据只在用户点击“换源”后展开，首屏更像追番详情页而不是线路面板。
- Loading/异常状态仍保留进度和诊断，方便判断是否正在匹配、是否需要换源。

English:

- The detail-page playback-source card now collapses into a compact summary when a recommended source is ready, showing only the episode, source, and quality.
- Recommended-source details, source information, and diagnostics now appear only after tapping Change Source, making the first screen feel more like an anime detail page than a route panel.
- Loading and error states still keep progress and diagnostics visible so users can understand matching or source issues.

### v0.2.72

中文：

- 详情页首屏去掉在线/BT/来源数量统计条，降低“线路测试工具”的观感。
- 详情页主操作统一为“播放/匹配播放源”，副操作改为“换源”，展开后才显示更细的来源与诊断信息。
- 播放器和来源面板里的旧“线路/BT 兜底”文案进一步收敛为“播放源/备用源/换源”，让术语更接近普通用户习惯。

English:

- Removed online/BT/source-count diagnostics from the detail hero to reduce the route-testing-tool feel.
- The detail primary action now reads as Play or Match Playback Source, while the secondary action becomes Change Source; detailed source diagnostics stay behind expansion.
- Legacy Route/BT Fallback wording in player and source panels was softened to Playback Source, Backup Source, and Change Source for a more user-facing vocabulary.

### v0.2.71

中文：

- 竖屏非全屏播放器底部控件进一步精简，只常驻进度、弹幕输入、弹幕开关、一个主入口和全屏按钮。
- 竖屏主入口优先显示“选集”；没有多集但存在多线路时才显示“线路”，避免把线路调试感暴露给普通观看流程。
- 倍速和完整线路管理保留在全屏控制层与播放器弹层中，形成“竖屏轻观看、横屏全功能”的层级。

English:

- The portrait non-fullscreen player controls are now more compact, keeping only progress, danmaku input, danmaku toggle, one primary action, and fullscreen.
- The portrait primary action prefers Episodes; Routes only appears when there are multiple routes but no multi-episode entry, reducing route-debug noise in normal viewing.
- Speed and full route management remain available in fullscreen controls and player panels, creating a clearer portrait-light, landscape-complete hierarchy.

### v0.2.70

中文：

- 播放器弹出的设置/线路/选集面板改为更贴近视频播放器的浮层：竖屏使用更稳定的半屏底部面板，横屏使用右侧内缩抽屉。
- 横屏面板不再贴满整条右边，保留上下和右侧留白，减少对画面的压迫感。
- 竖屏面板高度改为随屏幕比例自适应，小屏设备会自动降低最小高度，避免控件约束冲突。

English:

- Player option panels for settings, routes, and episodes now behave more like video-player overlays: a stable half-height bottom sheet in portrait and an inset right drawer in landscape.
- The landscape panel no longer fills the full right edge, leaving top, bottom, and side breathing room to reduce visual pressure on the video.
- Portrait panel height now adapts to screen size, with a smaller minimum height on compact devices to avoid layout constraint conflicts.

### v0.2.69

中文：

- 详情页展开线路后的“来源选择”从大卡片改为横向紧凑标签，减少线路区的工具感。
- 来源标签保留推荐标记、来源摘要和线路数，仍然能快速在推荐源、全部来源和其他来源之间切换。
- 这一轮继续向 Animeko 式“先选来源、再选线路”的信息层级靠拢，同时让页面更像追番详情页。

English:

- The detail-page source selector shown after expanding routes was changed from large cards to compact horizontal source tabs.
- Source tabs still show the recommended marker, source summary, and route count, while keeping quick switching between the recommended source, all sources, and other sources.
- This continues moving the route experience toward an Animeko-style source-first, route-second hierarchy while making the page feel more like an anime detail screen.

### v0.2.68

中文：

- 详情页线路加载完成后，现在会默认聚焦到自动排序出的最佳可播来源，而不是展开后先展示所有来源混合列表。
- 用户仍可点击“全部来源”查看完整线路，保留手动比较来源的能力。
- 新增 `recommendedSourceIdForRoutes()` 状态工具和单元测试，让“推荐源优先”的规则可验证、可复用。

English:

- After routes finish loading on the detail page, the route list now defaults to the best playable source selected by the automatic ranking instead of showing all sources mixed together first.
- Users can still tap All Sources to browse every route, preserving manual source comparison.
- Added a tested `recommendedSourceIdForRoutes()` UI-state helper so the source-first recommendation rule is reusable and verifiable.

### v0.2.67

中文：

- 横屏全屏播放器底部功能按钮由两行工具卡片改为更低矮的横向快捷胶囊，视觉上更接近国内视频 App 的播放控制层。
- 清晰度、倍速、线路、选集、缓存、弹幕等全屏功能完整保留，同时减少控制层对画面的遮挡。
- 故障时的“重试”和“下一线路”也沿用新的横向按钮样式，保持全屏状态下功能全面但不显得笨重。

English:

- The landscape fullscreen player action buttons were changed from tall two-line tool cards into lower horizontal quick-action pills, closer to China-market video player controls.
- Fullscreen features such as quality, speed, routes, episodes, cache, and danmaku remain available while taking less visual space over the video.
- Playback recovery actions like Retry and Next Route now share the same horizontal button style, keeping fullscreen controls complete without feeling heavy.

### v0.2.66

中文：

- 竖屏未全屏播放器底栏改成更接近国内长视频/弹幕 App 的布局：上方独立显示时间与细进度条，下方集中放弹幕输入、弹幕开关、线路、选集、倍速和全屏。
- “更多”入口不再占据竖屏主底栏，常用追番操作直接露出，用户不用先进入工具面板才能找线路或选集。
- 弹幕底部避让空间同步调整，减少控制栏出现时压住弹幕的观感问题。

English:

- The portrait non-fullscreen player bottom bar now follows a more China-market long-video/danmaku layout: time and slim progress on top, with danmaku input, danmaku toggle, routes, episodes, speed, and fullscreen below.
- The More entry no longer occupies the main portrait bar, so core anime-watching actions like route switching and episode selection are exposed directly.
- Danmaku bottom padding was adjusted to avoid comments colliding with the expanded portrait controls.

### v0.2.65

中文：

- 播放器内“播放线路”面板打开时现在会默认聚焦当前播放来源，而不是先混合展示全部来源。
- 线路列表标题改为“当前来源线路/全部来源线路”，用户能更清楚知道正在看的是当前来源下的线路还是全局线路。
- 用户仍可点击“全部来源”查看所有可用线路，保留 Animeko 式先选来源、再选线路的多源体验。

English:

- The in-player Route panel now opens focused on the current playback source instead of showing every source mixed together first.
- The route list heading now distinguishes Current Source Routes from All Source Routes, making the active filter clearer.
- Users can still tap All Sources to browse every route, preserving the Animeko-style source-first, route-second switching flow.

### v0.2.64

中文：

- 竖屏未全屏播放器点击画面后，中心控制现在只保留播放/暂停按钮，不再同时显示后退 10 秒和快进 10 秒。
- 横屏全屏播放器继续保留后退、播放/暂停、快进三件套，完整操作集中在全屏状态。
- 这一轮继续强化“竖屏轻观看、横屏全功能”的层级，减少竖屏视频画面上的按钮密度。

English:

- In portrait non-fullscreen playback, tapping the video now shows only the central play/pause button instead of also showing 10-second rewind and forward buttons.
- Landscape fullscreen playback still keeps rewind, play/pause, and forward controls, with the complete operation set concentrated in fullscreen.
- This further reinforces the lightweight portrait viewing and full-feature fullscreen control split.

### v0.2.63

中文：

- 竖屏未全屏播放器顶部覆盖层进一步精简，现在默认只保留返回按钮和轻量播放状态，不再在视频画面上重复显示完整番名和集数。
- 竖屏弹幕顶部预留同步降低，弹幕和画面可视区域更贴近真实观看状态。
- 横屏全屏仍保留完整标题、当前线路状态和退出全屏入口，继续区分“竖屏精简、全屏全面”的交互层级。

English:

- The portrait non-fullscreen top overlay is now lighter, keeping only the back button and a small playback status instead of repeating the full title and episode over the video.
- Portrait danmaku top padding was reduced to match the slimmer overlay, giving more usable video and danmaku space.
- Landscape fullscreen still keeps the full title, route status, and fullscreen exit controls, preserving the compact-portrait/full-feature-fullscreen split.

### v0.2.62

中文：

- 横屏全屏播放器移除右侧常驻快捷 Dock，避免和底部功能条重复，减少画面右侧遮挡。
- 横屏底部功能区改成图标、名称、当前值三段式按钮，集中展示弹幕、弹幕设置、清晰度、倍速、线路、选集、缓存和更多设置。
- 全屏状态继续保留完整功能入口，但视觉重心更接近 B 站式底部控制条，观看时更干净。

English:

- The landscape fullscreen player no longer shows the always-on right quick dock, avoiding duplicate controls and reducing video obstruction.
- The fullscreen bottom action area now uses icon, label, and current value buttons for danmaku, danmaku settings, quality, speed, routes, episodes, cache, and more.
- Fullscreen mode still keeps the complete control set, but the visual weight is now closer to a Bilibili-style bottom control strip.

### v0.2.61

中文：

- 竖屏观看页默认信息层级改为“番名、当前集、当前源、选集/换线路入口”优先，弱化协议、线路条数等工程化信息。
- 竖屏播放信息卡里的“换源”改为更明确的“换线路”，副标题显示当前来源，符合国内长视频 App 的理解习惯。
- 设置页展示版本号同步到 0.2.61，避免应用内版本信息继续停留在旧原型阶段。

English:

- The portrait watch page now prioritizes title, current episode, current source, and episode/source actions instead of exposing protocol and route details first.
- The portrait source action now says Switch Route with the current source as context, matching Chinese long-form video app wording more closely.
- The in-app settings version label now matches 0.2.61 instead of the old prototype version.

### v0.2.60

中文：

- 详情页顶部“立即观看/播放第 X 集”现在会尊重当前来源筛选，用户选中某个来源后会优先播放该来源下的推荐线路。
- 详情页线路筛选文案统一为“按来源筛选/全部来源”，和播放器内线路面板保持一致。
- 进播放前和进播放后的换源逻辑进一步对齐，减少“详情页选了来源但播放又跳到其他源”的割裂感。

English:

- The detail-page Watch Now / Play Episode action now respects the current source filter and prefers the best route from the selected source.
- Detail-page source filter wording now uses Source Filter / All Sources, matching the in-player source panel.
- Source selection is more consistent before and after entering playback, reducing cases where a selected source unexpectedly switches to another source.

### v0.2.59

中文：

- 播放线路面板新增“按来源筛选”，可直接点击“全部来源”或某个来源卡，只查看对应来源下的线路。
- 线路面板顶部文案从“线路诊断”收敛为“自动推荐”，保留推荐/当前/失败信息，但减少调试工具感。
- 换源路径更接近 Animeko 式多源体验：先选来源，再选具体线路，长列表不再混在一起。

English:

- The playback source panel now supports source filtering. Users can tap All Sources or a specific source card to view only routes from that source.
- The panel headline now says Auto Recommendation instead of Route Diagnostics, keeping recommendation/current/failure context with less tester-like wording.
- Source switching is closer to Animeko-style multi-source browsing: choose a source first, then pick a concrete route instead of scanning one mixed list.

### v0.2.58

中文：

- 竖屏播放器的“更多/播放设置”面板新增当前播放摘要，直接展示当前源、线路、清晰度、倍速、线路数和集数。
- 清晰度、倍速、选集、线路、弹幕和缓存仍保留在同一面板中，竖屏主控保持精简但点开后信息更明确。
- 这轮继续把播放器从“功能测试面板”收敛成国内用户熟悉的观看设置面板，减少切换线路和调整播放参数时的迷路感。

English:

- The portrait player More/settings panel now starts with a current playback summary, showing source, route, quality, speed, route count, and episode count.
- Quality, speed, episodes, sources, danmaku, and cache remain in one panel, keeping portrait controls lightweight while making the expanded state clearer.
- This continues moving the player away from a feature-testing panel toward a familiar Chinese video-app playback settings sheet.

### v0.2.57

中文：

- 横屏全屏顶部的线路状态从大卡片收敛为小胶囊入口，保留“当前状态 + 当前源 + 换源”但减少遮挡。
- 横屏底部正常播放时不再常驻展示线路详情，只在换源提示或播放错误时弹出轻量提示条。
- 继续按国内长视频 App 的控制层习惯推进：竖屏保持精简，全屏保留完整功能，但默认观看态更干净。

English:

- The landscape fullscreen source status is now a compact pill instead of a large card, keeping status, current source, and source switching with less video obstruction.
- The landscape bottom layer no longer keeps route details visible during normal playback. A lightweight notice strip appears only for source-switch notices or playback errors.
- This continues the Chinese long-form video app direction: portrait stays lightweight, fullscreen stays feature-rich, and passive viewing is cleaner by default.

### v0.2.56

中文：

- 详情页线路状态卡默认收起深度诊断信息，可播状态下只保留推荐播放、当前选集和查看线路入口。
- 只有在展开线路、加载中、无可播线路或失败时，才显示在线优先、BT 兜底、来源数和失败数等诊断细节。
- 详情页从“线路测试面板”继续收敛为“番剧详情 + 立即观看”的观感，同时保留 Animeko 式多源选择能力。

English:

- The detail page route card now hides deep diagnostics by default. In playable state it keeps only the recommended playback, selected episode, and route entry.
- Online-first, BT fallback, source count, and failure diagnostics appear only when routes are expanded, loading, empty, or failed.
- The detail page moves further from a route-testing panel toward an anime detail + watch-now experience while keeping Animeko-style multi-source selection.

### v0.2.55

中文：

- 横屏全屏播放器新增控制锁定按钮，锁定后隐藏顶部、中心、底部和侧边快捷控制，只保留左侧解锁入口。
- 锁定状态下点击画面不会重新展开控制层，减少横屏握持时误触进度、换源、返回等高风险操作。
- 按返回键会优先解除锁定，退出全屏时也会自动恢复普通控制状态，保持全屏功能完整但观看态更稳。

English:

- Added a control-lock button to the landscape fullscreen player. When locked, top, center, bottom, and side controls are hidden, leaving only a left-side unlock entry.
- Tapping the video while locked no longer reopens the controls, reducing accidental seeks, route switches, or back actions during landscape viewing.
- Back first unlocks the controls, and leaving fullscreen resets the player to the normal control state, keeping fullscreen powerful while making passive viewing steadier.

### v0.2.54

中文：

- 横屏全屏播放器底部改为单层操作区：左侧保留弹幕输入，右侧集中放弹幕、清晰度、倍速、线路、选集、缓存和更多。
- 减少全屏底部原本“输入条一行 + 功能按钮一行”的纵向占用，播放画面遮挡更少。
- 继续保持竖屏未全屏精简、横屏全屏功能全面的分层逻辑，更贴近国内视频 App 的播放器排布。

English:

- The landscape fullscreen bottom controls now use a single operation layer: danmaku input on the left and danmaku, quality, speed, source, episode, cache, and more actions on the right.
- Reduced the previous two-row bottom footprint to avoid covering too much of the video.
- This keeps portrait non-fullscreen lightweight while making landscape fullscreen complete, closer to familiar Chinese video-app player layouts.

### v0.2.53

中文：

- 竖屏未全屏播放页移除独立的合集工具卡，把当前集、当前源、画质/协议和线路数量合并到一个轻量播放信息条。
- 播放信息条新增“选集”和“换源”快捷入口，只有存在多集或多线路时展示，避免竖屏观看态堆满功能按钮。
- 线路诊断、失败提示和重试/下一线路仍只在异常时展开，保持竖屏精简、全屏功能全面的播放器分层。

English:

- Removed the separate collection utility card from the portrait non-fullscreen watch page and merged episode, source, quality/protocol, and route count into one lightweight playback strip.
- Added compact Episode and Source shortcuts to the strip, shown only when multiple episodes or routes exist to keep portrait viewing uncluttered.
- Route diagnostics, failure messages, retry, and next-route controls still expand only on playback issues, preserving lightweight portrait mode and full-feature fullscreen mode.

### v0.2.52

中文：

- 全屏播放器的选集面板顶部新增当前播放状态头，集中展示番名、当前集、总集数、当前源、画质/协议和线路数量。
- 换集前就能在同一个面板里确认当前使用的来源，减少在“选集”和“线路”面板之间来回切换。
- 本轮不做 HTML 原型，继续直接改原生 Compose 播放器 UI，方向更贴近 B站/腾讯/爱奇艺等国内视频 App 的使用习惯。

English:

- Added a current playback summary header to the fullscreen episode panel, showing title, current episode, total episodes, source, quality/protocol, and route count.
- Users can confirm the active source before switching episodes without bouncing between the episode and route panels.
- This update continues in native Compose UI instead of HTML prototypes, moving the player closer to familiar Chinese video-app interaction patterns.

### v0.2.51

中文：

- 播放页切换剧集成功后会显示短提示，明确告知本集是沿用当前来源还是回退到其他来源。
- 线路提示会在播放稳定后短暂保留再自动收起，避免一闪而过，也避免长期遮挡画面。
- 切集时会先缓存原始来源信息，异步解析完成后仍能准确判断是否真正沿用了原来源。

English:

- Episode switching now shows a short confirmation telling whether the new episode reused the current source or fell back to another source.
- Route notices remain visible briefly after playback stabilizes, avoiding both flicker and long-lived obstruction.
- The previous source is captured before async route resolution so the reuse/fallback message stays accurate.

### v0.2.50

中文：

- 播放页切换剧集时会优先沿用当前播放来源，减少每集都跳回默认线路的割裂感。
- 若当前来源在下一集不可播，会自动回退到排序后的最佳可播线路，继续保持在线源优先和 WebView-only 过滤。
- 新增线路优选单元测试，覆盖同来源延续和不可播来源回退。

English:

- Episode switching in the player now prefers the currently used source, reducing jarring route jumps between episodes.
- If the current source is not playable for the next episode, playback falls back to the best sorted playable route while keeping online-first and WebView-only filtering.
- Added route preference unit tests covering same-source continuity and fallback from unplayable sources.

### v0.2.49

中文：

- 竖屏未全屏播放器的轻量控制条新增细进度线，保留国内视频 App 常见的播放进度感。
- 进度线只展示当前播放进度，不增加额外按钮，继续保持竖屏观看态精简。
- 全屏播放器仍保留完整进度拖动、清晰度、倍速、线路、选集和弹幕设置入口。

English:

- Added a thin progress line to the portrait non-fullscreen lightweight controls for a familiar Chinese video-app playback feel.
- The progress line is informational only and does not add extra buttons, keeping portrait viewing compact.
- Fullscreen playback continues to provide full seeking, quality, speed, route, episode, and danmaku controls.

### v0.2.48

中文：

- 竖屏未全屏观看页默认收起线路统计和分布信息，让页面更像正常追番观看页。
- 当前播放卡片改为轻量状态条，只保留播放源、画质和必要的换源入口。
- 线路分布、失败提示和恢复按钮只在切源提示或播放异常时显示，完整线路能力继续放在全屏和面板里。

English:

- The portrait non-fullscreen watch page now hides route statistics and distribution details during normal playback.
- The current playback card is simplified into a lightweight status strip with source, quality, and the necessary switch-source entry.
- Route distribution, failure notices, and recovery actions only appear when route switching or playback issues need attention, while full route controls remain in fullscreen and panels.

### v0.2.47

中文：

- 横屏全屏顶部的当前线路状态胶囊现在可直接点击打开播放线路面板。
- 状态胶囊新增“换源”尾标，把线路状态从纯展示升级为全屏高频换源入口。
- 竖屏未全屏仍不增加额外线路按钮，保持轻量观看；完整换源能力继续集中在全屏和面板中。

English:

- The landscape fullscreen current-route status capsule can now open the route panel directly.
- Added a “Switch Source” tail label to the status capsule, turning route status from passive information into a high-frequency fullscreen route entry.
- The portrait non-fullscreen layout remains lightweight without extra route buttons, while full route switching stays in fullscreen and panels.

### v0.2.46

中文：

- 详情页海报首屏新增“即将播放”决策条，直接显示当前集、自动推荐来源和推荐线路详情。
- 自动找源、线路异常、暂无线路等状态会在首屏用不同颜色和文案提示，用户点播放前就能判断下一步会发生什么。
- 保持详情页原有线路排序和播放逻辑不变，只把 Animeko 式自动推荐结果前置到首屏。

English:

- Added an Up Next decision strip to the detail hero, showing the selected episode, auto-recommended source, and recommended route details.
- Auto-matching, route errors, and empty-route states now surface directly in the first viewport with distinct labels and colors before the user taps play.
- Kept the existing route sorting and playback behavior unchanged while moving the Animeko-style recommendation result into the hero.

### v0.2.45

中文：

- 播放页线路面板新增“来源概览”，按来源展示当前源、推荐源、可播数量、在线/BT 分布和失败降级数量。
- 手动换源更接近 Animeko 式聚合体验：用户先判断来源质量，再进入具体线路列表。
- 不改变自动线路排序和播放选择逻辑，只增强手动切源时的信息层级与可读性。

English:

- Added a Source Overview strip to the player route panel, showing the current source, recommended source, playable counts, online/BT distribution, and failed-route downgrade counts.
- Manual source switching now feels closer to Animeko-style aggregation: users can evaluate sources before choosing a specific route.
- Kept the existing automatic route sorting and playback selection unchanged while improving manual route-selection hierarchy and readability.

### v0.2.44

中文：

- 竖屏未全屏播放页的“当前线路”卡新增聚合状态条，展示线路数量、来源数量、在线/BT 分布和当前协议。
- 保持视频画面上的竖屏控制层精简，把线路透明度放在视频下方信息区，避免遮挡观看。
- 竖屏播放页更接近 Animeko 式聚合体验：用户不用展开线路面板，也能知道自动匹配到了哪些资源类型。

English:

- Added an aggregation status strip to the portrait non-fullscreen Current Route card, showing route count, source count, online/BT distribution, and the active protocol.
- Kept the portrait video overlay lightweight by placing route transparency below the video instead of covering playback.
- The portrait watch page now feels closer to Animeko-style aggregation: users can understand what the auto matcher found without opening the route panel.

### v0.2.43

中文：

- 横屏全屏右侧快捷 Dock 调整为国内播放器常见的常用控制层：弹幕、清晰、倍速、线路、选集、更多。
- 缓存入口保留在底部横向控制栏和“更多”面板中，避免右侧固定 Dock 显得像下载工具。
- 全屏底部横向控制栏新增“更多”入口，复杂播放设置统一收拢，竖屏未全屏状态继续保持精简。

English:

- Reworked the landscape fullscreen quick dock into a familiar Chinese video-player control stack: danmaku, quality, speed, routes, episodes, and More.
- Kept caching available from the bottom action row and More panel while removing it from the fixed right dock so the player feels less like a downloader.
- Added a More entry to the fullscreen bottom action row, keeping advanced playback settings grouped while the portrait non-fullscreen layout stays lightweight.

### v0.2.42

中文：

- 横屏全屏播放器顶部从单一线路标签升级为“当前线路 + 播放状态”的紧凑状态胶囊，更贴近国内长视频播放器的信息层级。
- 顶部状态会区分播放中、切源中和异常状态，让用户进入全屏后能快速判断当前线路是否稳定。
- 新增播放器状态模型测试，覆盖正常播放、切源提示和播放异常三类短状态标签。

English:

- Upgraded the landscape fullscreen player top bar from a single route pill to a compact current-route plus playback-status capsule, closer to Chinese long-form video player hierarchy.
- The top status now distinguishes playing, route switching, and error states so fullscreen playback communicates route stability faster.
- Added player overlay model coverage for normal playback, route-switch notices, and playback error status labels.

### v0.2.41

中文：

- 竖屏未全屏播放器底部控制行继续做轻量化：将“更多”和“全屏”文字按钮改成三点和全屏图标入口。
- 保留弹幕输入条、时间和弹幕开关的精简结构，未全屏状态不增加清晰度、线路、选集等重控件。
- 故障恢复场景的“重试/下一线路”文字按钮继续保留，确保异常状态下操作语义明确。

English:

- Refined the portrait non-fullscreen bottom control row by replacing the More and Fullscreen text buttons with compact icon actions.
- Kept the lightweight portrait structure around time, danmaku input, and danmaku toggle without adding heavy quality, route, or episode controls.
- Left the Retry and Next Route text actions in recovery states so error handling remains explicit.

### v0.2.40

中文：

- 详情页展开线路时新增“线路来源”标题区，直接显示来源数量、线路数量、推荐来源和当前筛选。
- 来源筛选卡在推荐源被选中时会标记为“推荐 · 当前”，用户切源时能更清楚当前看到的是哪一组线路。
- 保持自动推荐和线路排序逻辑不变，只增强详情页手动选源时的上下文和可读性。

English:

- Added a Route Sources header when the detail route list is expanded, showing source count, route count, the recommended source, and the current filter.
- Source filter cards now show “recommended + current” when the recommended source is selected, making manual source switching easier to understand.
- Kept the existing auto-recommendation and route sorting behavior unchanged while improving the context around manual source selection.

### v0.2.39

中文：

- 详情页线路诊断卡新增“推荐源摘要”：不展开线路列表也能看到当前自动推荐来源、画质/线路名和播放方式。
- 线路体验更接近 Animeko 式聚合逻辑：App 自动选择最佳线路，但把来源和播放方式明确展示给用户。
- 保持现有自动匹配和播放逻辑不变，只增强详情页对“正在用哪个源播放”的可读性。

English:

- Added a recommended-source summary to the detail route diagnostics card, showing the selected source, quality/route label, and playback protocol before the route list is expanded.
- The route experience is closer to Animeko-style aggregation: the app still chooses the best route automatically while making the source and playback mode explicit.
- Kept the existing matching and playback logic unchanged, focusing this update on detail-page route transparency.

### v0.2.38

中文：

- 横屏全屏右侧快捷 dock 增加实时状态：线路数、选集数量、弹幕开关、当前清晰度、倍速、缓存可用性会直接显示在入口上。
- 全屏入口继续保持完整功能密度，但每个按钮改成“图标 + 功能名 + 当前值”的结构，减少用户打开面板前的猜测。
- 竖屏未全屏播放层不增加额外按钮，继续保持精简观看，只把完整操作留给横屏全屏和播放器面板。

English:

- Added live state labels to the landscape fullscreen quick dock: route count, episode count, danmaku state, current quality, playback speed, and cache availability are now visible on the shortcuts.
- Kept fullscreen as the dense-control mode, but each shortcut now uses an icon, action name, and current value so users can understand state before opening a panel.
- Portrait non-fullscreen playback stays lightweight with no extra controls; full operations remain in landscape fullscreen and player drawers.

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
