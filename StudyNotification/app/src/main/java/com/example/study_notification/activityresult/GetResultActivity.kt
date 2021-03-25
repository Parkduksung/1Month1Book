package com.example.study_notification.activityresult

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.study_notification.R

class GetResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_and_get_result)


        setResult(RESULT_OK)

    }
}