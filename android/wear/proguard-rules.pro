# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Wear OS classes
-keep class androidx.wear.** { *; }
-keep interface androidx.wear.** { *; }

# Keep Health Services classes
-keep class androidx.health.services.** { *; }

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep data classes
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep Compose classes
-keep class androidx.compose.** { *; }
