package com.podcastgenerator.app.services

import com.podcastgenerator.app.models.WebContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL

/**
 * Service for parsing web content from URLs
 */
class WebContentParser {
    
    companion object {
        private const val TIMEOUT_MILLIS = 10000
        private const val USER_AGENT = "Mozilla/5.0 (Android; Mobile; rv:40.0) Gecko/40.0 Firefox/40.0"
    }

    /**
     * Extracts content from a single URL
     */
    suspend fun extractContent(url: String): Result<WebContent> = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .get()
            
            val title = extractTitle(document)
            val content = extractMainContent(document)
            val summary = generateSummary(content)
            
            if (content.isBlank()) {
                Result.failure(Exception("No content found at URL: $url"))
            } else {
                Result.success(
                    WebContent(
                        url = url,
                        title = title,
                        content = content,
                        summary = summary
                    )
                )
            }
        } catch (e: IOException) {
            Result.failure(Exception("Failed to fetch content from URL: $url - ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error parsing content from URL: $url - ${e.message}"))
        }
    }

    /**
     * Extracts content from multiple URLs
     */
    suspend fun extractContentFromUrls(urls: List<String>): List<WebContent> {
        val results = mutableListOf<WebContent>()
        
        urls.forEach { url ->
            extractContent(url).onSuccess { content ->
                results.add(content)
            }.onFailure { exception ->
                // Log error but continue with other URLs
                println("Failed to extract content from $url: ${exception.message}")
            }
        }
        
        return results
    }

    /**
     * Extracts the title from the document
     */
    private fun extractTitle(document: Document): String {
        return document.title().takeIf { it.isNotBlank() }
            ?: document.select("h1").first()?.text()
            ?: "Untitled"
    }

    /**
     * Extracts the main content from the document
     */
    private fun extractMainContent(document: Document): String {
        // Remove script and style elements
        document.select("script, style, nav, header, footer, aside").remove()
        
        // Try to find main content area
        val contentSelectors = listOf(
            "article",
            "[role=main]",
            ".content",
            ".post-content",
            ".entry-content",
            ".article-content",
            "main",
            ".main-content"
        )
        
        for (selector in contentSelectors) {
            val element = document.select(selector).first()
            if (element != null) {
                val text = element.text()
                if (text.length > 200) { // Ensure substantial content
                    return cleanText(text)
                }
            }
        }
        
        // Fallback to body content
        val bodyText = document.body()?.text() ?: ""
        return cleanText(bodyText)
    }

    /**
     * Cleans the extracted text
     */
    private fun cleanText(text: String): String {
        return text
            .replace(Regex("\\s+"), " ") // Replace multiple whitespace with single space
            .replace(Regex("\\n+"), "\n") // Replace multiple newlines with single newline
            .trim()
    }

    /**
     * Generates a summary of the content (first few sentences)
     */
    private fun generateSummary(content: String, maxLength: Int = 300): String {
        if (content.length <= maxLength) return content
        
        val sentences = content.split(Regex("[.!?]")).filter { it.isNotBlank() }
        var summary = ""
        
        for (sentence in sentences) {
            val newSummary = summary + sentence.trim() + ". "
            if (newSummary.length > maxLength) break
            summary = newSummary
        }
        
        return summary.trim().takeIf { it.isNotBlank() } 
            ?: content.take(maxLength) + "..."
    }

    /**
     * Validates if a URL is properly formatted
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            URL(url)
            url.startsWith("http://") || url.startsWith("https://")
        } catch (e: Exception) {
            false
        }
    }
}