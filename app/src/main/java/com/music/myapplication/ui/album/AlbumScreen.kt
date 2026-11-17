package com.music.myapplication.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.myapplication.R
import com.music.myapplication.domain.model.Album
import com.music.myapplication.domain.model.Song
import com.music.myapplication.ui.components.EmptyState
import com.music.myapplication.ui.components.MusicTopBar
import com.music.myapplication.ui.components.SongArtwork
import com.music.myapplication.ui.components.SongRow
import com.music.myapplication.ui.theme.MusicArtworkDark

@Composable
fun AlbumScreen(
    albumId: Long?,
    viewModel: AlbumViewModel,
    onBack: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(albumId) { viewModel.load(albumId) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MusicArtworkDark, Color(0xFF09090D)), endY = 900f)),
    ) {
        val title = (state as? AlbumUiState.Content)?.album?.title ?: stringResource(R.string.album_default_title)
        MusicTopBar(
            title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.album_back))
                }
            },
        )

        when (val content = state) {
            AlbumUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
            AlbumUiState.Missing -> EmptyState(
                stringResource(R.string.album_unavailable_title),
                stringResource(R.string.album_unavailable_message),
                Modifier.fillMaxSize(),
            )
            is AlbumUiState.Error -> EmptyState(
                stringResource(R.string.album_load_error_title),
                stringResource(R.string.album_error_message),
                Modifier.fillMaxSize(),
            )
            is AlbumUiState.Content -> AlbumContent(content.album, onPlaySong)
        }
    }
}

@Composable
private fun AlbumContent(album: Album, onPlaySong: (Song, List<Song>) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 21.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SongArtwork(
                url = album.artworkUrl,
                contentDescription = stringResource(R.string.artwork_album_description, album.title),
                size = 128.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = album.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = album.artistName,
                modifier = Modifier.padding(top = 5.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(40.dp))
        if (album.tracks.isEmpty()) {
            EmptyState(
                stringResource(R.string.album_no_tracks_title),
                stringResource(R.string.album_no_tracks_message),
                Modifier.weight(1f),
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(album.tracks, key = { _, song -> song.id }) { index, song ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.album_track_number, index + 1), modifier = Modifier.padding(end = 11.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                        SongRow(
                            song = song,
                            onClick = { onPlaySong(song, album.tracks) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
