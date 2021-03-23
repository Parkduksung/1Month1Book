package com.example.study_notification.calibration

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.study_notification.R

class CalibrationActivity : AppCompatActivity() {

    private val getPoint = mutableListOf<Pair<Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)

        findViewById<Button>(R.id.button).setOnClickListener {

            val copyGetPoint = getPoint.toTypedArray().copyOf()

            copyGetPoint.forEach {
                Log.d("결과", "x : ${it.first} y : ${it.second}")
            }

        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_MOVE) {
            getPoint.add(Pair(event.x, event.y))
            Log.d("결과", "x : ${event.x} y : ${event.y}")
        }
        return super.onTouchEvent(event)
    }
}