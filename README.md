# Chatting Application
파이어베이스를 사용한 오프라인 우선 방식의 채팅 앱입니다.

## 주요 구현
- 회원가입 / 로그인
- 메세지 전송 / 수신
- 프로필 변경
- 친구 검색 / 추가
- 미디어 전송 / 수신
- 프로필 수정

## 기술 스택
| 구분 | 내용 |
| --- | --- |
| Jetpack | Navigation, Compose, Lifecycle, ViewModel, Paging3, Room |
| Asynchronous Processing | Coroutine, Flow |
| Dependency Injection | Hilt |
| Third Party Library | Glide |
| Architecture | MVVM |
| Other | Firebase |

## 스크린샷
| 친구 목록 | 채팅 목록 | 내 프로필 |
| --- | --- | --- |
| <img src="https://github.com/user-attachments/assets/d47cfe67-b3ac-46d8-927e-0a69f14524ff" width="200"/> | <img src="https://github.com/user-attachments/assets/fc9d09d3-517b-4499-b99f-5861ac9dff1f" width="200"/> | <img src="https://github.com/user-attachments/assets/35596663-cafd-45cf-8f91-002669ae09b8" width="200"/> |

| 내 프로필 수정 | 친구 프로필 | 채팅 |
| --- | --- | --- |
| <img src="https://github.com/user-attachments/assets/304831bd-11f0-46c9-a8a6-7466953d125c" width="200"/> | <img src="https://github.com/user-attachments/assets/76c57554-4634-467e-8963-a96ca4ef9748" width="200"/> | <img src="https://github.com/user-attachments/assets/74e9f9d2-56f6-4479-ac51-77790863d5f6" width="200"/> |

| 회원가입 | 로그인 | 친구 검색 |
| --- | --- | --- |
| <img src="https://github.com/user-attachments/assets/04717381-fbdf-4cba-868a-a136ba2131ac" width="200"/> | <img src="https://github.com/user-attachments/assets/8b77d359-c1bf-4a2f-9f58-0313de961c90" width="200"/> | <img src="https://github.com/user-attachments/assets/6e918fb5-f07a-4b1a-85a3-b6284a46d557" width="200"/> |

## 주요 동영상
| 회원가입 | 로그인 | 프로필 변경 |
| --- | --- | --- |
| <img src="https://github.com/user-attachments/assets/0f1506cb-e9d4-4563-b5f6-5b0f77b1d612" width="200"/> | <img src="https://github.com/user-attachments/assets/626ed7f0-0c90-4852-9abb-a9e68108ac04" width="200"/> | <img src="https://github.com/user-attachments/assets/85534677-a04d-48c0-a115-f0f4856fef76" width="200"/> |

| 메세지 송신 | 친구 검색 |
| --- | --- |
| <img src="https://github.com/user-attachments/assets/c1a8eea2-54fa-42e6-8c38-3c5b9cc50c85" width="200"/> | <img src="https://github.com/user-attachments/assets/e0e7035c-99f0-47ae-bd19-f31d07d6bad4" width="200"/> |

## Firebase를 프로젝트에 추가

### 프로젝트 요구사항 확인

- API 수준 21(Lollipop) 이상 타겟팅
- Android 5.0 이상 사용
- 다음 버전 요구사항을 충족하는 [Jetpack(AndroidX)](https://developer.android.com/jetpack/androidx/migrate?hl=ko) 사용
    - `com.android.tools.build:gradle` v7.3.0 이상
    - `compileSdkVersion` 28 이상

### Firebase Console을 이용하여 Firebase 추가

- Firebase 프로젝트를 만들기
- Firebase 프로젝트에 앱을 등록하기
- Firebase 구성 파일 추가
    - Firebase Android 구성 파일(`google-services.json`)을 다운로드한 후 앱에 추가한다.
        - **루트 수준(프로젝트 수준)** Gradle 파일에서 Google 서비스 플러그인을 종속 항목으로 추가합니다.

            ```kotlin
            plugins {
              id("com.android.application") version "7.3.0" apply false
              // ...
            
              // Add the dependency for the Google services Gradle plugin
              id("com.google.gms.google-services") version "4.4.2" apply false
            }
            ```

        - **모듈(앱 수준)** Gradle 파일에서 Google 서비스 플러그인을 추가합니다.

        ```kotlin
        plugins {
          id("com.android.application")
        
          // Add the Google services Gradle plugin
          id("com.google.gms.google-services")
          // ...
        }
        ```

        - 앱에 FirebaseSdk를 추가한다

        ```kotlin
        dependencies {
          // ...
        
          // Import the Firebase BoM
          implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
        
          // When using the BoM, you don't specify versions in Firebase library dependencies
        
          // Add the dependency for the Firebase SDK for Google Analytics
          implementation("com.google.firebase:firebase-analytics")
        
          // TODO: Add the dependencies for any other Firebase products you want to use
          // See https://firebase.google.com/docs/android/setup#available-libraries
          // For example, add the dependencies for Firebase Authentication and Cloud Firestore
          implementation("com.google.firebase:firebase-auth")
          implementation("com.google.firebase:firebase-firestore")
      }
        
        ```


[더 자세한 내용은 Firebase 홈페이지에서 확인하세요](https://www.notion.so/Android-a2edd7544d854c9a82f95bafb97c2c31?pvs=21)

---

## Realtime database 세팅

Firebase를 프로젝트에 추가 후에 진행합니다.

1. [Firebase Console](https://console.firebase.google.com/project/_/database?hl=ko)의 **Realtime Database** 섹션으로 이동합니다. 기존 Firebase 프로젝트를 선택하라는 메시지가 표시됩니다. 데이터베이스 만들기 워크플로를 따릅니다.
2. Firebase Security Rules의 시작 모드를 선택합니다.

   테스트 모드로 시작해서 완료 후에는 데이터베이스 규칙을 정해야 합니다.

3. 데이터베이스의 위치를 선택합니다.

   [데이터베이스 위치](https://firebase.google.com/docs/projects/locations?hl=ko#rtdb-locations)에 따라 새 데이터베이스의 URL이 다음 형식 중 하나로 지정됩니다.

    - **`DATABASE_NAME**.firebaseio.com`(`us-central1`의 데이터베이스)
    - **`DATABASE_NAME**.**REGION**.firebasedatabase.app`(다른 모든 위치의 데이터베이스)
    - 주의점
        - us-central1 이외의 위치의 데이터베이스의 인스턴스를 만들어 액세스 할때는 주소를 넣는다.

        ```kotlin
        //us-central1
        val database = Firebase.database
        val myRef = database.getReference("message")
        
        //other
        //database의 주소는 realtime database의 console에서 확인한다.
        //val database = firebase.database("database 주소")
        ```

- **모듈(앱 수준) Gradle 파일**에서 Android용 Realtime Database 라이브러리의 종속 항목을 추가합니다. 버전 관리 제어에는 Firebase Android BoM 사용이 권장됩니다.

```kotlin
dependencies {
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")
}
```

[더 자세한 내용은 Realtime Database 홈페이지에서 확인하세요](https://www.notion.so/Google-Sign-In-794b0927940c4a91a3344a016c7dbdac?pvs=21)

---

## Firebase Storage 세팅

Firebase를 프로젝트에 추가 후에 사용할 수 있습니다.

### 기본 Cloude Stroage 버킷 만들기

1. [Firebase Console](https://console.firebase.google.com/?hl=ko)의 탐색창에서 **Storage**를 선택한다.
2. **시작하기**를 클릭한다.
3. 기본 버킷 [위치](https://firebase.google.com/docs/storage/locations?hl=ko)를 선택한다.
4. 기본 버킷의 Firebase Security Rules를 구성합니다.
5. **완료**를 클릭합니다.
6. 기본 버킷의 이름을 확인할 수 있다.
    1. 이름 형식은 **`PROJECT_ID**.firebasestorage.app`

### 공개 액세스 설정을 한다.

[공개 액세스 규칙 관련 페이지](https://firebase.google.com/docs/storage/security/rules-conditions?hl=ko&_gl=1*1kywhim*_up*MQ..*_ga*MjA2OTA5OTE1Mi4xNzQzMTI2NDA1*_ga_CW55HF8NVT*MTc0MzEyNjQwNC4xLjAuMTc0MzEyNjU1Mi4wLjAuMA..#public)

### 앱에 Cloud Storage Sdk 추가

```kotlin
dependencies {
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))

    // Add the dependency for the Cloud Storage library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-storage")
}
```

### 앱에서 Cloude Storage 설정

1. 앱의 코드베이스에 있는 Firebase 구성 파일(`google-services.json`)이 기본 Cloud Storage 버킷의 이름으로 업데이트되었는지 확인하기
2. FirebaseStorage 인스턴스를 만들어 Cloud Storage 버킷에 액세스하기

```kotlin
val storage = Firebase.storage

// Alternatively, explicitly specify the bucket name URL.
// val storage = Firebase.storage("gs://BUCKET_NAME")
```

[더 자세한 내용은 Cloude Storage 페이지에서 확인하세요](https://www.notion.so/Android-f5334d5ddc104e9dbce50c72658667fb?pvs=21)