package com.mzm.sample.cartoongan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.mzm.sample.cartoongan.databinding.ActivityMainBinding
import com.mzm.sample.cartoongan.ext.toBitmap
import com.mzm.sample.cartoongan.ml.LiteModelCartoonganInt81
import com.mzm.sample.cartoongan.ml.WhiteboxCartoonGanDr
import com.mzm.sample.cartoongan.ml.WhiteboxCartoonGanFp16
import kotlinx.coroutines.*
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.lang.Runnable
import java.util.concurrent.Executors

/**
 * Author: Margaret Maynard-Reid
 *
 * This is an Android sample app that showcases the following:
 *
 * 1. Jetpack navigation component - navigate between Fragments
 * 2. CameraX - permission check, camera setup, and image capture use case
 * 3. ML Model binding - easy import of .tflite model in Android Studio
 * 4. Transform a selfie image to a cartoon image with the whitebox_cartoon+_*.tflite model
 *
 * This MainActivity.kt is the main entry point into the sample app.
 * There is one single Activity with 3 Fragments:
 *
 * 1. PermissionsFragment.kt - check camera permission
 * 2. CameraFragment.kt - capture photo
 * 3. Selfie2CartoonFragment.kt - display the selfie & cartoon images
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

//    private val parentJob = Job()
//    private val coroutineScope = CoroutineScope(
//        Dispatchers.Main + parentJob
//    )

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        binding.run {
            vm = viewModel
        }



        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

    }

//    private fun getOutputAsync(bitmap: Bitmap): Deferred<Bitmap> =
//        // use async() to create a coroutine in an IO optimized Dispatcher for model inference
//        coroutineScope.async(Dispatchers.IO) {
//            val sourceImage = TensorImage.fromBitmap(bitmap)
//            return@async inferenceWithFp16Model(sourceImage).bitmap
//        }

//    private fun updateUI(outputBitmap: Bitmap) {
//        binding.viewConverted.setImageBitmap(outputBitmap)
//    }

//    private fun inferenceWithInt8Model(
//        sourceImage: TensorImage,
//        options: Model.Options
//    ): TensorImage {
//        val model = WhiteboxCartoonGanInt8.newInstance(requireContext(), options)
//
////        val model = WhiteboxCartoonGanInt8.newInstance(requireContext())
//        // Runs model inference and gets result.
//        val outputs = model.process(sourceImage)
//        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage
//
//        // Releases model resources if no longer used.
//        model.close()
//
//        return cartoonizedImage
//    }

    /**
     * Run inference with the dynamic range tflite model
     */
//    private fun inferenceWithDrModel(sourceImage: TensorImage): TensorImage {
//        val model = WhiteboxCartoonGanDr.newInstance(this, options)
//
//        // Runs model inference and gets result.
//        val outputs = model.process(sourceImage)
//        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage
//
//        // Releases model resources if no longer used.
//        model.close()
//        return cartoonizedImage
//    }

//
//    private fun inferenceWithFp16Model(sourceImage: TensorImage): TensorImage {
//        val t = System.currentTimeMillis()
//        val model = WhiteboxCartoonGanFp16.newInstance(this, options)
//        Log.d("결과", (System.currentTimeMillis() - t).toString())
//        // Runs model inference and gets result.
//        val outputs = model.process(sourceImage)
//        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage
//
//        // Releases model resources if no longer used.
//        model.close()
//
//        return cartoonizedImage
//    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            Runnable {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                val preview = Preview.Builder()
                    .build().also {
                        it.setSurfaceProvider(binding.viewConverter.createSurfaceProvider())
                    }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setImageQueueDepth(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                    .build()

                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    ImageAnalysis.Analyzer { image ->
                        runOnUiThread {

                            val t = System.currentTimeMillis()
                            viewModel.inputSource(binding.viewConverter.bitmap!!, this)
                            Log.d("결과", (System.currentTimeMillis() - t).toString())
                            image.close()
                        }
                    })
                try {
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, imageAnalysis, preview
                    )
                } catch (exc: Exception) {
                }
            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                finish()
            }
        }
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
