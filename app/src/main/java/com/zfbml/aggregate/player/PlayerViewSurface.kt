package com.zfbml.aggregate.player

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
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
    onSurfaceTap: (() -> Unit)? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            (LayoutInflater.from(context).inflate(R.layout.view_player, null) as PlayerView).apply {
                useController = false
                keepScreenOn = true
                setShutterBackgroundColor(Color.TRANSPARENT)
                bindSurfaceTap(onSurfaceTap)
                player = engine.player
            }
        },
        update = { view ->
            if (view.player !== engine.player) {
                view.player = engine.player
            }
            view.bindSurfaceTap(onSurfaceTap)
        },
    )
}

private fun PlayerView.bindSurfaceTap(onSurfaceTap: (() -> Unit)?) {
    isClickable = onSurfaceTap != null
    setOnTouchListener { _, event ->
        if (onSurfaceTap != null && event.action == MotionEvent.ACTION_UP) {
            onSurfaceTap()
        }
        onSurfaceTap != null
    }
}
