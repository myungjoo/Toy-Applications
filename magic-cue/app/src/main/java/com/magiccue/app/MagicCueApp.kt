package com.magiccue.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.magiccue.app.ui.screens.MagicCueScreen
import com.magiccue.app.ui.state.MagicCueViewModel
import com.magiccue.app.ui.theme.MagicCueTheme

@Composable
fun MagicCueApp(application: MagicCueApplication) {
    val factory = remember(application) {
        MagicCueViewModel.Factory(
            repository = application.repository,
            preferenceStore = application.preferenceStore
        )
    }
    val viewModel: MagicCueViewModel = viewModel(factory = factory)
    MagicCueApp(viewModel)
}

@Composable
fun MagicCueApp(viewModel: MagicCueViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MagicCueTheme {
        MagicCueScreen(
            state = state,
            onNotesChanged = viewModel::updateConversationNotes,
            onScenarioChanged = viewModel::updateScenario,
            onPersonaChanged = viewModel::updatePersona,
            onLatestQuestionChanged = viewModel::updateLatestQuestion,
            onToneChanged = viewModel::updateTone,
            onProviderChanged = viewModel::updateProvider,
            onApiKeyChanged = viewModel::updateApiKey,
            onModelChanged = viewModel::updateModel,
            onPersistConfig = viewModel::persistConfig,
            onGenerateCue = viewModel::generateCues,
            onResetContext = viewModel::resetContext
        )
    }
}
