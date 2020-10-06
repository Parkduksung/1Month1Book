package com.rsupport.litecam.util;

import android.hardware.Camera.CameraInfo;
import android.media.AudioFormat;
import android.media.CamcorderProfile;

import com.rsupport.litecam.record.RecordSet;

/**
 * Android 시스템의 profile 정보를 불러온다. 녹화 가능한 사이즈는 기본적으로 Android CamcorderProfile을 참조하여
 * 녹화 가능한 최대 해상도를 정의하고, 사운드의 녹음 채널과 bitrate를 정의한다. 샘플링 레이트는 기본값을 가진다.
 *
 * @author taehwan
 */
public class SystemRecordProfile {

    public class RecordProfile {
        int width;
        int height;
        int cameraFacing;

        /**
         * Record profile의 기본값으로 정의하며 이 경우 null 에 해당된다.
         */
        public RecordProfile() {
            this.width = -1;
            this.height = -1;
            this.cameraFacing = -1;
        }

        /**
         * Record profile의 cameraFacing을 정의하며, 이 경우 전면 카메라 정보에 해당된다.
         *
         * @param cameraFacing
         */
        public RecordProfile(int cameraFacing) {
            this.width = -1;
            this.height = -1;
            this.cameraFacing = cameraFacing;
        }

        /**
         * Record profile의 정보가 모두 있는 경우이며, 이 경우 후면 카메라 정보에 해당된다.
         *
         * @param profile
         * @param cameraFacing
         */
        public RecordProfile(CamcorderProfile profile, int cameraFacing) {
            this.width = profile.videoFrameWidth;
            this.height = profile.videoFrameHeight;
            this.cameraFacing = cameraFacing;
        }
    }

    /**
     * Syste의 RecordProfile의 정보를 체크하기 위해서 사용한다.
     *
     * @return
     */
    public static SystemRecordProfile create() {
        return new SystemRecordProfile();
    }

    /**
     * CamcorderProfile을 체크하여 Record의 정보를 셋팅한다.
     *
     * @return
     */
    public RecordProfile getCamcorderProfile() {
        CamcorderProfile profile = null;

        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        if (profile != null) {
            LLog.d(true, profile.toString());
            setRecordSet(profile);
            return new RecordProfile(profile, CameraInfo.CAMERA_FACING_BACK);

        } else { // 전면 카메라 정보를 저장한다.
            profile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
            if (profile != null) {
                setRecordSet(profile);
                return new RecordProfile(CameraInfo.CAMERA_FACING_FRONT);
            }
        }

        return new RecordProfile();
    }

    /**
     * rs record audio com.rsupport.setting
     * Audio의 Channel count, channel config, bitrate를 시스템의 camrecord시의 데이터를 가져온다.
     *
     * @param profile
     */
    private void setRecordSet(CamcorderProfile profile) {
        if (profile != null) {
            RecordSet.AUDIO_CHANNEL_COUNT = profile.audioChannels;
            if (RecordSet.AUDIO_CHANNEL_COUNT == 1) {
                RecordSet.RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
            } else {
                RecordSet.RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
            }

            RecordSet.DEFAULT_AUDIO_BIT_RATE = profile.audioBitRate;

            LLog.i(true, "Record audio channel count: " + RecordSet.AUDIO_CHANNEL_COUNT);
            LLog.i(true, "Record audio bitrate: " + RecordSet.DEFAULT_AUDIO_BIT_RATE);
            LLog.i(true, "Record audio samplerate: " + RecordSet.DEFAULT_SAMPLE_RATE);
        }
    }
}

