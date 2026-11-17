package com.music.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.music.myapplication.ui.navigation.MusicNavHost
import com.music.myapplication.ui.navigation.MusicSessionViewModel
import com.music.myapplication.ui.theme.MusicAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Match the launch window to the Compose splash while the first frame is prepared.
        window.setBackgroundDrawableResource(R.drawable.splash_background)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
        )
        setContent {
            val session: MusicSessionViewModel = hiltViewModel()
            MusicAITheme {
                MusicNavHost(session = session)
            }
        }
    }
}
