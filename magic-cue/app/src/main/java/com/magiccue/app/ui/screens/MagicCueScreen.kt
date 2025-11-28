package com.magiccue.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.magiccue.app.data.LLMProvider
import com.magiccue.app.domain.CueSuggestion
import com.magiccue.app.domain.ResponseTone
import com.magiccue.app.ui.state.MagicCueUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicCueScreen(
    state: MagicCueUiState,
    onNotesChanged: (String) -> Unit,
    onScenarioChanged: (String) -> Unit,
    onPersonaChanged: (String) -> Unit,
    onLatestQuestionChanged: (String) -> Unit,
    onToneChanged: (ResponseTone) -> Unit,
    onProviderChanged: (LLMProvider) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onPersistConfig: () -> Unit,
    onGenerateCue: () -> Unit,
    onResetContext: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Magic Cue") },
                actions = {
                    AssistChip(
                        onClick = onPersistConfig,
                        label = { Text("LLM 저장") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            ContextCard(
                conversationNotes = state.conversationNotes,
                scenario = state.scenario,
                persona = state.persona,
                latestQuestion = state.latestQuestion,
                onNotesChanged = onNotesChanged,
                onScenarioChanged = onScenarioChanged,
                onPersonaChanged = onPersonaChanged,
                onLatestQuestionChanged = onLatestQuestionChanged
            )

            Spacer(Modifier.height(16.dp))

            ToneSelector(selectedTone = state.tone, onToneChanged = onToneChanged)

            Spacer(Modifier.height(16.dp))

            ProviderConfigCard(
                provider = state.selectedProvider,
                apiKey = state.apiKey,
                model = state.model,
                onProviderChanged = onProviderChanged,
                onApiKeyChanged = onApiKeyChanged,
                onModelChanged = onModelChanged,
                onPersistConfig = onPersistConfig
            )

            Spacer(Modifier.height(16.dp))

            ActionRow(
                isLoading = state.isLoading,
                onGenerate = onGenerateCue,
                onReset = onResetContext
            )

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = state.summary.isNotBlank()) {
                SummaryCard(summary = state.summary, tips = state.confidenceTips)
            }

            Spacer(Modifier.height(8.dp))

            state.suggestions.forEach { suggestion ->
                CueCard(suggestion)
                Spacer(Modifier.height(8.dp))
            }

            AnimatedVisibility(visible = state.followUps.isNotEmpty()) {
                FollowUpCard(followUps = state.followUps)
            }
        }
    }
}

@Composable
private fun ContextCard(
    conversationNotes: String,
    scenario: String,
    persona: String,
    latestQuestion: String,
    onNotesChanged: (String) -> Unit,
    onScenarioChanged: (String) -> Unit,
    onPersonaChanged: (String) -> Unit,
    onLatestQuestionChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("컨텍스트", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = scenario,
                onValueChange = onScenarioChanged,
                label = { Text("상황") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = persona,
                onValueChange = onPersonaChanged,
                label = { Text("내 페르소나") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = latestQuestion,
                onValueChange = onLatestQuestionChanged,
                label = { Text("최근 상대 질문") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = conversationNotes,
                onValueChange = onNotesChanged,
                label = { Text("메모") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                minLines = 4,
                maxLines = 8
            )
        }
    }
}

@Composable
private fun ToneSelector(
    selectedTone: ResponseTone,
    onToneChanged: (ResponseTone) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("톤 선택", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResponseTone.values().forEach { tone ->
                    InputChip(
                        selected = selectedTone == tone,
                        onClick = { onToneChanged(tone) },
                        label = { Text(tone.label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderConfigCard(
    provider: LLMProvider,
    apiKey: String,
    model: String,
    onProviderChanged: (LLMProvider) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onPersistConfig: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("LLM 설정", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LLMProvider.values().forEach { option ->
                    FilterChip(
                        selected = provider == option,
                        onClick = { onProviderChanged(option) },
                        label = { Text(option.displayName) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChanged,
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = model,
                onValueChange = onModelChanged,
                label = { Text("모델") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onPersistConfig) {
                Text("설정 저장")
            }
        }
    }
}

@Composable
private fun ActionRow(
    isLoading: Boolean,
    onGenerate: () -> Unit,
    onReset: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = onGenerate,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("큐 생성")
            }
        }
        FilledTonalButton(
            modifier = Modifier.weight(1f),
            onClick = onReset
        ) {
            Text("초기화")
        }
    }
}

@Composable
private fun SummaryCard(summary: String, tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("요약", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(summary, style = MaterialTheme.typography.bodyMedium)
            if (tips.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Confidence Tips", fontWeight = FontWeight.SemiBold)
                tips.forEach { tip ->
                    Text("• $tip", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CueCard(suggestion: CueSuggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(suggestion.headline, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(suggestion.script, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FollowUpCard(followUps: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("후속 질문", style = MaterialTheme.typography.titleMedium)
            followUps.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
