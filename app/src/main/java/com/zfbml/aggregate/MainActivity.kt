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
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00A884),
                    secondary = Color(0xFF80CBC4),
                    background = Color(0xFF101418),
                    surface = Color(0xFF171C20),
                ),
            ) {
                AggregateApp(graph)
            }
        }
    }
}
