package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiVisualAssistantEngine
import com.example.ai.ChatMessage
import com.example.ai.MessageSender
import com.example.ai.NearbyPlace
import com.example.ai.SampleScene
import com.example.ai.VisualAnalysisResult
import com.example.data.AppDatabase
import com.example.data.ScanEntity
import com.example.data.ScanRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ScreenRoute {
    HOME, SCAN, ASK_AI, AROUND_ME, HISTORY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ScanRepository(db.scanDao(), application)
    private val aiEngine = AiVisualAssistantEngine()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Navigation State
    private val _currentRoute = MutableStateFlow(ScreenRoute.HOME)
    val currentRoute: StateFlow<ScreenRoute> = _currentRoute.asStateFlow()

    // Scan State
    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private val _selectedSample = MutableStateFlow<SampleScene?>(aiEngine.sampleScenes.first())
    val selectedSample: StateFlow<SampleScene?> = _selectedSample.asStateFlow()

    val sampleScenes: List<SampleScene> = aiEngine.sampleScenes

    private val _scanQuestion = MutableStateFlow("What is this and what can it be used for?")
    val scanQuestion: StateFlow<String> = _scanQuestion.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<VisualAnalysisResult?>(null)
    val analysisResult: StateFlow<VisualAnalysisResult?> = _analysisResult.asStateFlow()

    private val _scanSavedMessage = MutableStateFlow<String?>(null)
    val scanSavedMessage: StateFlow<String?> = _scanSavedMessage.asStateFlow()

    // Ask AI State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.AI,
                content = "Hello! I am your Reality Layer visual AI assistant. Ask me anything about physical objects, materials, plant species, architectural styles, or urban landmarks."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _askAiInput = MutableStateFlow("")
    val askAiInput: StateFlow<String> = _askAiInput.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _isListeningVoice = MutableStateFlow(false)
    val isListeningVoice: StateFlow<Boolean> = _isListeningVoice.asStateFlow()

    // Around Me State
    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _nearbyPlaces = MutableStateFlow<List<NearbyPlace>>(emptyList())
    val nearbyPlaces: StateFlow<List<NearbyPlace>> = _nearbyPlaces.asStateFlow()

    // History Scans Flow from Room
    val historyScans: StateFlow<List<ScanEntity>> = repository.allScans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedHistoryScan = MutableStateFlow<ScanEntity?>(null)
    val selectedHistoryScan: StateFlow<ScanEntity?> = _selectedHistoryScan.asStateFlow()

    fun navigateTo(route: ScreenRoute) {
        _currentRoute.value = route
    }

    fun onScanQuestionChange(newQuestion: String) {
        _scanQuestion.value = newQuestion
    }

    fun selectSampleScene(scene: SampleScene) {
        _selectedSample.value = scene
        _capturedBitmap.value = null
        _scanQuestion.value = scene.defaultQuestion
        _analysisResult.value = null
    }

    fun onBitmapCaptured(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
        _selectedSample.value = null
        _analysisResult.value = null
    }

    fun analyzeCurrentScan() {
        if (_isAnalyzing.value) return
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = aiEngine.analyzeImage(
                    bitmap = _capturedBitmap.value,
                    userQuestion = _scanQuestion.value,
                    selectedSample = _selectedSample.value
                )
                _analysisResult.value = result

                // Auto save scan to local Room database for History
                repository.saveScan(
                    objectName = result.objectName,
                    category = result.category,
                    question = _scanQuestion.value,
                    answer = result.summaryAnswer,
                    usefulTips = result.usefulTips.joinToString(" • "),
                    confidence = result.confidence,
                    bitmap = _capturedBitmap.value,
                    sampleResId = _selectedSample.value?.drawableRes
                )
                _scanSavedMessage.value = "Scan saved to History"
            } catch (e: Exception) {
                // handle gracefully
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearSavedMessage() {
        _scanSavedMessage.value = null
    }

    // Ask AI functions
    fun onAskAiInputChange(text: String) {
        _askAiInput.value = text
    }

    fun submitAskAiQuestion(customQuestion: String? = null) {
        val q = customQuestion ?: _askAiInput.value.trim()
        if (q.isBlank() || _isAiThinking.value) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            content = q
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _askAiInput.value = ""

        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                val response = aiEngine.answerGeneralAiQuestion(q)
                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    content = response
                )
                _chatMessages.value = _chatMessages.value + aiMsg
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun setVoiceListening(listening: Boolean) {
        _isListeningVoice.value = listening
    }

    fun onVoiceInputReceived(spokenText: String) {
        _isListeningVoice.value = false
        if (spokenText.isNotBlank()) {
            _askAiInput.value = spokenText
            submitAskAiQuestion(spokenText)
        }
    }

    // Around Me functions
    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
        if (granted) {
            fetchNearbyPlaces()
        }
    }

    fun fetchNearbyPlaces() {
        _isLoadingLocation.value = true
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    _currentLocation.value = location
                    _nearbyPlaces.value = aiEngine.getNearbyPlaces(
                        latitude = location?.latitude,
                        longitude = location?.longitude
                    )
                    _isLoadingLocation.value = false
                }
                .addOnFailureListener {
                    // Fallback to default coordinates
                    _nearbyPlaces.value = aiEngine.getNearbyPlaces(null, null)
                    _isLoadingLocation.value = false
                }
        } catch (e: SecurityException) {
            _nearbyPlaces.value = aiEngine.getNearbyPlaces(null, null)
            _isLoadingLocation.value = false
        }
    }

    // History functions
    fun selectHistoryItem(scan: ScanEntity?) {
        _selectedHistoryScan.value = scan
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
            if (_selectedHistoryScan.value?.id == id) {
                _selectedHistoryScan.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedHistoryScan.value = null
        }
    }
}
