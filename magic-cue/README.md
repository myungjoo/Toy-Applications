# Magic Cue (Pixel 10 Companion)

Magic Cue는 Pixel 10의 Magic Cue 기능을 벤치마킹한 안드로이드 앱으로, 통화나 미팅 중 즉석에서 사용할 수 있는 스크립트와 후속 질문, 자신감 체크리스트를 LLM(Gemini 또는 ChatGPT)으로 생성합니다.

## 핵심 기능
- **상황별 컨텍스트 편집기**: 시나리오, 페르소나, 최근 질문, 메모를 빠르게 입력하여 AI에게 전달할 수 있습니다.
- **톤 및 페르소나 제어**: 친근/전문/공감 등 원하는 톤을 선택할 수 있습니다.
- **LLM 이중 구성**: Gemini(Generative Language API) 또는 ChatGPT(OpenAI) 중 선택 가능하며, 사용자 API Key와 모델명을 UI에서 저장할 수 있습니다.
- **Magic Cue 피드**: 
  - Quick Prompt 카드(헤드라인 + 2문장 스크립트)
  - Follow-up 질문 리스트
  - 요약 및 Confidence Tips
- **오프라인 폴백**: API Key가 없거나 호출 실패 시에도 기본 템플릿 로직으로 실험적인 큐를 제공합니다.

## 아키텍처 개요
- **UI**: Jetpack Compose + Material 3, 단일 `MagicCueScreen`을 기준으로 상태 드리븐 UI 구성
- **상태 관리**: `MagicCueViewModel` + `StateFlow`
- **데이터 계층**: `MagicCueRepository`가 프롬프트 빌드, 네트워크 호출, 폴백 로직을 담당
- **LLM 통신**: `NetworkLLMClient`가 Gemini/ChatGPT 를 모두 지원하며, 응답 텍스트를 `CueJsonParser`로 파싱
- **설정 저장**: `LLMPreferenceStore`가 DataStore Preferences 기반으로 API Key/Provider/Model 을 저장

## LLM 설정 방법
1. 앱 실행 후 상단 **LLM 설정** 카드에서 Provider(예: Gemini)를 선택합니다.
2. API Key와 모델명(`gemini-1.5-flash`, `gpt-4o-mini` 등)을 입력합니다.
3. **설정 저장** 버튼을 누르면 DataStore에 암호화되지 않은 형태로 저장됩니다. 실제 배포 시 암호화 계층을 추가하는 것이 좋습니다.
4. 이후 `큐 생성` 버튼을 누르면 해당 설정으로 LLM을 호출합니다.

> **주의**: 저장된 Key는 기기 내 DataStore(SharedPreferences 유사) 에 평문으로 보관됩니다. 샘플 앱이므로 내부 테스트용으로만 사용하세요.

## 실행 방법
```bash
cd magic-cue
./gradlew tasks # Gradle sync 확인
./gradlew assembleDebug
```

## 테스트
프롬프트 생성 규칙을 검증하는 단위 테스트가 포함되어 있습니다.
```bash
cd magic-cue
./gradlew test
```

## 환경 변수/계정 사용
- **Gemini**: Google AI Studio에서 발급한 API Key 사용
- **ChatGPT**: OpenAI API Key 사용 (chat.completions endpoint)
- UI에서 Provider와 모델 이름만 바꿔도 즉시 적용되며, 앱은 HTTP Header에 `Authorization: Bearer <KEY>` 또는 `?key=` 쿼리를 붙여 호출합니다.

## 로컬 속성
필요하다면 `local.properties` 파일을 생성하여 SDK 경로를 지정하세요. 템플릿은 `local.properties.template`를 참고하세요.
