package com.rsupport.mobile.agent.ui.base;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Hyungu-PC on 2015-06-25.
 */
public class CommonActivity extends AppCompatActivity {
    /**
     * 이벤트 전달.
     * Activity 에서 호출한 타 class 에서 이벤트를 전달받을 때 사용
     * 필요에 따라 커스텀 사용
     */
    public void eventDelivery(int event) {
        return;
    }

    /**
     * List용
     */
    public void eventDelivery(int event, int pos) {
        return;
    }
}
