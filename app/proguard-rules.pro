# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

### sudoku app main proguard ###
-keep class kr.co.hs.sudoku.model.** { *; }
-keep interface kr.co.hs.sudoku.model.** { *; }
# 내부 private data 클래스
-keep class kr.co.hs.sudoku.datasource.impl.StageRemoteSourceImpl$DataList { *; }
-keep class kr.co.hs.sudoku.datasource.impl.StageRemoteSourceFromConfig$DataList { *; }
### sudoku app main proguard ###



# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE


-dontoptimize
#-keepattributes SourceFile, LineNumberTable

#-keep class com.jiran.xkeeperMobile.databinding.* { *; }
#-keep class com.jiran.xkeeperMobile.databinding.ActivityLoginBindingImpl { *; }

-renamesourcefileattribute ''
-keepattributes SourceFile, LineNumberTable,Signature,Exceptions,InnerClasses,EnclosingMethod, *Annotation*
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

#다음지도
-keepattributes Signature
-keepattributes Annotation



-keep class okhttp3.* { *; }
-keep interface okhttp3.* { *; }
-dontwarn okhttp3.*


-dontwarn com.google.firebase.messaging.TopicOperation$TopicOperations
-dontwarn com.google.firebase.**
-dontwarn kotlin.Cloneable$DefaultImpls

-keepclasseswithmembernames class * {
    native <methods>;
}



##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class * implements com.google.gson.GsonBuilder

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
##---------------End: proguard configuration for Gson  ----------




# The native PGS library wraps the Java PGS SDK using reflection.
-dontobfuscate
-keeppackagenames

# Needed for callbacks.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Needed for helper libraries.
-keep class com.google.example.games.juihelper.** {
  public protected *;
}
-keep class com.sample.helper.** {
  public protected *;
}

# Needed for GoogleApiClient and auth stuff.
-keep class com.google.android.gms.common.api.** {
  public protected *;
}

# Keep all of the "nearby" library, which is needed by the native PGS library
# at runtime (though deprecated).
-keep class com.google.android.gms.nearby.** {
  public protected *;
}

# Keep all of the public PGS APIs.
-keep class com.google.android.gms.games.** {
  public protected *;
}



##---Firebase start---##
####auth
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.okhttp.**
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

### admob
-keep public class com.google.firebase.analytics.FirebaseAnalytics {
    public *;
}

-keep public class com.google.android.gms.measurement.AppMeasurement {
    public *;
}
##---Firebase end---##