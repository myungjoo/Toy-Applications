# APK 다운로드 방법

## 방법 1: GitHub Actions에서 다운로드 (권장)

1. GitHub 저장소로 이동: https://github.com/myungjoo/Toy-Applications
2. **Actions** 탭 클릭
3. 최근 실행된 **Build APK** 워크플로우 클릭
4. **Artifacts** 섹션에서 **app-debug-apk** 클릭하여 다운로드

## 방법 2: 로컬에서 빌드

### 사전 요구사항
- Android SDK 설치
- JDK 17 설치

### 빌드 명령어

```bash
cd /workspace/webagent

# local.properties 파일 생성
echo "sdk.dir=/path/to/your/android/sdk" > local.properties

# 디버그 APK 빌드
./gradlew assembleDebug
```

빌드된 APK 위치:
- `app/build/outputs/apk/debug/app-debug.apk`

### 설치 방법

1. Android 기기에서 **설정 > 보안 > 알 수 없는 소스 허용** 활성화
2. APK 파일을 기기로 전송
3. 파일 관리자에서 APK 파일 실행하여 설치

## 참고사항

- 디버그 APK는 서명되지 않았으므로 설치 시 경고가 표시될 수 있습니다
- 릴리즈 APK는 서명 키가 필요합니다
- GitHub Actions에서 자동으로 빌드된 APK는 30일간 보관됩니다
