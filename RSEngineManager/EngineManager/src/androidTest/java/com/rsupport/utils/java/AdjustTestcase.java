package com.rsupport.utils.java;

import com.rsupport.srn30.adjust.AdjustFPS;
import com.rsupport.util.rslog.MLog;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

/**
 * Created by kwcho on 4/22/15.
 */
public class AdjustTestcase extends TestCase{

    public void testAdjustFPS(){
        CountDownLatch countDownLatch = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                AdjustFPS adjustFPS = new AdjustFPS();
                adjustFPS.init(30);
                while(true){
                    count++;
                    if(adjustFPS.isContinue() == true){
                        continue;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MLog.i("working... : " + count);
                }
            }
        }).start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
