package com.rsupport.mobile.agent.modules.sysinfo;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory;
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo;
import com.rsupport.mobile.agent.modules.sysinfo.appinfo.AppInfoCache;
import com.rsupport.mobile.agent.modules.sysinfo.appinfo.AppInfoHolder;
import com.rsupport.mobile.agent.modules.sysinfo.appinfo.ResourceUpdater;
import com.rsupport.mobile.agent.utils.Utility;
import com.rsupport.mobile.agent.utils.packet.PacketGenerator;
import com.rsupport.mobile.agent.utils.packet.PrefixLengthPacket;
import com.rsupport.mobile.agent.utils.packet.TypedByteArrayPacket;
import com.rsupport.mobile.agent.utils.packet.TypedByteArraySourcePacket;
import com.rsupport.mobile.agent.utils.packet.Utf16LEPacket;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.koin.java.KoinJavaComponent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Lazy;


public class ApplicationInfo {
    private static final String PREF_KEY_FILTER_APP_TYPE = "filter_app_type"; //$NON-NLS-1$
    public static final int APP_TYPE_SYS = 0;
    public static final int APP_TYPE_USER = 1;
    public static final int APP_TYPE_ALL = 2;

    private AppInfoCache appInfoCache;
    private Context mContext;
    private Lazy<RunningAppFactory> runningAppFactoryLazy = KoinJavaComponent.inject(RunningAppFactory.class);

    private ApplicationInfoHelper applicationInfoHelper;

    private PacketGenerator<String> utf16LEPacket = new Utf16LEPacket();
    private PacketGenerator<byte[]> prefixLengthPacket = new PrefixLengthPacket();
    private PacketGenerator<TypedByteArraySourcePacket> typedByteArrayPacket = new TypedByteArrayPacket();
    private ResourceUpdater resourceUpdater;

    public ApplicationInfo(Context context, ApplicationInfoHelper applicationInfoHelper, AppInfoCache appInfoCache) {
        this.mContext = context;
        this.applicationInfoHelper = applicationInfoHelper;
        this.appInfoCache = appInfoCache;
        this.resourceUpdater = new ResourceUpdater(context, appInfoCache);
    }

    public void close() {
        resourceUpdater.close();
        appInfoCache.close();
    }

    public boolean isAppRunning(String appName) {
        List<RunningAppInfo> runningAppInfos = runningAppFactoryLazy.getValue().create().getRunningAppInfos();
        if (isRunningAppNotFound(runningAppInfos)) return true;
        return isRunningAppByLabel(appName, runningAppInfos);
    }

    public byte[] getAppDetailInfoPacket(String appItem) {
        byte[] appDetailInfoBytes = getAppDetailInfoBytes(appItem);
        if (appDetailInfoBytes == null) return null;
        return prefixLengthPacket.create(appDetailInfoBytes);
    }

    public Uri getPackageUriFrom(String targetName) {
        if (targetName == null) return null;

        AppInfoHolder foundAppInfoHolder = findAppInfoHolder(targetName);
        if (foundAppInfoHolder == null) return null;

        return createUriFromAppInfoHolder(foundAppInfoHolder);
    }

    public boolean runApp(String targetName) {
        List<AppInfoHolder> appInfoHolders = appInfoCache.getAppInfoHolders();
        for (AppInfoHolder appInfoHolder : appInfoHolders) {
            String packageName = getIdentifierFromAppInfoHolder(appInfoHolder);
            if (!targetName.equals(packageName)) {
                continue;
            }

            if (launchIfBrowerPackage(appInfoHolder, packageName)) return true;

            Intent runIntent = applicationInfoHelper.getLaunchIntent(appInfoHolder);
            if (checkIntent(runIntent)) return false;

            try {
                applicationInfoHelper.startActivity(runIntent);
            } catch (ActivityNotFoundException e) {
                return launchIfContract(appInfoHolder, packageName);
            }
            return true;
        }
        return false;
    }

    /**
     * 설치된 앱정보를 반환한다.
     * {@link AppInfoCache}, {@link ResourceUpdater}, {@link AppInfoHolder}을 이용해서 데이터를 만들고,
     * {@link TypedByteArrayPacket} 을 이용해서 ByteArray 로 변환한다.
     *
     * @param appType {@link ApplicationInfo#APP_TYPE_SYS}, {@link ApplicationInfo#APP_TYPE_USER}, {@link ApplicationInfo#APP_TYPE_ALL},
     * @return 앱정보데이터
     */
    public byte[][] loadApps(final int appType) {
        appInfoCache.clear();

        List<android.content.pm.ApplicationInfo> allApps = applicationInfoHelper.getInstalledApplications();

        final List<android.content.pm.ApplicationInfo> filteredApps = filterApps(allApps, appType);

        for (int i = 0, size = filteredApps.size(); i < size; i++) {
            android.content.pm.ApplicationInfo info = filteredApps.get(i);
            AppInfoHolder holder = createAppInfoHolder(info);
            appInfoCache.getAppInfoHolders().add(holder);
        }

        appInfoCache.update();
        resourceUpdater.update();
        return getApplicationInfoPacket(appType);
    }

    @NotNull
    private AppInfoHolder createAppInfoHolder(android.content.pm.ApplicationInfo info) {
        AppInfoHolder holder = new AppInfoHolder();
        holder.appInfo = info;
        holder.enable = info.enabled;
        holder.removable = isRemovable(info);
        try {
            PackageInfo pi = applicationInfoHelper.getPackageInfo(info);
            if (pi == null) throw new NameNotFoundException("not found : " + info.packageName);

            holder.version = pi.versionName == null ? String.valueOf(pi.versionCode) : pi.versionName;
            holder.versionCode = pi.versionCode;
            if (info.sourceDir != null && info.sourceDir.contains("/data/app-private")) {
                holder.isPrivate = true;
            }
        } catch (NameNotFoundException e) {
            RLog.e(e.getLocalizedMessage());
        }
        return holder;
    }

    private byte[][] getApplicationInfoPacket(int appType) {
        boolean isAddImage = applicationInfoHelper.isWifiConnected();
        List<AppInfoHolder> appInfoHolders = appInfoCache.getAppInfoHolders();

        byte[][] appItemsBytes = new byte[appInfoHolders.size()][];

        try {
            List<RunningAppInfo> raps = runningAppFactoryLazy.getValue().create().getRunningAppInfos();
            for (int i = 0, n = appInfoHolders.size(); i < n; i++) {
                AppInfoHolder appInfo = appInfoHolders.get(i);

                appItemsBytes[i] = createAppInfoPacket(
                        0, // 항상 0 설정한다.
                        utf16LEPacket.create(appInfo.createItemString(appType, raps)),
                        createCompressedIconBytes(isAddImage, appInfo)
                );
            }
        } catch (Exception e) {
            RLog.e(e);
        }
        return appItemsBytes;
    }

    private byte[] createCompressedIconBytes(boolean isAddImage, AppInfoHolder appInfo) {
        if (!isAddImage) return null;

        byte[] bmpBytes = null;
        if (appInfo.icon != null && appInfo.icon.getClass().equals(BitmapDrawable.class)) {
            ByteArrayOutputStream oStream = new ByteArrayOutputStream();
            try {
                BitmapDrawable bd = (BitmapDrawable) appInfo.icon;
                bd.getBitmap().compress(CompressFormat.PNG, 100, oStream);
                bmpBytes = oStream.toByteArray();
            } finally {
                try {
                    oStream.close();
                } catch (IOException e) {
                    RLog.e(e);
                }
            }

        }
        return bmpBytes;
    }

    private byte[] createAppInfoPacket(int type, byte[] textBytes, byte[] bmpBytes) {
        return typedByteArrayPacket.create(new TypedByteArraySourcePacket(type, textBytes, bmpBytes));
    }

    private boolean isRunningAppByLabel(String appName, List<RunningAppInfo> runningAppInfos) {
        for (RunningAppInfo runningAppInfo : runningAppInfos) {
            if (appName.equals(applicationInfoHelper.getLabelName(runningAppInfo.getPkgName()))) {
                RLog.i("Running Success");
                return true;
            }
        }
        return false;
    }

    private boolean isRunningAppNotFound(List<RunningAppInfo> runningAppInfos) {
        if (runningAppInfos.size() <= 1) {
            RLog.i("Do not getRunningProcess List");
            return true;
        }
        return false;
    }

    private String getAppDetailInfo(@NotNull String appItem) throws NameNotFoundException {
        android.content.pm.ApplicationInfo appInfo = applicationInfoHelper.getApplicationInfo(appItem);

        String properties = "ClassName&/" + (appInfo.className == null ? "" : appInfo.className) + "&&";
        properties += "DataDir&/" + (appInfo.dataDir == null ? "" : appInfo.dataDir) + "&&";
        properties += "Name&/" + (appInfo.name == null ? "" : appInfo.name) + "&&";
        properties += "PackageName&/" + (appInfo.packageName == null ? "" : appInfo.packageName) + "&&";
        properties += "Permission&/" + (appInfo.permission == null ? "" : appInfo.permission) + "&&";
        properties += "SourceDir&/" + (appInfo.sourceDir == null ? "" : appInfo.sourceDir) + "&&";
        properties += "TargetSDKVersion&/" + appInfo.targetSdkVersion + "&&";
        properties += "UID&/" + appInfo.uid + "&&";
        return properties;
    }


    private byte[] getAppDetailInfoBytes(String appItem) {
        try {
            if (appItem == null) throw new IllegalArgumentException("must not null appItem");

            final String properties = getAppDetailInfo(appItem);

            return utf16LEPacket.create(properties);
        } catch (Exception e) {
            RLog.e(e.getLocalizedMessage());
        }
        return null;
    }

    private boolean isRemovable(android.content.pm.ApplicationInfo appInfo) {
        boolean isUpdatedSysApp = (appInfo.flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        boolean enabled = true;
        if (isUpdatedSysApp) {
            //none
        } else if ((appInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            enabled = false;
        }
        return enabled;
    }

    private List<android.content.pm.ApplicationInfo> filterApps(List<android.content.pm.ApplicationInfo> apps, int appType) {
        if (apps == null || apps.size() == 0) {
            return Collections.emptyList();
        }
        int type = getIntOption(mContext, PREF_KEY_FILTER_APP_TYPE, appType);
        if (type == APP_TYPE_SYS) {
            List<android.content.pm.ApplicationInfo> sysApps = new ArrayList<>();
            for (int i = 0, size = apps.size(); i < size; i++) {
                android.content.pm.ApplicationInfo ai = apps.get(i);
                if ((ai.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
                    sysApps.add(ai);
                }
            }
            return sysApps;
        } else if (type == APP_TYPE_USER) {
            List<android.content.pm.ApplicationInfo> userApps = new ArrayList<android.content.pm.ApplicationInfo>();
            for (int i = 0, size = apps.size(); i < size; i++) {
                android.content.pm.ApplicationInfo ai = apps.get(i);
                if ((ai.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                    userApps.add(ai);
                }
            }
            return userApps;
        }
        return apps;
    }

    private int getIntOption(Context ac, String key, int defValue) {
        return ac.getSharedPreferences(getLocalClassName(ac), Context.MODE_PRIVATE).getInt(key, defValue);
    }

    private List<AppInfoHolder> getAppInfoHolder() {
        return appInfoCache.getAppInfoHolders();
    }

    private Uri createUriFromAppInfoHolder(AppInfoHolder foundAppInfoHolder) {
        return Uri.parse("package:" + foundAppInfoHolder.appInfo.packageName);
    }

    @Nullable
    private AppInfoHolder findAppInfoHolder(String targetName) {
        AppInfoHolder foundAppInfoHolder = null;
        for (AppInfoHolder appInfoHolder : getAppInfoHolder()) {
            final String packageName = getIdentifierFromAppInfoHolder(appInfoHolder);
            if (isMatchAppInfo(targetName, appInfoHolder, packageName)) {
                foundAppInfoHolder = appInfoHolder;
            }
        }
        return foundAppInfoHolder;
    }

    private boolean isMatchAppInfo(String targetName, AppInfoHolder appInfoHolder, String packageName) {
        if (targetName.equals(packageName)) {
            return true;
        } else return targetName.equals(appInfoHolder.appInfo.packageName);
    }

    private String getIdentifierFromAppInfoHolder(AppInfoHolder appInfoHolder) {
        String packageName;
        if (appInfoHolder.label != null) {
            packageName = appInfoHolder.label.toString();
        } else {
            packageName = appInfoHolder.appInfo.packageName;
        }
        return packageName;
    }

    private boolean launchIfContract(AppInfoHolder appInfoHolder, String packageName) {
        if (appInfoHolder.appInfo.packageName.contains("contact")) {
            return applicationInfoHelper.startContactActivity(packageName);
        }
        return false;
    }

    private boolean launchIfBrowerPackage(AppInfoHolder appInfoHolder, String packageName) {
        if (appInfoHolder.appInfo.packageName.contains("browser")) {
            Utility.callBrowser(mContext, "www.google.com");
            return true;
        }
        return false;
    }

    private boolean checkIntent(Intent runIntent) {
        return runIntent == null;
    }

    private String getLocalClassName(Context context) {
        final String pkg = context.getPackageName();
        final String cls = context.getClass().getName();
        int packageLen = pkg.length();
        if (!cls.startsWith(pkg) || cls.length() <= packageLen
                || cls.charAt(packageLen) != '.') {
            return cls;
        }
        return cls.substring(packageLen + 1);
    }
}