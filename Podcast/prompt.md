# Podcast Script Generation Prompt

This document describes the prompt template used by the Podcast Generator application to create podcast scripts from web content using LLM technology.

## Location

The prompt is implemented in:
- **File:** `app/src/main/java/com/podcastgenerator/app/services/LLMService.kt`
- **Function:** `createPrompt()` (lines 78-100)
- **Usage:** Called by `generatePodcastScript()` method before making LLM API calls

## Prompt Template

The application uses a dynamic prompt template that adapts based on the selected podcast style and speakers:

### Base Template

```
You are creating a podcast script based on the following web content. 

Instructions:
- {STYLE_INSTRUCTIONS}
- Keep the conversation natural and engaging
- Break down complex topics into digestible segments
- Include smooth transitions between topics
- Make it sound like a real conversation between {NUMBER_OF_SPEAKERS} people
- Each speaker turn should be clearly marked with "SPEAKER_NAME:"
- Keep individual speaking segments to 2-3 sentences for natural flow

Speakers: {SPEAKER_NAMES}

Content to discuss:
{WEB_CONTENT}

Generate a podcast script:
```

### Style-Specific Instructions

The `{STYLE_INSTRUCTIONS}` placeholder is replaced based on the selected `PodcastStyle`:

#### Conversation Style
```
Create a natural conversation where {SPEAKER_NAMES} discuss the topics in an engaging way.
```

#### Q&A Style
```
Structure this as a Q&A session where one person asks questions and the other provides answers.
```

#### Debate Style
```
Present this as a friendly debate where {SPEAKER_NAMES} present different perspectives.
```

#### Interview Style
```
Format this as an interview where one person interviews the other about the topics.
```

## Dynamic Variables

The prompt template includes several dynamic variables that are populated at runtime:

- `{STYLE_INSTRUCTIONS}`: Instructions specific to the chosen podcast style
- `{SPEAKER_NAMES}`: Names of all speakers joined with "and" 
- `{NUMBER_OF_SPEAKERS}`: Count of speakers participating
- `{WEB_CONTENT}`: Combined and formatted content from all provided URLs

## Content Processing

Before being included in the prompt, web content goes through processing:

1. **Content Combination**: Multiple web articles are combined with "--- ARTICLE BREAK ---" separators
2. **Formatting**: Each article includes:
   - Title: {article_title}
   - URL: {article_url}
   - Content: {extracted_content}

## Example Usage

For a conversation-style podcast with speakers "Alex" and "Sam" discussing a web article about AI:

```
You are creating a podcast script based on the following web content. 

Instructions:
- Create a natural conversation where Alex and Sam discuss the topics in an engaging way.
- Keep the conversation natural and engaging
- Break down complex topics into digestible segments
- Include smooth transitions between topics
- Make it sound like a real conversation between 2 people
- Each speaker turn should be clearly marked with "SPEAKER_NAME:"
- Keep individual speaking segments to 2-3 sentences for natural flow

Speakers: Alex and Sam

Content to discuss:
Title: The Future of AI Technology
URL: https://example.com/ai-article

Content:
[Article content about AI developments...]

Generate a podcast script:
```

## Implementation Notes

- The prompt is designed to work with various LLM models and can be easily customized
- Current implementation includes placeholder LLM API calls that can be replaced with actual integrations
- The prompt emphasizes natural conversation flow and proper speaker attribution
- Content length is kept manageable by limiting individual speaking segments to 2-3 sentences