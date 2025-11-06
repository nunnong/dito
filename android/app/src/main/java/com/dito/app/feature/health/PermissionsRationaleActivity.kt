package com.dito.app.feature.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Health Connect 개인정보처리방침 및 권한 사용 근거 화면
 */
class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PermissionsRationaleScreen(
                    onClose = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionsRationaleScreen(onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("건강 데이터 권한 안내") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "앱에서 건강 데이터를 사용하는 이유",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Dito 앱은 다음과 같은 건강 데이터에 접근합니다:",
                style = MaterialTheme.typography.bodyLarge
            )

            PermissionItem(
                title = "걸음 수",
                description = "일일 활동량을 파악하여 건강한 생활 습관을 유도합니다."
            )

            PermissionItem(
                title = "심박수",
                description = "스트레스 수준을 모니터링하고 적절한 휴식을 권장합니다."
            )

            PermissionItem(
                title = "수면 데이터",
                description = "수면 패턴을 분석하여 더 나은 수면 습관을 제안합니다."
            )

            PermissionItem(
                title = "이동 거리",
                description = "일일 이동 활동을 추적하여 운동 목표 달성을 돕습니다."
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "데이터 처리 방침",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "• 수집된 데이터는 앱 내에서만 사용됩니다\n" +
                        "• 사용자의 동의 없이 제3자와 공유되지 않습니다\n" +
                        "• 언제든지 권한을 취소할 수 있습니다\n" +
                        "• 데이터는 안전하게 암호화되어 저장됩니다",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("확인")
            }
        }
    }
}

@Composable
private fun PermissionItem(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
