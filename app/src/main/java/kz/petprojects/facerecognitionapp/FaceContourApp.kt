package kz.petprojects.facerecognitionapp

import android.Manifest
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import java.util.concurrent.Executors

@ExperimentalGetImage
@Composable
fun FaceContourApp(
    viewModel: FaceContourViewModel = get()
) {
    val context = LocalContext.current
    val executor = Executors.newSingleThreadExecutor()
    val facesState = remember { mutableStateListOf<Face>() }

    val scope = rememberCoroutineScope()
    val showGlasses by viewModel.showGlasses.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }

    if (!hasCameraPermission) {
        RequestPermission(
            permission = Manifest.permission.CAMERA,
            onPermissionGranted = { hasCameraPermission = true }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = androidx.camera.view.PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().apply {
                            setAnalyzer(executor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    detectFaces(inputImage) { faces ->
                                        facesState.clear()
                                        facesState.addAll(faces)
                                        imageProxy.close()
                                    }
                                }
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        (context as ComponentActivity),
                        androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    FaceContourOverlay(
        faces = facesState,
        showGlasses = showGlasses
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = {
                scope.launch {
                    viewModel.toggleGlasses()
                }
            },
            modifier = Modifier
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = if (showGlasses) "Hide Glasses" else "Show Glasses",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}


fun detectFaces(image: InputImage, onSuccess: (List<Face>) -> Unit) {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .enableTracking()
        .build()

    val detector = FaceDetection.getClient(options)
    detector.process(image)
        .addOnSuccessListener { faces ->
            onSuccess(faces)
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}

@Composable
fun RequestPermission(permission: String, onPermissionGranted: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onPermissionGranted()
    }

    SideEffect {
        launcher.launch(permission)
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun PreviewScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingActionButton(
                onClick = { },
                modifier = Modifier
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Show Glasses",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}