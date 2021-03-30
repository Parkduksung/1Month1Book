package com.example.study_notification.macaddress.chormebook;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtil {
//    public static String getLocalIP() {
//        String localIP = "127.0.0.1";
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()) {
//                        if (inetAddress.getHostAddress().toString().contains(".") &&
//                                !inetAddress.getHostAddress().toString().contains(":")) {
//
//                            localIP = inetAddress.getHostAddress().toString();
//                            return localIP;
//                        }
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            Log.e("GlobalStatic", ex.getLocalizedMessage());
//        }
//        return localIP;
//    }
//
//
//    /**
//     * 이동 통신사 이름 얻어 오기
//     * @param activity
//     * @return String
//     */
//    public static String getOperatorName(AppCompatActivity activity){
//        String ret = "3";
//        TelephonyManager tm = (TelephonyManager) activity.getSystemService
//                (activity.TELEPHONY_SERVICE);
//        if (tm.getNetworkOperatorName().toLowerCase().equals("sktelecom"))
//            ret = "0";
//        else if (tm.getNetworkOperatorName().toLowerCase().equals("olleh"))
//            ret = "1";
//        else if (tm.getNetworkOperatorName().toLowerCase().equals("lg u+"))
//            ret = "2";
//
//        return ret;
//    }
//
//    /**
//     * 로밍 여부 확인
//     * @param activity
//     * @return boolean
//     */
//    public static String getRoamingState(AppCompatActivity activity){
//        TelephonyManager tm = (TelephonyManager) activity.getSystemService
//                (activity.TELEPHONY_SERVICE);
//        return tm.isNetworkRoaming() ?  "1" : "0";
//    }

//    /**
//     * MAC 주소 일치 체크용 메소드. 대/소문자 및 콜론 여부에 따라 단순 문자열 비교로는 판별이 되지 않음
//     * @param mac1
//     * @param mac2
//     * @return
//     */
//    public static boolean isEqualMac(String mac1, String mac2) {
//        if(!StringUtil.isEmpty(mac1) && !StringUtil.isEmpty(mac2)) {
//            mac1 = mac1.replaceAll("[^0-9A-Fa-f]", "");
//            mac2 = mac2.replaceAll("[^0-9A-Fa-f]", "");
//
//            RLog.d("MAC compare: " + mac1 + " / " + mac2);
//
//            return (mac1.compareToIgnoreCase(mac2) == 0);
//        }
//
//        return false;
//    }
//

    /**
     * MAC 주소 조회. Android M 부터 WifiManager 를 통한 MAC 주소 조회 불가.
     * 대체방법으로 Network interface 목록에서 wlan0 이름을 가진 장치의 MAC 을 조회하는것으로 처리되고 있었으나,
     * 가져오지 못하는 경우(단말)이 있어, API가 지원되지 않는 경우는 MAC 주소를 임의 생성한 값으로 대체하는것으로 처리할것.
     * 조회되지 않는 경우에 API에서의 반환값은 02:00:00:00:00:00 이다.
     * <p>
     * 본 메소드에서는 구분자를 제외하고 12자리로 반환한다.
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //WifiManager를 통한 MAC 조회 불가
            return getWifiMacAddress();
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) return "";

        String macAddress = "";

        try {

            macAddress = wifiManager.getConnectionInfo().getMacAddress();

            if (macAddress == null &&
                    !wifiManager.isWifiEnabled()) {

                wifiManager.setWifiEnabled(true);
                macAddress = wifiManager.getConnectionInfo().getMacAddress();
                wifiManager.setWifiEnabled(false);
            }

            if (macAddress != null) {
                macAddress = macAddress.toUpperCase();

                //구분자 제거하고 반환
                Pattern p = Pattern.compile("([0-9A-F]{2}).([0-9A-F]{2}).([0-9A-F]{2}).([0-9A-F]{2}).([0-9A-F]{2}).([0-9A-F]{2})");
                Matcher m = p.matcher(macAddress);
                if (m.matches()) {
                    macAddress = String.format("%s%s%s%s%s%s", m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6));
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return (macAddress == null) ? "" : macAddress;
    }

    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X", aMac));
                }
                return buf.toString();
            }
        } catch (Exception exp) {

            exp.printStackTrace();
        }

        return "";
    }

    public static String getMacAddress1(String interfaceName) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (TextUtils.equals(networkInterface.getName(), interfaceName)) {
                    byte[] bytes = networkInterface.getHardwareAddress();
                    StringBuilder builder = new StringBuilder();
                    for (byte b : bytes) {
                        builder.append(String.format("%02X:", b));
                    }

                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }

                    return builder.toString();
                }
            }

            return "<EMPTY>";
        } catch (SocketException e) {
            Log.e("결", "Get Mac Address Error", e);
            return "<ERROR>";
        }
    }

    public static String getMac() {
        String macSerial = "";
        String str = "";
        try {//from  w w w .  jav a2s  .  co m
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }

        } catch (IOException ex) {

            Log.d("결과", ex.toString());
            ex.printStackTrace();
        }
        return macSerial;
    }

}
