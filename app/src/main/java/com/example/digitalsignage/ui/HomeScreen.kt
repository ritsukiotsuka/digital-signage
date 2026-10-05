package com.example.digitalsignage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.digitalsignage.ui.theme.Coral
import com.example.digitalsignage.ui.theme.Forest
import com.example.digitalsignage.ui.theme.Leaf

@Composable
fun HomeScreen(onCameraClick: () -> Unit, onTouchClick: () -> Unit, onSettingsClick: () -> Unit) {
    ForestBackground {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).size(56.dp),
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = "設定", tint = Forest, modifier = Modifier.size(30.dp))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🌳", fontSize = 76.sp)
            Text(
                "せいくらべの森",
                style = MaterialTheme.typography.displayMedium,
                color = Forest,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "きょうの せたけを はかって\nおおきくなった しるしを のこそう！",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 22.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Forest.copy(alpha = .82f),
            )
            Spacer(Modifier.height(52.dp))
            MeasurementChoiceButton(
                title = "カメラではかる",
                subtitle = "まえに立つだけ かんたん！",
                icon = { Icon(Icons.Outlined.CameraAlt, null, Modifier.size(50.dp)) },
                color = Coral,
                onClick = onCameraClick,
            )
            Spacer(Modifier.height(22.dp))
            MeasurementChoiceButton(
                title = "せんを引いてはかる",
                subtitle = "あたまの上に ゆびで線を引こう",
                icon = { Icon(Icons.Outlined.TouchApp, null, Modifier.size(50.dp)) },
                color = Leaf,
                onClick = onTouchClick,
            )
        }
    }
}

@Composable
private fun MeasurementChoiceButton(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    color: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = RoundedCornerShape(34.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
    ) {
        icon()
        Column(Modifier.padding(start = 22.dp)) {
            Text(title, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text(subtitle, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .88f))
        }
    }
}
