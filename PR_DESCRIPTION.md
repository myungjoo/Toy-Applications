# WebAgent Android 앱 구현

## 개요

스마트폰 활동을 모니터링하고 AI 기반 추천을 제공하는 Android 애플리케이션입니다.

## 주요 기능

### 1. 데이터 수집
- **SMS 모니터링**: 수신/발신 SMS 자동 수집
- **알림 모니터링**: 앱 알림 내용 수집 (Notification Listener Service)
- **키보드 입력 모니터링**: 사용자 입력 패턴 분석 (Accessibility Service)
- **이메일 모니터링**: 이메일 수신 내용 수집

### 2. AI 기반 추천 시스템
- **Gemini API** 및 **ChatGPT API** 지원
- 다음 유형의 추천 제공:
  - ⏰ 약속 리마인더
  - 💪 운동 추천
  - 💬 메시지/이메일 보내기 알림
  - ⚙️ 전화기 설정 변경 안내

### 3. 사용자 인터페이스
- **WebView 기반 Web UI**: 현대적인 웹 인터페이스
- **오버레이 UI**: 추천 사항을 화면 오버레이로 표시
- **설정 화면**: API 키 및 모니터링 옵션 관리

### 4. 데이터 저장
- **Room Database**: 이벤트 및 추천 사항 영구 저장
- 효율적인 데이터 조회 및 관리

## 기술 스택

- **언어**: Kotlin
- **아키텍처**: Android SDK
- **데이터베이스**: Room Database
- **네트워크**: OkHttp
- **UI**: WebView, Material Design
- **서비스**: 
  - Accessibility Service (키보드 모니터링)
  - Notification Listener Service (알림 모니터링)
  - Foreground Service (데이터 수집)

## 빌드 방법

1. Android SDK 설치
2. `local.properties` 파일 생성:
   ```
   sdk.dir=/path/to/your/android/sdk
   ```
3. 빌드 실행:
   ```bash
   ./gradlew assembleDebug
   ```

APK는 `app/build/outputs/apk/debug/app-debug.apk`에 생성됩니다.

## 설정 방법

1. **API 키 설정**: 설정 화면에서 Gemini 또는 ChatGPT API 키 입력
2. **권한 설정**:
   - SMS 수신 권한
   - 알림 접근 권한 (설정 > 접근성 > 알림 접근)
   - 접근성 서비스 (설정 > 접근성 > WebAgent)
   - 오버레이 권한 (설정 > 앱 > 특별 액세스 > 다른 앱 위에 표시)
3. **모니터링 활성화**: 설정에서 각 기능 활성화/비활성화

## 파일 구조

```
/webagent/
├── app/
│   ├── src/main/
│   │   ├── java/com/webagent/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── SettingsActivity.kt
│   │   │   ├── data/          # 데이터베이스 및 모델
│   │   │   ├── network/       # LLM 클라이언트
│   │   │   └── service/       # 각종 서비스
│   │   ├── res/               # 리소스 파일
│   │   └── assets/            # HTML 파일
│   └── build.gradle.kts
├── build.gradle.kts
└── README.md
```

## 주의사항

- 이 앱은 사용자의 개인 정보를 수집합니다. 사용 전 개인정보 보호정책을 확인하세요.
- 접근성 서비스와 알림 리스너 서비스는 수동으로 활성화해야 합니다.
- API 키는 안전하게 보관하세요.

## 변경 사항

- WebView 기반 Web UI 구현
- SMS, 알림, 키보드 입력, 이메일 모니터링 기능 추가
- Room Database를 사용한 데이터 저장 구현
- Gemini/ChatGPT API 연동 및 AI 기반 추천 시스템 구현
- 오버레이 UI로 추천 사항 표시 기능 추가
- 설정 화면에서 API 키 및 모니터링 옵션 관리 기능 추가
