# Android profile

- Gradle 명령과 module 경계는 각 소비 repository의 `AGENTS.md`를 정본으로 사용합니다.
- Kotlin/Android 변경은 기존 architecture, lifecycle, coroutine, persistence 계약을 우선합니다.
- dependency 추가는 필요성과 대안을 먼저 설명합니다.
- 가능한 경우 unit test, lint와 관련 assemble task를 실행합니다.
