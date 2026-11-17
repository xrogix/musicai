package com.music.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MusicAccent,
    onPrimary = Color(0xFF09090D),
    secondary = MusicAccentLight,
    tertiary = Color(0xFFFFB5A7),
    background = Color(0xFF09090D),
    onBackground = Color(0xFFF3F7F8),
    surface = Color(0xFF111117),
    onSurface = Color(0xFFF3F7F8),
    surfaceVariant = MusicSurfaceVariant,
    onSurfaceVariant = MusicOnSurfaceVariant,
    outline = MusicOutline,
)

@Composable
fun MusicAITheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
