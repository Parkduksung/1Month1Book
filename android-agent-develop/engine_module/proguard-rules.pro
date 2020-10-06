# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Hyungu-PC\AppData\Local\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-libraryjars ./libs/knoxsdk.jar
#-libraryjars ./libs/supportlib.jar
#-libraryjars ./libs/vncremote.jar
#
#
#-dontoptimize
#-dontpreverify
#
#-dontwarn com.samsung.**
#-keepnames class com.samsung.** { *; }
#-keep class com.samsung.** { *; }
#-keepnames interface com.samsung.** { *; }
#-keep interface com.samsung.** { *; }
#-keep enum com.samsung.** { *; }
#-keepclassmembers class com.samsung.** { *; }
-keep public class com.samsung.android.knox.** { *; }
-dontwarn com.samsung.android.knox.**