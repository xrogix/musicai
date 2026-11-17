package com.music.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.music.myapplication.ui.album.AlbumScreen
import com.music.myapplication.ui.album.AlbumViewModel
import com.music.myapplication.ui.home.HomeScreen
import com.music.myapplication.ui.home.HomeViewModel
import com.music.myapplication.ui.player.PlayerScreen
import com.music.myapplication.ui.player.PlayerViewModel
import com.music.myapplication.R
import kotlinx.coroutines.delay

@Composable
fun MusicNavHost(
    session: MusicSessionViewModel,
    navController: NavHostController = rememberNavController(),
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        NavHost(
            navController = navController,
            startDestination = MusicDestinations.SPLASH,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(MusicDestinations.SPLASH) {
                SplashScreen()
                LaunchedEffect(Unit) {
                    delay(SPLASH_DURATION_MILLIS)
                    navController.navigate(MusicDestinations.HOME) {
                        popUpTo(MusicDestinations.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable(MusicDestinations.HOME) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onPlaySong = { song, queue ->
                        session.selectSong(song, queue)
                        navController.navigate(MusicDestinations.PLAYER) { launchSingleTop = true }
                    },
                    onViewAlbum = { song ->
                        session.selectAlbum(song.albumId)
                        navController.navigate(MusicDestinations.ALBUM)
                    },
                )
            }
            composable(MusicDestinations.PLAYER) {
                val viewModel: PlayerViewModel = hiltViewModel()
                val sessionState by session.state.collectAsStateWithLifecycle()
                val song = sessionState.selectedSong
                if (song == null) {
                    EmptyNavigationState(stringResource(R.string.player_no_song_selected)) {
                        navController.popBackStack()
                    }
                } else {
                    PlayerScreen(
                        song = song,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onPrevious = { session.move(-1)?.let(viewModel::open) },
                        onNext = { session.move(1)?.let(viewModel::open) },
                        onViewAlbum = {
                            viewModel.pause()
                            session.selectAlbum(song.albumId)
                            navController.navigate(MusicDestinations.ALBUM)
                        },
                    )
                }
            }
            composable(MusicDestinations.ALBUM) {
                val viewModel: AlbumViewModel = hiltViewModel()
                val sessionState by session.state.collectAsStateWithLifecycle()
                AlbumScreen(
                    albumId = sessionState.selectedAlbumId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onPlaySong = { song, queue ->
                        session.selectSong(song, queue)
                        navController.popBackStack()
                        navController.navigate(MusicDestinations.PLAYER) { launchSingleTop = true }
                    },
                )
            }
        }
    }
}

private const val SPLASH_DURATION_MILLIS = 1_150L

@Composable
private fun EmptyNavigationState(message: String, onBack: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.material3.TextButton(onClick = onBack) { Text(stringResource(R.string.navigation_back)) }
    }
}
