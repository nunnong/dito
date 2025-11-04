package com.dito.app.ui.screen.settings

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*

@Composable
fun SettingTab() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = Color(0xFFFFFFFF),
            )
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 1.dp,)
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFFFFFF),
                    )
                    .padding(vertical = 6.dp,)
            ){
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                ){
                    Text("설정",
                        color = Color(0xFF000000),
                        fontSize = 32.sp,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(bottom = 1.dp,)
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFFFFFF),
                    )
                    .padding(16.dp)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp,horizontal = 10.dp,)
                ){
                    Text("내 정보",
                        color = Color(0xFF000001),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp,)
                ){
                    Text("닉네임 변경",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "닉네임 변경",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp,)
                ){
                    Text("미션 빈도 변경",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "미션 빈도 변경",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp,)
                ){
                    Text("서비스 이용약관",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "서비스 이용약관",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp,)
                ){
                    Text("개인정보 처리방침",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "개인정보 처리방침",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp,)
                ){
                    Text("문의하기",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "문의하기",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(bottom = 1.dp,)
                        .fillMaxWidth()
                        .padding(vertical = 19.dp,horizontal = 10.dp,)
                ){
                    Text("로그아웃",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}
