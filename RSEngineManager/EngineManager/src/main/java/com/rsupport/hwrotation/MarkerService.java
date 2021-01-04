package com.rsupport.hwrotation;

// service for detect ro.sf.hwrotation value.
import android.app.Service;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class MarkerService extends Service {

	MarkerView mView;

	@Override
	public void onCreate() {
		super.onCreate();
		boolean canDrawView = true;

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            canDrawView = Settings.canDrawOverlays(this);
        }

        if(canDrawView){
            mView = new MarkerView(this);
            int[] loc = new int[2];
            mView.getLocationOnScreen(loc);
            Rect rc = new Rect();
            mView.getGlobalVisibleRect(rc);
            Log.i("HUD ", rc.toShortString());
        }

	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        if(mView != null)
        {
        	mView.remove();
            mView = null;
        }
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}