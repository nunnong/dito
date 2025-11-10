# 스크린타임 동기화 통합 가이드

## 개요
Android 앱에서 자동으로 스크린타임 데이터를 수집하여 Backend API로 전송하고, Backend가 MongoDB에 저장합니다.

## 아키텍처
```
Android (15분마다)
  ├─ 1. 로컬 Realm 저장 (오프라인 대비)
  └─ 2. Backend API 호출
       └─ Backend → MongoDB 저장
```

## 필요한 설정 작업

### 1. WorkManager 초기화
앱 시작 시 `MainActivity` 또는 `Application` 클래스에서 WorkManager를 설정해야 합니다.

```kotlin
// MainActivity.kt 또는 DitoApplication.kt
import com.dito.app.core.background.ScreenTimeSyncWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 스크린타임 동기화 시작 (15분 주기)
        ScreenTimeSyncWorker.setupPeriodicSync(this)
    }
}
```

### 2. 그룹 참여 시 active_group_id 저장
사용자가 그룹에 참여하면 `GroupPreferenceManager`를 사용하여 그룹 ID를 저장해야 합니다.

```kotlin
// GroupViewModel.kt 또는 그룹 참여 성공 시
import com.dito.app.core.storage.GroupPreferenceManager

fun onGroupJoinSuccess(groupId: Long) {
    // 활성 그룹 ID 저장
    GroupPreferenceManager.setActiveGroupId(context, groupId)

    // 즉시 한번 동기화 (선택사항)
    ScreenTimeSyncWorker.triggerImmediateSync(context)
}
```

### 3. 그룹 탈퇴 시 active_group_id 삭제
```kotlin
fun onGroupLeave() {
    // 활성 그룹 ID 삭제
    GroupPreferenceManager.clearActiveGroupId(context)
}
```

### 4. 권한 요청
스크린타임 수집을 위해 `PACKAGE_USAGE_STATS` 권한이 필요합니다.

```kotlin
// 권한 요청 UI 예시
import com.dito.app.core.util.UsageStatsPermissionHelper

Button(onClick = {
    UsageStatsPermissionHelper.requestPermission(context)
}) {
    Text("스크린타임 권한 허용")
}

// 권한 확인
val hasPermission = UsageStatsPermissionHelper.hasPermission(context)
```

## 데이터 흐름

### 저장 (Android → Backend → MongoDB)
1. Android: 15분마다 `ScreenTimeSyncWorker` 자동 실행
2. Android: 오늘 하루 총 스크린타임 수집 (분 단위)
3. Android: `POST /screen-time/update` API 호출
4. Backend: 데이터 받아서 MongoDB에 저장
   - `screen_time_daily_summary` 컬렉션 (일별 요약)
   - `screen_time_snapshots` 컬렉션 (스냅샷, 30일 TTL)

### 조회 (Android → Backend ← MongoDB)
1. Android: 랭킹 화면 접속
2. Android: `GET /challenges/groups/{groupId}/ranking` API 호출
3. Backend: MongoDB에서 데이터 조회 및 랭킹 계산
4. Backend: 결과 반환
5. Android: 화면에 표시

## 필요한 SharedPreferences 데이터

| Key | Type | 설명 | 저장 시점 |
|-----|------|------|----------|
| `user_id` | Long | 사용자 ID | 로그인 성공 시 (✅ 이미 구현됨) |
| `active_group_id` | Long | 활성 그룹 ID | 그룹 참여 성공 시 (⚠️ 구현 필요) |

## Backend API 엔드포인트

### 스크린타임 업데이트
```
POST /screen-time/update
Authorization: Bearer {token}
Content-Type: application/json

{
  "groupId": 123,
  "date": "2025-01-07",
  "totalMinutes": 480
}
```

### 그룹 랭킹 조회
```
GET /challenges/groups/{groupId}/ranking
Authorization: Bearer {token}
```

## 로그 확인
Android Studio Logcat에서 다음 태그로 필터링:
- `ScreenTimeSyncWorker`: 동기화 로그
- `ScreenTimeCollector`: 스크린타임 수집 로그
- `GroupPreferenceManager`: 그룹 ID 저장/조회 로그

## 주의사항
1. Android WorkManager의 최소 주기는 15분입니다 (5분 불가)
2. 스크린타임 수집을 위해 `PACKAGE_USAGE_STATS` 권한이 반드시 필요합니다
3. 그룹에 참여하지 않은 경우 스크린타임 동기화가 자동으로 스킵됩니다
4. 오프라인 시 로컬 Realm에 저장되고, 온라인 복구 시 자동으로 재시도됩니다
