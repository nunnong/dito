package com.dito.app.core.ui.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dito.app.R

// 둥근모꼴
val DungGeunMo = FontFamily(
    Font(R.font.dunggeunmo, FontWeight.Normal)
)

// KoPub 돋움체
val KoPubDotum = FontFamily(
    Font(R.font.kopub_dotum_light, FontWeight.Light),
    Font(R.font.kopub_dotum_medium, FontWeight.Medium),
    Font(R.font.kopub_dotum_bold, FontWeight.Bold)
)

val DitoTypography = Typography(

    displayLarge = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),

    displayMedium = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),

    displaySmall = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

     headlineLarge = TextStyle(
         fontFamily = DungGeunMo,
         fontWeight = FontWeight.Normal,
         fontSize = 32.sp,
         lineHeight = 40.sp,
         letterSpacing = 0.sp
     ),

    headlineMedium = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    titleLarge = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.0094).em
    ),

    titleSmall = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.0071).em
    ),

    bodyLarge = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.03125).em
    ),

    bodyMedium = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.0179).em
    ),

    bodySmall = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.0333).em
    ),

    labelLarge = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.0071).em
    ),

    labelMedium = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.0417).em
    ),

    labelSmall = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.0455).em
    )
)

// 커스텀 TextStyle 정의
object DitoCustomTextStyles {
    // 둥근모꼴 Title 스타일
    val titleDLarge = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    val titleDMedium = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.0094).em
    )

    val titleDSmall = TextStyle(
        fontFamily = DungGeunMo,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.0071).em
    )

    // KoPub 돋움체 Title 스타일
    val titleKLarge = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    val titleKMedium = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.0094).em
    )

    val titleKSmall = TextStyle(
        fontFamily = KoPubDotum,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.0071).em
    )
}