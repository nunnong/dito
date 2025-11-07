package com.dito.app.feature.health

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dito.app.core.data.health.HealthData
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Health Connect 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        // 권한 설정 후 돌아왔을 때 권한 재확인
        viewModel.checkPermissions()
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("건강 데이터") }
            )
        }
    ) { paddingValues ->
        when {
            !uiState.isHealthConnectAvailable -> {
                HealthConnectNotAvailableContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            !uiState.hasPermissions -> {
                PermissionRequiredContent(
                    modifier = Modifier.padding(paddingValues),
                    onRequestPermission = {
                        // Health Connect 권한 요청
                        permissionLauncher.launch(viewModel.getRequiredPermissions())
                    }
                )
            }

            uiState.isLoading -> {
                LoadingContent(modifier = Modifier.padding(paddingValues))
            }

            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    modifier = Modifier.padding(paddingValues),
                    onRetry = { viewModel.loadTodayHealthData() }
                )
            }

            else -> {
                HealthDataContent(
                    healthData = uiState.healthData,
                    modifier = Modifier.padding(paddingValues),
                    onRefresh = { viewModel.loadTodayHealthData() }
                )
            }
        }
    }
}

@Composable
private fun HealthConnectNotAvailableContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Health Connect를 사용할 수 없습니다",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Health Connect 앱을 설치해주세요",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                }
                context.startActivity(intent)
            }
        ) {
            Text("Health Connect 설치하기")
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "건강 데이터 권한 필요",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "걸음 수, 심박수, 수면, 이동거리 데이터를 읽기 위해 권한이 필요합니다.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("권한 허용하기")
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "오류 발생",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}

@Composable
private fun HealthDataContent(
    healthData: HealthData?,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "오늘의 건강 데이터",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (healthData == null) {
            Text("데이터가 없습니다")
        } else {
            // 걸음 수
            healthData.steps?.let { steps ->
                HealthDataCard(
                    title = "걸음 수",
                    value = "${steps.count} 걸음",
                    subtitle = "오늘"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 심박수
            healthData.heartRate?.let { heartRate ->
                val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
                HealthDataCard(
                    title = "심박수",
                    value = "${heartRate.beatsPerMinute} BPM",
                    subtitle = "최근 측정: ${heartRate.time.atZone(java.time.ZoneId.systemDefault()).format(formatter)}"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 수면
            healthData.sleep?.let { sleep ->
                val hours = sleep.durationMinutes / 60
                val minutes = sleep.durationMinutes % 60
                HealthDataCard(
                    title = "수면",
                    value = "${hours}시간 ${minutes}분",
                    subtitle = "${sleep.stages.size}개 수면 단계"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 이동 거리
            healthData.distance?.let { distance ->
                val kilometers = distance.distanceMeters / 1000.0
                HealthDataCard(
                    title = "이동 거리",
                    value = String.format("%.2f km", kilometers),
                    subtitle = "오늘"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Button(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("새로고침")
        }
    }
}

@Composable
private fun HealthDataCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
