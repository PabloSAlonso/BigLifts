# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Supabase models
-keep class com.biglifts.workouttracker.data.models.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class com.biglifts.workouttracker.** {
    *** Companion;
}
-keepclasseswithmembers class com.biglifts.workouttracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Supabase
-keep class io.github.jan.supabase.** { *; }

# General Android rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep class * implements java.lang.annotation.Annotation { *; }

# Keep data classes
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}