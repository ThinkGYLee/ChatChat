# AGENTS.md

## 📂 프로젝트 구조 및 모듈 역할
- `app/`: Android 앱 진입점, DI 설정, Compose navigation 관리
- `feature/`: UI 기능 모듈 (Compose 화면, ViewModel) → `core`, `domain`, `util` 의존
- `domain/`: 비즈니스 로직 (use case, model, repository interface), 단위 테스트는 `domain/src/test` 에 위치
- `data/`: 데이터 계층 (Room, Firebase, repository 구현체, DI 모듈)
- `core/`: 공통 기반 클래스 (예: `BaseViewModel`, state helper)
- `util/`: 공용 유틸리티 (네트워크, 시간, URL 탐지 등)
- `build-logic/`: Gradle convention plugin (Hilt, Room, Compose 등)

---

## ⚙️ 빌드, 테스트, 개발 명령어
- 디버그 APK 빌드: `./gradlew :app:assembleDebug`
- 단위 테스트 실행: `./gradlew test` (특정 모듈만: `:domain:test`)
- 계측 테스트 실행: `./gradlew :app:connectedDebugAndroidTest`
- Lint 검사: `./gradlew lint`
- 코드 포맷팅: `./gradlew spotlessApply` (검사만: `spotlessCheck`)
- 클린 빌드: `./gradlew clean`

---

## ✍️ 코딩 스타일 및 네이밍 규칙
- **기본**
  - Kotlin(JDK 17), Jetpack Compose, Hilt DI, Room, Firebase 사용
  - Spotless + ktlint 기반 코드 포맷팅, 2칸 들여쓰기, 파일 마지막은 newline 추가
  - 하나의 파일에는 하나의 최상위 선언만, 테스트 파일은 `Test.kt`로 끝남
  - 패키지 구조는 feature/domain 경계를 반영 (예: `com.gyleedev.feature.chatroom`)

- **Compose**
  - Composable 함수명: `PascalCase`
  - 파라미터: `camelCase`
  - Preview 함수: `Preview` 접미사 사용

- **Kotlin**
  - 함수명: `camelCase`
  - 클래스명: `PascalCase`
  - 상수: `UPPER_SNAKE_CASE`

- **Gradle**
  - Kotlin DSL (`.kts`)만 사용
  - 버전 관리: **Version Catalogs** (`libs.versions.toml`)
  - 공통 빌드 로직: `:build-logic`

- **Room**
  - 모든 외래키에는 반드시 `indices` 지정
  - `exportSchema = true` 설정 유지

---

## 🧪 테스트 규칙
- 프레임워크: JUnit5, Coroutines Test, MockK
- 위치: 단위 테스트 → `*/src/test`, 계측 테스트 → `*/src/androidTest`
- 네이밍: `<Subject>Test.kt`, 테스트 메서드는 `Given_When_Then` 또는 `should` 스타일
- 새로운 use case, repository 동작에는 반드시 테스트 추가
- 빠른 실행: `./gradlew :domain:test`

---

## 🔒 보안 및 설정
- 개인 키나 민감 정보는 절대 커밋하지 않음
- `google-services.json`과 API 키는 개발 환경용만 저장
- `local.properties`는 로컬에만 유지
- Room schema 파일은 `data/schemas/` 디렉토리에 저장

---

## 🔗 Commit & Pull Request 규칙
- Commit 메시지: 간결하고 명령형으로 작성, 관련된 변경 묶어서 기록
  - 예시:
    - `ChatRoomViewModel: add participants to state`
    - `MessageDao: make getLastMessage nullable`
- Pull Request:
  - 명확한 설명 작성
  - 관련 issue 연결
  - 변경 사항 목록 작성
  - UI 변경 시 스크린샷 또는 gif 첨부
  - 테스트 방법 포함
- PR 전 체크리스트:
  - `spotlessCheck`, `test`, `assembleDebug` 모두 통과
  - secrets, debug 로그 포함되지 않음

### 📝 Commit 메시지 포맷
<Type>(Scope) : Subject
<BLANK LINE>
Body (선택)
<BLANK LINE>
Tail (선택)

#### 1) Type (필수, **영문 키워드**)
- **Feat**: 새로운 기능 추가
- **Fix**: 버그 수정
- **Design**: UI 변경
- **Style**: 코드 포맷팅/린트 수정
- **Refactor**: 리팩터링
- **Comment**: 주석 추가/수정
- **Docs**: 문서 작성/수정
- **Test**: 테스트 관련 작업
- **Chore**: 빌드/환경/도구 관련 변경
- **Rename**: 파일/폴더 이동 및 이름 변경
- **Remove**: 코드/파일 삭제
- **!BREAKING CHANGE**: 호환성 깨지는 변경
- **!HOTFIX**: 치명적 긴급 수정

#### 2) Scope (선택, **영문**)
- 영향 범위를 **모듈/기능명**으로 명확히 기재
  - 예: `app`, `feature:chat`, `data:room`, `domain:user`, `build-logic`

#### 3) Subject (필수, **영문**)
- 50자 이내, **대문자 시작 + 동사 원형**
- 마침표 금지
- 예: `Feat(ChatList) : Add ChatListScreen`

#### 4) Body (선택, **한국어 권장**)
- 상세 구현 내용은 **한국어로 작성**
- “무엇을/왜”를 구체적으로 설명
- 단락/불릿 사용 가능
- 변경 이유, 대안, 영향도를 포함

#### 5) Tail (선택, **영문 키워드 + 이슈번호**)
- 이슈 연결 정보 기재 (영문 키워드 사용)
  - `Resolves: #61` (자동 close)
  - `Related to: #50, #57`
  - `Ref: #45`

#### ✅ 예시
Feat(ChatList) : Add ChatListScreen

채팅 리스트 화면 UI 구성

아이템 클릭 시 채팅방 진입 네비게이션 추가

페이징 처리를 위한 ViewModel 스텁 준비

Resolves: #61
Ref: #45
Related to: #50, #57

---

## 🤖 Codex AI 답변 지침
> 이 섹션은 Codex CLI가 답변할 때 지켜야 할 규칙을 정의합니다.

### 언어 & 출력 형식
- 모든 답변은 **한국어**로 작성
- 코드 출력은 반드시 하나의 코드블록(```kotlin, ```gradle, ```xml 등)으로 제공
- 설명은 코드 뒤에 **한 줄 요약**으로 작성

### 아키텍처 규칙
- 클린 아키텍처 원칙 준수
  - `:feature`, `:data` → `:domain` 의존
  - `:domain`은 다른 모듈에 의존하지 않음
- Compose UI는 `@Composable` 단위로 작성
- 상태 관리는 ViewModel에서 수행, UI는 StateFlow 구독

### 테스트 규칙
- JUnit5 + MockK 사용
- 성공/실패 케이스 모두 포함
- Firebase, Room 등 외부 의존성은 Mock 또는 Fake로 대체

### 스타일 규칙
- Kotlin 함수명: `camelCase`
- Composable 함수: `PascalCase`
- Gradle 스크립트: Kotlin DSL (`build.gradle.kts`)
- AndroidX/Compose는 최신 안정 버전 가정

### 답변 예시 형식
```kotlin
fun example() {
    println("Hello Codex")
}
