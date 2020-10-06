package com.rsupport.mobile.agent.modules.function;

import java.util.Hashtable;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.modules.net.channel.DataChannelImpl;
import com.rsupport.mobile.agent.modules.net.protocol.MessageID;
import com.rsupport.mobile.agent.utils.Utility;
import com.rsupport.util.log.RLog;


public class ScreenNameSender implements Runnable {
    private final String className = "ScreenNameSender";

    private final int INTERVAL = 1500; // 1.5ms
    private ActivityManager activityManager = null;
    private PackageManager packageManager = null;
    private boolean isRunning = false;
    private Thread thread = null;
    private RunningTaskInfo oldTaskInfo = null;
    private String oldClassName = null;

    private Hashtable<String, String> screenNameTable = null;

    public DataChannelImpl mChannel;

    private final String[][] stringTableBasic = new String[][]{
            {"com.android.settings.Settings", String.valueOf(R.string.screen_name_settings_Settings)},
            {"com.android.settings.WirelessSettings", String.valueOf(R.string.screen_name_settings_WirelessSettings)},
            {"com.android.settings.wifi.WifiSettings", String.valueOf(R.string.screen_name_settings_wifi_WifiSettings)},
            {"com.android.settings.bluetooth.BluetoothSettings", String.valueOf(R.string.screen_name_settings_bluetooth_BluetoothSettings)},
            {"com.android.settings.TetherSettings", String.valueOf(R.string.screen_name_settings_TetherSettings)},
            {"com.android.settings.wifi.WifiApSettings", String.valueOf(R.string.screen_name_settings_wifi_WifiApSettings)},
            {"com.android.settings.vpn.VpnSettings", String.valueOf(R.string.screen_name_settings_vpn_VpnSettings)},
            {"com.android.settings.vpn.VpnTypeSelection", String.valueOf(R.string.screen_name_settings_vpn_VpnTypeSelection)},
            {"com.android.phone.Settings", String.valueOf(R.string.screen_name_phone_Settings)},
            {"com.android.settings.ApnSettings", String.valueOf(R.string.screen_name_settings_ApnSettings)},
            {"com.android.phone.NetworkSetting", String.valueOf(R.string.screen_name_phone_NetworkSetting)},
            {"com.android.phone.CallFeaturesSetting", String.valueOf(R.string.screen_name_phone_CallFeaturesSetting)},
            {"com.android.phone.FdnSetting", String.valueOf(R.string.screen_name_phone_FdnSetting)},
            {"com.android.phone.FdnList", String.valueOf(R.string.screen_name_phone_FdnList)},
            {"com.android.phone.GsmUmtsCallForwardOptions", String.valueOf(R.string.screen_name_phone_GsmUmtsCallForwardOptions)},
            {"com.android.phone.GsmUmtsAdditionalCallOptions", String.valueOf(R.string.screen_name_phone_GsmUmtsAdditionalCallOptions)},
            {"com.android.phone.sip.SipSettings", String.valueOf(R.string.screen_name_phone_sip_SipSettings)},
            {"com.android.phone.sip.SipEditor", String.valueOf(R.string.screen_name_phone_sip_SipEditor)},
            {"com.android.settings.SoundSettings", String.valueOf(R.string.screen_name_settings_SoundSettings)},
            {"com.android.settings.DisplaySettings", String.valueOf(R.string.screen_name_settings_DisplaySettings)},
            {"com.android.settings.SecuritySettings", String.valueOf(R.string.screen_name_settings_SecuritySettings)},
            {"com.android.settings.ChooseLockGeneric", String.valueOf(R.string.screen_name_settings_ChooseLockGeneric)},
            {"com.android.settings.ChooseLockPatternExample", String.valueOf(R.string.screen_name_settings_ChooseLockPatternExample)},
            {"com.android.settings.ChooseLockPattern", String.valueOf(R.string.screen_name_settings_ChooseLockPattern)},
            {"com.android.settings.ChooseLockPassword", String.valueOf(R.string.screen_name_settings_ChooseLockPassword)},
            {"com.android.settings.IccLockSettings", String.valueOf(R.string.screen_name_settings_IccLockSettings)},
            {"com.android.settings.DeviceAdminSettings", String.valueOf(R.string.screen_name_settings_DeviceAdminSettings)},
            {"com.android.settings.ApplicationSettings", String.valueOf(R.string.screen_name_settings_ApplicationSettings)},
            {"com.android.settings.ManageApplications", String.valueOf(R.string.screen_name_settings_ManageApplications)},
            {"com.android.settings.RunningServices", String.valueOf(R.string.screen_name_settings_RunningServices)},
            {"com.android.settings.applications.StorageUse", String.valueOf(R.string.screen_name_settings_applications_StorageUse)},
            {"com.android.settings.DevelopmentSettings", String.valueOf(R.string.screen_name_settings_DevelopmentSettings)},
            {"com.android.settings.ManageAccountsSettings", String.valueOf(R.string.screen_name_settings_ManageAccountsSettings)},
            {"com.android.settings.AddAccountSettings", String.valueOf(R.string.screen_name_settings_AddAccountSettings)},
            {"com.android.email.activity.setup.AccountSetupBasics", String.valueOf(R.string.screen_name_email_activity_setup_AccountSetupBasics)},
            {"com.android.email.activity.setup.AccountSetupExchange", String.valueOf(R.string.screen_name_email_activity_setup_AccountSetupExchange)},
            {"com.google.android.gsf.login.AccountIntroActivity", String.valueOf(R.string.screen_name_android_gsf_login_AccountIntroActivity)},
            {"com.android.settings.PrivacySettings", String.valueOf(R.string.screen_name_settings_PrivacySettings)},
            {"com.android.settings.MasterClear", String.valueOf(R.string.screen_name_settings_MasterClear)},
            {"com.android.settings.deviceinfo.Memory", String.valueOf(R.string.screen_name_settings_deviceinfo_Memory)},
            {"com.android.settings.MediaFormat", String.valueOf(R.string.screen_name_settings_MediaFormat)},
            {"com.android.settings.LanguageSettings", String.valueOf(R.string.screen_name_settings_LanguageSettings)},
            {"com.android.settings.LocalePicker", String.valueOf(R.string.screen_name_settings_LocalePicker)},
            {"com.android.settings.UserDictionarySettings", String.valueOf(R.string.screen_name_settings_UserDictionarySettings)},
            {"com.android.inputmethod.latin.LatinIMESettings", String.valueOf(R.string.screen_name_inputmethod_latin_LatinIMESettings)},
            {"com.google.android.inputmethod.korean.KoreanImeSettings", String.valueOf(R.string.screen_name_android_inputmethod_korean_KoreanImeSettings)},
            {"com.android.settings.VoiceInputOutputSettings", String.valueOf(R.string.screen_name_settings_VoiceInputOutputSettings)},
            {"com.google.android.voicesearch.VoiceSearchPreferences", String.valueOf(R.string.screen_name_android_voicesearch_VoiceSearchPreferences)},
            {"com.android.settings.TextToSpeechSettings", String.valueOf(R.string.screen_name_settings_TextToSpeechSettings)},
            {"com.svox.pico.EngineSettings", String.valueOf(R.string.screen_name_pico_EngineSettings)},
            {"com.android.settings.AccessibilitySettings", String.valueOf(R.string.screen_name_settings_AccessibilitySettings)},
            {"com.android.settings.DateTimeSettings", String.valueOf(R.string.screen_name_settings_DateTimeSettings)},
            {"com.android.settings.ZoneList", String.valueOf(R.string.screen_name_settings_ZoneList)},
            {"com.android.settings.DeviceInfoSettings", String.valueOf(R.string.screen_name_settings_DeviceInfoSettings)},
            {"com.google.android.gsf.update.SystemUpdateActivity", String.valueOf(R.string.screen_name_android_gsf_update_SystemUpdateActivity)},
            {"com.android.settings.deviceinfo.Status", String.valueOf(R.string.screen_name_settings_deviceinfo_Status)},
            {"com.android.settings.fuelgauge.PowerUsageSummary", String.valueOf(R.string.screen_name_settings_fuelgauge_PowerUsageSummary)},
            {"com.android.settings.SettingsLicenseActivity", String.valueOf(R.string.screen_name_settings_SettingsLicenseActivity)},
            {"com.google.android.gsf.settings.SettingsTosActivity", String.valueOf(R.string.screen_name_android_gsf_settings_SettingsTosActivity)}
    };

    private final String[][] stringTableICS = new String[][]{
            {"com.android.settings.Settings", String.valueOf(R.string.screen_name_settings_Settings)},
            {"com.android.settings.Settings$WirelessSettingsActivity", String.valueOf(R.string.screen_name_settings_WirelessSettings)},
            {"com.android.settings.Settings$WifiSettingsActivity", String.valueOf(R.string.screen_name_settings_wifi_WifiSettings)},
            {"com.android.settings.Settings$AdvancedWifiSettingsActivity", String.valueOf(R.string.screen_name_settings_wifi_WifiSettings_advance)},
            {"com.android.settings.Settings$BluetoothSettingsActivity", String.valueOf(R.string.screen_name_settings_bluetooth_BluetoothSettings)},
            {"com.android.settings.Settings$DataUsageSummaryActivity", String.valueOf(R.string.screen_name_settings_DataUsage)},
            {"com.android.settings.Settings$TetherSettingsActivity", String.valueOf(R.string.screen_name_settings_TetherSettings)},
            {"com.android.phone.Settings", String.valueOf(R.string.screen_name_phone_Settings)},
            {"com.android.settings.Settings$SoundSettingsActivity", String.valueOf(R.string.screen_name_settings_SoundSettings)},
            {"com.android.settings.Settings$DisplaySettingsActivity", String.valueOf(R.string.screen_name_settings_DisplaySettings)},
            {"com.android.settings.Settings$StorageSettingsActivity", String.valueOf(R.string.screen_name_settings_applications_StorageUse)},
            {"com.android.settings.Settings$PowerUsageSummaryActivity", String.valueOf(R.string.screen_name_settings_fuelgauge_PowerUsageSummary)},
            {"com.android.settings.Settings$ManageApplicationsActivity", String.valueOf(R.string.screen_name_settings_ApplicationSettings)},
            {"com.android.settings.Settings$ManageAccountsSettingsActivity", String.valueOf(R.string.screen_name_settings_ManageAccountsSettings)},
            {"com.android.settings.Settings$LocationSettingsActivity", String.valueOf(R.string.screen_name_settings_SecuritySettings)},
            {"com.android.settings.Settings$SecuritySettingsActivity", String.valueOf(R.string.screen_name_settings_SecuritySettings)},
            {"com.android.settings.Settings$InputMethodAndLanguageSettingsActivity", String.valueOf(R.string.screen_name_settings_LanguageSettings)},
            {"com.android.settings.Settings$PrivacySettingsActivity", String.valueOf(R.string.screen_name_settings_BackUp)},
            {"com.android.settings.Settings$DateTimeSettingsActivity", String.valueOf(R.string.screen_name_settings_DateTimeSettings)},
            {"com.android.settings.Settings$AccessibilitySettingsActivity", String.valueOf(R.string.screen_name_settings_AccessibilitySettings)},
            {"com.android.settings.Settings$DevelopmentSettingsActivity", String.valueOf(R.string.screen_name_settings_DevelopmentSettings)},
            {"com.android.settings.Settings$DeviceInfoSettingsActivity", String.valueOf(R.string.screen_name_settings_DeviceAdminSettings)},

            // old
            {"com.android.settings.wifi.WifiApSettings", String.valueOf(R.string.screen_name_settings_wifi_WifiApSettings)},
            {"com.android.settings.vpn.VpnSettings", String.valueOf(R.string.screen_name_settings_vpn_VpnSettings)},
            {"com.android.settings.vpn.VpnTypeSelection", String.valueOf(R.string.screen_name_settings_vpn_VpnTypeSelection)},
            {"com.android.settings.ApnSettings", String.valueOf(R.string.screen_name_settings_ApnSettings)},
            {"com.android.phone.NetworkSetting", String.valueOf(R.string.screen_name_phone_NetworkSetting)},
            {"com.android.phone.CallFeaturesSetting", String.valueOf(R.string.screen_name_phone_CallFeaturesSetting)},
            {"com.android.phone.FdnSetting", String.valueOf(R.string.screen_name_phone_FdnSetting)},
            {"com.android.phone.FdnList", String.valueOf(R.string.screen_name_phone_FdnList)},
            {"com.android.phone.GsmUmtsCallForwardOptions", String.valueOf(R.string.screen_name_phone_GsmUmtsCallForwardOptions)},
            {"com.android.phone.GsmUmtsAdditionalCallOptions", String.valueOf(R.string.screen_name_phone_GsmUmtsAdditionalCallOptions)},
            {"com.android.phone.sip.SipSettings", String.valueOf(R.string.screen_name_phone_sip_SipSettings)},
            {"com.android.phone.sip.SipEditor", String.valueOf(R.string.screen_name_phone_sip_SipEditor)},
            {"com.android.settings.ChooseLockGeneric", String.valueOf(R.string.screen_name_settings_ChooseLockGeneric)},
            {"com.android.settings.ChooseLockPatternExample", String.valueOf(R.string.screen_name_settings_ChooseLockPatternExample)},
            {"com.android.settings.ChooseLockPattern", String.valueOf(R.string.screen_name_settings_ChooseLockPattern)},
            {"com.android.settings.ChooseLockPassword", String.valueOf(R.string.screen_name_settings_ChooseLockPassword)},
            {"com.android.settings.IccLockSettings", String.valueOf(R.string.screen_name_settings_IccLockSettings)},
            {"com.android.settings.ManageApplications", String.valueOf(R.string.screen_name_settings_ManageApplications)},
            {"com.android.settings.RunningServices", String.valueOf(R.string.screen_name_settings_RunningServices)},
            {"com.android.settings.AddAccountSettings", String.valueOf(R.string.screen_name_settings_AddAccountSettings)},
            {"com.android.email.activity.setup.AccountSetupBasics", String.valueOf(R.string.screen_name_email_activity_setup_AccountSetupBasics)},
            {"com.android.email.activity.setup.AccountSetupExchange", String.valueOf(R.string.screen_name_email_activity_setup_AccountSetupExchange)},
            {"com.google.android.gsf.login.AccountIntroActivity", String.valueOf(R.string.screen_name_android_gsf_login_AccountIntroActivity)},
            {"com.android.settings.PrivacySettings", String.valueOf(R.string.screen_name_settings_PrivacySettings)},
            {"com.android.settings.MasterClear", String.valueOf(R.string.screen_name_settings_MasterClear)},
            {"com.android.settings.deviceinfo.Memory", String.valueOf(R.string.screen_name_settings_deviceinfo_Memory)},
            {"com.android.settings.MediaFormat", String.valueOf(R.string.screen_name_settings_MediaFormat)},
            {"com.android.settings.LocalePicker", String.valueOf(R.string.screen_name_settings_LocalePicker)},
            {"com.android.settings.UserDictionarySettings", String.valueOf(R.string.screen_name_settings_UserDictionarySettings)},
            {"com.android.inputmethod.latin.LatinIMESettings", String.valueOf(R.string.screen_name_inputmethod_latin_LatinIMESettings)},
            {"com.google.android.inputmethod.korean.KoreanImeSettings", String.valueOf(R.string.screen_name_android_inputmethod_korean_KoreanImeSettings)},
            {"com.android.settings.VoiceInputOutputSettings", String.valueOf(R.string.screen_name_settings_VoiceInputOutputSettings)},
            {"com.google.android.voicesearch.VoiceSearchPreferences", String.valueOf(R.string.screen_name_android_voicesearch_VoiceSearchPreferences)},
            {"com.android.settings.TextToSpeechSettings", String.valueOf(R.string.screen_name_settings_TextToSpeechSettings)},
            {"com.svox.pico.EngineSettings", String.valueOf(R.string.screen_name_pico_EngineSettings)},
            {"com.android.settings.ZoneList", String.valueOf(R.string.screen_name_settings_ZoneList)},
            {"com.android.settings.DeviceInfoSettings", String.valueOf(R.string.screen_name_settings_DeviceInfoSettings)},
            {"com.google.android.gsf.update.SystemUpdateActivity", String.valueOf(R.string.screen_name_android_gsf_update_SystemUpdateActivity)},
            {"com.android.settings.deviceinfo.Status", String.valueOf(R.string.screen_name_settings_deviceinfo_Status)},
            {"com.android.settings.SettingsLicenseActivity", String.valueOf(R.string.screen_name_settings_SettingsLicenseActivity)},
            {"com.google.android.gsf.settings.SettingsTosActivity", String.valueOf(R.string.screen_name_android_gsf_settings_SettingsTosActivity)}
    };

    public ScreenNameSender(Context context, DataChannelImpl channel) {
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = (PackageManager) context.getPackageManager();
        screenNameTable = new Hashtable<String, String>();
        setTable();
        mChannel = channel;
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                sendTopRunningTask();
                Thread.sleep(INTERVAL);
            }
        } catch (InterruptedException ie) {
            isRunning = false;
        }
    }

    public void stop() {
        RLog.i("thread : " + thread);
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void destroy() {
        stop();
        if (screenNameTable != null) {
            screenNameTable.clear();
            screenNameTable = null;
        }
    }


    public void start() {
        RLog.i("thread : " + thread);
        isRunning = true;
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private void sendScreenName(String message) {
        RLog.i("message : " + message);
        if (message != null && isRunning == true) {
            try {
                byte[] tempByteArray = message.getBytes("UTF-16LE");
                byte[] sendByteArray = new byte[tempByteArray.length + 2];
                System.arraycopy(tempByteArray, 0, sendByteArray, 0, tempByteArray.length);
                mChannel.sendPacket(MessageID.rcpMobile, MessageID.rcpMobileActivityApp, sendByteArray, sendByteArray.length);
            } catch (Exception e) {
                RLog.e(e.toString());
            }
        }
    }


    private void sendTopRunningTask() {
        if (activityManager != null) {
            RunningTaskInfo currentTaskInfo = activityManager.getRunningTasks(1).get(0);
            String currentPackag = getPackageName(currentTaskInfo);
            String currentClass = getClassName(currentTaskInfo);

            // different package name
            if (currentPackag != null && currentPackag.equals(getPackageName(oldTaskInfo)) == false) {
                if (screenNameTable != null && screenNameTable.containsKey(currentClass) == true) {
                    // send com.rsupport.setting
                    sendScreenName(getScreenName(screenNameTable.get(currentClass)));
                    oldClassName = currentClass;
                } else {
                    // send application name
                    try {
                        String packageName = currentTaskInfo.topActivity.getPackageName();
                        sendScreenName(getApplicationLabel(packageManager, packageName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                oldTaskInfo = currentTaskInfo;
            } else {
                if (screenNameTable != null && screenNameTable.containsKey(currentClass) == true && currentClass.equals(oldClassName) == false) {
                    // send com.rsupport.setting
                    sendScreenName(getScreenName(screenNameTable.get(currentClass)));
                    oldTaskInfo = currentTaskInfo;
                    oldClassName = currentClass;
                }
            }
        }
    }


    private String getApplicationLabel(PackageManager packageManager, String packageName) throws NameNotFoundException {
        ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        String label = packageManager.getApplicationLabel(info).toString();
        return label;
    }

    private String getPackageName(RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo != null) {
            return runningTaskInfo.topActivity.getPackageName();
        }
        return null;
    }

    private String getClassName(RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo != null) {
            return runningTaskInfo.topActivity.getClassName();
        }
        return null;
    }

    private String[][] mStringTable;

    private String[][] getStringTable() {
        String[][] ret = null;
        if (Utility.isICS()) ret = stringTableICS;
        else ret = stringTableBasic;
        return ret;
    }

    private void setTable() {
        if (screenNameTable != null) {
            screenNameTable.clear();
        }

        if (mStringTable == null) {
            mStringTable = getStringTable();
        }

        for (int i = 0; i < mStringTable.length; i++) {
            String key = mStringTable[i][0];
            String value = mStringTable[i][1];
            if (screenNameTable != null) {
                screenNameTable.put(key, value);
            }
        }
    }

    private String getScreenName(String key) {
        String message = null;
        try {
            int value = Integer.parseInt(key);
            message = Utility.getString(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}