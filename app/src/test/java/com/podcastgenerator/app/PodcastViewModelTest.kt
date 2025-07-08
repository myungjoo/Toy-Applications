package com.podcastgenerator.app

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.podcastgenerator.app.models.*
import com.podcastgenerator.app.viewmodel.PodcastViewModel
import com.podcastgenerator.app.viewmodel.PodcastUIState
import com.podcastgenerator.app.viewmodel.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for PodcastViewModel
 */
@ExperimentalCoroutinesApi
class PodcastViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockApplication: Application

    private lateinit var viewModel: PodcastViewModel

    @Mock
    private lateinit var uiStateObserver: Observer<PodcastUIState>

    @Mock
    private lateinit var scriptObserver: Observer<PodcastScript?>

    @Mock
    private lateinit var errorObserver: Observer<String?>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = PodcastViewModel(mockApplication)
        
        // Observe LiveData
        viewModel.uiState.observeForever(uiStateObserver)
        viewModel.currentScript.observeForever(scriptObserver)
        viewModel.errorMessage.observeForever(errorObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.uiState.removeObserver(uiStateObserver)
        viewModel.currentScript.removeObserver(scriptObserver)
        viewModel.errorMessage.removeObserver(errorObserver)
    }

    @Test
    fun testInitialState() {
        val initialState = viewModel.uiState.value
        assertNotNull(initialState)
        assertFalse(initialState!!.isLoading)
        assertFalse(initialState.hasScript)
        assertFalse(initialState.isPlaying)
        assertNull(initialState.errorMessage)
        assertNull(initialState.currentSegment)
    }

    @Test
    fun testValidateUrls() {
        val validUrls = listOf("https://example.com", "http://test.com")
        val invalidUrls = listOf("not-a-url", "ftp://example.com")
        val mixedUrls = validUrls + invalidUrls

        val result = viewModel.validateUrls(mixedUrls)
        
        assertEquals(2, result.validUrls.size)
        assertEquals(2, result.invalidUrls.size)
        assertFalse(result.isValid) // Because there are invalid URLs
        assertTrue(result.validUrls.contains("https://example.com"))
        assertTrue(result.validUrls.contains("http://test.com"))
    }

    @Test
    fun testValidateUrlsWithOnlyValidUrls() {
        val validUrls = listOf("https://example.com", "http://test.com")
        
        val result = viewModel.validateUrls(validUrls)
        
        assertEquals(2, result.validUrls.size)
        assertEquals(0, result.invalidUrls.size)
        assertTrue(result.isValid)
    }

    @Test
    fun testValidateUrlsWithEmptyList() {
        val result = viewModel.validateUrls(emptyList())
        
        assertEquals(0, result.validUrls.size)
        assertEquals(0, result.invalidUrls.size)
        assertFalse(result.isValid) // Empty list is not valid
    }

    @Test
    fun testGeneratePodcastScriptWithInvalidUrls() = runTest {
        val invalidUrls = listOf("not-a-url", "invalid")
        
        viewModel.generatePodcastScript(invalidUrls, PodcastStyle.CONVERSATION)
        
        // Advance time to let coroutines complete
        advanceUntilIdle()
        
        // Should not have a script and should have an error
        assertNull(viewModel.currentScript.value)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun testClearError() {
        // Simulate an error
        viewModel.generatePodcastScript(emptyList(), PodcastStyle.CONVERSATION)
        runTest { advanceUntilIdle() }
        
        // Clear the error
        viewModel.clearError()
        
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun testReset() {
        // Create a mock script to set
        val mockScript = PodcastScript(
            id = "test",
            title = "Test Script",
            sourceUrls = listOf("https://example.com"),
            segments = listOf(
                ScriptSegment(
                    id = "seg1",
                    speaker = Speaker("1", "Alex"),
                    content = "Test content"
                )
            ),
            speakers = defaultSpeakers()
        )
        
        // Set some state
        viewModel.reset()
        
        val state = viewModel.uiState.value
        assertNotNull(state)
        assertFalse(state!!.isLoading)
        assertFalse(state.hasScript)
        assertFalse(state.isPlaying)
        assertNull(viewModel.currentScript.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun testGetScriptSegmentsWithNoScript() {
        val segments = viewModel.getScriptSegments()
        assertTrue(segments.isEmpty())
    }

    @Test
    fun testGetScriptTitleWithNoScript() {
        val title = viewModel.getScriptTitle()
        assertEquals("Podcast Script", title)
    }

    @Test
    fun testGeneratePodcastScriptWithValidUrls() = runTest {
        val validUrls = listOf("https://example.com")
        
        viewModel.generatePodcastScript(validUrls, PodcastStyle.CONVERSATION)
        
        // Check that loading state is set
        val initialState = viewModel.uiState.value
        assertNotNull(initialState)
        
        // Advance time to complete the coroutine
        advanceUntilIdle()
        
        // After completion, should have a script
        val script = viewModel.currentScript.value
        assertNotNull(script)
        
        val finalState = viewModel.uiState.value
        assertNotNull(finalState)
        assertTrue(finalState!!.hasScript)
        assertFalse(finalState.isLoading)
    }

    @Test
    fun testDifferentPodcastStyles() = runTest {
        val validUrls = listOf("https://example.com")
        val styles = listOf(
            PodcastStyle.CONVERSATION,
            PodcastStyle.QNA,
            PodcastStyle.DEBATE,
            PodcastStyle.INTERVIEW
        )
        
        styles.forEach { style ->
            viewModel.reset()
            viewModel.generatePodcastScript(validUrls, style)
            advanceUntilIdle()
            
            val script = viewModel.currentScript.value
            assertNotNull("Script should not be null for style $style", script)
        }
    }

    @Test
    fun testPlayPodcastWithoutScript() {
        viewModel.playPodcast()
        
        // Should have an error message
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun testValidationResultDataClass() {
        val validUrls = listOf("https://example.com")
        val invalidUrls = listOf("invalid")
        
        val result = ValidationResult(
            validUrls = validUrls,
            invalidUrls = invalidUrls,
            isValid = false
        )
        
        assertEquals(validUrls, result.validUrls)
        assertEquals(invalidUrls, result.invalidUrls)
        assertFalse(result.isValid)
    }

    @Test
    fun testPodcastUIStateDataClass() {
        val segment = ScriptSegment(
            id = "test",
            speaker = Speaker("1", "Test"),
            content = "Test content"
        )
        
        val state = PodcastUIState(
            isLoading = true,
            hasScript = true,
            isPlaying = false,
            loadingMessage = "Loading...",
            errorMessage = "Error",
            currentSegment = segment
        )
        
        assertTrue(state.isLoading)
        assertTrue(state.hasScript)
        assertFalse(state.isPlaying)
        assertEquals("Loading...", state.loadingMessage)
        assertEquals("Error", state.errorMessage)
        assertEquals(segment, state.currentSegment)
    }
}