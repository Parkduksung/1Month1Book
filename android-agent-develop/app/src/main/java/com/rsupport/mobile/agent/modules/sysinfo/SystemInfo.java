package com.rsupport.mobile.agent.modules.sysinfo;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.text.format.Formatter;
import android.util.Log;
import android.view.WindowManager;

import com.rsupport.mobile.agent.BuildConfig;
import com.rsupport.mobile.agent.modules.sysinfo.phone.AccessPoint;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneAccount;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneBattery;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneDevice;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneMemory;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneNetwork;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneNumber;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneRooting;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneSettings;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneStateProvider;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneStorage;
import com.rsupport.mobile.agent.utils.DisplaySize;
import com.rsupport.mobile.agent.utils.WindowDisplay;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class SystemInfo {
    private static final String NO = "NO";
    private static final String YES = "YES";
    private static final String APPEND_MARK = "&&";
    private static final String INFO_TRUE = YES;
    private static final String INFO_FALSE = NO;
    private static final String INFO_ON = "ON";
    private static final String INFO_OFF = "OFF";
    private static final String INFO_EMPTY = "EMPTY";
    private static final String MEMORY_HIGH = "2";
    private static final String MEMORY_MID = "1";
    private static final String MEMORY_LOW = "0";
    private static final String ROOT_YES = "0";
    private static final String ROOT_NO = "1";
    private static final String WIFI_HIGH = "2";
    private static final String WIFI_MID = "1";
    private static final String WIFI_LOW = "0";
    private static final String MOBILE_HIGH = "2";
    private static final String MOBILE_MID = "1";
    private static final String MOBILE_LOW = "0";

    private final String INFO_USE = "USE";
    private final String INFO_UNUSE = "UNUSE";

    private static final int BASIC_INDEX_RESOLUTION = 13; //해상도
    private static final int BASIC_INDEX_AIR_MODE = 19;                     //비행 모드
    //	private static final int BASIC_INDEX_DEVICE_ID = 20;                      //디바이스 ID
    private static final int BASIC_INDEX_BLUETOOTH = 21;                               //블루투스
    private static final int BASIC_INDEX_LANGUAGE = 22;                      //언어
    private static final int BASIC_INDEX_ACCOUNT = 23;                       //계정
    private static final int BASIC_INDEX_BGD_DATA = 24;                      //백그라운드 데이터
    private static final int BASIC_INDEX_AUTO_ASNC = 25;                               //자동 동기화
    private static final int BASIC_INDEX_NO_SOUND = 26;                                //무음 모드
    //	private static final int BASIC_INDEX_LOCK_TELEPHONE = 27;              //전화 잠금 방식
    private static final int BASIC_INDEX_PASSWORD = 28;                      //비밀번호
    private static final int BASIC_INDEX_ROAMMING = 29;                               //로밍
    //	private static final int BASIC_INDEX_CELL_ROCATION = 30;                //셀위치
//	private static final int BASIC_INDEX_CELL_ID = 31;                          //셀ID
    private static final int BASIC_INDEX_RINGTON = 32;                        //링톤크기
    private static final int BASIC_INDEX_SPEAKER = 33;                         //스피커크기
    //	private static final int BASIC_INDEX_RAM = 34;                                        //RAM
//	private static final int BASIC_INDEX_USER_PROTOCOL = 35;               //사용자프로토콜
    private static final int BASIC_INDEX_WIFI_EXTEND = 36;                  //WiFi
    private static final int BASIC_INDEX_APK_VERSION = 37;
    private static final int BASIC_INDEX_DETAIL_MEMORY = 100;          //메모리 상세
    private static final int BASIC_INDEX_DETAIL_WIFI_LEFT = 101;              //왼쪽 WiFi 상세
    private static final int BASIC_INDEX_DETAIL_WIFI_RIGHT = 102;               //오른쪽 WiFi 상세
    private static final int BASIC_INDEX_DETAIL_BATTERY = 103;                  //베터리 상세

    private Context context;

    private static class MyMemoryInfo {
        final static int BASIC_INDEX_MEMORY_TOTAL = 0;                //Total
        final static int BASIC_INDEX_MEMORY_FREE = 1;                 //Free
        //public final static int BASIC_INDEX_MEMORY_IDLE = 2;                  //Idle
        final static int BASIC_INDEX_MEMORY_THRESHOLD = 3;         //Threshold
        final static int BASIC_INDEX_MEMORY_BUFFERS = 4;            //Buffers
        final static int BASIC_INDEX_MEMORY_CACHED = 5;             //Cached

    }

    private static class MyApInfo {
        final static int BASIC_INDEX_AP_BSSID = 0;                        //BSSID
        final static int BASIC_INDEX_AP_MACADDRESS = 1;              //MAC Address
        final static int BASIC_INDEX_AP_LINKSPEED = 2;                  //Link Speed
        final static int BASIC_INDEX_AP_SIGNAL_STRENGTH = 3;        //Signal Strenght
        final static int BASIC_INDEX_AP_DHCP_SERVER = 4;              //DHCP Server
        final static int BASIC_INDEX_AP_GATEWAY = 5;                    //GateWay
        final static int BASIC_INDEX_AP_IPADDRESS = 6;                  //IP Address
        final static int BASIC_INDEX_AP_NETMASK = 7;                   //Netmask
        final static int BASIC_INDEX_AP_DNS1 = 8;                        //DNS1
        final static int BASIC_INDEX_AP_DNS2 = 9;                        //DNS2
        final static int BASIC_INDEX_AP_LEASE_DURATION = 10;         //Lease Duration
    }

    private static class MyBatteryInfo {
        final static int BASIC_INDEX_BATERRY_LEVEL = 0; //Level
        final static int BASIC_INDEX_BATERRY_STATUS = 1; //Status
        final static int BASIC_INDEX_BATERRY_TECH = 2; //Technology
        final static int BASIC_INDEX_BATERRY_VOLTAGE = 3; //Voltage (mV)
        final static int BASIC_INDEX_BATERRY_TEMPERATURE = 4; //Temperature
        final static int BASIC_INDEX_BATERRY_PLUGGED = 5; //Plugged
    }


    private PhoneNumber phoneNumber;
    private PhoneNetwork phoneNetwork;
    private PhoneBattery phoneBattery;
    private PhoneRooting phoneRooting;
    private PhoneDevice phoneDevice;
    private PhoneStorage phoneStorage;
    private PhoneMemory phoneMemory;
    private PhoneSettings phoneSettings;
    private PhoneAccount phoneAccount;
    private DisplaySize displaySize;

    public SystemInfo(Context context) {
        this.context = context;
        this.displaySize = new WindowDisplay((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        this.phoneNumber = new PhoneNumber(context);
        this.phoneBattery = new PhoneBattery(context);
        this.phoneRooting = new PhoneRooting();
        this.phoneDevice = new PhoneDevice(context);
        this.phoneStorage = new PhoneStorage(context);
        this.phoneMemory = new PhoneMemory(context, new PhoneMemory.ProcessMemInfoFileFactory());
        this.phoneSettings = new PhoneSettings(context);
        this.phoneAccount = new PhoneAccount(context);
        this.phoneNetwork = new PhoneNetwork(context, phoneNumber, new PhoneStateProvider(context));
    }

    public void close() {
        phoneNetwork.close();
        phoneBattery.close();
    }

    @NotNull
    public String getSystemInfo() {
        try {
            return "PhoneNumber" + "&/" + getPhoneNumber() + "&&" +
                    "OperatorName" + "&/" + getOperatorName() + "&&" +
                    "RootingInfo" + "&/" + getRootState() + "&&" +
                    "ModelName" + "&/" + getModelName() + "&&" +
                    "FirmwareVersion" + "&/" + getFirmwareVersion() + "&&" +
                    "OSVersion" + "&/" + getOSVersion() + "&&" +
                    "BatteryStatus" + "&/" + getBatteryStatus() + "&&" +
                    "InternalStorageSize" + "&/" + getInternalStorageSize() + "&&" +
                    "ExternalStorageSize" + "&/" + getExternalStorageSize() + "&&" +
                    "MobileSpeed" + "&/" + getMobileSpeed() + "&&" +
                    "WifiSpeed" + "&/" + getWifiSpeed() + "&&" +
                    "MemoryState" + "&/" + getMemoryState() + "&&" +
                    "SIMSerialNumber" + "&/" + getSIMSerialNumber() + "&&" +
                    "BuildNumber" + "&/" + getBuildNumber() + "&&" +
                    "PhoneNumber" + "&/" + getPhoneNumber() + "&&" +
                    "Imei" + "&/" + getImei() + "&&" +
                    "LanguageSetState" + "&/" + getLanguageSetState() + "&&" +
                    "APKVersion" + "&/" + getAPKVersion();
        } catch (Exception e) {
            RLog.e(Log.getStackTraceString(e));
            return "";
        }
    }

    @NotNull
    public String getBasicInfo() {
        String basicInfo =
                "0" + "&/" + getPhoneNumber() + "&/" + "&&" +
                        "1" + "&/" + getOperatorName() + "&/" + "&&" +
                        "2" + "&/" + getRootState() + "&/" + getRootStateColor() + "&&" +
                        "3" + "&/" + getModelName() + "&/" + "&&" +
                        "4" + "&/" + getFirmwareVersion() + "&/" + "&&" +
                        "5" + "&/" + getOSVersion() + "&/" + "&&" +
                        "6" + "&/" + getBatteryStatus() + "&/" + "&&" +
                        "7" + "&/" + getInternalStorageSize() + "&/&/" + getInternalStoragePercent() + "&&" + //available"
                        "8" + "&/" + getExternalStorageSize() + "&/&/" + getExternalStoragePercent() + "&&" + //available"
                        "9" + "&/" + getMobileSpeed() + "&/" + getMobileSpeedColor() + "&&" +
                        "10" + "&/" + getWifiSpeed() + "&/" + getWifiSpeedColor() + "&&" +
                        "11" + "&/" + getMemoryState() + "&/" + getMemoryStateColor() + "&/" + getMemoryPercent() + "&&" + //available
                        "12" + "&/" + getSIMSerialNumber() + "&/" + "&&" +
//		   	"13" + "&/" + getScreenResolution() + "&/" + "&&" +
                        "14" + "&/" + getBuildNumber() + "&/" + "&&" +
                        "15" + "&/" + getPhoneNumber() + "&/" + "&&" +
                        "16" + "&/" + getImei() + "&/";

        basicInfo +=
                APPEND_MARK + getFormattedInfo(BASIC_INDEX_AIR_MODE, isPlaneMode()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_WIFI_EXTEND, getConnectedWifiSSID()) +
                        //APPEND_MARK + getFormattedInfo(BASIC_INDEX_DEVICE_ID, getImei()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_RESOLUTION, getResolution()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_BLUETOOTH, getBluetoothSetState() == 0 ? INFO_OFF : INFO_ON) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_LANGUAGE, getLanguageSetState()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_ACCOUNT, getAllAccounts()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_BGD_DATA, getBackgroundDataSetting()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_AUTO_ASNC, isAutoSyncMode()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_NO_SOUND, getRingerMode()) +
                        //APPEND_MARK + getFormattedInfo(BASIC_INDEX_LOCK_TELEPHONE, "Unknown") +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_PASSWORD, isShowingTextPassword()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_ROAMMING, getDataRoamSetting()) +
                        //APPEND_MARK + getFormattedInfo(BASIC_INDEX_CELL_ROCATION, "Unknown") +
                        //APPEND_MARK + getFormattedInfo(BASIC_INDEX_CELL_ID, "Unknown") +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_RINGTON, String.valueOf(getRingerVolume())) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_SPEAKER, String.valueOf(getMediaVolume())) +
                        //APPEND_MARK + getFormattedInfo(BASIC_INDEX_RAM, getMemoryInfo()) +
                        //APPEND_MARK + getFormattedInfo(BASIC_INDEX_USER_PROTOCOL, "Unknown");
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_APK_VERSION, getAPKVersion()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_DETAIL_WIFI_LEFT, getAllWifiList()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_DETAIL_WIFI_RIGHT, getApInfoString()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_DETAIL_MEMORY, getMemoryInfoString()) +
                        APPEND_MARK + getFormattedInfo(BASIC_INDEX_DETAIL_BATTERY, getBatteryInfoString());
        return basicInfo;
    }

    @NotNull
    public String getSettingInfo() {
        return "0" + "&/" + getStateString(getAirplaneSetState()) + "&/" + "&&" +
                "1" + "&/" + getStateString(getWifiSetState()) + "&/" + "&&" +
                "2" + "&/" + getStateString(getBluetoothSetState()) + "&/" + "&&" +
                "3" + "&/" + getLanguageSetState() + "&/" + "&&" +
                "5" + "&/" + getStateString(getSoundSetState()) + "&/";
    }

    public int getDisplayWidth() {
        return displaySize.getWidth();
    }

    public int getDisplayHeight() {
        return displaySize.getHeight();
    }

    public String getModelName() {
        return phoneDevice.getModelName();
    }

    public String getManufacturer() {
        return phoneDevice.getManufacturer();
    }

    public String getFirmwareVersion() {
        return phoneDevice.getFirmwareVersion();
    }

    public String getOSName() {
        return "android";
    }

    private String getBatteryInfoString() {
        PhoneBattery.BatteryInfo batteryInfo = phoneBattery.getBatteryInfo();
        return getFormattedInfoExt(MyBatteryInfo.BASIC_INDEX_BATERRY_LEVEL, batteryInfo.getLevel()) +
                getFormattedInfoExt(MyBatteryInfo.BASIC_INDEX_BATERRY_PLUGGED, batteryInfo.getPlug()) +
                getFormattedInfoExt(MyBatteryInfo.BASIC_INDEX_BATERRY_STATUS, batteryInfo.getStatus()) +
                getFormattedInfoExt(MyBatteryInfo.BASIC_INDEX_BATERRY_TECH, batteryInfo.getTech()) +
                getFormattedInfoExt(MyBatteryInfo.BASIC_INDEX_BATERRY_TEMPERATURE, batteryInfo.getTemperature()) +
                getFormattedInfoExt(MyBatteryInfo.BASIC_INDEX_BATERRY_VOLTAGE, batteryInfo.getVolt());
    }

    private String getMemoryInfoString() {
        PhoneMemory.MemoryInfo memoryInfo = phoneMemory.getMemoryInfo();
        if (memoryInfo.getEmpty()) return INFO_EMPTY;
        return getFormattedInfoExt(MyMemoryInfo.BASIC_INDEX_MEMORY_TOTAL, Formatter.formatFileSize(context, memoryInfo.getTotal())) +
                getFormattedInfoExt(MyMemoryInfo.BASIC_INDEX_MEMORY_FREE, Formatter.formatFileSize(context, memoryInfo.getAvailable())) +
                getFormattedInfoExt(MyMemoryInfo.BASIC_INDEX_MEMORY_BUFFERS, Formatter.formatFileSize(context, memoryInfo.getBuffers())) +
                getFormattedInfoExt(MyMemoryInfo.BASIC_INDEX_MEMORY_CACHED, Formatter.formatFileSize(context, memoryInfo.getCached())) +
                getFormattedInfoExt(MyMemoryInfo.BASIC_INDEX_MEMORY_THRESHOLD, Formatter.formatFileSize(context, memoryInfo.getThreshold()))
                ;
    }

    private String getApInfoString() {
        AccessPoint accessPoint = phoneNetwork.getAccessPointInfo();

        if (accessPoint.getEmpty()) {
            return INFO_EMPTY;
        }
        return getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_BSSID, accessPoint.getBssid()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_MACADDRESS, accessPoint.getMacAddress()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_LINKSPEED, accessPoint.getLinkSpeed()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_SIGNAL_STRENGTH, accessPoint.getSignalStrength()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_DHCP_SERVER, accessPoint.getServerAddress()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_GATEWAY, accessPoint.getGateway()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_IPADDRESS, accessPoint.getIpAddress()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_NETMASK, accessPoint.getNetmask()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_DNS1, accessPoint.getDns1()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_DNS2, accessPoint.getDns2()) +
                getFormattedInfoExt(MyApInfo.BASIC_INDEX_AP_LEASE_DURATION, accessPoint.getLeaseDuration());
    }

    private String getFormattedInfo(int infoType, String info) {
        return infoType + "&/" + (info == null ? INFO_EMPTY : info) + "&/";
    }

    private String getFormattedInfoExt(int infoType, String info) {
        return infoType + "||" + (info == null ? INFO_EMPTY : info) + "//";
    }

    @SuppressLint("MissingPermission")
    private String getPhoneNumber() {
        return phoneNumber.getPhoneNumber();
    }

    private String getOperatorName() {
        return phoneNetwork.getOperatorName();
    }

    private String getRootState() {
        if (phoneRooting.isRooting()) {
            return YES;
        } else {
            return NO;
        }
    }

    private String getRootStateColor() {
        if (getRootState().equals(YES)) {
            return ROOT_YES;
        } else {
            return ROOT_NO;
        }
    }

    private String getOSVersion() {
        return phoneDevice.getOSVersion();
    }

    private String getBatteryStatus() {
        return phoneBattery.getBatteryInfo().getLevel();
    }

    private String getInternalStorageSize() {
        return phoneStorage.getInternalStorageSize();
    }

    private String getExternalStorageSize() {
        return phoneStorage.getExternalStorageSize();
    }

    private int getInternalStoragePercent() {
        return phoneStorage.getInternalStoragePercent();
    }

    private int getExternalStoragePercent() {
        return phoneStorage.getExternalStoragePercent();
    }

    private String getWifiSpeed() {
        return phoneNetwork.getWifiSpeed();
    }

    private String getWifiSpeedColor() {
        if (getWifiSpeed().equals("")) {
            return WIFI_LOW;
        }
        int strength = Integer.parseInt(getWifiSpeed().replace("%", ""));
        String index;
        if (strength >= 80) {
            index = WIFI_HIGH;
        } else if (strength > 20) {
            index = WIFI_MID;
        } else {
            index = WIFI_LOW;
        }
        return index;
    }

    private String getMobileSpeed() {
        return phoneNetwork.getMobileSpeed();
    }

    private String getMobileSpeedColor() {
        String mSpeed = getMobileSpeed();
        if (mSpeed.equals(INFO_EMPTY)) {
            return MOBILE_LOW;
        }
        int strength = Integer.parseInt(mSpeed.replace("%", ""));
        String index;
        if (strength >= 80) {
            index = MOBILE_HIGH;
        } else if (strength > 20) {
            index = MOBILE_MID;
        } else {
            index = MOBILE_LOW;
        }
        return index;
    }


    private String getBuildNumber() {
        return phoneDevice.getBuildNumber();
    }

    @SuppressLint("MissingPermission")
    private String getImei() {
        return phoneDevice.getImei();
    }

    private int getAirplaneSetState() {
        return phoneSettings.getAirplaneSetState();
    }

    private int getWifiSetState() {
        return phoneSettings.getWifiSetState();
    }

    private int getBluetoothSetState() {
        return phoneSettings.getBluetoothSetState();
    }

    private String getLanguageSetState() {
        return phoneSettings.getLanguageSetState();
    }

    private int getSoundSetState() {
        return phoneSettings.getSoundSetState();
    }

    private String getStateString(int state) {
        if (state == 0) {
            return "OFF";
        } else {
            return "ON";
        }
    }

    @SuppressLint("MissingPermission")
    private String getSIMSerialNumber() {
        return phoneDevice.getSIMSerialNumber();
    }

    private String getMemoryState() {
        String[] memState = getMemInfo();
        return memState[1] + "(AVAILABLE)" + " / " + memState[0] + "(TOTAL)";
    }

    private String getMemoryStateColor() {
        PhoneMemory.MemoryInfo memoryInfo = phoneMemory.getMemoryInfo();
        if (memoryInfo.getEmpty()) return MEMORY_HIGH;

        long total = memoryInfo.getTotal();
        long free = memoryInfo.getAvailable();
        if (total / 10 * 8 <= free) {
            return MEMORY_HIGH;
        } else if (total / 10 * 8 > free && total / 10 * 2 < free) {
            return MEMORY_MID;
        } else {
            return MEMORY_LOW;
        }
    }

    private String[] getMemInfo() {
        PhoneMemory.MemoryInfo memoryInfo = phoneMemory.getMemoryInfo();
        if (memoryInfo.getEmpty()) return new String[]{INFO_EMPTY, INFO_EMPTY};
        return new String[]{changeFormatToMB(memoryInfo.getTotal()), changeFormatToMB(memoryInfo.getAvailable())};
    }

    private int getMemoryPercent() {
        try {
            PhoneMemory.MemoryInfo memoryInfo = phoneMemory.getMemoryInfo();
            if (memoryInfo.getEmpty()) throw new IllegalArgumentException("memoryInfo empty");
            return (int) memoryInfo.getPercent();
        } catch (Exception e) {
            RLog.e(e);
            return 0;
        }
    }

    private String changeFormatToMB(long mem) {
        return (mem / (1024 * 1024)) + " MB";
    }

    private String getConnectedWifiSSID() {
        return phoneNetwork.getConnectedWifiSSID();
    }

    private String getAllWifiList() {
        List<ScanResult> scanResult = phoneNetwork.getWifiScanResult();

        if (scanResult.isEmpty()) return INFO_EMPTY;

        StringBuilder sb = new StringBuilder();
        for (ScanResult sr : scanResult) {
            if (!sr.SSID.isEmpty()) {
                sb.append(sr.SSID).append("//");
            }
        }

        if (sb.length() == 0) return INFO_EMPTY;

        return sb.toString();
    }

    private String isPlaneMode() {
        return phoneSettings.getPlaneMode() > 0 ? INFO_TRUE : INFO_FALSE;
    }

    private String getResolution() {
        return displaySize.getWidth() + "(W)x" + displaySize.getHeight() + "(H)";
    }

    private String getAllAccounts() {
        Account[] accounts = phoneAccount.getAccount();
        if (accounts.length == 0) return INFO_EMPTY;
        return createAccountData(accounts);
    }

    @NotNull
    private String createAccountData(Account[] accounts) {
        StringBuilder ret = new StringBuilder();
        for (Account a : accounts) {
            if (a.name.contains("@")) {
                ret.append(a.name).append(", ");
            }
        }
        int length = ret.length();
        if (length > 2)
            ret.delete(length - 2, length);

        return ret.toString();
    }

    private String getBackgroundDataSetting() {
        // ICS 이후에는 항상 true 이다
        return INFO_TRUE;
    }

    private String isAutoSyncMode() {
        return phoneSettings.getAutoSyncMode() == 1 ? INFO_TRUE : INFO_FALSE;
    }

    private String getRingerMode() {
        return phoneSettings.getRingerMode();
    }

    private int getRingerVolume() {
        return phoneSettings.getRingerVolume();
    }

    private int getMediaVolume() {
        return phoneSettings.getMediaVolume();
    }

    private String isShowingTextPassword() {
        return phoneSettings.getShowingTextPassword() > 0 ? INFO_USE : INFO_UNUSE;
    }

    private String getDataRoamSetting() {
        return phoneSettings.getDataRoamSetting() > 0 ? INFO_USE : INFO_UNUSE;
    }

    private String getAPKVersion() {
        return BuildConfig.VERSION_NAME;
    }
}