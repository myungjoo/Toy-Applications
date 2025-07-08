package com.podcastgenerator.app

import com.podcastgenerator.app.models.*
import com.podcastgenerator.app.services.LLMService
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for LLMService
 */
class LLMServiceTest {

    private lateinit var llmService: LLMService

    @Before
    fun setup() {
        llmService = LLMService()
    }

    @Test
    fun testGeneratePodcastScriptSuccess() = runBlocking {
        val webContents = listOf(
            WebContent(
                url = "https://example.com",
                title = "Test Article",
                content = "This is a test article with some content for podcast generation.",
                summary = "Test summary"
            )
        )
        
        val speakers = defaultSpeakers()
        val style = PodcastStyle.CONVERSATION
        
        val result = llmService.generatePodcastScript(webContents, style, speakers)
        
        assertTrue(result.isSuccess)
        val script = result.getOrNull()
        assertNotNull(script)
        script?.let {
            assertEquals(1, it.sourceUrls.size)
            assertEquals("https://example.com", it.sourceUrls.first())
            assertTrue(it.segments.isNotEmpty())
            assertEquals(ScriptStatus.COMPLETED, it.status)
            assertEquals(speakers.size, it.speakers.size)
        }
    }

    @Test
    fun testGeneratePodcastScriptWithMultipleContents() = runBlocking {
        val webContents = listOf(
            WebContent(
                url = "https://example1.com",
                title = "Article 1",
                content = "First article content"
            ),
            WebContent(
                url = "https://example2.com",
                title = "Article 2", 
                content = "Second article content"
            )
        )
        
        val result = llmService.generatePodcastScript(
            webContents, 
            PodcastStyle.DEBATE, 
            defaultSpeakers()
        )
        
        assertTrue(result.isSuccess)
        val script = result.getOrNull()
        assertNotNull(script)
        script?.let {
            assertEquals(2, it.sourceUrls.size)
            assertTrue(it.title.contains("2 Topics"))
        }
    }

    @Test
    fun testGeneratePodcastScriptWithEmptyContent() = runBlocking {
        val emptyWebContents = emptyList<WebContent>()
        
        val result = llmService.generatePodcastScript(
            emptyWebContents,
            PodcastStyle.CONVERSATION,
            defaultSpeakers()
        )
        
        // Should handle empty content gracefully
        assertTrue(result.isSuccess)
        val script = result.getOrNull()
        assertNotNull(script)
        script?.let {
            assertTrue(it.sourceUrls.isEmpty())
        }
    }

    @Test
    fun testScriptGenerationWithDifferentStyles() = runBlocking {
        val webContent = listOf(
            WebContent(
                url = "https://test.com",
                title = "Test",
                content = "Test content"
            )
        )
        
        // Test all podcast styles
        val styles = listOf(
            PodcastStyle.CONVERSATION,
            PodcastStyle.QNA,
            PodcastStyle.DEBATE,
            PodcastStyle.INTERVIEW
        )
        
        styles.forEach { style ->
            val result = llmService.generatePodcastScript(webContent, style, defaultSpeakers())
            assertTrue("Failed for style $style", result.isSuccess)
            
            val script = result.getOrNull()
            assertNotNull("Script is null for style $style", script)
        }
    }

    @Test
    fun testScriptSegmentGeneration() = runBlocking {
        val webContent = listOf(
            WebContent(
                url = "https://test.com",
                title = "Test Article",
                content = "This is test content for generating script segments."
            )
        )
        
        val result = llmService.generatePodcastScript(
            webContent,
            PodcastStyle.CONVERSATION,
            defaultSpeakers()
        )
        
        assertTrue(result.isSuccess)
        val script = result.getOrNull()
        assertNotNull(script)
        
        script?.let {
            assertTrue("Script should have segments", it.segments.isNotEmpty())
            
            // Check that segments have proper structure
            it.segments.forEach { segment ->
                assertNotNull("Segment should have an ID", segment.id)
                assertNotNull("Segment should have a speaker", segment.speaker)
                assertNotNull("Segment should have content", segment.content)
                assertTrue("Segment content should not be empty", segment.content.isNotBlank())
                assertNotNull("Segment should have a type", segment.type)
            }
            
            // Check that we have both speakers represented
            val speakerIds = it.segments.map { segment -> segment.speaker.id }.distinct()
            assertTrue("Should have multiple speakers", speakerIds.size >= 1)
        }
    }

    @Test
    fun testDefaultSpeakers() {
        val speakers = defaultSpeakers()
        assertEquals(2, speakers.size)
        assertEquals("speaker1", speakers[0].id)
        assertEquals("Alex", speakers[0].name)
        assertEquals("speaker2", speakers[1].id)
        assertEquals("Sam", speakers[1].name)
    }

    @Test
    fun testScriptIdGeneration() = runBlocking {
        val webContent = listOf(
            WebContent(
                url = "https://test.com",
                title = "Test",
                content = "Test content"
            )
        )
        
        val result1 = llmService.generatePodcastScript(
            webContent, 
            PodcastStyle.CONVERSATION, 
            defaultSpeakers()
        )
        val result2 = llmService.generatePodcastScript(
            webContent, 
            PodcastStyle.CONVERSATION, 
            defaultSpeakers()
        )
        
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        
        val script1 = result1.getOrNull()
        val script2 = result2.getOrNull()
        
        assertNotNull(script1)
        assertNotNull(script2)
        assertNotEquals("Script IDs should be unique", script1?.id, script2?.id)
    }

    @Test
    fun testAlternativeLLMApiCall() = runBlocking {
        val result = llmService.LLM_API_CALL_ALTERNATIVE(
            "Test input",
            "test-model",
            mapOf("temperature" to 0.7)
        )
        
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun testBatchLLMApiCall() = runBlocking {
        val inputs = listOf("Input 1", "Input 2", "Input 3")
        val results = llmService.LLM_API_BATCH_CALL(inputs)
        
        assertEquals(inputs.size, results.size)
        results.forEach { result ->
            assertNotNull(result)
            assertTrue(result.isNotBlank())
        }
    }
}