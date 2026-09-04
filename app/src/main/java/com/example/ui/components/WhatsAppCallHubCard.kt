package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CallUiState
import com.example.util.AppLinksManager

@Composable
fun WhatsAppCallHubCard(
    state: CallUiState,
    onGenerateWhatsAppOnly: () -> Unit,
    onToggleWhatsAppOnlyMode: () -> Unit,
    onUpdateAudioLink: (String) -> Unit,
    onUpdateVideoLink: (String) -> Unit,
    onUpdateMessage: (String) -> Unit,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentNumber = state.currentGeneratedResult?.formattedNumber.orEmpty()
    val rawNumber = state.currentGeneratedResult?.rawNumber.orEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF25D366).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .testTag("whatsapp_call_hub_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "WhatsApp Hub",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WhatsApp Dispatch & Call Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Call & send audio/video links directly in WhatsApp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // WhatsApp Mode Filter Chip
                FilterChip(
                    selected = state.isWhatsAppOnlyMode,
                    onClick = onToggleWhatsAppOnlyMode,
                    label = {
                        Text(
                            text = if (state.isWhatsAppOnlyMode) "WA Only: ON" else "WA Only",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF25D366),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("toggle_wa_only_mode_chip")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Requested Button: "WhatsApp Generator Numbers Only"
            Button(
                onClick = onGenerateWhatsAppOnly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("whatsapp_generator_numbers_only_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WhatsApp Generator Numbers Only",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Generated Number Tag Box
            Surface(
                color = Color(0xFF25D366).copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WhatsApp Recipient Number",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (currentNumber.isNotBlank()) currentNumber else "No number generated yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (currentNumber.isNotBlank()) {
                        Surface(
                            color = Color(0xFF25D366),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "READY FOR WA",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Write Audio & Video Call Links:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Audio Call Link input
            OutlinedTextField(
                value = state.customWhatsAppAudioLink,
                onValueChange = onUpdateAudioLink,
                label = { Text("Audio / Voice Call Link") },
                placeholder = { Text("https://audio.gennum.app/listen?id=demo") },
                leadingIcon = {
                    Icon(Icons.Default.Audiotrack, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { onUpdateAudioLink("https://audio.gennum.app/listen?id=${(1000..9999).random()}") }) {
                        Icon(Icons.Default.Link, contentDescription = "Auto audio link", modifier = Modifier.size(16.dp))
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wa_audio_link_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Audio Link presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ElevatedAssistChip(
                    onClick = { onUpdateAudioLink("https://audio.gennum.app/listen?voice=delivery") },
                    label = { Text("Delivery Note", fontSize = 10.sp) }
                )
                ElevatedAssistChip(
                    onClick = { onUpdateAudioLink("https://audio.gennum.app/listen?voice=verification") },
                    label = { Text("OTP Verify", fontSize = 10.sp) }
                )
                ElevatedAssistChip(
                    onClick = { onUpdateAudioLink("https://audio.gennum.app/listen?voice=welcome") },
                    label = { Text("Greeting", fontSize = 10.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Video Call Link input
            OutlinedTextField(
                value = state.customWhatsAppVideoLink,
                onValueChange = onUpdateVideoLink,
                label = { Text("Video Call Room Link") },
                placeholder = { Text("https://meet.google.com/new") },
                leadingIcon = {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { onUpdateVideoLink("https://meet.jit.si/gennum_${(10000..99999).random()}") }) {
                        Icon(Icons.Default.Link, contentDescription = "New WebRTC room", modifier = Modifier.size(16.dp))
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wa_video_link_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE65100),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Video Link presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ElevatedAssistChip(
                    onClick = { onUpdateVideoLink("https://meet.google.com/new") },
                    label = { Text("Google Meet", fontSize = 10.sp) }
                )
                ElevatedAssistChip(
                    onClick = { onUpdateVideoLink("https://zoom.us/join") },
                    label = { Text("Zoom", fontSize = 10.sp) }
                )
                ElevatedAssistChip(
                    onClick = { onUpdateVideoLink("https://meet.jit.si/gennum_room") },
                    label = { Text("Jitsi WebRTC", fontSize = 10.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Custom message text input
            OutlinedTextField(
                value = state.customWhatsAppMessageText,
                onValueChange = onUpdateMessage,
                label = { Text("WhatsApp Call Intro Message") },
                placeholder = { Text("Write call introduction text...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wa_message_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live WhatsApp payload preview
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "WhatsApp Message Preview (Sent to Recipient):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF25D366)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.customWhatsAppMessageText}\n📞 Phone: $currentNumber\n🎙 Audio: ${state.customWhatsAppAudioLink}\n📹 Video: ${state.customWhatsAppVideoLink}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Big Button: "Take Me to WhatsApp & Call"
            Button(
                onClick = {
                    AppLinksManager.openWhatsAppWithCallPayload(
                        context = context,
                        rawNumber = rawNumber,
                        audioLink = state.customWhatsAppAudioLink,
                        videoLink = state.customWhatsAppVideoLink,
                        customMessage = state.customWhatsAppMessageText
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("open_whatsapp_call_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Take Me to WhatsApp & Start Call",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Call Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Call in WA
                OutlinedButton(
                    onClick = {
                        AppLinksManager.startWhatsAppVoiceCall(context, rawNumber)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wa_voice_call_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF25D366))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voice Call", fontSize = 12.sp)
                }

                // Video Call in WA
                OutlinedButton(
                    onClick = {
                        AppLinksManager.startWhatsAppVideoCall(context, rawNumber)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wa_video_call_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF25D366))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Video Call", fontSize = 12.sp)
                }

                // Copy
                IconButton(
                    onClick = {
                        val fullText = "${state.customWhatsAppMessageText}\n$currentNumber\nAudio: ${state.customWhatsAppAudioLink}\nVideo: ${state.customWhatsAppVideoLink}"
                        onCopyText(fullText)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("wa_copy_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
