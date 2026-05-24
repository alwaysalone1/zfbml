package com.zfbml.aggregate.player

import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.zfbml.aggregate.R

@OptIn(UnstableApi::class)
@Composable
fun PlayerViewSurface(
    engine: ExoPlayerEngine,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            (LayoutInflater.from(context).inflate(R.layout.view_player, null) as PlayerView).apply {
                useController = false
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
