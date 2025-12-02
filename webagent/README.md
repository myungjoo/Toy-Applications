# WebAgent Android App

스마트폰 활동을 모니터링하고 AI 기반 추천을 제공하는 Android 애플리케이션입니다.

## 주요 기능

- **SMS 모니터링**: 수신/발신 SMS 자동 수집
- **알림 모니터링**: 앱 알림 내용 수집
- **키보드 입력 모니터링**: 사용자 입력 패턴 분석 (접근성 서비스 사용)
- **이메일 모니터링**: 이메일 수신 내용 수집
- **AI 기반 추천**: Gemini 또는 ChatGPT를 사용한 지능형 추천
  - 약속 리마인더
  - 운동 추천
  - 메시지/이메일 보내기 알림
  - 전화기 설정 변경 안내
- **오버레이 UI**: 추천 사항을 화면 오버레이로 표시
- **Web UI**: WebView 기반 현대적인 웹 인터페이스

## 설정 방법

1. **API 키 설정**
   - 설정 화면에서 Gemini 또는 ChatGPT API 키를 입력하세요
   - LLM Provider를 선택하세요

2. **권한 설정**
   - SMS 수신 권한
   - 알림 접근 권한 (Notification Listener Service 활성화)
   - 접근성 서비스 활성화 (키보드 모니터링)
   - 오버레이 권한 (설정 > 앱 > 특별 액세스 > 다른 앱 위에 표시)

3. **모니터링 활성화**
   - 설정에서 각 모니터링 기능을 활성화/비활성화할 수 있습니다

## 빌드 방법

```bash
./gradlew assembleDebug
```

## 기술 스택

- Kotlin
- Android SDK
- Room Database
- OkHttp
- WebView
- Accessibility Service
- Notification Listener Service
- Gemini API / ChatGPT API

## 주의사항

- 이 앱은 사용자의 개인 정보를 수집합니다. 사용 전 개인정보 보호정책을 확인하세요.
- 접근성 서비스와 알림 리스너 서비스는 수동으로 활성화해야 합니다.
- API 키는 안전하게 보관하세요.
