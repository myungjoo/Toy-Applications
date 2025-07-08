# Podcast Generator

An Android application that generates podcast-like scripts from web URLs using LLM technology and provides voice output through text-to-speech functionality.

## Features

- **URL Input**: Accept multiple web page URLs for content extraction
- **Web Content Parsing**: Extract and clean main content from web pages using JSoup
- **LLM Integration**: Generate conversational podcast scripts using placeholder LLM API calls
- **Multiple Podcast Styles**: Support for Conversation, Q&A, Debate, and Interview formats
- **Text-to-Speech**: Voice output of generated scripts with different voices for speakers
- **Modern UI**: Clean, Material Design 3 interface built with Jetpack Compose
- **Playback Controls**: Full media controls including play, pause, stop, skip, and segment selection

## Architecture

The application follows MVVM (Model-View-ViewModel) architecture with the following components:

### Core Components

1. **Models** (`com.podcastgenerator.app.models`)
   - `PodcastScript`: Complete script with segments and metadata
   - `ScriptSegment`: Individual speaking parts with speaker and content
   - `Speaker`: Speaker information with voice characteristics
   - `WebContent`: Extracted web page content
   - `PodcastStyle`: Enumeration of available script styles

2. **Services** (`com.podcastgenerator.app.services`)
   - `WebContentParser`: Extracts content from URLs using JSoup
   - `LLMService`: Handles LLM API calls for script generation (with placeholders)
   - `VoiceService`: Manages text-to-speech functionality

3. **ViewModel** (`com.podcastgenerator.app.viewmodel`)
   - `PodcastViewModel`: Coordinates services and manages UI state

4. **UI** (`com.podcastgenerator.app`)
   - `MainActivity`: Main activity with Jetpack Compose UI
   - Modern Material Design 3 components

### LLM Integration

The application includes placeholder LLM API calls that can be easily replaced with actual LLM implementations:

```kotlin
// PLACEHOLDER LLM API CALL - Replace with actual implementation
private suspend fun LLM_API_CALL(inputString: String): String {
    // TODO: Replace this with actual LLM API integration
    // Example: 
    // val response = llmClient.generateText(inputString)
    // return response.text
    
    return generateSimulatedPodcastScript(inputString)
}
```

Additional placeholder methods are provided for different LLM configurations:
- `LLM_API_CALL_ALTERNATIVE`: For different models or parameters
- `LLM_API_BATCH_CALL`: For batch processing multiple inputs

## Dependencies

The project uses the following major dependencies:

- **Jetpack Compose**: Modern UI toolkit
- **Material Design 3**: UI components and theming
- **Lifecycle Components**: ViewModel and LiveData
- **Coroutines**: Asynchronous programming
- **OkHttp/Retrofit**: HTTP networking
- **JSoup**: HTML parsing for web content extraction
- **Text-to-Speech**: Android's built-in TTS engine

## Installation

1. Clone the repository
2. Open the project in Android Studio
3. Sync the Gradle files
4. Build and run the application on an Android device or emulator (API level 24+)

## Usage

### Generating a Podcast Script

1. **Enter URLs**: Input one or more web page URLs (one per line)
2. **Select Style**: Choose from Conversation, Q&A, Debate, or Interview
3. **Generate**: Tap "Generate Podcast Script" to create the script
4. **Wait**: The app will extract content and generate the script using LLM

### Playing the Script

1. **Play Controls**: Use the media controls to play, pause, or stop the script
2. **Navigation**: Skip between segments or tap individual segments to jump to them
3. **Visual Feedback**: Current segment is highlighted during playback
4. **Voice Differentiation**: Different speakers use different voice characteristics

## Testing

The project includes comprehensive test coverage:

### Unit Tests
- `WebContentParserTest`: Tests URL validation and content extraction
- `LLMServiceTest`: Tests script generation and LLM placeholder functionality
- `PodcastViewModelTest`: Tests application logic and state management

### Integration Tests
- `MainActivityTest`: Tests UI components and user interactions

Run tests using:
```bash
./gradlew test           # Unit tests
./gradlew connectedAndroidTest  # Instrumentation tests
```

## Development

### Project Structure

```
app/
├── src/main/java/com/podcastgenerator/app/
│   ├── models/              # Data models
│   ├── services/            # Business logic services
│   ├── viewmodel/           # ViewModels
│   ├── ui/theme/            # UI theming
│   └── MainActivity.kt      # Main UI
├── src/test/                # Unit tests
└── src/androidTest/         # Instrumentation tests
```

### Adding Real LLM Integration

To integrate with a real LLM service:

1. Replace the placeholder methods in `LLMService.kt`
2. Add necessary API client dependencies
3. Configure authentication and endpoint URLs
4. Update error handling for network failures

Example integration:
```kotlin
private suspend fun LLM_API_CALL(inputString: String): String {
    val response = openAiClient.generateText(
        prompt = inputString,
        maxTokens = 2000,
        temperature = 0.7
    )
    return response.text
}
```

### Customizing Voice Output

The `VoiceService` can be extended to support:
- Different TTS engines
- Custom voice models
- Audio effects and processing
- Export to audio files

## Technical Requirements

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Language**: Kotlin
- **Architecture**: MVVM with Jetpack Compose

## Permissions

The app requires the following permissions:
- `INTERNET`: For web content extraction
- `ACCESS_NETWORK_STATE`: For network connectivity checks

## Future Enhancements

Potential improvements for the application:

1. **Advanced LLM Features**
   - Support for multiple LLM models
   - Fine-tuning capabilities
   - Streaming responses

2. **Enhanced Audio**
   - Custom voice training
   - Audio effects and background music
   - Export to podcast formats

3. **Content Management**
   - Save and manage generated scripts
   - Share functionality
   - Offline capabilities

4. **Advanced Parsing**
   - Support for more content types (PDFs, videos)
   - Better content extraction algorithms
   - Multi-language support

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For questions or issues, please open an issue in the project repository.