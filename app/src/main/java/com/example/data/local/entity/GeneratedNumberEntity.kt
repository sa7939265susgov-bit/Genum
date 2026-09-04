package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_numbers")
data class GeneratedNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val formattedNumber: String,
    val rawNumber: String,
    val countryName: String,
    val countryCode: String,
    val countryDialCode: String,
    val flag: String,
    val carrierName: String,
    val carrierBadge: String,
    val carrierColor: Long,
    val audioTitle: String?,
    val audioFilePath: String?,
    val audioDurationMs: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val ringDurationSeconds: Int = 5,
    val deliveryStatus: String = "Pending", // "5s Ring Delivered", "Connected Call", "Generated"
    val callAttempts: Int = 0
)
