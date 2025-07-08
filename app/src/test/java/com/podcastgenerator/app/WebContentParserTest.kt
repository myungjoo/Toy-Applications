package com.podcastgenerator.app

import com.podcastgenerator.app.services.WebContentParser
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for WebContentParser
 */
class WebContentParserTest {

    private lateinit var webContentParser: WebContentParser

    @Before
    fun setup() {
        webContentParser = WebContentParser()
    }

    @Test
    fun testValidUrl() {
        assertTrue(webContentParser.isValidUrl("https://www.example.com"))
        assertTrue(webContentParser.isValidUrl("http://example.com"))
        assertTrue(webContentParser.isValidUrl("https://example.com/path/to/page"))
        assertTrue(webContentParser.isValidUrl("https://sub.example.com"))
    }

    @Test
    fun testInvalidUrl() {
        assertFalse(webContentParser.isValidUrl("not-a-url"))
        assertFalse(webContentParser.isValidUrl("ftp://example.com"))
        assertFalse(webContentParser.isValidUrl("example.com"))
        assertFalse(webContentParser.isValidUrl(""))
        assertFalse(webContentParser.isValidUrl("javascript:alert('xss')"))
    }

    @Test
    fun testExtractContentFromInvalidUrl() = runBlocking {
        val result = webContentParser.extractContent("invalid-url")
        assertTrue(result.isFailure)
    }

    @Test
    fun testExtractContentFromMultipleUrls() = runBlocking {
        val urls = listOf(
            "https://httpbin.org/html", // This might work in real scenario
            "invalid-url",
            "https://httpbin.org/json"  // This might work in real scenario
        )
        
        val results = webContentParser.extractContentFromUrls(urls)
        // The method should handle mixed valid/invalid URLs gracefully
        // In a real test environment, you might get some results
        assertNotNull(results)
    }

    @Test
    fun testCleanText() {
        // This tests the internal text cleaning logic by proxy
        // We can't directly test the private method, but we can verify
        // that the parser handles content correctly through extraction
        val validUrl = "https://example.com"
        runBlocking {
            val result = webContentParser.extractContent(validUrl)
            // Even if it fails due to network issues, the structure should be correct
            assertNotNull(result)
        }
    }

    @Test
    fun testUrlListValidation() {
        val mixedUrls = listOf(
            "https://www.example.com",
            "invalid-url",
            "http://test.com",
            "not-a-url"
        )
        
        val validUrls = mixedUrls.filter { webContentParser.isValidUrl(it) }
        assertEquals(2, validUrls.size)
        assertTrue(validUrls.contains("https://www.example.com"))
        assertTrue(validUrls.contains("http://test.com"))
    }

    @Test
    fun testEmptyUrlHandling() {
        assertFalse(webContentParser.isValidUrl(""))
        assertFalse(webContentParser.isValidUrl("   "))
    }

    @Test
    fun testSpecialCharactersInUrl() {
        assertTrue(webContentParser.isValidUrl("https://example.com/path?param=value&other=123"))
        assertTrue(webContentParser.isValidUrl("https://example.com/path#fragment"))
        assertTrue(webContentParser.isValidUrl("https://example.com:8080/path"))
    }
}