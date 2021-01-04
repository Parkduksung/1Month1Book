package com.rsupport.utils.coordinate;

import android.test.AndroidTestCase;

import com.rsupport.util.FrameRateChecker;
import com.rsupport.util.rslog.MLog;

/**
 * Created by kwcho on 12/30/15.
 */
public class FrameRateCheckTest extends AndroidTestCase{

    private String TAG = "RsupS";

    @Override
    protected void setUp() throws Exception {
        MLog.setTag(TAG);
    }

    public void testChecker() throws Exception {
        final FrameRateChecker frameRateChecker = new FrameRateChecker();
        frameRateChecker.setOnFrameRateUpdateListener(new FrameRateChecker.OnFrameRateUpdateListener() {
            @Override
            public void update(int frameRate, float frameRateAVG) {
                MLog.v("update : " + frameRate + ", frameRateAVG : " + frameRateAVG);
            }
        });

        Thread tt = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    frameRateChecker.check();
                    long startTime = System.nanoTime();

                    while(System.nanoTime() - startTime < 1000000000/30){
                    }
                }
            }
        });
        tt.start();
        tt.join();


    }
}
