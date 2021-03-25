package com.example.study_notification.activityresult

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.study_notification.R

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_and_get_result)


        startActivityForResult(Intent(this, GetResultActivity::class.java), 1000)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == RESULT_OK && requestCode == 1000){
            Log.d("결과", "$requestCode $resultCode")
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}