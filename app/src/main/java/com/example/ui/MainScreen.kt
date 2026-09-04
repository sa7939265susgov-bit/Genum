package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ActiveCallDialog
import com.example.ui.components.AddSoundDialog
import com.example.ui.components.ApkInstallDialog
import com.example.ui.components.DeliverySection
import com.example.ui.components.GeneratorSection
import com.example.ui.components.HistorySection
import com.example.ui.components.WorldTimesSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CallViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val historyList by viewModel.savedHistoryFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Display user feedback snackbars
    LaunchedEffect(state.userNotice) {
        state.userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    // Active Simulated Call Dialog
    ActiveCallDialog(
        state = state,
        onEndCall = { viewModel.endCallSimulation() },
        onToggleSpeaker = { viewModel.toggleSpeaker() },
        onToggleMute = { viewModel.toggleMute() },
        onToggleKeypad = { viewModel.toggleKeypad() },
        onKeypadDigit = { viewModel.pressKeypadDigit(it) }
    )

    // Add Sound & Audio Studio Dialog
    if (state.showAddSoundDialog) {
        AddSoundDialog(
            state = state,
            presets = viewModel.audioCallManager.audioPresets,
            onDismiss = { viewModel.setAddSoundDialogVisible(false) },
            onSelectPreset = { viewModel.selectPreset(it) },
            onSetTtsVoiceText = { viewModel.setTtsVoiceText(it) },
            onStartRecording = { viewModel.startRecordingAudio() },
            onStopRecording = { viewModel.stopRecordingAudio() },
            onImportAudio = { uri, name -> viewModel.importAudioFile(uri, name) },
            onClearStorage = { viewModel.clearAudioStorage() },
            onTogglePreview = { viewModel.toggleSoundPreview() }
        )
    }

    // Direct APK Package Installer Pop-out Dialog
    if (state.showApkInstallDialog) {
        ApkInstallDialog(
            onDismiss = { viewModel.setApkInstallDialogVisible(false) }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .testTag("app_logo_image")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Bahrain (Batelco • stc • Zain) & Worldwide",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Live Bahrain Time Badge in Top Bar
                        val bhTime = state.worldTimes.firstOrNull { it.country.code == "BH" }
                        if (bhTime != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🇧🇭", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = bhTime.timeFormatted.substringBeforeLast(" "),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setApkInstallDialogVisible(true) },
                        modifier = Modifier.testTag("top_bar_install_apk_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = "Install APK",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.Phone, contentDescription = "Generator") },
                    label = { Text("Generator", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_generator")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "5s Delivery") },
                    label = { Text("5s Delivery", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_delivery")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Default.Public, contentDescription = "World Times") },
                    label = { Text("World Times", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_world_times")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_item_history")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        GeneratorSection(
                            state = state,
                            onSelectCountry = { viewModel.selectCountry(it) },
                            onSelectCarrier = { viewModel.selectCarrier(it) },
                            onOpenAddSoundDialog = { viewModel.setAddSoundDialogVisible(true) },
                            onTogglePreview = { viewModel.toggleSoundPreview() },
                            onClearStorage = { viewModel.clearAudioStorage() },
                            onGenerateNumber = { viewModel.generateNumber() },
                            onGenerateWhatsAppOnly = { viewModel.generateWhatsAppNumberOnly() },
                            onToggleWhatsAppOnlyMode = { viewModel.toggleWhatsAppOnlyMode() },
                            onUpdateAudioLink = { viewModel.setCustomWhatsAppAudioLink(it) },
                            onUpdateVideoLink = { viewModel.setCustomWhatsAppVideoLink(it) },
                            onUpdateMessage = { viewModel.setCustomWhatsAppMessageText(it) },
                            onOpenApkInstallDialog = { viewModel.setApkInstallDialogVisible(true) },
                            onCopyNumber = { viewModel.copyToClipboard(it) },
                            onOpenDialer = { rawNumber ->
                                val intent = viewModel.getDialerIntent(rawNumber)
                                context.startActivity(intent)
                            },
                            onCallAndHearAudio = { viewModel.startCallAndHearAudio() },
                            onStart5sDeliveryCall = { viewModel.start5SecondDeliveryCall() }
                        )
                    }
                }
                1 -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        DeliverySection(
                            state = state,
                            onStart5sDeliveryCall = { viewModel.start5SecondDeliveryCall() },
                            onOpenDialer = { rawNumber ->
                                val intent = viewModel.getDialerIntent(rawNumber)
                                context.startActivity(intent)
                            },
                            onStartPhoneAssistant = { viewModel.startPhoneRingAssistant() },
                            onCancelPhoneAssistant = { viewModel.cancelPhoneRingAssistant() }
                        )
                    }
                }
                2 -> {
                    WorldTimesSection(
                        worldTimes = state.worldTimes,
                        onSelectCountryAndGenerate = { country ->
                            viewModel.selectCountry(country)
                            selectedTabIndex = 0
                        }
                    )
                }
                3 -> {
                    HistorySection(
                        historyList = historyList,
                        onCopy = { viewModel.copyToClipboard(it) },
                        onOpenDialer = { rawNumber ->
                            val intent = viewModel.getDialerIntent(rawNumber)
                            context.startActivity(intent)
                        },
                        onCallAndHear = { entity ->
                            viewModel.startCallAndHearAudio(entity)
                        },
                        on5sRingTest = { entity ->
                            viewModel.start5SecondDeliveryCall(entity)
                        },
                        onDeleteItem = { entity ->
                            viewModel.deleteHistoryItem(entity)
                        },
                        onClearAll = {
                            viewModel.clearAllHistory()
                        }
                    )
                }
            }
        }
    }
}
