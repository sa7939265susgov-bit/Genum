package com.example.data.model

import androidx.compose.ui.graphics.Color

data class Carrier(
    val id: String,
    val name: String,
    val prefixes: List<String>,
    val totalDigits: Int, // Total digits including prefix, excluding country code
    val brandColor: Long,
    val badgeText: String,
    val description: String
)

data class Country(
    val id: String,
    val name: String,
    val code: String,
    val dialCode: String,
    val flag: String,
    val timezoneId: String,
    val carriers: List<Carrier>
)

object CountryCarrierRepository {
    // Bahrain carriers specifically highlighted as requested
    val batelcoCarrier = Carrier(
        id = "batelco",
        name = "Batelco",
        prefixes = listOf("39", "38", "17"),
        totalDigits = 8,
        brandColor = 0xFFD81B60, // Batelco magenta/red
        badgeText = "Batelco 4G/5G",
        description = "Bahrain Telecommunications Company (Batelco)"
    )

    val stcBahrainCarrier = Carrier(
        id = "stc_bh",
        name = "stc Bahrain",
        prefixes = listOf("33", "34", "35"),
        totalDigits = 8,
        brandColor = 0xFF4F008C, // stc vibrant purple
        badgeText = "stc BH 5G",
        description = "stc Bahrain High-Speed Network"
    )

    val zainBahrainCarrier = Carrier(
        id = "zain_bh",
        name = "Zain Bahrain",
        prefixes = listOf("36", "37"),
        totalDigits = 8,
        brandColor = 0xFF00897B, // Zain teal/turquoise
        badgeText = "Zain BH 5G",
        description = "Zain Bahrain Mobile Telecommunications"
    )

    val bahrain = Country(
        id = "BH",
        name = "Bahrain",
        code = "BH",
        dialCode = "+973",
        flag = "🇧🇭",
        timezoneId = "Asia/Bahrain",
        carriers = listOf(batelcoCarrier, stcBahrainCarrier, zainBahrainCarrier)
    )

    val saudiArabia = Country(
        id = "SA",
        name = "Saudi Arabia",
        code = "SA",
        dialCode = "+966",
        flag = "🇸🇦",
        timezoneId = "Asia/Riyadh",
        carriers = listOf(
            Carrier("stc_sa", "stc KSA", listOf("50", "53", "55"), 9, 0xFF4F008C, "stc KSA", "stc Saudi Arabia"),
            Carrier("mobily_sa", "Mobily", listOf("54", "56"), 9, 0xFF0083CA, "Mobily", "Etihad Etisalat Mobily"),
            Carrier("zain_sa", "Zain KSA", listOf("58", "59"), 9, 0xFF00897B, "Zain KSA", "Zain Saudi Arabia")
        )
    )

    val uae = Country(
        id = "AE",
        name = "United Arab Emirates",
        code = "AE",
        dialCode = "+971",
        flag = "🇦🇪",
        timezoneId = "Asia/Dubai",
        carriers = listOf(
            Carrier("etisalat_ae", "e& (Etisalat)", listOf("50", "54", "56"), 9, 0xFF719E19, "e& UAE", "Emirates Telecommunications"),
            Carrier("du_ae", "du", listOf("52", "55", "58"), 9, 0xFF00A3E0, "du Telecom", "du Emirates")
        )
    )

    val kuwait = Country(
        id = "KW",
        name = "Kuwait",
        code = "KW",
        dialCode = "+965",
        flag = "🇰🇼",
        timezoneId = "Asia/Kuwait",
        carriers = listOf(
            Carrier("zain_kw", "Zain Kuwait", listOf("90", "94", "97"), 8, 0xFF00897B, "Zain KW", "Zain Mobile Telecom"),
            Carrier("ooredoo_kw", "Ooredoo Kuwait", listOf("60", "65", "66"), 8, 0xFFED1C24, "Ooredoo KW", "Ooredoo Kuwait Network"),
            Carrier("stc_kw", "stc Kuwait", listOf("50", "51", "55"), 8, 0xFF4F008C, "stc KW", "stc Kuwait")
        )
    )

    val qatar = Country(
        id = "QA",
        name = "Qatar",
        code = "QA",
        dialCode = "+974",
        flag = "🇶🇦",
        timezoneId = "Asia/Qatar",
        carriers = listOf(
            Carrier("ooredoo_qa", "Ooredoo Qatar", listOf("33", "55", "66"), 8, 0xFFED1C24, "Ooredoo QA", "Ooredoo Qatar"),
            Carrier("vodafone_qa", "Vodafone Qatar", listOf("70", "77"), 8, 0xFFE60000, "Vodafone QA", "Vodafone Qatar")
        )
    )

    val oman = Country(
        id = "OM",
        name = "Oman",
        code = "OM",
        dialCode = "+968",
        flag = "🇴🇲",
        timezoneId = "Asia/Muscat",
        carriers = listOf(
            Carrier("omantel", "Omantel", listOf("91", "92", "99"), 8, 0xFFFF6600, "Omantel", "Oman Telecommunications"),
            Carrier("ooredoo_om", "Ooredoo Oman", listOf("94", "95", "96"), 8, 0xFFED1C24, "Ooredoo OM", "Ooredoo Oman")
        )
    )

    val usa = Country(
        id = "US",
        name = "United States",
        code = "US",
        dialCode = "+1",
        flag = "🇺🇸",
        timezoneId = "America/New_York",
        carriers = listOf(
            Carrier("att_us", "AT&T", listOf("212", "310", "415", "646"), 10, 0xFF00A8E0, "AT&T US", "AT&T Wireless"),
            Carrier("verizon_us", "Verizon", listOf("206", "312", "702", "917"), 10, 0xFFCD040B, "Verizon US", "Verizon 5G Ultra"),
            Carrier("tmobile_us", "T-Mobile", listOf("404", "512", "718", "832"), 10, 0xFFE20074, "T-Mobile US", "T-Mobile USA")
        )
    )

    val uk = Country(
        id = "GB",
        name = "United Kingdom",
        code = "GB",
        dialCode = "+44",
        flag = "🇬🇧",
        timezoneId = "Europe/London",
        carriers = listOf(
            Carrier("vodafone_uk", "Vodafone UK", listOf("7710", "7720", "7831"), 10, 0xFFE60000, "Vodafone UK", "Vodafone UK Mobile"),
            Carrier("ee_uk", "EE", listOf("7900", "7940", "7970"), 10, 0xFF007B85, "EE UK", "EE 5G Network"),
            Carrier("o2_uk", "O2 UK", listOf("7700", "7730", "7850"), 10, 0xFF032B80, "O2 UK", "Virgin Media O2")
        )
    )

    val india = Country(
        id = "IN",
        name = "India",
        code = "IN",
        dialCode = "+91",
        flag = "🇮🇳",
        timezoneId = "Asia/Kolkata",
        carriers = listOf(
            Carrier("jio_in", "Jio", listOf("7000", "7001", "7002"), 10, 0xFF0F3BBE, "Jio 5G", "Reliance Jio Infocomm"),
            Carrier("airtel_in", "Airtel", listOf("9810", "9820", "9830"), 10, 0xFFE40000, "Airtel", "Bharti Airtel India")
        )
    )

    val egypt = Country(
        id = "EG",
        name = "Egypt",
        code = "EG",
        dialCode = "+20",
        flag = "🇪🇬",
        timezoneId = "Africa/Cairo",
        carriers = listOf(
            Carrier("vodafone_eg", "Vodafone Egypt", listOf("10"), 10, 0xFFE60000, "Vodafone EG", "Vodafone Egypt"),
            Carrier("orange_eg", "Orange Egypt", listOf("12"), 10, 0xFFFF7900, "Orange EG", "Orange Egypt Network"),
            Carrier("etisalat_eg", "Etisalat Misr", listOf("11"), 10, 0xFF719E19, "Etisalat EG", "Etisalat Misr")
        )
    )

    val jordan = Country(
        id = "JO",
        name = "Jordan",
        code = "JO",
        dialCode = "+962",
        flag = "🇯🇴",
        timezoneId = "Asia/Amman",
        carriers = listOf(
            Carrier("zain_jo", "Zain Jordan", listOf("79"), 9, 0xFF00897B, "Zain JO", "Zain Jordan"),
            Carrier("orange_jo", "Orange Jordan", listOf("77"), 9, 0xFFFF7900, "Orange JO", "Orange Telecom Jordan")
        )
    )

    val germany = Country(
        id = "DE",
        name = "Germany",
        code = "DE",
        dialCode = "+49",
        flag = "🇩🇪",
        timezoneId = "Europe/Berlin",
        carriers = listOf(
            Carrier("telekom_de", "Deutsche Telekom", listOf("151", "160", "170"), 10, 0xFFE20074, "Telekom DE", "Deutsche Telekom"),
            Carrier("vodafone_de", "Vodafone DE", listOf("152", "162", "172"), 10, 0xFFE60000, "Vodafone DE", "Vodafone Deutschland")
        )
    )

    val france = Country(
        id = "FR",
        name = "France",
        code = "FR",
        dialCode = "+33",
        flag = "🇫🇷",
        timezoneId = "Europe/Paris",
        carriers = listOf(
            Carrier("orange_fr", "Orange France", listOf("6", "7"), 9, 0xFFFF7900, "Orange FR", "Orange France"),
            Carrier("sfr_fr", "SFR", listOf("6", "7"), 9, 0xFFE2001A, "SFR", "SFR Mobile")
        )
    )

    val japan = Country(
        id = "JP",
        name = "Japan",
        code = "JP",
        dialCode = "+81",
        flag = "🇯🇵",
        timezoneId = "Asia/Tokyo",
        carriers = listOf(
            Carrier("docomo_jp", "NTT DOCOMO", listOf("70", "80", "90"), 10, 0xFFCC0000, "DOCOMO", "NTT DOCOMO Japan"),
            Carrier("softbank_jp", "SoftBank", listOf("70", "80", "90"), 10, 0xFF888888, "SoftBank", "SoftBank Mobile")
        )
    )

    val australia = Country(
        id = "AU",
        name = "Australia",
        code = "AU",
        dialCode = "+61",
        flag = "🇦🇺",
        timezoneId = "Australia/Sydney",
        carriers = listOf(
            Carrier("telstra_au", "Telstra", listOf("400", "410", "420"), 9, 0xFF0051FF, "Telstra", "Telstra 5G Australia"),
            Carrier("optus_au", "Optus", listOf("401", "411", "421"), 9, 0xFF00918E, "Optus", "Optus Mobile Australia")
        )
    )

    val allCountries: List<Country> = listOf(
        bahrain,
        saudiArabia,
        uae,
        kuwait,
        qatar,
        oman,
        usa,
        uk,
        india,
        egypt,
        jordan,
        germany,
        france,
        japan,
        australia
    )

    /**
     * Generates a realistic phone number for the given country and carrier.
     */
    fun generatePhoneNumber(
        country: Country,
        carrier: Carrier,
        isWhatsAppOnly: Boolean = false
    ): GeneratedNumberResult {
        // In WhatsApp-only mode, filter out fixed landlines (like Bahrain 17 landline)
        val validPrefixes = if (isWhatsAppOnly) {
            val filtered = carrier.prefixes.filter { prefix ->
                // Filter out non-mobile prefixes
                if (country.code == "BH") prefix != "17" else true
            }
            if (filtered.isNotEmpty()) filtered else carrier.prefixes
        } else {
            carrier.prefixes
        }

        val prefix = validPrefixes.random()
        val remainingLength = carrier.totalDigits - prefix.length
        val randomDigits = StringBuilder()
        repeat(remainingLength) {
            randomDigits.append((0..9).random())
        }
        val nationalNumber = "$prefix$randomDigits"
        val rawInternational = "${country.dialCode}$nationalNumber"

        // Format nicely with spaces
        val formatted = when {
            country.code == "BH" -> {
                // e.g. +973 39 123 456
                "${country.dialCode} $prefix ${nationalNumber.substring(prefix.length, prefix.length + 3)} ${nationalNumber.substring(prefix.length + 3)}"
            }
            country.code in listOf("SA", "AE", "KW", "QA", "OM") -> {
                // e.g. +966 50 123 4567
                val mid = prefix.length + 3
                if (mid < nationalNumber.length) {
                    "${country.dialCode} $prefix ${nationalNumber.substring(prefix.length, mid)} ${nationalNumber.substring(mid)}"
                } else {
                    "${country.dialCode} $prefix ${nationalNumber.substring(prefix.length)}"
                }
            }
            country.code == "US" -> {
                // e.g. +1 (212) 555-1234
                val areaCode = prefix
                val subscriber = nationalNumber.substring(prefix.length)
                val p1 = if (subscriber.length >= 3) subscriber.substring(0, 3) else subscriber
                val p2 = if (subscriber.length > 3) subscriber.substring(3) else ""
                "${country.dialCode} ($areaCode) $p1-$p2"
            }
            else -> {
                "${country.dialCode} $prefix ${nationalNumber.substring(prefix.length)}"
            }
        }

        return GeneratedNumberResult(
            formattedNumber = formatted,
            rawNumber = rawInternational,
            country = country,
            carrier = carrier,
            isWhatsAppVerified = isWhatsAppOnly || (country.code == "BH" && prefix != "17")
        )
    }
}

data class GeneratedNumberResult(
    val formattedNumber: String,
    val rawNumber: String,
    val country: Country,
    val carrier: Carrier,
    val isWhatsAppVerified: Boolean = true
)
