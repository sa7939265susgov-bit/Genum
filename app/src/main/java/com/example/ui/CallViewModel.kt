package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioCallManager
import com.example.audio.AudioPreset
import com.example.data.local.AppDatabase
import com.example.data.local.entity.GeneratedNumberEntity
import com.example.data.model.Carrier
import com.example.data.model.Country
import com.example.data.model.CountryCarrierRepository
import com.example.data.model.GeneratedNumberResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class CallPhase {
    IDLE,
    RINGING_5S,
    CONNECTED,
    ENDED
}

enum class AudioSourceType {
    PRESET,
    TTS,
    RECORDED,
    IMPORTED
}

data class WorldTimeItem(
    val country: Country,
    val timeFormatted: String,
    val dateFormatted: String,
    val gmtOffset: String,
    val isRecommendedCallTime: Boolean,
    val timeStatusText: String
)

data class CallUiState(
    val selectedCountry: Country = CountryCarrierRepository.bahrain,
    val selectedCarrier: Carrier = CountryCarrierRepository.batelcoCarrier,
    val currentGeneratedResult: GeneratedNumberResult? = null,
    val currentEntityId: Long? = null,
    
    // Audio configuration & storage
    val audioSourceType: AudioSourceType = AudioSourceType.PRESET,
    val selectedPreset: AudioPreset? = null,
    val customTtsText: String = "This is an automated Bahrain delivery verification call. Your connection is confirmed.",
    val importedAudioFileName: String? = null,
    val recordedAudioFile: File? = null,
    val recordedDurationMs: Long = 0L,
    val isRecording: Boolean = false,
    val recordingTimeSeconds: Int = 0,
    val audioAmplitudes: List<Float> = emptyList(),
    val isPreviewPlaying: Boolean = false,
    val audioStorageSizeFormatted: String = "0 KB (Zero-Storage Preset)",
    val showAddSoundDialog: Boolean = false,
    
    // Call simulation state
    val isInCallDialog: Boolean = false,
    val callPhase: CallPhase = CallPhase.IDLE,
    val ringRemainingSeconds: Float = 5.0f,
    val callDurationSeconds: Int = 0,
    val isSpeakerOn: Boolean = true,
    val isMuted: Boolean = false,
    val isKeypadOpen: Boolean = false,
    val keypadInput: String = "",
    val deliveryEnsured: Boolean = false,
    val lastDeliveryMessage: String? = null,
    
    // Notification / Toast
    val userNotice: String? = null,
    
    // World time ticking
    val worldTimes: List<WorldTimeItem> = emptyList(),
    
    // Phone Ring Delivery Assistant (for physical phone dialer)
    val phoneRingAssistantRunning: Boolean = false,
    val phoneRingAssistantCountdown: Int = 5,

    // WhatsApp Direct Generator & Audio/Video Call Hub
    val isWhatsAppOnlyMode: Boolean = false,
    val customWhatsAppAudioLink: String = "https://audio.gennum.app/listen?id=demo",
    val customWhatsAppVideoLink: String = "https://meet.google.com/new",
    val customWhatsAppMessageText: String = "Hello! I am calling you via the generated Gennum number. Here are the audio and video links to connect:",
    val showApkInstallDialog: Boolean = false
) {
    val activeSoundTitle: String
        get() = when (audioSourceType) {
            AudioSourceType.PRESET -> selectedPreset?.title ?: "Telecom Audio Dispatch"
            AudioSourceType.TTS -> "Voice Text: ${customTtsText.take(28)}..."
            AudioSourceType.RECORDED -> "Recorded Voice Memo (${recordingTimeSeconds}s)"
            AudioSourceType.IMPORTED -> importedAudioFileName ?: "Imported Sound File"
        }
}

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.generatedNumberDao()
    val audioCallManager = AudioCallManager(application)

    private val _uiState = MutableStateFlow(
        CallUiState(
            selectedPreset = audioCallManager.audioPresets.firstOrNull()
        )
    )
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    val savedHistoryFlow = dao.getAllNumbersFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var callJob: Job? = null
    private var ringingSession: AudioCallManager.RingingSession? = null
    private var recordingJob: Job? = null
    private var clockJob: Job? = null
    private var assistantJob: Job? = null

    init {
        // Initial generation for instant readiness
        generateNumber()
        startWorldClockTicker()
        updateAudioStorageSize()
    }

    fun selectCountry(country: Country) {
        val carrier = country.carriers.firstOrNull() ?: CountryCarrierRepository.batelcoCarrier
        _uiState.update {
            it.copy(
                selectedCountry = country,
                selectedCarrier = carrier
            )
        }
        generateNumber()
    }

    fun selectCarrier(carrier: Carrier) {
        _uiState.update { it.copy(selectedCarrier = carrier) }
        generateNumber()
    }

    fun selectPreset(preset: AudioPreset) {
        audioCallManager.stopPlayback()
        _uiState.update {
            it.copy(
                audioSourceType = AudioSourceType.PRESET,
                selectedPreset = preset,
                recordedAudioFile = null,
                isPreviewPlaying = false,
                audioStorageSizeFormatted = "0 KB (Zero-Storage Preset)",
                userNotice = "Sound selected: ${preset.title}"
            )
        }
    }

    fun setTtsVoiceText(text: String) {
        audioCallManager.stopPlayback()
        _uiState.update {
            it.copy(
                audioSourceType = AudioSourceType.TTS,
                customTtsText = text,
                selectedPreset = null,
                recordedAudioFile = null,
                isPreviewPlaying = false,
                audioStorageSizeFormatted = "0 KB (Synthesized Voice)",
                userNotice = "Voice sound generated from text!"
            )
        }
    }

    fun importAudioFile(uri: Uri, displayName: String?) {
        viewModelScope.launch {
            val file = audioCallManager.importAudioFromUri(uri)
            if (file != null && file.exists()) {
                val sizeStr = audioCallManager.formatStorageSize(file.length())
                _uiState.update {
                    it.copy(
                        audioSourceType = AudioSourceType.IMPORTED,
                        recordedAudioFile = file,
                        selectedPreset = null,
                        importedAudioFileName = displayName ?: file.name,
                        audioStorageSizeFormatted = sizeStr,
                        isPreviewPlaying = false,
                        userNotice = "Sound file imported ($sizeStr)!"
                    )
                }
            } else {
                _uiState.update { it.copy(userNotice = "Could not import audio file.") }
            }
        }
    }

    fun setAddSoundDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAddSoundDialog = visible) }
    }

    /**
     * Toggles play/stop preview of the currently selected sound.
     */
    fun toggleSoundPreview() {
        if (_uiState.value.isPreviewPlaying) {
            audioCallManager.stopPlayback()
            _uiState.update { it.copy(isPreviewPlaying = false) }
        } else {
            val state = _uiState.value
            val filePath = state.recordedAudioFile?.absolutePath
            val presetId = state.selectedPreset?.id
            val ttsText = if (state.audioSourceType == AudioSourceType.TTS) state.customTtsText else null

            _uiState.update { it.copy(isPreviewPlaying = true) }

            audioCallManager.playAudio(
                filePath = filePath,
                presetId = presetId,
                ttsText = ttsText,
                onCompletion = {
                    _uiState.update { it.copy(isPreviewPlaying = false) }
                }
            )
        }
    }

    fun clearAudioStorage() {
        audioCallManager.stopPlayback()
        val bytes = audioCallManager.clearAudioStorage()
        val freedFormatted = audioCallManager.formatStorageSize(bytes)
        _uiState.update {
            it.copy(
                audioSourceType = AudioSourceType.PRESET,
                selectedPreset = audioCallManager.audioPresets.firstOrNull(),
                recordedAudioFile = null,
                isPreviewPlaying = false,
                audioStorageSizeFormatted = "0 KB (Zero-Storage Preset)",
                userNotice = "Storage freed: Cleaned $freedFormatted of audio cache!"
            )
        }
    }

    private fun updateAudioStorageSize() {
        val bytes = audioCallManager.getAudioStorageBytes()
        val formatted = if (bytes == 0L) {
            if (_uiState.value.audioSourceType == AudioSourceType.PRESET) "0 KB (Zero-Storage Preset)"
            else "0 KB"
        } else {
            audioCallManager.formatStorageSize(bytes)
        }
        _uiState.update { it.copy(audioStorageSizeFormatted = formatted) }
    }

    /**
     * Generates a new phone number based on currently selected country and carrier
     */
    fun generateNumber(isWhatsAppOnlyOverride: Boolean? = null) {
        val whatsAppOnly = isWhatsAppOnlyOverride ?: _uiState.value.isWhatsAppOnlyMode
        val country = _uiState.value.selectedCountry
        val carrier = _uiState.value.selectedCarrier
        val result = CountryCarrierRepository.generatePhoneNumber(country, carrier, whatsAppOnly)

        viewModelScope.launch {
            val activeAudioTitle = _uiState.value.activeSoundTitle
            val entity = GeneratedNumberEntity(
                formattedNumber = result.formattedNumber,
                rawNumber = result.rawNumber,
                countryName = result.country.name,
                countryCode = result.country.code,
                countryDialCode = result.country.dialCode,
                flag = result.country.flag,
                carrierName = result.carrier.name,
                carrierBadge = result.carrier.badgeText,
                carrierColor = result.carrier.brandColor,
                audioTitle = activeAudioTitle,
                audioFilePath = _uiState.value.recordedAudioFile?.absolutePath,
                audioDurationMs = _uiState.value.recordedDurationMs
            )
            val newId = dao.insertNumber(entity)
            _uiState.update {
                it.copy(
                    currentGeneratedResult = result,
                    currentEntityId = newId
                )
            }
        }
    }

    /**
     * Toggles WhatsApp Only mode on/off
     */
    fun toggleWhatsAppOnlyMode() {
        val newMode = !_uiState.value.isWhatsAppOnlyMode
        _uiState.update {
            it.copy(
                isWhatsAppOnlyMode = newMode,
                userNotice = if (newMode) "WhatsApp Numbers Only mode ACTIVATED" else "Standard Telecom mode restored"
            )
        }
        generateNumber(isWhatsAppOnlyOverride = newMode)
    }

    /**
     * Dedicated button action: "WhatsApp Generator Numbers Only"
     */
    fun generateWhatsAppNumberOnly() {
        _uiState.update {
            it.copy(
                isWhatsAppOnlyMode = true,
                userNotice = "Generated WhatsApp-compatible mobile number!"
            )
        }
        generateNumber(isWhatsAppOnlyOverride = true)
    }

    fun setCustomWhatsAppAudioLink(link: String) {
        _uiState.update { it.copy(customWhatsAppAudioLink = link) }
    }

    fun setCustomWhatsAppVideoLink(link: String) {
        _uiState.update { it.copy(customWhatsAppVideoLink = link) }
    }

    fun setCustomWhatsAppMessageText(text: String) {
        _uiState.update { it.copy(customWhatsAppMessageText = text) }
    }

    fun setApkInstallDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showApkInstallDialog = visible) }
    }

    fun startRecordingAudio() {
        audioCallManager.stopPlayback()
        _uiState.update { it.copy(isPreviewPlaying = false) }

        val started = audioCallManager.startRecording(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isRecording = true,
                        recordingTimeSeconds = 0,
                        audioAmplitudes = emptyList()
                    )
                }
                startRecordingMonitoring()
            },
            onError = { err ->
                _uiState.update { it.copy(userNotice = err) }
            }
        )
        if (!started) {
            _uiState.update { it.copy(userNotice = "Could not access microphone.") }
        }
    }

    private fun startRecordingMonitoring() {
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            var seconds = 0
            while (isActive && _uiState.value.isRecording) {
                delay(200)
                val amp = (audioCallManager.getMaxAmplitude() / 32767f).coerceIn(0.1f, 1.0f)
                _uiState.update { current ->
                    val list = (current.audioAmplitudes + amp).takeLast(25)
                    current.copy(audioAmplitudes = list)
                }
                delay(800)
                seconds++
                _uiState.update { it.copy(recordingTimeSeconds = seconds) }
            }
        }
    }

    fun stopRecordingAudio() {
        recordingJob?.cancel()
        val file = audioCallManager.stopRecording()
        val durationMs = (_uiState.value.recordingTimeSeconds * 1000L).coerceAtLeast(2000L)
        val sizeStr = if (file != null) audioCallManager.formatStorageSize(file.length()) else "0 KB"

        _uiState.update {
            it.copy(
                audioSourceType = AudioSourceType.RECORDED,
                isRecording = false,
                recordedAudioFile = file,
                recordedDurationMs = durationMs,
                selectedPreset = null,
                audioStorageSizeFormatted = sizeStr,
                userNotice = "Voice recorded (${_uiState.value.recordingTimeSeconds}s, $sizeStr compact)!"
            )
        }
    }

    fun clearCustomAudio() {
        audioCallManager.stopPlayback()
        _uiState.update {
            it.copy(
                audioSourceType = AudioSourceType.PRESET,
                selectedPreset = audioCallManager.audioPresets.firstOrNull(),
                recordedAudioFile = null,
                recordedDurationMs = 0L,
                isPreviewPlaying = false,
                audioStorageSizeFormatted = "0 KB (Zero-Storage Preset)",
                userNotice = "Reset to preset telecom audio."
            )
        }
    }

    /**
     * 5-Second Ring Delivery Assurance Call:
     * Rings for 5.0 seconds with authentic ringback tone & vibration, then automatically terminates.
     */
    fun start5SecondDeliveryCall(entity: GeneratedNumberEntity? = null) {
        endCallSimulation()

        val activeEntity = entity ?: run {
            val res = _uiState.value.currentGeneratedResult ?: return
            GeneratedNumberEntity(
                id = _uiState.value.currentEntityId ?: 0L,
                formattedNumber = res.formattedNumber,
                rawNumber = res.rawNumber,
                countryName = res.country.name,
                countryCode = res.country.code,
                countryDialCode = res.country.dialCode,
                flag = res.country.flag,
                carrierName = res.carrier.name,
                carrierBadge = res.carrier.badgeText,
                carrierColor = res.carrier.brandColor,
                audioTitle = _uiState.value.activeSoundTitle,
                audioFilePath = _uiState.value.recordedAudioFile?.absolutePath,
                audioDurationMs = _uiState.value.recordedDurationMs
            )
        }

        _uiState.update {
            it.copy(
                isInCallDialog = true,
                callPhase = CallPhase.RINGING_5S,
                ringRemainingSeconds = 5.0f,
                deliveryEnsured = false,
                lastDeliveryMessage = null,
                callDurationSeconds = 0,
                keypadInput = ""
            )
        }

        ringingSession = audioCallManager.startRingTone()

        callJob?.cancel()
        callJob = viewModelScope.launch {
            val totalSteps = 50 // 5.0s in 100ms intervals
            for (step in 1..totalSteps) {
                delay(100)
                val remaining = (5.0f - (step * 0.1f)).coerceAtLeast(0.0f)
                _uiState.update { it.copy(ringRemainingSeconds = remaining) }
            }

            // Exactly 5 seconds passed: Stop ring and automatically end call!
            ringingSession?.stop()
            audioCallManager.playCallEndedTone()

            val successMsg = "Call ended automatically at 5s. Delivery Ensured!"
            _uiState.update {
                it.copy(
                    callPhase = CallPhase.ENDED,
                    deliveryEnsured = true,
                    lastDeliveryMessage = successMsg
                )
            }

            if (activeEntity.id > 0) {
                dao.updateDeliveryStatus(activeEntity.id, "✅ 5s Ring Delivered")
            }

            delay(2500)
            _uiState.update { it.copy(isInCallDialog = false) }
        }
    }

    /**
     * Normal Call with Audio:
     * Rings for 1.5s, then answers and plays the user's attached sound in normal call mode!
     */
    fun startCallAndHearAudio(entity: GeneratedNumberEntity? = null) {
        endCallSimulation()

        val filePath = entity?.audioFilePath ?: _uiState.value.recordedAudioFile?.absolutePath
        val presetId = _uiState.value.selectedPreset?.id
        val ttsText = if (_uiState.value.audioSourceType == AudioSourceType.TTS) _uiState.value.customTtsText else null

        _uiState.update {
            it.copy(
                isInCallDialog = true,
                callPhase = CallPhase.RINGING_5S,
                ringRemainingSeconds = 2.0f,
                callDurationSeconds = 0,
                deliveryEnsured = false,
                keypadInput = ""
            )
        }

        ringingSession = audioCallManager.startRingTone()

        callJob?.cancel()
        callJob = viewModelScope.launch {
            // Ring for 2 seconds then answer
            delay(2000)
            ringingSession?.stop()

            // Call connected!
            _uiState.update {
                it.copy(
                    callPhase = CallPhase.CONNECTED,
                    deliveryEnsured = true
                )
            }

            // Play the attached audio message through the normal call audio channel
            audioCallManager.playAudio(
                filePath = filePath,
                presetId = presetId,
                ttsText = ttsText,
                onCompletion = {
                    // When audio message finishes
                    _uiState.update { it.copy(lastDeliveryMessage = "Voice message transmission completed.") }
                }
            )

            // Track call duration
            var duration = 0
            while (isActive && _uiState.value.callPhase == CallPhase.CONNECTED) {
                delay(1000)
                duration++
                _uiState.update { it.copy(callDurationSeconds = duration) }
            }
        }
    }

    fun endCallSimulation() {
        callJob?.cancel()
        ringingSession?.stop()
        audioCallManager.stopPlayback()
        audioCallManager.playCallEndedTone()

        _uiState.update {
            it.copy(
                callPhase = CallPhase.ENDED,
                isInCallDialog = false
            )
        }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleKeypad() {
        _uiState.update { it.copy(isKeypadOpen = !it.isKeypadOpen) }
    }

    fun pressKeypadDigit(digit: Char) {
        audioCallManager.playDtmf(digit)
        _uiState.update { it.copy(keypadInput = it.keypadInput + digit) }
    }

    fun copyToClipboard(number: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated Phone Number", number)
        clipboard.setPrimaryClip(clip)
        _uiState.update { it.copy(userNotice = "Copied: $number") }
    }

    fun getDialerIntent(rawNumber: String): Intent {
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(rawNumber)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun startPhoneRingAssistant() {
        assistantJob?.cancel()
        _uiState.update {
            it.copy(
                phoneRingAssistantRunning = true,
                phoneRingAssistantCountdown = 5,
                userNotice = "Phone Ring Assistant Started: Let phone ring for 5 seconds!"
            )
        }

        assistantJob = viewModelScope.launch {
            for (sec in 5 downTo 1) {
                _uiState.update { it.copy(phoneRingAssistantCountdown = sec) }
                delay(1000)
            }
            // Finished 5 seconds!
            audioCallManager.playCallEndedTone()
            _uiState.update {
                it.copy(
                    phoneRingAssistantRunning = false,
                    phoneRingAssistantCountdown = 0,
                    userNotice = "5 Seconds Reached! Hang up now to complete delivery verification."
                )
            }
        }
    }

    fun cancelPhoneRingAssistant() {
        assistantJob?.cancel()
        _uiState.update { it.copy(phoneRingAssistantRunning = false) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }

    fun deleteHistoryItem(item: GeneratedNumberEntity) {
        viewModelScope.launch {
            dao.deleteNumber(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.deleteAll()
            _uiState.update { it.copy(userNotice = "History cleared") }
        }
    }

    private fun startWorldClockTicker() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                updateWorldTimes()
                delay(1000)
            }
        }
    }

    private fun updateWorldTimes() {
        val now = Date()
        val items = CountryCarrierRepository.allCountries.map { country ->
            val tz = TimeZone.getTimeZone(country.timezoneId)
            
            val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.US).apply {
                timeZone = tz
            }
            val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.US).apply {
                timeZone = tz
            }
            val hourFormat = SimpleDateFormat("H", Locale.US).apply {
                timeZone = tz
            }

            val hour24 = hourFormat.format(now).toIntOrNull() ?: 12
            val isRecommended = hour24 in 9..20
            val statusText = when {
                hour24 in 9..19 -> "Recommended Calling Time"
                hour24 in 20..22 -> "Evening / Off-Peak"
                else -> "Night Time (Rest Hours)"
            }

            val offsetMillis = tz.getOffset(now.time)
            val offsetHours = offsetMillis / (1000 * 60 * 60)
            val gmtOffset = "GMT" + (if (offsetHours >= 0) "+$offsetHours" else "$offsetHours")

            WorldTimeItem(
                country = country,
                timeFormatted = timeFormat.format(now),
                dateFormatted = dateFormat.format(now),
                gmtOffset = gmtOffset,
                isRecommendedCallTime = isRecommended,
                timeStatusText = statusText
            )
        }

        _uiState.update { it.copy(worldTimes = items) }
    }

    override fun onCleared() {
        super.onCleared()
        audioCallManager.release()
    }
}
