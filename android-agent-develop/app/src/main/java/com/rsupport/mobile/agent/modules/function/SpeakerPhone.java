package com.rsupport.mobile.agent.modules.function;


import android.content.Context;
import android.media.AudioManager;

import com.rsupport.util.log.RLog;


public class SpeakerPhone {
    private final String className = "SpeakerPhone";

    private AudioManager audioManager;
    private int maxVolumn;
    private int nowVolumn;


    public SpeakerPhone(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        nowVolumn = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        maxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        RLog.i("nv : " + nowVolumn + ", mv : " + maxVolumn);
    }

    public void startSpeakerPhone() {
        RLog.i("startSpeakerPhone");
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVolumn - 1, 0);
        audioManager.setSpeakerphoneOn(true);
//		new Thread(new Runnable() {
//			public void run() {
//				Looper.prepare();
//				if (checkSpeakerPhoneStatus()) {
//					Utility.showBigFontMessage(Utility.getString(R.string.speakerphone_on));
//				}
//				Looper.loop();
//			}
//		}).start();
    }

    public void stopSpeakerPhone() {
        stopSpeakerPhoneWithoutToast();
//		new Thread(new Runnable() {
//			public void run() {
//				Looper.prepare();
//				if (!checkSpeakerPhoneStatus()) {
//					Utility.showBigFontMessage(Utility.getString(R.string.speakerphone_off));
//				}
//				Looper.loop();
//			}
//		}).start();
    }

    public void stopSpeakerPhoneWithoutToast() {
        RLog.i("stopSpeakerPhoneWithoutToast");
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, nowVolumn, 0);
        audioManager.setSpeakerphoneOn(false);
    }

    public boolean checkSpeakerPhoneStatus() {
        return audioManager.isSpeakerphoneOn();
    }

}
