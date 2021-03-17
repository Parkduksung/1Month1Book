package com.example.lifecycleactivity

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.lifecycleactivity.databinding.ActivityChromeBookBinding

class ChromeBookActivity : AppCompatActivity() {

    private lateinit var chromeBookViewModel: ChromeBookViewModel

    private lateinit var binding: ActivityChromeBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chrome_book)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        chromeBookViewModel = ViewModelProvider(this).get(ChromeBookViewModel::class.java)

        Log.d("결과", "ChromeBookActivity onCreate")

        if (packageManager.hasSystemFeature("org.chromium.arc.device_management")) {
            Log.d("결과", "크름북 0")
        } else {
            Log.d("결과", "크름북 X")
        }


        binding.toastButton.setOnClickListener {
            Toast.makeText(this, "show Toast!", Toast.LENGTH_SHORT).show()
        }

        binding.dialogButton.setOnClickListener {
            AlertDialog.Builder(this).setMessage("showDialog").setNeutralButton(
                "ok"
            ) { _, _ -> }.create().show()
        }

        binding.intentButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    override fun onStart() {
        Log.d("결과", "ChromeBookActivity onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d("결과", "ChromeBookActivity onResume")
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("결과", "ChromeBookActivity onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        Log.d("결과", "ChromeBookActivity onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("결과", "ChromeBookActivity onStop")
        super.onStop()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("결과", "ChromeBookActivity onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d("결과", "ChromeBookActivity onDestroy")
        super.onDestroy()
    }
}