package com.example.Praktikum9_235150201111049AnaZahra

import android.content.ContentValues
import android.content.Context
//import android.graphics.Camera
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.Camera
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.suspendCancellableCoroutine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val cameraPermission = android.Manifest.permission.CAMERA
            val cameraPermissionGranted = remember { mutableStateOf(false) }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                cameraPermissionGranted.value = granted
            }

            LaunchedEffect(Unit) {
                launcher.launch(cameraPermission)
            }
            KameraScreen()
        }
    }
}

@Composable
fun CameraPreview(onPreviewReady: (PreviewView) -> Unit) {
    AndroidView(factory = { context ->
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            post { onPreviewReady(this) }
        }
    })
}

suspend fun bindPreview(
    context: Context,
    owner: LifecycleOwner,
    previewView: PreviewView
): Pair<Preview, Camera> {
    val provider = suspendCancellableCoroutine<ProcessCameraProvider> { cont ->
        val f = ProcessCameraProvider.getInstance(context)
        f.addListener({ cont.resume(f.get()) {} }, ContextCompat.getMainExecutor(context))
    }

    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
    val selector = CameraSelector.DEFAULT_BACK_CAMERA

    provider.unbindAll()
    val camera = provider.bindToLifecycle(owner, selector, preview)
    return preview to camera
}

fun bindWithImageCapture(
    provider: ProcessCameraProvider,
    owner: LifecycleOwner,
    preview: Preview,
    selector: CameraSelector
): ImageCapture {
    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    provider.unbindAll()
    provider.bindToLifecycle(owner, selector, preview, imageCapture)
    return imageCapture
}

fun outputOptions(ctx: Context, name: String): ImageCapture.OutputFileOptions {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KameraKu")
    }
    val resolver = ctx.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
    return ImageCapture.OutputFileOptions.Builder(resolver, uri, values).build()
}

fun takePhoto(ctx: Context, ic: ImageCapture, onSaved: (Uri) -> Unit) {
    val options = outputOptions(ctx, "IMG_${System.currentTimeMillis()}")
    ic.takePicture(options, ContextCompat.getMainExecutor(ctx),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSaved(output.savedUri!!)
            }
            override fun onError(exception: ImageCaptureException) {
                // tampilkan error
            }
        }
    )
}

@Composable
fun KameraScreen() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraDisplayBox(
            onPreviewReady = { pv ->
                previewView = pv
            }
        )

        LaunchedEffect(previewView) {
            previewView?.let { pv ->
                val (previewObj, cameraObj) = bindPreview(context, owner, pv)
                preview = previewObj
                camera = cameraObj
            }
        }

        Button(
            onClick = {
                scope.launch {
                    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                    preview?.let { p ->
                        imageCapture = bindWithImageCapture(
                            cameraProvider,
                            owner,
                            p,
                            CameraSelector.DEFAULT_BACK_CAMERA
                        )
                        imageCapture?.let { ic ->
                            takePhoto(context, ic) { uri ->
                                Log.d("KameraKu", "Foto tersimpan di $uri")
                            }
                        }
                    }
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Ambil Foto")
        }
    }
}

@Composable
fun CameraDisplayBox(onPreviewReady: (PreviewView) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    post { onPreviewReady(this) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}