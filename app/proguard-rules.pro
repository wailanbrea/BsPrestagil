# ================================================================================================
# PROGUARD RULES PARA BSPRESTAGIL - PRODUCCIÓN
# ================================================================================================

# Mantener información de línea para stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Mantener anotaciones y signatures
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ================================================================================================
# KOTLIN & COROUTINES
# ================================================================================================
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# ================================================================================================
# JETPACK COMPOSE
# ================================================================================================
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# ================================================================================================
# ROOM DATABASE
# ================================================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep class com.example.bsprestagil.data.models.** { *; }
-keep class com.example.bsprestagil.data.dao.** { *; }
-keep class com.example.bsprestagil.data.database.** { *; }

# ================================================================================================
# FIREBASE
# ================================================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Functions
-keep class com.google.firebase.functions.** { *; }

# ================================================================================================
# GSON (Para serialización)
# ================================================================================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Mantener modelos de datos para GSON
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ================================================================================================
# SECURITY CRYPTO
# ================================================================================================
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ================================================================================================
# BIOMETRIC
# ================================================================================================
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ================================================================================================
# WORKMANAGER
# ================================================================================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# ================================================================================================
# CAMERAX
# ================================================================================================
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ================================================================================================
# ZXING (QR Codes)
# ================================================================================================
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.journeyapps.barcodescanner.**

# ================================================================================================
# COIL (Image Loading)
# ================================================================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ================================================================================================
# iTextPDF (Generación de PDFs)
# ================================================================================================
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ================================================================================================
# PAGING 3
# ================================================================================================
-keep class androidx.paging.** { *; }
-dontwarn androidx.paging.**

# ================================================================================================
# NAVIGATION
# ================================================================================================
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# ================================================================================================
# MODELOS DE LA APP
# ================================================================================================
-keep class com.example.bsprestagil.data.models.** { *; }
-keepclassmembers class com.example.bsprestagil.data.models.** { *; }

# ================================================================================================
# VIEWMODELS
# ================================================================================================
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# ================================================================================================
# ENUMS
# ================================================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================================================================================================
# PARCELABLE
# ================================================================================================
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ================================================================================================
# SERIALIZABLE
# ================================================================================================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ================================================================================================
# REMOVER LOGS EN PRODUCCIÓN
# ================================================================================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ================================================================================================
# OPTIMIZACIONES
# ================================================================================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ================================================================================================
# WARNINGS A IGNORAR
# ================================================================================================
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn org.slf4j.**
-dontwarn org.apache.**