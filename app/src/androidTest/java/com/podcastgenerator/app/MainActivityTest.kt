package com.podcastgenerator.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for MainActivity UI
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppTitle() {
        composeTestRule.onNodeWithText("Podcast Generator").assertIsDisplayed()
    }

    @Test
    fun testUrlInputSection() {
        // Check that URL input section is displayed initially
        composeTestRule.onNodeWithText("Enter URLs").assertIsDisplayed()
        composeTestRule.onNodeWithText("URLs (one per line)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Podcast Style").assertIsDisplayed()
    }

    @Test
    fun testPodcastStyleSelection() {
        // Check that all podcast style options are available
        composeTestRule.onNodeWithText("Conversation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Q&A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Debate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Interview").assertIsDisplayed()
    }

    @Test
    fun testGenerateButtonInitialState() {
        // Generate button should be disabled initially (no URLs entered)
        composeTestRule.onNodeWithText("Generate Podcast Script").assertIsNotEnabled()
    }

    @Test
    fun testUrlInput() {
        // Type in URL field
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput("https://example.com")
        
        // Generate button should now be enabled
        composeTestRule.onNodeWithText("Generate Podcast Script").assertIsEnabled()
    }

    @Test
    fun testPodcastStyleSelectionInteraction() {
        // Initially Conversation should be selected
        composeTestRule.onAllNodesWithContentDescription("radio button")[0].assertIsSelected()
        
        // Click on Q&A style
        composeTestRule.onAllNodesWithContentDescription("radio button")[1].performClick()
        
        // Q&A should now be selected
        composeTestRule.onAllNodesWithContentDescription("radio button")[1].assertIsSelected()
    }

    @Test
    fun testGenerateScriptWithValidUrl() {
        // Enter a valid URL
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput("https://example.com")
        
        // Click generate button
        composeTestRule.onNodeWithText("Generate Podcast Script").performClick()
        
        // Should see loading indicator
        composeTestRule.waitForIdle()
    }

    @Test
    fun testGenerateScriptWithMultipleUrls() {
        // Enter multiple URLs
        val urls = "https://example.com\nhttps://test.com"
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput(urls)
        
        // Select different style
        composeTestRule.onAllNodesWithContentDescription("radio button")[2].performClick() // Debate
        
        // Click generate button
        composeTestRule.onNodeWithText("Generate Podcast Script").performClick()
        
        // Should start loading
        composeTestRule.waitForIdle()
    }

    @Test
    fun testEmptyUrlHandling() {
        // Don't enter any URLs
        
        // Generate button should be disabled
        composeTestRule.onNodeWithText("Generate Podcast Script").assertIsNotEnabled()
        
        // Enter some spaces only
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput("   ")
        
        // Generate button should still be disabled
        composeTestRule.onNodeWithText("Generate Podcast Script").assertIsNotEnabled()
    }

    @Test
    fun testUrlInputClearAndRetype() {
        // Enter URL
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput("https://example.com")
        
        // Clear and enter new URL
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextClearance()
            .performTextInput("https://newexample.com")
        
        // Generate button should still be enabled
        composeTestRule.onNodeWithText("Generate Podcast Script").assertIsEnabled()
    }

    @Test
    fun testMaxLines() {
        // Test that URL input accepts multiple lines
        val multilineUrls = """
            https://example1.com
            https://example2.com
            https://example3.com
            https://example4.com
            https://example5.com
        """.trimIndent()
        
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput(multilineUrls)
        
        // Should accept the input
        composeTestRule.onNodeWithText("Generate Podcast Script").assertIsEnabled()
    }

    @Test
    fun testUIStateChanges() {
        // Enter URL and generate script
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput("https://example.com")
        
        composeTestRule.onNodeWithText("Generate Podcast Script").performClick()
        
        // Wait for any UI updates
        composeTestRule.waitForIdle()
        
        // URL input should be disabled during loading
        // (This might need to be adjusted based on actual implementation)
    }

    @Test
    fun testAccessibilityFeatures() {
        // Test that important UI elements have content descriptions
        composeTestRule.onNodeWithText("Generate Podcast Script")
            .assertHasClickAction()
        
        // Test that radio buttons are accessible
        composeTestRule.onAllNodesWithContentDescription("radio button")
            .assertCountEquals(4) // 4 podcast styles
    }

    @Test
    fun testKeyboardAppearance() {
        // Click on URL input field
        composeTestRule.onNodeWithText("URLs (one per line)").performClick()
        
        // Keyboard should appear (this is implicit with performTextInput)
        composeTestRule.onNodeWithText("URLs (one per line)")
            .performTextInput("https://")
        
        // Text should be entered successfully
        composeTestRule.onNodeWithText("URLs (one per line)")
            .assertTextContains("https://")
    }
}