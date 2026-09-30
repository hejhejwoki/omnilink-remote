package com.omnilink.remote

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalIndication
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RemoteScreen(viewModel: RemoteViewModel = viewModel()) {
    val haptic = LocalHapticFeedback.current
    val indication = LocalIndication.current
    val ip by viewModel.ipAddress.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()
    var showIpDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF121212), Color.Black)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("OmniLink Remote", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(status, color = Color.LightGray.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    TextButton(onClick = { showIpDialog = true }) {
                        Text("IP Settings", color = Color.Cyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Quick Apps", color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLauncherButton("Netflix", Color.Red, "am start -n com.netflix.ninja/.MainActivity", viewModel, haptic, indication)
                AppLauncherButton("YouTube", Color(0xFFFF0000), "am start -n com.google.android.youtube.tv/com.google.android.apps.youtube.tv.activity.ShellActivity", viewModel, haptic, indication)
                AppLauncherButton("Prime", Color.Blue, "am start -n com.amazon.amazonvideo.livingroom/com.amazon.amazonvideo.livingroom.MainActivity", viewModel, haptic, indication)
                AppLauncherButton("Settings", Color.Gray, "am start -a android.settings.SETTINGS", viewModel, haptic, indication)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassRocker("input keyevent 24", "input keyevent 25", "VOL", viewModel, haptic, indication)
                GlassDPad(viewModel, haptic, indication)
                GlassRocker("input keyevent 192", "input keyevent 193", "CH", viewModel, haptic, indication)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                GlassButton("BACK", "input keyevent 4", viewModel, haptic, indication)
                GlassButton("HOME", "input keyevent 3", viewModel, haptic, indication)
                GlassButton("POWER", "input keyevent 26", viewModel, haptic, indication, color = Color.Red)
            }
        }

        if (showIpDialog) {
            IpSettingsDialog(
                currentIp = ip,
                onDismiss = { showIpDialog = false },
                onSave = { newIp ->
                    viewModel.updateIp(newIp)
                    showIpDialog = false
                }
            )
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
    ) {
        content()
    }
}

@Composable
fun AppLauncherButton(name: String, color: Color, command: String, viewModel: RemoteViewModel, haptic: HapticFeedback, indication: Indication) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(stiffness = 300f), label = "AppScale")

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = indication
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.sendCommand(command)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(name, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun GlassDPad(viewModel: RemoteViewModel, haptic: HapticFeedback, indication: Indication) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(2.dp, Color.Cyan.copy(alpha = 0.2f), CircleShape)
    ) {
        DPadButton(Icons.Filled.KeyboardArrowUp, "input keyevent 19", Modifier.align(Alignment.TopCenter), viewModel, haptic, indication)
        DPadButton(Icons.Filled.KeyboardArrowDown, "input keyevent 20", Modifier.align(Alignment.BottomCenter), viewModel, haptic, indication)
        DPadButton(Icons.Filled.KeyboardArrowLeft, "input keyevent 21", Modifier.align(Alignment.CenterStart), viewModel, haptic, indication)
        DPadButton(Icons.Filled.KeyboardArrowRight, "input keyevent 22", Modifier.align(Alignment.CenterEnd), viewModel, haptic, indication)
        
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, spring(stiffness = 400f), label = "OkScale")
        
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(80.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .background(Color.Cyan.copy(alpha = 0.2f))
                .border(1.dp, Color.Cyan.copy(alpha = 0.5f), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = indication
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.sendCommand("input keyevent 66")
                },
            contentAlignment = Alignment.Center
        ) {
            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DPadButton(icon: ImageVector, command: String, modifier: Modifier, viewModel: RemoteViewModel, haptic: HapticFeedback, indication: Indication) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.8f else 1f, spring(stiffness = 300f), label = "DPadScale")

    Box(
        modifier = modifier
            .padding(16.dp)
            .size(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(
                interactionSource = interactionSource,
                indication = indication
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.sendCommand(command)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun GlassRocker(upCommand: String, downCommand: String, label: String, viewModel: RemoteViewModel, haptic: HapticFeedback, indication: Indication) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(30.dp))
    ) {
        RockerButton(Icons.Filled.KeyboardArrowUp, upCommand, viewModel, haptic, indication)
        Text(label, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
        RockerButton(Icons.Filled.KeyboardArrowDown, downCommand, viewModel, haptic, indication)
    }
}

@Composable
fun RockerButton(icon: ImageVector, command: String, viewModel: RemoteViewModel, haptic: HapticFeedback, indication: Indication) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.8f else 1f, spring(stiffness = 300f), label = "RockerScale")
    
    Box(
        modifier = Modifier
            .size(50.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = indication
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.sendCommand(command)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun GlassButton(text: String, command: String, viewModel: RemoteViewModel, haptic: HapticFeedback, indication: Indication, color: Color = Color.White) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(stiffness = 300f), label = "BtnScale")

    Box(
        modifier = Modifier
            .width(90.dp)
            .height(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = indication
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.sendCommand(command)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun IpSettingsDialog(currentIp: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var ipText by remember { mutableStateOf(currentIp) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("TV IP Address", color = Color.White) },
        text = {
            TextField(
                value = ipText,
                onValueChange = { ipText = it },
                label = { Text("IP Address", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(ipText) }) {
                Text("Save", color = Color.Cyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}