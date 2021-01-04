package com.tutorialwing.textureview

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.TextView

import java.io.IOException

import android.Manifest.permission.CAMERA
import android.view.Surface

class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private var mTextureView: TextureView? = null
    private var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val result = checkPermission()
        if (result) {
            setTextureView()
        }
    }

    private fun setTextureView() {
        mTextureView = TextureView(this)
        mTextureView!!.surfaceTextureListener = this
        setContentView(mTextureView)
    }

    private fun setMessageView() {
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.info)
        textView?.setText(R.string.permission_required)
    }

    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                    showPermissionAlert()
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(CAMERA), CAMERA_REQUEST_CODE)
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    private fun showPermissionAlert() {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission Required")
        alertBuilder.setMessage("Permission to access camera is needed to run this application")
        alertBuilder.setPositiveButton(android.R.string.yes) { dialog, which -> ActivityCompat.requestPermissions(this@MainActivity, arrayOf(CAMERA), CAMERA_REQUEST_CODE) }
        val alert = alertBuilder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setTextureView()
            } else if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                setMessageView()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        mCamera = Camera.open()

        val previewSize = mCamera!!.parameters.pictureSize
        mTextureView!!.layoutParams = FrameLayout.LayoutParams(previewSize.width, previewSize.height, Gravity.CENTER)


        try {
            mCamera!!.setPreviewTexture(surfaceTexture)
            mCamera!!.startPreview()
        } catch (ioe: IOException) {
            // Something bad happened
        }

        mTextureView!!.alpha = 1.0f;
        mTextureView!!.rotation = 90.0f;
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        // Ignored, Camera does all the work for us
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        mCamera!!.stopPreview()
        mCamera!!.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
        // Invoked every time there's a new Camera preview frame.
    }

    companion object {

        val CAMERA_REQUEST_CODE = 100
    }
}
