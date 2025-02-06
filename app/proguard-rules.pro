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

# Firebase SDK optimizations
# Keep FirebaseAuth
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Keep FirebaseDatabase
-keep class com.google.firebase.database.** { *; }
-dontwarn com.google.firebase.database.**

# Glide optimizations
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Keep WebRTC related classes for Agora SDKs
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# Keep ViewModel classes
-keep class androidx.lifecycle.ViewModel { *; }
-dontwarn androidx.lifecycle.ViewModel

# Keep AndroidX navigation components
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Keep AppCompat and Material components
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep classes for Glide image loading
-keep class com.bumptech.glide.load.model.** { *; }

# Keep the public methods of your custom classes (e.g., if using ViewModels or Custom Views)
-keepclassmembers class * {
    public <methods>;
}

# Preserve the line number and source file name for debugging
-keepattributes SourceFile,LineNumberTable
