// #member1
// App entry point: sets the theme and hands off to the nav graph.
package com.example.mini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mini.nav.AppNavGraph
import com.example.mini.ui.theme.MINITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MINITheme {
                AppNavGraph()
            }
        }
    }
}
