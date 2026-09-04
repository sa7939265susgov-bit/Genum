package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioPreset
import com.example.data.model.Carrier
import com.example.data.model.Country
import com.example.data.model.CountryCarrierRepository
import com.example.ui.AudioSourceType
import com.example.ui.CallUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorSection(
    state: CallUiState,
    onSelectCountry: (Country) -> Unit,
    onSelectCarrier: (Carrier) -> Unit,
    onOpenAddSoundDialog: () -> Unit,
    onTogglePreview: () -> Unit,
    onClearStorage: () -> Unit,
    onGenerateNumber: () -> Unit,
    onGenerateWhatsAppOnly: () -> Unit,
    onToggleWhatsAppOnlyMode: () -> Unit,
    onUpdateAudioLink: (String) -> Unit,
    onUpdateVideoLink: (String) -> Unit,
    onUpdateMessage: (String) -> Unit,
    onOpenApkInstallDialog: () -> Unit,
    onCopyNumber: (String) -> Unit,
    onOpenDialer: (String) -> Unit,
    onCallAndHearAudio: () -> Unit,
    onStart5sDeliveryCall: () -> Unit
) {
    var countryDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 0: APK Installation Quick Banner (Tapping triggers Android package installer pop-out)
        Surface(
            color = Color(0xFF2E7D32).copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenApkInstallDialog() }
                .testTag("open_apk_install_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.InstallMobile,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Install Gennum APK on Physical Phone",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "Tap for instant package installer pop-out",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                Button(
                    onClick = onOpenApkInstallDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Install", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 1: Network & Carrier Selection
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bahrain Carriers & Global Network",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select Batelco, stc, Zain or any country",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Country Selector Button
                    Box {
                        OutlinedButton(
                            onClick = { countryDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("country_selector_button")
                        ) {
                            Text(text = "${state.selectedCountry.flag} ${state.selectedCountry.code}")
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Select Country",
                                modifier = Modifier.padding(start = 4.dp).size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = countryDropdownExpanded,
                            onDismissRequest = { countryDropdownExpanded = false }
                        ) {
                            CountryCarrierRepository.allCountries.forEach { country ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${country.flag} ${country.name} (${country.dialCode})")
                                    },
                                    onClick = {
                                        onSelectCountry(country)
                                        countryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Carrier Selection Chips
                Text(
                    text = "Carriers in ${state.selectedCountry.name}:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedCountry.carriers.forEach { carrier ->
                        val isSelected = carrier.id == state.selectedCarrier.id
                        val carrierColor = Color(carrier.brandColor)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) carrierColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) carrierColor else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectCarrier(carrier) }
                                .testTag("carrier_chip_${carrier.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = carrier.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) carrierColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = carrier.badgeText,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Prefixes hint for selected carrier
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Official Prefixes: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.selectedCarrier.prefixes.joinToString(", ") { "+${state.selectedCountry.dialCode} $it..." },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(state.selectedCarrier.brandColor)
                    )
                }
            }
        }

        // Section 2: Sound & Audio Studio (Addresses "There is no audio / I want to add sound / too much storage")
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .testTag("audio_section_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sound & Audio Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Heard when calling the generated number",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // + Add / Change Sound Button
                    Button(
                        onClick = onOpenAddSoundDialog,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("open_add_sound_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Sound", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Audio Display Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val soundIcon = when (state.audioSourceType) {
                                    AudioSourceType.PRESET -> Icons.Default.Audiotrack
                                    AudioSourceType.TTS -> Icons.Default.RecordVoiceOver
                                    AudioSourceType.RECORDED -> Icons.Default.Mic
                                    AudioSourceType.IMPORTED -> Icons.Default.FolderOpen
                                }
                                Icon(
                                    imageVector = soundIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = state.activeSoundTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (state.audioSourceType) {
                                            AudioSourceType.PRESET -> "Zero-Storage Built-in Telecom Voice"
                                            AudioSourceType.TTS -> "Zero-Storage Text-to-Speech Voice"
                                            AudioSourceType.RECORDED -> "Ultra-compact 32kbps Voice Recording"
                                            AudioSourceType.IMPORTED -> "Custom File from Device Storage"
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Storage Footprint Pill
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = state.audioStorageSizeFormatted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions inside Active Audio Box: [Preview Sound] and [Free Storage]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play / Stop Sound Preview Button
                            Button(
                                onClick = onTogglePreview,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isPreviewPlaying) Color(0xFFD32F2F) else Color(0xFF1976D2)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preview_audio_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isPreviewPlaying) "Stop Preview" else "Play Sound Preview",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Clear Storage / Free Cache Button
                            OutlinedButton(
                                onClick = onClearStorage,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("clear_audio_storage_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Free Storage", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Generate Number CTAs
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dedicated Button: WhatsApp Generator Numbers Only
            Button(
                onClick = onGenerateWhatsAppOnly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("whatsapp_generator_numbers_only_cta"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366)
                )
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WhatsApp Generator Numbers Only",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            // General Carrier Generator Button
            Button(
                onClick = onGenerateNumber,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_number_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(state.selectedCarrier.brandColor)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate ${state.selectedCarrier.name} Number",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Section 4: Pop-out Generated Number Hero Card
        state.currentGeneratedResult?.let { result ->
            val carrierColor = Color(result.carrier.brandColor)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, carrierColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .testTag("generated_number_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = result.country.flag, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = result.carrier.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = carrierColor
                                )
                                Text(
                                    text = "${result.country.name} • ${result.carrier.badgeText}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Quick Copy Button
                        IconButton(
                            onClick = { onCopyNumber(result.formattedNumber) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Number",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large formatted number pop-out
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        carrierColor.copy(alpha = 0.12f),
                                        carrierColor.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = result.formattedNumber,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Audio Attached: ${state.activeSoundTitle}",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Open Device Dialer
                        OutlinedButton(
                            onClick = { onOpenDialer(result.rawNumber) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialer_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dialer", fontSize = 12.sp)
                        }

                        // Call & Hear Audio
                        Button(
                            onClick = onCallAndHearAudio,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("call_audio_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhoneForwarded, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call & Hear", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5s Ring Delivery Test Button
                    Button(
                        onClick = onStart5sDeliveryCall,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("ring_5s_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "5-Second Ring Delivery Test",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section 5: WhatsApp Direct Dispatch & Call Hub (Audio & Video Links)
        WhatsAppCallHubCard(
            state = state,
            onGenerateWhatsAppOnly = onGenerateWhatsAppOnly,
            onToggleWhatsAppOnlyMode = onToggleWhatsAppOnlyMode,
            onUpdateAudioLink = onUpdateAudioLink,
            onUpdateVideoLink = onUpdateVideoLink,
            onUpdateMessage = onUpdateMessage,
            onCopyText = onCopyNumber
        )

        // Section 6: Connected Apps & Shortcuts (YouTube, WhatsApp, SMS, Telegram, Google, Share)
        AppLinksCard(
            currentNumber = state.currentGeneratedResult?.formattedNumber ?: "+${state.selectedCountry.dialCode} ${state.selectedCarrier.prefixes.first()}00000",
            carrierName = state.currentGeneratedResult?.carrier?.name ?: state.selectedCarrier.name,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
