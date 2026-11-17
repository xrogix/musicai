package com.music.myapplication.ui.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.myapplication.R
import com.music.myapplication.domain.model.Song
import com.music.myapplication.domain.model.SongSearchSource
import com.music.myapplication.domain.model.SongSearchState
import com.music.myapplication.ui.components.EmptyState
import com.music.myapplication.ui.components.MusicTopBar
import com.music.myapplication.ui.components.SectionHeading
import com.music.myapplication.ui.components.SongArtwork
import com.music.myapplication.ui.components.SongRow
import com.music.myapplication.ui.theme.MusicField
import com.music.myapplication.ui.theme.MusicSheet

private val Ink = Color(0xFF09090D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPlaySong: (Song, List<Song>) -> Unit,
    onViewAlbum: (Song) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val searchFocusRequester = remember { FocusRequester() }
    val searchToggleDescription = stringResource(if (searchExpanded) R.string.search_close else R.string.search_open)
    val searchLabel = stringResource(R.string.search_label)

    LaunchedEffect(searchExpanded) {
        if (searchExpanded) searchFocusRequester.requestFocus() else keyboard?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        MusicTopBar(
            title = { Text(stringResource(R.string.home_title), style = MaterialTheme.typography.titleLarge) },
            actions = {
                IconButton(
                    onClick = { searchExpanded = !searchExpanded },
                    modifier = Modifier.semantics {
                        contentDescription = searchToggleDescription
                    },
                ) {
                    Icon(
                        painter = painterResource(if (searchExpanded) R.drawable.ic_close else R.drawable.ic_search),
                        contentDescription = null,
                    )
                }
            },
        )

        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            if (searchExpanded) {
                TextField(
                    value = state.query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester)
                        .semantics { contentDescription = searchLabel },
                    placeholder = { Text(searchLabel) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); viewModel.search() }),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedContainerColor = MusicField,
                        unfocusedContainerColor = MusicField,
                    ),
                )
                Spacer(Modifier.height(12.dp))
            }

            when (val search = state.search) {
                SongSearchState.Idle -> RecentSongs(state.recentSongs, onPlaySong, onMore = { selectedSong = it })
                is SongSearchState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.search_loading, search.query), modifier = Modifier.padding(top = 14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is SongSearchState.Error -> {
                    if (state.recentSongs.isNotEmpty()) {
                        Column(Modifier.fillMaxSize()) {
                            Text(stringResource(R.string.search_connection_error), color = Color(0xFFFFB5A7), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(20.dp))
                            RecentSongs(state.recentSongs, onPlaySong, onMore = { selectedSong = it })
                        }
                    } else {
                        EmptyState(
                            stringResource(R.string.search_unavailable_title),
                            stringResource(R.string.search_error_message),
                            Modifier.weight(1f),
                        )
                    }
                }
                is SongSearchState.Results -> SearchResults(search, onPlaySong, onMore = { selectedSong = it }, onLoadMore = viewModel::loadMore)
            }
        }
    }

    selectedSong?.let { song ->
        ModalBottomSheet(
            onDismissRequest = { selectedSong = null },
            containerColor = MusicSheet,
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SongArtwork(
                    url = song.artworkUrl,
                    contentDescription = stringResource(R.string.artwork_album_description, song.albumTitle ?: song.title),
                    size = 64.dp
                )
                Text(
                    text = song.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = song.artistName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { selectedSong = null; onViewAlbum(song) },
                    enabled = song.albumId != null,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 22.dp),
                ) {
                    Text(
                        text = stringResource(R.string.album_action_view),
                        color = if (song.albumId != null)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSongs(
    songs: List<Song>,
    onPlay: (Song, List<Song>) -> Unit,
    onMore: (Song) -> Unit,
) {
    Spacer(Modifier.height(8.dp))
    if (songs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            EmptyState(stringResource(R.string.search_start_title), stringResource(R.string.search_start_message))
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(songs, key = Song::id) { song ->
                SongRow(song = song, onClick = { onPlay(song, songs) }, onMore = { onMore(song) })
            }
        }
    }
}

@Composable
private fun SearchResults(
    search: SongSearchState.Results,
    onPlay: (Song, List<Song>) -> Unit,
    onMore: (Song) -> Unit,
    onLoadMore: () -> Unit,
) {
    val songs = search.visibleSongs
    Column(Modifier.fillMaxSize()) {
        SectionHeading(
            stringResource(R.string.search_results_title),
            stringResource(R.string.search_results_count, songs.size, search.result.songs.size),
        )
        Text(
            stringResource(if (search.result.source == SongSearchSource.CACHE) R.string.search_source_cache else R.string.search_source_catalog),
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        if (songs.isEmpty()) {
            EmptyState(
                stringResource(R.string.search_no_results_title),
                stringResource(R.string.search_no_results_message),
                Modifier.weight(1f),
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 18.dp)) {
                items(songs, key = Song::id) { song ->
                    SongRow(song = song, onClick = { onPlay(song, songs) }, onMore = { onMore(song) })
                }
            }
        }
        if (search.canLoadMore) {
            TextButton(onClick = onLoadMore, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.search_load_more), color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

private fun stateSongs(state: HomeUiState): List<Song> =
    (state.search as? SongSearchState.Results)?.visibleSongs ?: state.recentSongs
