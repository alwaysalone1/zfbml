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

## Search, Index, And Overlay Findings

- Bilibili exposes search as a platform surface, not a plain text box: its resource map includes search, bangumi, player, and danmaku preference surfaces that point to scoped discovery and visible playback decisions.
- Dandanplay's manifest separates `SearchActivity`, `SearchAdvancedActivity`, anime detail, cache, and danmaku-source settings. For ZFBML this argues for making source scope and result source visible before the user opens detail.
- Tencent Video and Youku both show large-app indexing patterns around search, preload, detail preloading, download/cache, and player services. The matching work should not be hidden as diagnostics; it should be summarized in normal UI.
- ZFBML implementation direction: the search page should show searchable source count, failed sources, result count, and quick source filters. Result filtering should be a first-class index interaction, not a debug message.
- Updated player direction: fullscreen controls, danmaku, route panels, and transient notices need explicit anti-obstruction rules so they do not cover the decisive video area or fight with each other.

## Current Implementation Focus

This pass exposes route prefetching in the detail page. The app already warms nearby episodes through `SourceRegistry.prefetchRouteCandidates`; the UI now surfaces whether adjacent episodes are warming, warmed, queued, or waiting for fallback source coverage.

The next pass after route prefetching adds visible search source coverage and per-source result filtering, based on the search/index findings above.

This pass adds player anti-obstruction support for danmaku. `DanmakuSafeArea` lets the layout engine reserve top, bottom, start, and end zones, and the player now derives those zones from visible controls, fullscreen side dock, option panels, lock state, and route/error notices.
