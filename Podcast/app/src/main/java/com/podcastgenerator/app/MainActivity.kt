package com.podcastgenerator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.podcastgenerator.app.models.*
import com.podcastgenerator.app.ui.theme.PodcastGeneratorTheme
import com.podcastgenerator.app.viewmodel.PodcastViewModel
import com.podcastgenerator.app.viewmodel.PodcastUIState
import com.podcastgenerator.app.services.VoiceService

class MainActivity : ComponentActivity() {
    
    private val viewModel: PodcastViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PodcastGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PodcastGeneratorApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastGeneratorApp(viewModel: PodcastViewModel) {
    val uiState by viewModel.uiState.observeAsState(PodcastUIState())
    val currentScript by viewModel.currentScript.observeAsState()
    val playbackStatus by viewModel.playbackStatus.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    // Show error message if any
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You could show a snackbar here
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Title
        Text(
            text = "Podcast Generator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!uiState.hasScript) {
            // URL Input Section
            URLInputSection(
                onGenerateScript = { urls, style ->
                    viewModel.generatePodcastScript(urls, style)
                },
                isLoading = uiState.isLoading,
                loadingMessage = uiState.loadingMessage
            )
        } else {
            // Script Display and Playback Section
            ScriptDisplaySection(
                script = currentScript,
                uiState = uiState,
                playbackStatus = playbackStatus,
                onPlay = { viewModel.playPodcast() },
                onPause = { viewModel.pausePodcast() },
                onResume = { viewModel.resumePodcast() },
                onStop = { viewModel.stopPodcast() },
                onSkipNext = { viewModel.skipToNext() },
                onSkipPrevious = { viewModel.skipToPrevious() },
                onPlaySegment = { index -> viewModel.playSegment(index) },
                onReset = { viewModel.reset() }
            )
        }

        // Error Display
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun URLInputSection(
    onGenerateScript: (List<String>, PodcastStyle) -> Unit,
    isLoading: Boolean,
    loadingMessage: String?
) {
    var urlText by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(PodcastStyle.CONVERSATION) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Enter URLs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { Text("URLs (one per line)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                maxLines = 5,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Podcast Style",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Style Selection
            PodcastStyleSelector(
                selectedStyle = selectedStyle,
                onStyleSelected = { selectedStyle = it },
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                LoadingSection(loadingMessage)
            } else {
                Button(
                    onClick = {
                        val urls = urlText.split("\n")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        if (urls.isNotEmpty()) {
                            onGenerateScript(urls, selectedStyle)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = urlText.isNotBlank()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Podcast Script")
                }
            }
        }
    }
}

@Composable
fun PodcastStyleSelector(
    selectedStyle: PodcastStyle,
    onStyleSelected: (PodcastStyle) -> Unit,
    enabled: Boolean = true
) {
    val styles = listOf(
        PodcastStyle.CONVERSATION to "Conversation",
        PodcastStyle.QNA to "Q&A",
        PodcastStyle.DEBATE to "Debate",
        PodcastStyle.INTERVIEW to "Interview"
    )
    
    LazyColumn(
        modifier = Modifier.height(160.dp)
    ) {
        items(styles) { (style, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedStyle == style,
                    onClick = { onStyleSelected(style) },
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LoadingSection(loadingMessage: String?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = loadingMessage ?: "Loading...",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScriptDisplaySection(
    script: PodcastScript?,
    uiState: PodcastUIState,
    playbackStatus: VoiceService.PlaybackStatus?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onPlaySegment: (Int) -> Unit,
    onReset: () -> Unit
) {
    if (script == null) return
    
    Column {
        // Script Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = script.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Sources: ${script.sourceUrls.size} URL(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Segments: ${script.segments.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Playback Controls
        PlaybackControls(
            isPlaying = uiState.isPlaying,
            playbackStatus = playbackStatus,
            onPlay = onPlay,
            onPause = onPause,
            onResume = onResume,
            onStop = onStop,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Script Segments
        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Script",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    TextButton(onClick = onReset) {
                        Text("New Script")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    itemsIndexed(script.segments) { index, segment ->
                        ScriptSegmentItem(
                            segment = segment,
                            index = index,
                            isCurrentSegment = playbackStatus?.currentSegmentIndex == index,
                            onClick = { onPlaySegment(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    playbackStatus: VoiceService.PlaybackStatus?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Progress indicator
            playbackStatus?.let { status ->
                if (status.totalSegments > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Segment ${status.currentSegmentIndex + 1}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "of ${status.totalSegments}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = if (status.totalSegments > 0) {
                            (status.currentSegmentIndex + 1).toFloat() / status.totalSegments.toFloat()
                        } else 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSkipPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                
                if (isPlaying) {
                    IconButton(onClick = onPause) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                    }
                } else {
                    IconButton(onClick = if (playbackStatus?.currentSegmentIndex == 0) onPlay else onResume) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }
                
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
                
                IconButton(onClick = onSkipNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptSegmentItem(
    segment: ScriptSegment,
    index: Int,
    isCurrentSegment: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentSegment) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = segment.speaker.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentSegment) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (isCurrentSegment) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Currently playing",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = segment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentSegment) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PodcastGeneratorTheme {
        // Preview content
    }
}