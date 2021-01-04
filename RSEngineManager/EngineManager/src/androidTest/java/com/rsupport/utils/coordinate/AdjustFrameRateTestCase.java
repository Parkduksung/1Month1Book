package com.rsupport.utils.coordinate;

import android.test.AndroidTestCase;

import com.rsupport.srn30.adjust.AdjustFPS;
import com.rsupport.util.rslog.MLog;

/**
 * Created by kwcho on 12/29/15.
 */
public class AdjustFrameRateTestCase extends AndroidTestCase{
    private String TAG = "RsupS";

    private AdjustFPS adjustFrameRate = null;
    private RendererSimple rendererSimple = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MLog.setTag(TAG);
        adjustFrameRate = new AdjustFPS();
        rendererSimple = new RendererSimple();
    }

    public void testIsRendering() throws Exception {
        assertFalse(adjustFrameRate.isContinue());
    }

    public void testDefaultFps() throws Exception {
        adjustFrameRate.init(-10);
        new RenderThreadWrapper().process();
    }

    public void test10Fps() throws Exception {
        adjustFrameRate.init(10);
        new RenderThreadWrapper().process();
    }

    public void test20Fps() throws Exception {
        adjustFrameRate.init(20);

        new RenderThreadWrapper().process();
    }

    public void test30Fps() throws Exception {
        adjustFrameRate.init(30);

        new RenderThreadWrapper().process();
    }

    public void test60Fps() throws Exception {
        adjustFrameRate.init(60);

        new RenderThreadWrapper().process();
    }

    public void test120Fps() throws Exception {
        adjustFrameRate.init(120);

        new RenderThreadWrapper().process();
    }

    public void test10To60Fps() throws Exception {
        for(int i = 1; i <= 6; i++){
            rendererSimple.reset();
            adjustFrameRate.init(i * 10);
            new RenderThreadWrapper().process();
        }
    }

    public void test10FpsSleep() throws Exception {
        adjustFrameRate.init(10);

        new RenderThreadWrapper().process(true);
    }

    public void test60FpsSleep() throws Exception {
        adjustFrameRate.init(60);

        new RenderThreadWrapper().process(true);
    }

    public void test1To60FpsUseSleep() throws Exception {
        for(int i = 1; i <= 6; i++){
            rendererSimple.reset();
            adjustFrameRate.init(i * 10);
            new RenderThreadWrapper().process(true);
        }
    }

    public void testWorking10FpsSleep() throws Exception {
        adjustFrameRate.init(10);
        rendererSimple.setForceWorkingTimeMs(25);
        new RenderThreadWrapper().process(true);
    }

    public void testWorking30FpsSleep() throws Exception {
        adjustFrameRate.init(30);
        rendererSimple.setForceWorkingTimeMs(25);
        new RenderThreadWrapper().process(true);
    }


    public void testWorking30FpsSleepYield() throws Exception {
        adjustFrameRate.init(new AdjustFPS.ThreadYield(2, 20), 30);
        rendererSimple.setForceWorkingTimeMs(60);
        new RenderThreadWrapper().process(true);
    }

    class RenderThreadWrapper{
        private boolean isStop = false;

        public void process(final boolean isSleep){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(isStop == false){
                        if(adjustFrameRate.isContinue(isSleep) == false){
                            rendererSimple.doRendering();
                        }
                    }
                }
            });
            rendererSimple.setFinishListener(new RendererSimple.OnFinishListener() {
                @Override
                public void onFinish() {
                    isStop = true;
                }
            });
            rendererSimple.startTick();
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void process(){
            process(false);
        }
    }
}
