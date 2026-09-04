package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

data class LinkStatusInfo(
    val rawUrl: String,
    val isValid: Boolean,
    val scheme: String,
    val appName: String,
    val statusDescription: String,
    val isAppInstalled: Boolean? = null
)

object AppLinksManager {

    /**
     * Clean phone number digits for URL schemes
     */
    fun sanitizeNumberForUrl(rawNumber: String): String {
        return rawNumber.replace("[^0-9]".toRegex(), "")
    }

    /**
     * Analyze and inspect any user input link to provide real-time status
     */
    fun analyzeLink(context: Context, url: String): LinkStatusInfo {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            return LinkStatusInfo(
                rawUrl = "",
                isValid = false,
                scheme = "none",
                appName = "None",
                statusDescription = "Enter or paste an app or video call link"
            )
        }

        val uri = try {
            Uri.parse(trimmed)
        } catch (_: Exception) {
            null
        }

        if (uri == null || uri.scheme.isNullOrBlank()) {
            return LinkStatusInfo(
                rawUrl = trimmed,
                isValid = false,
                scheme = "unknown",
                appName = "Invalid Format",
                statusDescription = "Missing URL scheme (e.g. https://, whatsapp://, zoomus://)"
            )
        }

        val scheme = uri.scheme!!.lowercase()
        val host = uri.host?.lowercase().orEmpty()

        val (appName, targetPackage) = when {
            host.contains("youtube.com") || host.contains("youtu.be") -> "YouTube" to "com.google.android.youtube"
            scheme == "whatsapp" || host.contains("wa.me") || host.contains("whatsapp.com") -> "WhatsApp" to "com.whatsapp"
            scheme == "zoomus" || host.contains("zoom.us") -> "Zoom Video Meetings" to "us.zoom.videomeetings"
            host.contains("meet.google.com") -> "Google Meet" to "com.google.android.apps.meetings"
            scheme == "skype" || host.contains("skype.com") -> "Skype Video Call" to "com.skype.raider"
            scheme == "tg" || host.contains("t.me") || host.contains("telegram.me") -> "Telegram" to "org.telegram.messenger"
            scheme == "viber" || host.contains("viber.com") -> "Viber" to "com.viber.voip"
            scheme == "sgnl" || host.contains("signal.me") -> "Signal" to "org.thoughtcrime.securesms"
            scheme == "tel" -> "Phone Dialer" to null
            scheme == "smsto" || scheme == "sms" -> "SMS Messenger" to null
            host.contains("meet.jit.si") -> "Jitsi WebRTC Video" to null
            scheme == "https" || scheme == "http" -> "Web Browser / Cloud App" to null
            else -> "Custom Scheme ($scheme)" to null
        }

        val isInstalled = if (targetPackage != null) {
            try {
                context.packageManager.getPackageInfo(targetPackage, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        } else {
            null
        }

        val statusText = when {
            isInstalled == true -> "Verified active app on device ($appName)"
            isInstalled == false -> "App not detected on device; will launch via browser fallback"
            scheme in listOf("tel", "smsto", "sms") -> "System Telephony/SMS Protocol Ready"
            scheme in listOf("https", "http") -> "Universal Web / Mobile Link Ready"
            else -> "Protocol '$scheme' detected and ready to dispatch"
        }

        return LinkStatusInfo(
            rawUrl = trimmed,
            isValid = true,
            scheme = scheme,
            appName = appName,
            statusDescription = statusText,
            isAppInstalled = isInstalled
        )
    }

    /**
     * Dispatch any custom link or URL entered by the user
     */
    fun launchCustomLink(context: Context, url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            Toast.makeText(context, "Please enter a link first", Toast.LENGTH_SHORT).show()
            return false
        }

        // Auto-prefix if missing scheme
        val finalUrl = if (!trimmed.contains("://") && !trimmed.startsWith("tel:") && !trimmed.startsWith("smsto:") && !trimmed.startsWith("skype:")) {
            "https://$trimmed"
        } else {
            trimmed
        }

        val uri = try {
            Uri.parse(finalUrl)
        } catch (e: Exception) {
            Toast.makeText(context, "Invalid link syntax", Toast.LENGTH_SHORT).show()
            return false
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            Toast.makeText(context, "Opening link...", Toast.LENGTH_SHORT).show()
            true
        } catch (_: Exception) {
            // Fallback: try web browser if custom scheme failed
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(finalUrl)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                true
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to launch link on this device", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    // ==========================================
    // Video Call Integrations
    // ==========================================

    /**
     * WhatsApp Video Call
     */
    fun startWhatsAppVideoCall(context: Context, rawNumber: String) {
        val digits = sanitizeNumberForUrl(rawNumber)
        // Primary: wa.me direct chat with video invite
        val url = if (digits.isNotBlank()) "https://wa.me/$digits?text=Starting%20Video%20Call%20via%20Gennum" else "https://api.whatsapp.com"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to web browser WhatsApp link
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Google Meet Video Call Room
     */
    fun startGoogleMeet(context: Context) {
        val meetUrl = "https://meet.google.com/new"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch Google Meet", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Zoom Video Call Meeting
     */
    fun startZoomMeeting(context: Context) {
        val zoomAppUri = Uri.parse("zoomus://zoom.us/join")
        val intent = Intent(Intent.ACTION_VIEW, zoomAppUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zoom.us/join")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open Zoom", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Skype Video Call
     */
    fun startSkypeVideo(context: Context, rawNumber: String) {
        val digits = sanitizeNumberForUrl(rawNumber)
        val skypeUri = if (digits.isNotBlank()) {
            Uri.parse("skype:+$digits?call&video=true")
        } else {
            Uri.parse("skype:?call&video=true")
        }
        val intent = Intent(Intent.ACTION_VIEW, skypeUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.skype.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Skype not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Cross-platform WebRTC Jitsi Video Call Room (100% free, no account needed)
     */
    fun startJitsiMeetVideoCall(context: Context, rawNumber: String) {
        val digits = sanitizeNumberForUrl(rawNumber).takeLast(6).ifBlank { "GennumRoom" }
        val roomUrl = "https://meet.jit.si/Gennum_Video_$digits"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(roomUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser for video call", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Telegram Video/Voice Call
     */
    fun startTelegramCall(context: Context) {
        val url = "https://t.me"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram not installed", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Viber Call
     */
    fun startViberCall(context: Context, rawNumber: String) {
        val digits = sanitizeNumberForUrl(rawNumber)
        val url = if (digits.isNotBlank()) "viber://chat?number=%2B$digits" else "viber://forward"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Viber not installed", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Signal Call
     */
    fun startSignalCall(context: Context, rawNumber: String) {
        val digits = sanitizeNumberForUrl(rawNumber)
        val url = if (digits.isNotBlank()) "https://signal.me/#p/+$digits" else "https://signal.org"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Signal", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // Standard Shortcuts & Links
    // ==========================================

    fun openYouTube(context: Context, query: String? = null) {
        val webUrl = if (query.isNullOrBlank()) {
            "https://www.youtube.com"
        } else {
            "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
        }
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
            setPackage("com.google.android.youtube")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(appIntent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open YouTube", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openWhatsApp(context: Context, rawNumber: String) {
        val cleanDigits = sanitizeNumberForUrl(rawNumber)
        val url = if (cleanDigits.isNotBlank()) {
            "https://wa.me/$cleanDigits"
        } else {
            "https://api.whatsapp.com/send"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun openTelegram(context: Context) {
        startTelegramCall(context)
    }

    fun openPhoneDialer(context: Context, number: String) {
        val dialUri = Uri.parse("tel:${Uri.encode(number)}")
        val intent = Intent(Intent.ACTION_DIAL, dialUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No phone dialer app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSms(context: Context, number: String) {
        val cleanDigits = sanitizeNumberForUrl(number)
        val smsUri = Uri.parse("smsto:$cleanDigits")
        val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
            putExtra("sms_body", "Hello from Gennum Phone Generator!")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchGoogle(context: Context, query: String) {
        val url = "https://www.google.com/search?q=${Uri.encode(query)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareNumber(context: Context, number: String, carrierName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Generated Number: $number ($carrierName)\nCreated with Gennum Phone Number Generator"
            )
            putExtra(Intent.EXTRA_SUBJECT, "Gennum Generated Number")
        }
        val chooser = Intent.createChooser(shareIntent, "Share Number via...").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No sharing app found", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launch WhatsApp with pre-composed payload containing the generated phone number,
     * the custom audio note / call link, and the video call room link.
     */
    fun openWhatsAppWithCallPayload(
        context: Context,
        rawNumber: String,
        audioLink: String,
        videoLink: String,
        customMessage: String
    ) {
        val digits = sanitizeNumberForUrl(rawNumber)
        val textBuilder = StringBuilder()
        if (customMessage.isNotBlank()) {
            textBuilder.append(customMessage.trim()).append("\n\n")
        }
        if (digits.isNotBlank()) {
            textBuilder.append("📞 Gennum Phone: +$digits\n")
        }
        if (audioLink.isNotBlank()) {
            textBuilder.append("🎙 Audio/Voice Call Link: ${audioLink.trim()}\n")
        }
        if (videoLink.isNotBlank()) {
            textBuilder.append("📹 Video Call Room: ${videoLink.trim()}\n")
        }
        textBuilder.append("\nTap the links above to connect to audio & video call!")

        val encoded = Uri.encode(textBuilder.toString())
        val waUrl = if (digits.isNotBlank()) {
            "https://wa.me/$digits?text=$encoded"
        } else {
            "https://wa.me/?text=$encoded"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            Toast.makeText(context, "Opening WhatsApp with number & audio/video links...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            // Fallback: regular browser or chooser
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Start WhatsApp direct voice call prompt for the given number
     */
    fun startWhatsAppVoiceCall(context: Context, rawNumber: String) {
        val digits = sanitizeNumberForUrl(rawNumber)
        val url = if (digits.isNotBlank()) "https://wa.me/$digits?text=Starting%20Voice%20Call%20via%20Gennum" else "https://api.whatsapp.com"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallback)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Triggers the Android Package Installer pop-out to install or update the app APK,
     * without having to type or search manually.
     */
    fun triggerApkInstallationFlow(context: Context) {
        val cacheApk = java.io.File(context.cacheDir, "app-debug.apk")
        val downloadsApk = java.io.File(context.getExternalFilesDir(null), "app-debug.apk")
        val targetApk = when {
            cacheApk.exists() && cacheApk.length() > 0 -> cacheApk
            downloadsApk.exists() && downloadsApk.length() > 0 -> downloadsApk
            else -> null
        }

        if (targetApk != null) {
            try {
                val apkUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    targetApk
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(installIntent)
                Toast.makeText(context, "Opening Android Package Installer...", Toast.LENGTH_LONG).show()
                return
            } catch (_: Exception) {
                // Continue to system prompt
            }
        }

        // Fallback: Open direct download in system browser/installer
        try {
            val downloadUri = Uri.parse("https://ais-dev-zmyjm3e63bwcwburdqx2pf-555314931699.europe-west2.run.app")
            val browserIntent = Intent(Intent.ACTION_VIEW, downloadUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            Toast.makeText(context, "Launching APK installer...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Installer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share the direct APK installation link or file to WhatsApp
     */
    fun shareApkToWhatsApp(context: Context, downloadUrl: String = "https://ais-dev-zmyjm3e63bwcwburdqx2pf-555314931699.europe-west2.run.app") {
        val message = "📲 *Gennum Android APK Installation Link*:\n$downloadUrl\n\nTap the link on your Android phone to download and install Gennum immediately!"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=${Uri.encode(message)}")).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }, "Share APK via...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }
}
