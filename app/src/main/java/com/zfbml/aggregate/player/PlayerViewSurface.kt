package com.zfbml.aggregate.player

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
    val enabled = onSurfaceTap != null
    val listener = View.OnTouchListener { _, event ->
        if (onSurfaceTap != null && event.action == MotionEvent.ACTION_UP) {
            onSurfaceTap()
        }
        enabled
    }
    val touchListener = if (enabled) listener else null
    bindTapListenerRecursive(touchListener)
    post { bindTapListenerRecursive(touchListener) }
}

private fun View.bindTapListenerRecursive(listener: View.OnTouchListener?) {
    isClickable = listener != null
    setOnTouchListener(listener)
    if (this is ViewGroup) {
        setOnHierarchyChangeListener(
            if (listener != null) {
                object : ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: View?, child: View?) {
                        child?.bindTapListenerRecursive(listener)
                    }

                    override fun onChildViewRemoved(parent: View?, child: View?) = Unit
                }
            } else {
                null
            },
        )
        for (index in 0 until childCount) {
            getChildAt(index).bindTapListenerRecursive(listener)
        }
    }
}
