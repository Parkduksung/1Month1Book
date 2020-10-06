package com.rsupport.mobile.agent.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.rsupport.mobile.agent.R;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import config.EngineConfigSetting;

import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;

public class Utility {

    public static Context mainContext = null;
    private static final String TAG = "Utility";

    // check valid url
    public static boolean isValidUrl(String url) {// throws Exception
        String regex = "https?://[-\\w.]+(:\\d+)?(/([\\w/_.]*)?)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }


    public static boolean isICS() {
        boolean ret = false;
        if (Build.VERSION.RELEASE.substring(0, 1).equals("4")) {
            ret = true;
        }
        return ret;
    }

    public static void showBigFontMessage(String message) {
        if (mainContext != null) {
            LayoutInflater inflater = ((Activity) mainContext)
                    .getLayoutInflater();
            View layout = inflater.inflate(R.layout.notification,
                    (ViewGroup) ((Activity) mainContext)
                            .findViewById(R.id.toast_layout_root));

            TextView text = (TextView) layout.findViewById(R.id.message);

            Toast toast = new Toast(mainContext);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setView(layout);
            toast.setDuration(Toast.LENGTH_LONG);

            text.setText("\n" + message + "\n");
            toast.show();
        }
    }

    public static boolean isWifiReady() {
        return isWifiReady(mainContext);
    }

    public static boolean isWifiReady(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        /*
         * way 1 DetailedState ni_ds =
         * WifiInfo.getDetailedStateOf(wi.getSupplicantState()); if
         * ((wi.getIpAddress() != 0) && (ni_ds == DetailedState.CONNECTED ||
         * ni_ds == DetailedState.OBTAINING_IPADDR)) { return true; }
         */

        if (wifiManager.isWifiEnabled() == true && wifiInfo.getSSID() != null) {
            RLog.d("SSID:" + wifiInfo.getSSID());
            return true;
        }
        return false;
    }

    public static boolean isMobileConnected(Context context) {
        if (mainContext != null) {
            try {
                ConnectivityManager manager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                return manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                        .isConnected();
            } catch (Exception e) {
                RLog.e(e.getMessage());
                return false;
            }
        }
        return false;
    }

    public static boolean isOnline(Context contex) {
        if (!Utility.isMobileConnected(contex) && !Utility.isWifiReady(contex)
                && !Utility.isEtherNet(contex) && !Utility.isNetworkAvailable(contex)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isEtherNet(Context context) {
        boolean ret;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
            ret = true;
        else
            ret = false;
        return ret;
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean value = false;
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            value = true;
        }
        return value;
    }

    public static Resources getResources() {
        if (mainContext != null) {
            return mainContext.getResources();
        } else {
            return null;
        }
    }

    public static String getString(int id) {
        return getResources().getString(id);
    }

    public static String extractNumber(String text) {
        return text.replaceAll("[^\\d]", ""); // same regex : [^0-9]
    }

    public static String getEncodeString(String target) {
        return Base64.encodeBytes(target.getBytes(EngineConfigSetting.UTF_8));
    }

    public static String getDecodeString(String target) {
        try {
            return new String(Base64.decode(target), EngineConfigSetting.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getLocale() {
        if (mainContext == null)
            return null;

        Locale locale = mainContext.getResources().getConfiguration().locale;
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    public static String getLanguage() {
        if (mainContext == null)
            return null;

        Locale locale = mainContext.getResources().getConfiguration().locale;
        return locale.getLanguage();
    }

    public static void callBrowser(Context context, String url) {
        RLog.i("callBrowser");
        if (url == null)
            return;
        if (!url.contains("http://") || !url.contains("https://")) {
            url = "http://" + url;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            RLog.e(e);
        }
    }

    public static boolean isKoreaNetOper(Context context) {
        String netOper = "";
        TelephonyManager tm;
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) return false;
        netOper = tm.getNetworkOperator();
        if (netOper != null && netOper.length() > 0) {
            if (netOper.substring(0, 3).equals("450")) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public static String getImei(Activity activity) {
        String deviceID = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        if (deviceID != null && deviceID.length() > 0 && !deviceID.equals("0")) {
            return deviceID;
        } else {
            return "EMPTY";
        }
    }


    public static String getKeyboardEx(Context context) {
        String ret = "";
        String runningInpout = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);

        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> listInput = inputManager
                .getEnabledInputMethodList();

        for (InputMethodInfo input : listInput) {
            ServiceInfo serviceInfo = input.getServiceInfo();
            if (runningInpout.equals(input.getId())) {
                PackageManager pm = context.getPackageManager();
                Intent intent = new Intent(InputMethod.SERVICE_INTERFACE);
                @SuppressLint("WrongConstant") List<ResolveInfo> services = pm.queryIntentServices(intent, PackageManager.GET_SERVICES);
                for (ResolveInfo resolveInfo : services) {
                    if (resolveInfo.serviceInfo.packageName.equals(input
                            .getPackageName())) {
                        ret = resolveInfo
                                .loadLabel(context.getPackageManager())
                                .toString();
                        return ret;
                    }
                }
            }
        }
        return ret;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + ((i >> 24) & 0xFF);
    }

    private static boolean isAvailIP(String ip) {
        boolean ret = false;
        if (ip.contains(":"))
            return ret;
        if (ip.equals("127.0.0.1"))
            return ret;
        return true;
    }

    public static String getInetAddress() {
        String ret = "";
        Enumeration<NetworkInterface> interface_list;
        try {
            interface_list = java.net.NetworkInterface.getNetworkInterfaces();
            while (interface_list.hasMoreElements()) {
                NetworkInterface iface = interface_list.nextElement();
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                for (; addrs.hasMoreElements(); ) {
                    InetAddress addr = addrs.nextElement();
                    String ipaddr = addr.toString().substring(1);
                    ret = ipaddr;
                    if (isAvailIP(ipaddr))
                        return ret;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getIPAddress(Context context) {
        String ret = "";
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        DhcpInfo di = wifiManager.getDhcpInfo();
        if (di != null && di.ipAddress > 0) {
            ret = intToIp(di.ipAddress);
        } else {
            ret = getInetAddress();
        }
        return ret;
    }


    public static boolean isScreenState() {
        PowerManager pm = (PowerManager) mainContext
                .getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;

        if (Build.VERSION.SDK_INT >= 20) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }

        return isScreenOn;
    }

    public static String getCallPackageName() {
        return mainContext.getPackageName();
    }

    private static ResolveInfo getHomeLauncher() {
        Intent homeIntent = new Intent();
        homeIntent.setAction(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = mainContext.getPackageManager();

        List<ResolveInfo> list = pm.queryIntentActivities(homeIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        RLog.i("ResolveInfo List : " + list);
        if (list.size() == 1) {
            return list.get(0);
        } else {
            ResolveInfo resolveInfo = pm.resolveActivity(homeIntent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (list.get(i).activityInfo.packageName
                        .equals(resolveInfo.activityInfo.packageName) == true) {
                    return list.get(i);
                }
            }
            return list.get(0);
        }
    }

    public static String getHomePackageName() {
        return getHomeLauncher().activityInfo.packageName;
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pkgInfo.versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return "";
    }


    /**
     * 사진 파일의 카메라가 가진 회전각도를 얻음
     **/
    public static int getOrientationFromImageUri(Uri uri) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(uri.getPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

    public static boolean checkAcccountValidate(String id) {

        Pattern p = Pattern.compile("^[a-zA-Z0-9]{4,24}$");
        Matcher m = p.matcher(id);
        return m.matches();
    }

    public static boolean invalidWordCheck(String str) {

        char[] err_char = new char[]{'\\', '/', ':', '*', '?', '\"', '<', '>', '|', '%', '+', ';'};
        for (int i = 0; i < str.length(); i++) {
            for (int j = 0; j < err_char.length; j++) {
                if (str.charAt(i) == err_char[j]) {
                    return false;
                }
            }

        }
        return true;
    }

    public static boolean isLandscape(Point displaySize, Display display) {
        int rotation = display.getRotation();
        RLog.i("rotation : " + rotation + " X::::: " + displaySize.x + " Y ::::" + displaySize.y);
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (displaySize.x > displaySize.y) { // Rotation이 0, 180 인데 실제 가로가 더 넓은 경우
                    return true;
                }
                break;

            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (displaySize.x < displaySize.y) { // Rotation이 90, 270 인데 가로디폴트 단말
                    return true;
                }

                break;

        }
        return false;
    }

    public static String getSystemLanguage(Context context) {
        String ret = "en";
        String locale = context.getResources().getConfiguration().locale.toString();

        if (locale.contains("ko")) {
            ret = "ko";
        } else if (locale.contains("en")) {
            ret = "en";
        } else if (locale.contains("ja")) {
            ret = "ja";
        } else if (locale.equals("zh") || locale.contains("zh_CN")) {
            ret = "zh-cn";
        } else if (locale.contains("zh_TW")) {
            ret = "zh-tw";
        }

        return ret;
    }

    public static void releaseAlarm(Context context) {
//        RLog.i("releaseAlarm()");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent Intent = new Intent(GlobalStatic.CONNETING_MESSAGE_ID);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, Intent, 0);
        alarmManager.cancel(pIntent);

    }

    /**
     * 삼성 프린터 / HCI STB 소프트웨어 인코딩
     *
     * @return
     */
    public static boolean isSoftIncoding() {
        return (GlobalStatic.IS_SAMSUNGPRINTER_BUILD || GlobalStatic.IS_HCI_BUILD);

    }

    public static boolean isExistRsperm(Context context) {
        try {
            List<ApplicationInfo> appInfos = context.getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (int i = 0; i < appInfos.size(); i++) {
                String pkgName = appInfos.get(i).packageName;
                if (pkgName.contains("com.rsupport.rsperm") && !isExcludeEnginePkgName(pkgName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            RLog.e("getApplicationList fail : " + Log.getStackTraceString(e));
        }

        return false;
    }

    public static boolean isExcludeEnginePkgName(String pkgName) {
        final String SKT_ENGINE_PACKAGE = "com.rsupport.engine.input2";
        final String NTT_ENGINE_PACKAGE = "com.rsupport.rsperm.ntt";
        ArrayList<String> excludeEngines = new ArrayList();
        excludeEngines.add(SKT_ENGINE_PACKAGE);
        excludeEngines.add(NTT_ENGINE_PACKAGE);
        return excludeEngines.contains(pkgName);
    }

    public static boolean isSamsungPreint3Th() {
        if (Build.MODEL.equals("sec_smdkc210")) {
            return true;
        }
        return false;
    }
}
