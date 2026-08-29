# ==============================================================================
# Arrow Escape ProGuard & R8 Optimization Rules
# ==============================================================================

# Preserve debugging information in release stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ------------------------------------------------------------------------------
# Kotlin & Coroutines
# ------------------------------------------------------------------------------
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }

# ------------------------------------------------------------------------------
# AndroidX Room Database & Entities
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract <methods>;
}
-keep class com.mitsara.arrowescape.data.** { *; }
-keepclassmembers class com.mitsara.arrowescape.data.** { *; }

# ------------------------------------------------------------------------------
# Game Models & State
# ------------------------------------------------------------------------------
-keep class com.mitsara.arrowescape.model.** { *; }
-keepclassmembers class com.mitsara.arrowescape.model.** { *; }
-keep enum com.mitsara.arrowescape.model.** { *; }

# ------------------------------------------------------------------------------
# Monetization & Ads
# ------------------------------------------------------------------------------
-keep class com.mitsara.arrowescape.monetization.** { *; }
-keepclassmembers class com.mitsara.arrowescape.monetization.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keepinterface com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ------------------------------------------------------------------------------
# Android Architecture Components & ViewModels
# ------------------------------------------------------------------------------
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(...);
}
-keep class com.mitsara.arrowescape.ui.viewmodel.** { *; }

# ------------------------------------------------------------------------------
# Audio & System Services
# ------------------------------------------------------------------------------
-keep class com.mitsara.arrowescape.audio.** { *; }
-keepclassmembers class com.mitsara.arrowescape.audio.** { *; }

