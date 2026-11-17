package com.music.myapplication.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.myapplication.domain.model.Song
import com.music.myapplication.ui.components.MusicTopBar
import com.music.myapplication.ui.components.SongArtwork
import com.music.myapplication.ui.theme.MusicArtworkDark
import com.music.myapplication.ui.theme.MusicMutedTrack
import com.music.myapplication.R

private val Background = Color(0xFF09090D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: Song,
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onViewAlbum: () -> Unit,
) {
    val playback by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val hasPreview = !song.previewUrl.isNullOrBlank()
    val storeUrl = song.storeUrl
    var menuExpanded by remember { mutableStateOf(false) }
    var sliderPositionMillis by remember(song.id) {
        mutableFloatStateOf(playback.positionMillis.toFloat())
    }
    var isDraggingSlider by remember(song.id) { mutableStateOf(false) }
    var pendingSeekMillis by remember(song.id) { mutableStateOf<Long?>(null) }
    val durationMillis = playback.durationMillis.coerceAtLeast(1)
    val displayedPositionMillis = if (isDraggingSlider || pendingSeekMillis != null) {
        sliderPositionMillis.toLong()
    } else {
        playback.positionMillis
    }

    LaunchedEffect(
        song.id,
        playback.positionMillis,
        playback.durationMillis,
        isDraggingSlider,
        pendingSeekMillis,
    ) {
        if (isDraggingSlider) return@LaunchedEffect

        val pendingSeek = pendingSeekMillis
        val playerReachedPendingSeek = pendingSeek == null ||
            (playback.positionMillis - pendingSeek) in -500L..500L

        if (playerReachedPendingSeek) {
            sliderPositionMillis = playback.positionMillis.toFloat()
            if (pendingSeek != null) pendingSeekMillis = null
        } else {
            sliderPositionMillis = pendingSeek.toFloat()
        }
    }

    LaunchedEffect(song.id) { viewModel.open(song) }
    DisposableEffect(song.id) {
        onDispose { viewModel.pause() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MusicArtworkDark, Background), endY = 1050f)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MusicTopBar(
            title = { Text(stringResource(R.string.player_title), style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.semanticsLabel(stringResource(R.string.navigation_back))) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null,
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.semanticsLabel(stringResource(R.string.player_more_options)),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = null,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_view_album)) },
                        enabled = song.albumId != null,
                        onClick = { menuExpanded = false; onViewAlbum() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_open_apple_music)) },
                        enabled = !storeUrl.isNullOrBlank(),
                        onClick = {
                            menuExpanded = false
                            storeUrl?.let { url -> runCatching { uriHandler.openUri(url) } }
                        },
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SongArtwork(
                song.artworkUrl,
                stringResource(R.string.artwork_caption, song.albumTitle ?: song.title),
                modifier = Modifier.padding(top = 100.dp),
                size = 296.dp,
            )
            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        song.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        song.artistName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            val progressColor = MaterialTheme.colorScheme.onSurface
            Slider(
                value = sliderPositionMillis.coerceIn(0f, durationMillis.toFloat()),
                onValueChange = {
                    sliderPositionMillis = it
                    pendingSeekMillis = null
                    isDraggingSlider = true
                },
                onValueChangeFinished = {
                    val targetPosition = sliderPositionMillis.toLong()
                        .coerceIn(0L, playback.durationMillis.coerceAtLeast(0))
                    sliderPositionMillis = targetPosition.toFloat()
                    pendingSeekMillis = targetPosition
                    isDraggingSlider = false
                    viewModel.seekTo(targetPosition)
                },
                valueRange = 0f..durationMillis.toFloat(),
                enabled = hasPreview && playback.isReady && playback.durationMillis > 0,
                colors = SliderDefaults.colors(
                    thumbColor = progressColor,
                    activeTrackColor = progressColor,
                    inactiveTrackColor = MusicMutedTrack,
                ),
                thumb = {
                    Canvas(Modifier.size(14.dp)) {
                        drawCircle(progressColor)
                    }
                },
                track = { sliderState ->
                    Canvas(Modifier.fillMaxWidth().height(4.dp)) {
                        val progress = (sliderState.value / durationMillis.toFloat()).coerceIn(0f, 1f)
                        val centerY = size.height / 2f
                        val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl
                        val startX = if (isRtl) size.width else 0f
                        val endX = if (isRtl) 0f else size.width

                        drawLine(
                            color = MusicMutedTrack,
                            start = Offset(startX, centerY),
                            end = Offset(endX, centerY),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round,
                        )
                        if (progress > 0f) {
                            drawLine(
                                color = progressColor,
                                start = Offset(startX, centerY),
                                end = Offset(startX + (endX - startX) * progress, centerY),
                                strokeWidth = size.height,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(displayedPositionMillis), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Text(
                    if (playback.durationMillis > 0) {
                        stringResource(
                            R.string.player_time_remaining,
                            formatTime((playback.durationMillis - displayedPositionMillis).coerceAtLeast(0)),
                        )
                    } else {
                        stringResource(R.string.player_time_unknown)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = { viewModel.togglePlayback(song) },
                        enabled = hasPreview,
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Background),
                    ) {
                        if (playback.isBuffering) CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Background, strokeWidth = 2.dp)
                        else Icon(
                            painter = painterResource(
                                if (playback.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                            ),
                            contentDescription = stringResource(if (playback.isPlaying) R.string.player_pause else R.string.player_play),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(
                        onClick = {
                            if (playback.positionMillis > RESTART_TRACK_THRESHOLD_MILLIS) {
                                viewModel.seekTo(0L)
                            } else {
                                onPrevious()
                            }
                        },
                        modifier = Modifier.semanticsLabel(stringResource(R.string.player_previous_track)),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_backward),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onNext, modifier = Modifier.semanticsLabel(stringResource(R.string.player_next_track))) {
                        Icon(
                            painter = painterResource(R.drawable.ic_forward),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                IconButton(
                    onClick = viewModel::toggleRepeating,
                    enabled = hasPreview,
                    modifier = Modifier.semanticsLabel(
                        stringResource(if (playback.isRepeating) R.string.player_repeat_off else R.string.player_repeat_on)
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_repeat),
                        contentDescription = null,
                        tint = if (playback.isRepeating) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            if (!hasPreview) {
                Text(
                    stringResource(R.string.player_preview_unavailable),
                    modifier = Modifier.padding(top = 13.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (playback.errorMessage != null) {
                Text(stringResource(R.string.player_error_message), modifier = Modifier.padding(top = 13.dp), color = Color(0xFFFFB5A7), textAlign = TextAlign.Center)
            } else if (!playback.isReady && !playback.isBuffering) {
                Text(stringResource(R.string.player_preparing_preview), modifier = Modifier.padding(top = 13.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    return stringResource(R.string.player_time_format, totalSeconds / 60, totalSeconds % 60)
}

private fun Modifier.semanticsLabel(label: String): Modifier =
    this.then(Modifier.semantics { contentDescription = label })

private const val RESTART_TRACK_THRESHOLD_MILLIS = 3_000L
