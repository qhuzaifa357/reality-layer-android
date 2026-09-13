package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val objectName: String,
    val category: String,
    val question: String,
    val answer: String,
    val usefulTips: String = "",
    val confidence: Float = 0.95f,
    val imageFilePath: String? = null,
    val sampleResId: Int? = null
)
