package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ScanRepository(
    private val scanDao: ScanDao,
    private val context: Context
) {
    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()

    suspend fun saveScan(
        objectName: String,
        category: String,
        question: String,
        answer: String,
        usefulTips: String,
        confidence: Float,
        bitmap: Bitmap?,
        sampleResId: Int? = null
    ): Long = withContext(Dispatchers.IO) {
        var filePath: String? = null
        if (bitmap != null) {
            filePath = saveBitmapToInternalStorage(bitmap)
        }

        val scan = ScanEntity(
            objectName = objectName,
            category = category,
            question = question,
            answer = answer,
            usefulTips = usefulTips,
            confidence = confidence,
            imageFilePath = filePath,
            sampleResId = sampleResId
        )
        scanDao.insertScan(scan)
    }

    suspend fun deleteScan(id: Long) = withContext(Dispatchers.IO) {
        scanDao.deleteScanById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        scanDao.clearAllScans()
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        val fileName = "scan_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    suspend fun loadBitmap(filePath: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (filePath == null) return@withContext null
        try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
