package com.example.digitalsignage.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.digitalsignage.camera.PoseFrame
import com.example.digitalsignage.camera.PoseGuidance
import com.example.digitalsignage.camera.PoseHeightAnalyzer
import com.example.digitalsignage.data.SignageSettings
import com.example.digitalsignage.ui.theme.Coral
import com.example.digitalsignage.ui.theme.Forest
import com.example.digitalsignage.ui.theme.Leaf
import com.example.digitalsignage.ui.theme.Sky
import com.example.digitalsignage.ui.theme.Sun
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun CameraMeasurementScreen(settings: SignageSettings, onBack: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        CameraPermissionScreen(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) }, onBack = onBack)
        return
    }
    CameraExperience(settings, onBack)
}

@Composable
private fun CameraExperience(settings: SignageSettings, onBack: () -> Unit) {
    var latestFrame by remember { mutableStateOf(PoseFrame(PoseGuidance.NO_PERSON)) }
    var frameVersion by remember { mutableIntStateOf(0) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var finalHeight by remember { mutableStateOf<Float?>(null) }
    var countdown by remember { mutableStateOf<Int?>(null) }
    val samples = remember { mutableStateListOf<Float>() }
    val latestSettings by rememberUpdatedState(settings)
    val analyzer = remember {
        PoseHeightAnalyzer(
            settings = { latestSettings },
            onFrame = {
                latestFrame = it
                frameVersion++
            },
        )
    }

    DisposableEffect(Unit) { onDispose { analyzer.close() } }

    LaunchedEffect(frameVersion, finalHeight) {
        if (finalHeight != null) return@LaunchedEffect
        val height = latestFrame.heightCm
        if (latestFrame.guidance == PoseGuidance.HOLD_STILL && height != null && height in 55f..230f) {
            samples += height
            while (samples.size > 18) samples.removeAt(0)
        } else {
            samples.clear()
            countdown = null
        }
    }
    val stable = samples.size >= 12 && ((samples.maxOrNull() ?: 0f) - (samples.minOrNull() ?: 0f)) < 3.2f
    LaunchedEffect(stable, finalHeight) {
        if (!stable || finalHeight != null) return@LaunchedEffect
        for (number in settings.countdownSeconds downTo 1) {
            countdown = number
            delay(1_000)
        }
        finalHeight = samples.sorted()[samples.size / 2]
        countdown = null
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(analyzer = analyzer, onError = { cameraError = it })
        CameraGuideOverlay(latestFrame, stable)
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(22.dp).size(58.dp).background(Color.White.copy(.88f), CircleShape),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "戻る", tint = Forest)
        }

        val message = cameraError ?: latestFrame.guidance.message
        StatusPill(message = message, good = stable, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 54.dp))

        countdown?.let {
            Text(
                it.toString(),
                color = Sun,
                fontSize = 150.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        finalHeight?.let { height ->
            CameraResult(
                height = height,
                onRetry = {
                    finalHeight = null
                    samples.clear()
                },
                onDone = onBack,
            )
        }
    }
}

@Composable
@androidx.annotation.OptIn(ExperimentalLensFacing::class)
private fun CameraPreview(analyzer: PoseHeightAnalyzer, onError: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
    DisposableEffect(lifecycleOwner, previewView) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        future.addListener({
            try {
                val activeProvider = future.get()
                provider = activeProvider
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(analysisExecutor, analyzer) }
                val externalCamera = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_EXTERNAL)
                    .build()
                val selector = when {
                    activeProvider.hasCamera(externalCamera) -> externalCamera
                    activeProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    activeProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    else -> error("利用できるカメラがありません")
                }
                activeProvider.unbindAll()
                activeProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            } catch (_: Exception) {
                onError("カメラを開始できませんでした")
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            provider?.unbindAll()
            analysisExecutor.shutdown()
        }
    }
}

@Composable
private fun CameraGuideOverlay(frame: PoseFrame, stable: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Color.Black.copy(alpha = .18f))
        val guideWidth = size.width * .62f
        val guideHeight = size.height * .72f
        val topLeft = Offset((size.width - guideWidth) / 2f, size.height * .12f)
        drawOval(
            color = if (stable) Leaf else Color.White.copy(.8f),
            topLeft = topLeft,
            size = Size(guideWidth, guideHeight),
            style = Stroke(width = if (stable) 12f else 7f),
        )
        if (frame.headY != null && frame.footY != null) {
            val head = size.height * frame.headY
            val foot = size.height * frame.footY
            drawLine(Coral, Offset(size.width * .15f, head), Offset(size.width * .85f, head), 7f, StrokeCap.Round)
            drawLine(Color.White.copy(.8f), Offset(size.width * .3f, foot), Offset(size.width * .7f, foot), 5f, StrokeCap.Round)
        }
    }
}

@Composable
private fun StatusPill(message: String, good: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(horizontal = 34.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = if (good) Leaf.copy(.94f) else Forest.copy(.92f)),
    ) {
        Row(
            Modifier.padding(horizontal = 26.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.CameraAlt, null, tint = Color.White, modifier = Modifier.size(30.dp))
            Text(message, Modifier.padding(start = 12.dp), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BoxScope.CameraResult(height: Float, onRetry: () -> Unit, onDone: () -> Unit) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, spring(dampingRatio = .55f, stiffness = Spring.StiffnessLow)) }
    Canvas(Modifier.fillMaxSize()) {
        val colors = listOf(Coral, Sun, Leaf, Sky, Color.White)
        repeat(34) { index ->
            val x = ((index * 71) % 100) / 100f * size.width
            val y = ((index * 43) % 70) / 100f * size.height
            drawCircle(colors[index % colors.size].copy(reveal.value), 6f + index % 4 * 2f, Offset(x, y))
        }
    }
    AnimatedVisibility(
        visible = reveal.value > .15f,
        enter = fadeIn() + scaleIn(initialScale = .55f),
        modifier = Modifier.align(Alignment.Center),
    ) {
        Card(
            modifier = Modifier.padding(horizontal = 54.dp),
            shape = RoundedCornerShape(42.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(.97f)),
            elevation = CardDefaults.cardElevation(16.dp),
        ) {
            Column(Modifier.padding(42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 すごい！ はかれたよ 🎉", fontSize = 25.sp, fontWeight = FontWeight.Black, color = Forest)
                Text(formatHeight(height), fontSize = 60.sp, fontWeight = FontWeight.Black, color = Coral)
                Text("きょうの 柱のきず", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Forest)
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Leaf)) {
                        Icon(Icons.Outlined.Refresh, null)
                        Text("もういちど", Modifier.padding(start = 6.dp), fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = Forest)) {
                        Text("おわる", fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "カメラによる推定値です",
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionScreen(onRequest: () -> Unit, onBack: () -> Unit) {
    ForestBackground {
        Column(
            Modifier.fillMaxSize().padding(48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("📷", fontSize = 78.sp)
            Text("カメラを つかっていい？", fontSize = 29.sp, fontWeight = FontWeight.Black, color = Forest)
            Text(
                "せたけを はかるためだけに使います。\n写真や映像は保存しません。",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 22.dp),
                fontSize = 18.sp,
            )
            Button(onClick = onRequest, modifier = Modifier.fillMaxWidth().height(68.dp)) {
                Text("カメラを許可", fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Forest),
            ) { Text("もどる") }
        }
    }
}

private val PoseGuidance.message: String
    get() = when (this) {
        PoseGuidance.NO_PERSON -> "わくの中に 立ってね"
        PoseGuidance.SHOW_FULL_BODY -> "あたまから 足まで うつしてね"
        PoseGuidance.MOVE_TO_CENTER -> "まんなかに 立ってね"
        PoseGuidance.MOVE_CLOSER -> "もうすこし 前へ"
        PoseGuidance.MOVE_BACK -> "もうすこし うしろへ"
        PoseGuidance.HOLD_STILL -> "いいね！ そのまま じっとしてね"
    }
