package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLinksManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppLinksCard(
    currentNumber: String,
    carrierName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var customLinkInput by remember { mutableStateOf("") }
    val linkStatusInfo = remember(customLinkInput) {
        AppLinksManager.analyzeLink(context, customLinkInput)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_links_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.VideoCall,
                        contentDescription = "Video Calling & Apps",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Video Call & Connected Apps",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Call via external apps or inspect custom link status",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-header 1: Video Calling Apps
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Instant Video Calling",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Video
                AppShortcutChip(
                    label = "WhatsApp Video",
                    icon = Icons.Default.Videocam,
                    color = Color(0xFF25D366),
                    testTag = "btn_video_whatsapp",
                    onClick = {
                        AppLinksManager.startWhatsAppVideoCall(context, currentNumber)
                    }
                )

                // Google Meet
                AppShortcutChip(
                    label = "Google Meet",
                    icon = Icons.Default.VideoCall,
                    color = Color(0xFF00832D),
                    testTag = "btn_video_meet",
                    onClick = {
                        AppLinksManager.startGoogleMeet(context)
                    }
                )

                // Zoom Meetings
                AppShortcutChip(
                    label = "Zoom",
                    icon = Icons.Default.VideoCall,
                    color = Color(0xFF2D8CFF),
                    testTag = "btn_video_zoom",
                    onClick = {
                        AppLinksManager.startZoomMeeting(context)
                    }
                )

                // Skype Video
                AppShortcutChip(
                    label = "Skype Video",
                    icon = Icons.Default.Videocam,
                    color = Color(0xFF00AFF0),
                    testTag = "btn_video_skype",
                    onClick = {
                        AppLinksManager.startSkypeVideo(context, currentNumber)
                    }
                )

                // Free WebRTC Video Room
                AppShortcutChip(
                    label = "WebRTC Video",
                    icon = Icons.Default.Language,
                    color = Color(0xFF175499),
                    testTag = "btn_video_webrtc",
                    onClick = {
                        AppLinksManager.startJitsiMeetVideoCall(context, currentNumber)
                    }
                )

                // Viber Call
                AppShortcutChip(
                    label = "Viber",
                    icon = Icons.Default.Videocam,
                    color = Color(0xFF7360F2),
                    testTag = "btn_video_viber",
                    onClick = {
                        AppLinksManager.startViberCall(context, currentNumber)
                    }
                )

                // Signal Call
                AppShortcutChip(
                    label = "Signal",
                    icon = Icons.Default.Videocam,
                    color = Color(0xFF3A76F0),
                    testTag = "btn_video_signal",
                    onClick = {
                        AppLinksManager.startSignalCall(context, currentNumber)
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-header 2: Other Connected Apps & YouTube
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Apps & Tools",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // YouTube Link
                AppShortcutChip(
                    label = "YouTube",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFFE53935),
                    testTag = "btn_link_youtube",
                    onClick = {
                        AppLinksManager.openYouTube(context, "Bahrain telecom number generator")
                    }
                )

                // Phone Dialer
                AppShortcutChip(
                    label = "Dialer",
                    icon = Icons.Default.Dialpad,
                    color = Color(0xFF1976D2),
                    testTag = "btn_link_dialer",
                    onClick = {
                        AppLinksManager.openPhoneDialer(context, currentNumber)
                    }
                )

                // SMS
                AppShortcutChip(
                    label = "SMS",
                    icon = Icons.Default.Send,
                    color = Color(0xFF8E24AA),
                    testTag = "btn_link_sms",
                    onClick = {
                        AppLinksManager.openSms(context, currentNumber)
                    }
                )

                // Telegram
                AppShortcutChip(
                    label = "Telegram",
                    icon = Icons.Default.Send,
                    color = Color(0xFF0088CC),
                    testTag = "btn_link_telegram",
                    onClick = {
                        AppLinksManager.openTelegram(context)
                    }
                )

                // Google Search
                AppShortcutChip(
                    label = "Google",
                    icon = Icons.Default.Search,
                    color = Color(0xFFFB8C00),
                    testTag = "btn_link_google",
                    onClick = {
                        AppLinksManager.searchGoogle(context, "$carrierName $currentNumber")
                    }
                )

                // Share to Any App
                AppShortcutChip(
                    label = "Share All...",
                    icon = Icons.Default.Share,
                    color = MaterialTheme.colorScheme.primary,
                    testTag = "btn_link_share",
                    onClick = {
                        AppLinksManager.shareNumber(context, currentNumber, carrierName)
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // Interactive "Link Status" & Custom Link Dispatcher
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Link Status & Custom App Link",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "Paste any link to test its status and launch into other apps:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            // Input field with paste button
            OutlinedTextField(
                value = customLinkInput,
                onValueChange = { customLinkInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_custom_link"),
                placeholder = {
                    Text(
                        "e.g. https://meet.google.com/xyz or whatsapp://call",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (customLinkInput.isNotEmpty()) {
                        IconButton(onClick = { customLinkInput = "" }) {
                            Text("✕", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    customLinkInput = clipText
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentPaste,
                                contentDescription = "Paste from Clipboard",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )

            // Quick Preset Link Pills
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickPresetPill(label = "Meet Room") {
                    customLinkInput = "https://meet.google.com/new"
                }
                QuickPresetPill(label = "Zoom Link") {
                    customLinkInput = "https://zoom.us/join"
                }
                QuickPresetPill(label = "WhatsApp") {
                    val digits = AppLinksManager.sanitizeNumberForUrl(currentNumber)
                    customLinkInput = "https://wa.me/$digits"
                }
                QuickPresetPill(label = "YouTube") {
                    customLinkInput = "https://www.youtube.com"
                }
            }

            // Live "Link Status" Display Card
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (linkStatusInfo.isValid) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (linkStatusInfo.isValid) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("link_status_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (linkStatusInfo.isValid) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "Link Status Icon",
                            tint = if (linkStatusInfo.isValid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (customLinkInput.isBlank()) "Link Status: Idle (Paste a link)"
                            else if (linkStatusInfo.isValid) "Link Status: Active & Ready"
                            else "Link Status: Pending Scheme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (linkStatusInfo.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (linkStatusInfo.isValid) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = linkStatusInfo.appName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = linkStatusInfo.statusDescription,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (linkStatusInfo.isValid && linkStatusInfo.scheme != "none") {
                        Text(
                            text = "Detected Protocol: ${linkStatusInfo.scheme}://",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Launch / Open Link Button
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val linkToLaunch = customLinkInput.ifBlank { "https://meet.jit.si/Gennum_Video_${AppLinksManager.sanitizeNumberForUrl(currentNumber).takeLast(6)}" }
                    AppLinksManager.launchCustomLink(context, linkToLaunch)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_launch_custom_link"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Launch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (customLinkInput.isNotBlank()) "Open in App / Browser" else "Open Default Video Call Room",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun QuickPresetPill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AppShortcutChip(
    label: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    ElevatedAssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.testTag(testTag)
    )
}
