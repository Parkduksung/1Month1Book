-repackageclasses ''

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-dontshrink

-keepattributes Signature
-keepattributes InnerClasses
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable


-keep class sun.misc.Unsafe { *; }
-keep class com.rsupport.mobile.agent.dao.** {*;}

-keep public class * extends control
-keep public class rsupport.AndroidViewer.TitleBar
-keep public class rsupport.AndroidViewer.TitleBarRefresh
-keep public class rsupport.AndroidViewer.TitleBarSub
-keep public class rsupport.AndroidViewer.TitleBarSubCustom

-keep class com.google.** {*;}
-keep class org.eclipse.paho.client.** {*;}
-keep class com.rsupport.hxengine.** {*;}
-keep class com.rsupport.jni.** {*;}

-dontwarn android.app.**

-dontwarn org.xiph.speex.**
-dontwarn android.support.v4.**
-dontwarn org.eclipse.paho.client.mqttv3.**
-dontwarn org.apache.commons.logging.**
-dontwarn com.rsupport.knox.**
-dontwarn com.rsupport.media.**

-keep,allowshrinking class rsupport.AndroidViewer.SettingActivity {
    <fields>;
    <methods>;
}

-keep,allowshrinking class android.app.* {
    <fields>;
    <methods>;
}

-keep,allowshrinking class control.* {
    <fields>;
    <methods>;
}

-libraryjars ./libs/commons-codec-1.3.jar
-libraryjars ./libs/http_common_sy.jar
-libraryjars ./libs/httpmime-4.1.3.jar

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class android.app.enterprise.** {*;}
-keep class android.app.enterprise.remotecontrol.** {*;}


-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class com.rsupport.android.engine.install.gson.dto.EngineGSon.** {*;}

-keepclasseswithmembers class * implements com.rsupport.android.engine.install.gson.IGSon {
  *;
}


-keepclasseswithmembers class com.rsupport.android.engine.install.config.Configuration {
  *;
}

-keepclasseswithmembers class com.rsupport.android.engine.install.sort.MarketSort {
  *;
}

-keepclasseswithmembers class com.rsupport.android.engine.install.EngineContextFactory{
  *;
}

-keepclasseswithmembers class com.rsupport.android.engine.install.exception.InstallException{
  *;
}

-keepclasseswithmembers class com.rsupport.android.engine.install.finder.AbstractEngineFinder{
  *;
}

-keepclasseswithmembers class com.rsupport.android.engine.install.finder.Parameter{
  *;
}

-keep interface com.rsupport.android.engine.install.finder.IFinder {
  *;
}

-keep interface com.rsupport.android.engine.install.IEngineContext {
  *;
}

-keep interface com.rsupport.android.engine.install.installer.IEngineInstaller {
  *;
}

-keep interface com.rsupport.android.engine.install.installer.IEngineInstaller$OnInstallEventListener {
  *;
}

-keep interface com.rsupport.android.engine.install.installer.IEngineInstaller$OnDownloadEventListener {
  *;
}

-keep interface com.rsupport.android.engine.install.installer.IEngineInstaller$OnSelectListener {
  *;
}

-keep interface com.rsupport.android.engine.install.installer.IEngineInstaller$OnCancelListener {
  *;
}

# rxjava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontwarn rx.internal.util.unsafe.**
-dontwarn java.lang.invoke**