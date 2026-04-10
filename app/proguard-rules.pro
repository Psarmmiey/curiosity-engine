# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.curiosityengine.app.**$$serializer { *; }
-keepclassmembers class com.curiosityengine.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.curiosityengine.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Supabase SDK
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Domain models (must not be obfuscated for Room/serialization)
-keep class com.curiosityengine.app.data.model.** { *; }
-keep class com.curiosityengine.app.data.local.entity.** { *; }
-keep class com.curiosityengine.app.data.remote.dto.** { *; }

# Coil
-dontwarn coil.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Glance widget
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# Google Fonts
-dontwarn com.google.android.gms.fonts.**

# Timber
-dontwarn org.slf4j.**
