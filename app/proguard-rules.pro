# ProGuard / R8 Rules for Job Tracker

# Keep Room database classes and schemas
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Data Models & Entities
-keep class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.local.** { *; }
-keep class com.example.domain.model.** { *; }
-keepclassmembers class com.example.domain.model.** { *; }

# Keep ViewModels and State Holders
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Coroutines & Flow
-dontwarn kotlinx.coroutines.**
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# OkHttp & Moshi (used for Gemini API and JSON parsing)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class com.squareup.moshi.** { *; }
-keepattributes *JavascriptInterface*

# Optimize line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

