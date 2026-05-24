package com.zfbml.aggregate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.zfbml.aggregate.ui.AggregateApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val graph = (application as ZfbmlApp).graph
        val initialQuery = intent.getStringExtra(EXTRA_INITIAL_QUERY)
            ?: intent.dataString
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFF5C8A),
                    secondary = Color(0xFF32D3E6),
                    background = Color(0xFF0D0D10),
                    surface = Color(0xFF18181C),
                ),
            ) {
                AggregateApp(graph, initialQuery = initialQuery)
            }
        }
    }

    private companion object {
        const val EXTRA_INITIAL_QUERY = "zfbml.query"
    }
}
