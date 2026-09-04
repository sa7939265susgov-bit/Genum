package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.CallPhase
import com.example.ui.CallUiState
import com.example.util.AppLinksManager

@Composable
fun ActiveCallDialog(
    state: CallUiState,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleKeypad: () -> Unit,
    onKeypadDigit: (Char) -> Unit
) {
    if (!state.isInCallDialog) return

    val context = LocalContext.current
    var isVideoOptionsOpen by remember { mutableStateOf(false) }

    val result = state.currentGeneratedResult
    val carrierColor = Color(result?.carrier?.brandColor ?: 0xFFD81B60)

    Dialog(
        onDismissRequest = onEndCall,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("active_call_dialog"),
            color = Color(0xFF0F141C)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF161F2E),
                                Color(0xFF0B0E14),
                                Color(0xFF07090D)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Call Details
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        // Carrier & Country Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E2838))
                                .border(1.dp, carrierColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = result?.country?.flag ?: "🇧🇭",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = result?.carrier?.name ?: "Telecom Network",
                                color = carrierColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Formatted Phone Number
                        Text(
                            text = result?.formattedNumber ?: "+973 39 000 000",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Call Status / Phase
                        when (state.callPhase) {
                            CallPhase.RINGING_5S -> {
                                Text(
                                    text = "Ringing... Auto-End in %.1fs".format(state.ringRemainingSeconds),
                                    color = Color(0xFFFFA726),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Ringing Countdown Bar
                                val progress = (state.ringRemainingSeconds / 5.0f).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFFFFA726),
                                    trackColor = Color(0xFF334155),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "5-Second Delivery Verification Active",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            CallPhase.CONNECTED -> {
                                val minutes = state.callDurationSeconds / 60
                                val seconds = state.callDurationSeconds % 60
                                val timerStr = "%02d:%02d".format(minutes, seconds)

                                Text(
                                    text = "Connected ($timerStr)",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔊 Playing Attached Audio Message in Call",
                                    color = Color(0xFF81C784),
                                    fontSize = 13.sp
                                )
                            }
                            CallPhase.ENDED -> {
                                Text(
                                    text = state.lastDeliveryMessage ?: "Call Ended",
                                    color = if (state.deliveryEnsured) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            else -> {}
                        }
                    }

                    // Center Avatar / Pulsing Wave Area
                    Box(
                        modifier = Modifier
                            .size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.callPhase == CallPhase.RINGING_5S) {
                            // Concentric Pulsing Ring
                            PulsingRings(color = carrierColor)
                        }

                        // Central Caller Icon
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            carrierColor.copy(alpha = 0.8f),
                                            carrierColor.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = result?.country?.flag ?: "📞",
                                fontSize = 42.sp
                            )
                        }
                    }

                    // Keypad or Audio Wave Visualization
                    if (state.isKeypadOpen) {
                        KeypadGrid(
                            onDigit = onKeypadDigit,
                            currentInput = state.keypadInput
                        )
                    } else if (state.callPhase == CallPhase.CONNECTED) {
                        // Animated Audio Waveform
                        AudioVisualizerBars(carrierColor = carrierColor)
                    } else {
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Delivery Confirmation Card if ensured
                    if (state.deliveryEnsured) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E3A2F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✅", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Delivery Ensured & Verified",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "5-second ring completed without drop.",
                                        color = Color(0xFFC8E6C9),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // In-Call Action Buttons (Mute, Keypad, Speaker, Hangup)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute button
                            CallActionButton(
                                icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                label = if (state.isMuted) "Unmute" else "Mute",
                                isActive = state.isMuted,
                                onClick = onToggleMute,
                                testTag = "call_mute_button"
                            )

                            // Video Call button
                            CallActionButton(
                                icon = Icons.Default.Videocam,
                                label = "Video Call",
                                isActive = isVideoOptionsOpen,
                                onClick = { isVideoOptionsOpen = !isVideoOptionsOpen },
                                testTag = "call_video_button"
                            )

                            // Keypad button
                            CallActionButton(
                                icon = Icons.Default.Dialpad,
                                label = "Keypad",
                                isActive = state.isKeypadOpen,
                                onClick = onToggleKeypad,
                                testTag = "call_keypad_button"
                            )

                            // Speaker button
                            CallActionButton(
                                icon = if (state.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                label = "Speaker",
                                isActive = state.isSpeakerOn,
                                onClick = onToggleSpeaker,
                                testTag = "call_speaker_button"
                            )
                        }

                        // Video App Switcher Sheet
                        if (isVideoOptionsOpen) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("video_call_options_card"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Switch Call to Video App:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val currentNum = state.currentGeneratedResult?.formattedNumber.orEmpty()
                                        ElevatedAssistChip(
                                            onClick = {
                                                AppLinksManager.startWhatsAppVideoCall(context, currentNum)
                                            },
                                            label = { Text("WhatsApp", fontSize = 11.sp) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        ElevatedAssistChip(
                                            onClick = {
                                                AppLinksManager.startGoogleMeet(context)
                                            },
                                            label = { Text("Meet", fontSize = 11.sp) },
                                            leadingIcon = {
                                                Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color(0xFF00832D), modifier = Modifier.size(14.dp))
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        ElevatedAssistChip(
                                            onClick = {
                                                AppLinksManager.startZoomMeeting(context)
                                            },
                                            label = { Text("Zoom", fontSize = 11.sp) },
                                            leadingIcon = {
                                                Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color(0xFF2D8CFF), modifier = Modifier.size(14.dp))
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Large Red End Call Button
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935))
                                .clickable { onEndCall() }
                                .testTag("end_call_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "End Call",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingRings(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale1)
            .border(2.dp, color.copy(alpha = alpha1), CircleShape)
    )
}

@Composable
fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color.White else Color(0xFF1E293B)
                )
                .clickable { onClick() }
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF0F172A) else Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp
        )
    }
}

@Composable
fun AudioVisualizerBars(carrierColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 38f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 25f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 15f, targetValue = 45f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 35f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 18f, targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(50.dp)
    ) {
        listOf(h1, h2, h3, h4, h5, h2, h3, h1).forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(carrierColor)
            )
        }
    }
}

@Composable
fun KeypadGrid(
    onDigit: (Char) -> Unit,
    currentInput: String
) {
    val keys = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('*', '0', '#')
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        if (currentInput.isNotEmpty()) {
            Text(
                text = currentInput,
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                row.forEach { digit ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable { onDigit(digit) }
                            .testTag("keypad_digit_$digit"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit.toString(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
