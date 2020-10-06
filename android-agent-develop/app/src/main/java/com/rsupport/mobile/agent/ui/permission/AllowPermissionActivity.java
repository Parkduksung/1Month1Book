package com.rsupport.mobile.agent.ui.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rsupport.mobile.agent.R;

import java.util.ArrayList;
import java.util.List;

import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoActivity;
import com.rsupport.mobile.agent.constant.PreferenceConstant;
import com.rsupport.util.log.RLog;

/**
 * 1 다른앱 위에 그리기
 * 2 퍼미션
 * 3 배터리 절전 해제 팝업
 */

public class AllowPermissionActivity extends RVCommonActivity {

    private static final int EVENT_ID_NO = 0;
    private static final int EVENT_ID_SYSTEM_ALERT = 100;
    private static final int EVENT_ID_CALL_SETTING_DETAIL = 200;
    private static final int EVENT_ID_SYSTEM_SETTING = 300;

    private static final int REQUEST_SYSTEM_ALERT_CODE = 10000;
    private static final int REQUEST_BATTERY_CODE = 20000;
    private static final int REQUEST_SYSTEM_SETTING = 30000;
    private static final int PERMISSIONCODE = 10001;
    private static final String IS_FIRST_KEY = "agenet_allow_permission_first";
    private final String KEY_REQUEST_BATTERY = "request_battery";

    private boolean isSystemAlertPermission = false;
    private boolean isSystemSettingPermission = false;
    private boolean isPermissions = false;
    private boolean isBatterySaveModeRequest = false;


    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_allow_permission, R.layout.layout_common_bg_margin);
        setBottomTitle(R.string.call_permission_dialog, null);
    }


    public void Click_ButtomTitle(View v) {
        if (isIgnoringBatteryOptimizations() && isPermissions && isSystemAlertPermission && isSystemSettingPermission) {
            startAgentInfoActivity();
        } else {
            checkDrawOverlayPermission();
        }
    }

    private void checkBatteryMode() {
        isPermissions = true;

        if (!isIgnoringBatteryOptimizations()) {
            isBatterySaveModeRequest = true;
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, REQUEST_BATTERY_CODE);
        } else {
            Click_ButtomTitle(null);
        }
    }

    private boolean isIgnoringBatteryOptimizations() {
        String packageName = this.getPackageName();
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_REQUEST_BATTERY, isBatterySaveModeRequest);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isBatterySaveModeRequest = savedInstanceState.getBoolean(KEY_REQUEST_BATTERY);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SYSTEM_ALERT_CODE) {
            if (Settings.canDrawOverlays(this)) {
                isSystemAlertPermission = true;
                checkSystemSettingPermission();
            } else {
                showAlertDialog(null, getString(R.string.system_alert_permission), RVDialog.STYLE_NOTICE,
                        R.string.computer_active_ok, EVENT_ID_SYSTEM_ALERT, R.string.computer_active_cancel, EVENT_ID_NO);
            }
        }
        if (requestCode == REQUEST_SYSTEM_SETTING) {
            if (Settings.System.canWrite(this)) {
                isSystemSettingPermission = true;
                checkpermission();
            } else {
                showAlertDialog(null, getString(R.string.dialog_system_setting_permission), RVDialog.STYLE_NOTICE,
                        R.string.computer_active_ok, EVENT_ID_SYSTEM_SETTING, R.string.computer_active_cancel, EVENT_ID_NO);
            }
        }
        if (requestCode == REQUEST_BATTERY_CODE) {
            if (isIgnoringBatteryOptimizations() && isPermissions && isSystemAlertPermission) {
                startAgentInfoActivity();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    //다른 앱 위에 그리기 퍼미션
    private void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_SYSTEM_ALERT_CODE);
            } else {
                isSystemAlertPermission = true;
                checkSystemSettingPermission();
            }
        }
    }

    private void checkSystemSettingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isSystemSettingPermission = false;
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_SYSTEM_SETTING);
            } else {
                isSystemSettingPermission = true;
                checkpermission();
            }
        }
    }

    @Override
    protected void onResume() {
        if (isIgnoringBatteryOptimizations() && isPermissions && isSystemAlertPermission) {
            startAgentInfoActivity();
        }
        super.onResume();
    }

    private void startAgentInfoActivity() {
        startActivity(AgentInfoActivity.forFirstIntent(this));
    }

    private void checkpermission() {
        if (checkAndRequestPermission(PERMISSIONCODE, new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE})) {

            isPermissions = true;
            if (!isIgnoringBatteryOptimizations()) {
                checkBatteryMode();
            } else {
                Click_ButtomTitle(null);
            }
        }
    }

    public boolean checkAndRequestPermission(int permissionRequestCode, String... permissions) {
        String[] requiredPermissions = getRequiredPermissions(this, permissions);

        if (requiredPermissions.length > 0) {
            ActivityCompat.requestPermissions(this, requiredPermissions, permissionRequestCode);

            pref = getSharedPreferences(PreferenceConstant.RV_PREF_LOGIN, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            if (pref.getBoolean(IS_FIRST_KEY, true)) {

                editor.putBoolean(IS_FIRST_KEY, false);
                editor.commit();
            }

            return false;
        } else {
            return true;
        }
    }

    public String[] getRequiredPermissions(Context context, String... permissions) {
        List<String> requiredPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(permission);
            }
        }

        return requiredPermissions.toArray(new String[requiredPermissions.size()]);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONCODE) {

            for (int i = 0; i < permissions.length; i++) {
                RLog.i("Premission Result ::" + permissions[i] + " :: " + grantResults[i]);
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    boolean isFirstCall = pref.getBoolean(IS_FIRST_KEY, true);

                    // 다시보지 않기 체크시 설정페이지로 이동
                    if (!showRationale && !isFirstCall) {
                        callSettingDialog();
                        return;
                    }

                }
            }
            checkpermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void callSettingDialog() {
        showAlertDialog(null, getString(R.string.setting_permission_des), RVDialog.STYLE_NOTICE,
                R.string.common_ok, EVENT_ID_CALL_SETTING_DETAIL, R.string.computer_active_cancel, EVENT_ID_NO);
    }

    private void startSettingDetail() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, PERMISSIONCODE);
    }


    @Override
    public void eventDelivery(int event) {
        switch (event) {
            case EVENT_ID_SYSTEM_ALERT:
                checkDrawOverlayPermission();
                break;
            case EVENT_ID_SYSTEM_SETTING:
                checkSystemSettingPermission();
                break;
            case EVENT_ID_CALL_SETTING_DETAIL:
                startSettingDetail();
                break;
            case EVENT_ID_NO:
                dialog.dismiss();
                break;
        }
        super.eventDelivery(event);
    }
}
