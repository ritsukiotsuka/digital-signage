package com.example.digitalsignage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.digitalsignage.data.SignageSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settings: SignageSettings, onSave: (SignageSettings) -> Unit, onBack: () -> Unit) {
    var draft by remember(settings) { mutableStateOf(SettingsDraft.from(settings)) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設置・計測設定", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionTitle("タッチフレーム")
            Text("画面を縦置きした状態で、床から表示領域までを実測してください。", style = MaterialTheme.typography.bodyMedium)
            NumberField("画面下端の床からの高さ", "cm", draft.displayBottom) { draft = draft.copy(displayBottom = it) }
            NumberField("表示領域の実寸（縦）", "cm", draft.displayHeight) { draft = draft.copy(displayHeight = it) }

            SectionTitle("カメラ")
            NumberField("カメラ中心の床からの高さ", "cm", draft.cameraHeight) { draft = draft.copy(cameraHeight = it) }
            NumberField("立ち位置までの水平距離", "cm", draft.subjectDistance) { draft = draft.copy(subjectDistance = it) }
            NumberField("カメラの垂直画角", "度", draft.verticalFov) { draft = draft.copy(verticalFov = it) }
            NumberField("カメラ上向き角度（下向きはマイナス）", "度", draft.cameraTilt) { draft = draft.copy(cameraTilt = it) }

            SectionTitle("共通補正")
            NumberField("計測結果への加算補正", "cm", draft.correction) { draft = draft.copy(correction = it) }
            NumberField("撮影カウントダウン", "秒", draft.countdown) { draft = draft.copy(countdown = it) }
            Text(
                "PoC設置時は既知の身長で複数回測定し、誤差の平均を「加算補正」に入力してください。",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = { draft.toSettings()?.let(onSave) },
                enabled = draft.toSettings() != null,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text("設定を保存", modifier = Modifier.padding(vertical = 8.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
private fun NumberField(label: String, suffix: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { candidate ->
            if (candidate.matches(Regex("-?\\d{0,4}(\\.\\d{0,2})?"))) onValueChange(candidate)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

private data class SettingsDraft(
    val displayBottom: String,
    val displayHeight: String,
    val cameraHeight: String,
    val subjectDistance: String,
    val verticalFov: String,
    val cameraTilt: String,
    val correction: String,
    val countdown: String,
) {
    fun toSettings(): SignageSettings? {
        val result = SignageSettings(
            displayBottomHeightCm = displayBottom.toFloatOrNull() ?: return null,
            displayPhysicalHeightCm = displayHeight.toFloatOrNull() ?: return null,
            cameraHeightCm = cameraHeight.toFloatOrNull() ?: return null,
            subjectDistanceCm = subjectDistance.toFloatOrNull() ?: return null,
            cameraVerticalFovDegrees = verticalFov.toFloatOrNull() ?: return null,
            cameraTiltDegrees = cameraTilt.toFloatOrNull() ?: return null,
            heightCorrectionCm = correction.toFloatOrNull() ?: return null,
            countdownSeconds = countdown.toIntOrNull() ?: return null,
        )
        return result.takeIf {
            it.displayBottomHeightCm in 0f..250f &&
                it.displayPhysicalHeightCm in 20f..250f &&
                it.cameraHeightCm in 50f..300f &&
                it.subjectDistanceCm in 50f..600f &&
                it.cameraVerticalFovDegrees in 20f..120f &&
                it.cameraTiltDegrees in -45f..45f &&
                it.heightCorrectionCm in -30f..30f &&
                it.countdownSeconds in 1..10
        }
    }

    companion object {
        fun from(settings: SignageSettings) = SettingsDraft(
            displayBottom = settings.displayBottomHeightCm.clean(),
            displayHeight = settings.displayPhysicalHeightCm.clean(),
            cameraHeight = settings.cameraHeightCm.clean(),
            subjectDistance = settings.subjectDistanceCm.clean(),
            verticalFov = settings.cameraVerticalFovDegrees.clean(),
            cameraTilt = settings.cameraTiltDegrees.clean(),
            correction = settings.heightCorrectionCm.clean(),
            countdown = settings.countdownSeconds.toString(),
        )

        private fun Float.clean() = if (this % 1f == 0f) toInt().toString() else toString()
    }
}
