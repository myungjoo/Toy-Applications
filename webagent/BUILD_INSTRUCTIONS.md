# APK 빌드 방법

## 사전 요구사항

1. Android SDK 설치
2. `local.properties` 파일 생성:
   ```
   sdk.dir=/path/to/your/android/sdk
   ```

## 빌드 명령어

```bash
./gradlew assembleDebug
```

빌드된 APK는 다음 위치에 생성됩니다:
- `app/build/outputs/apk/debug/app-debug.apk`

## 릴리즈 빌드

```bash
./gradlew assembleRelease
```

릴리즈 APK는 다음 위치에 생성됩니다:
- `app/build/outputs/apk/release/app-release.apk`
