package com.zfbml.aggregate.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun PlayerViewSurface(
    engine: ExoPlayerEngine,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                useController = true
                keepScreenOn = true
                player = engine.player
            }
        },
        update = { view ->
            if (view.player !== engine.player) {
                view.player = engine.player
            }
        },
    )
}
