package com.zfbml.aggregate.player

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.zfbml.aggregate.R

@OptIn(UnstableApi::class)
@Composable
fun PlayerViewSurface(
    engine: ExoPlayerEngine,
    onSurfaceTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val currentSurfaceTap by rememberUpdatedState(onSurfaceTap)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            (LayoutInflater.from(context).inflate(R.layout.view_player, null) as PlayerView).apply {
                useController = false
                keepScreenOn = true
                setShutterBackgroundColor(Color.TRANSPARENT)
                player = engine.player
                installSurfaceTapForwarder(currentSurfaceTap)
            }
        },
        update = { view ->
            if (view.player !== engine.player) {
                view.player = engine.player
            }
            view.installSurfaceTapForwarder(currentSurfaceTap)
        },
    )
}

private fun View.installSurfaceTapForwarder(onSurfaceTap: (() -> Unit)?) {
    isClickable = onSurfaceTap != null
    setOnTouchListener(
        onSurfaceTap?.let { tap ->
            View.OnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> true
                    MotionEvent.ACTION_UP -> {
                        tap()
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> true
                    else -> false
                }
            }
        },
    )
    if (this is ViewGroup) {
        for (index in 0 until childCount) {
            getChildAt(index).installSurfaceTapForwarder(onSurfaceTap)
        }
    }
}
