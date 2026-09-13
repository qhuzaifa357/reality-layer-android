package com.example.ai

import androidx.annotation.DrawableRes

data class VisualAnalysisResult(
    val objectName: String,
    val category: String,
    val confidence: Float,
    val summaryAnswer: String,
    val primaryUse: String,
    val usefulTips: List<String>,
    val keyFacts: List<String>,
    val suggestedQuestions: List<String>
)

data class SampleScene(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    @DrawableRes val drawableRes: Int? = null,
    val defaultQuestion: String = "What is this and how does it work?"
)

data class NearbyPlace(
    val id: String,
    val name: String,
    val category: String,
    val distanceMeters: Int,
    val description: String,
    val visualSignature: String,
    val tips: String
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}
