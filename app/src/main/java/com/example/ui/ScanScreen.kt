package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ai.VisualAnalysisResult
import com.example.ui.components.RealityCategoryChip
import com.example.ui.components.RealityTopBar
import com.example.ui.components.ScanningRadarLine
import com.example.ui.theme.RealityAccent
import com.example.ui.theme.RealityPrimary
import com.example.ui.theme.RealityPrimaryDark
import com.example.ui.theme.RealitySecondary
import com.example.ui.theme.RealitySuccess

@Composable
fun ScanScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val capturedBitmap by viewModel.capturedBitmap.collectAsState()
    val selectedSample by viewModel.selectedSample.collectAsState()
    val scanQuestion by viewModel.scanQuestion.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val scanSavedMessage by viewModel.scanSavedMessage.collectAsState()

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onBitmapCaptured(bitmap)
        }
    }

    // Permission Launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is needed to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Picker Launcher
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.onBitmapCaptured(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val quickQuestions = listOf(
        "What is this?",
        "What can it be used for?",
        "What useful info can I get?",
        "How do I care for or maintain it?",
        "What are its key characteristics?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("scan_screen")
    ) {
        RealityTopBar(
            title = "Scan & Analyze",
            subtitle = "Visual AI Object Inspector",
            onBackClick = { viewModel.navigateTo(ScreenRoute.HOME) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera & Input Selection Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                takePictureLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("open_camera_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RealityPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Photo",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Photo",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            pickMediaLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("pick_gallery_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RealityPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Upload Photo",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gallery",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Sample scenes row (so users in emulator can test multiple objects instantly)
            item {
                Column {
                    Text(
                        text = "Or choose a test object scene:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.sampleScenes) { sample ->
                            val isSelected = selectedSample?.id == sample.id && capturedBitmap == null
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectSampleScene(sample) },
                                label = {
                                    Text(
                                        text = sample.title,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RealityPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = RealityPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Image Preview Viewfinder Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(RealityPrimary.copy(alpha = 0.5f), RealityAccent.copy(alpha = 0.5f))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("captured_image_preview")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            capturedBitmap != null -> {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Captured Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            selectedSample?.drawableRes != null -> {
                                Image(
                                    painter = painterResource(id = selectedSample!!.drawableRes!!),
                                    contentDescription = selectedSample!!.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Capture a photo or pick a sample scene above",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // AR Focus Target Overlay UI
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Top left corner marker
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopStart)
                                    .border(
                                        width = 3.dp,
                                        color = RealityAccent,
                                        shape = RoundedCornerShape(topStart = 8.dp)
                                    )
                            )
                            // Top right corner marker
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .border(
                                        width = 3.dp,
                                        color = RealityAccent,
                                        shape = RoundedCornerShape(topEnd = 8.dp)
                                    )
                            )
                            // Bottom left corner marker
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomStart)
                                    .border(
                                        width = 3.dp,
                                        color = RealityAccent,
                                        shape = RoundedCornerShape(bottomStart = 8.dp)
                                    )
                            )
                            // Bottom right corner marker
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                                    .border(
                                        width = 3.dp,
                                        color = RealityAccent,
                                        shape = RoundedCornerShape(bottomEnd = 8.dp)
                                    )
                            )

                            // Status pill at top
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier.align(Alignment.TopCenter)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isAnalyzing) RealityAccent else RealitySuccess)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAnalyzing) "ANALYZING TENSORS..." else "READY FOR SCAN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Scanning animation beam when analyzing
                        if (isAnalyzing) {
                            ScanningRadarLine(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }

            // Input box for the user's question
            item {
                Column {
                    Text(
                        text = "What would you like to know about this?",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scanQuestion,
                        onValueChange = { viewModel.onScanQuestionChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_question_input"),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("e.g., What is this? What can it be used for?") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = RealityPrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RealityPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick question suggestion chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickQuestions.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.clickable { viewModel.onScanQuestionChange(prompt) }
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // "Analyze" button
            item {
                Button(
                    onClick = { viewModel.analyzeCurrentScan() },
                    enabled = !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("analyze_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RealityPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analyzing visual features...",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = RealityAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyze with AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Saved confirmation notice
            if (scanSavedMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RealitySuccess.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RealitySuccess.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RealitySuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scanSavedMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = RealitySuccess
                            )
                        }
                    }
                }
            }

            // AI Result Card
            if (analysisResult != null) {
                item {
                    AiResultCard(
                        result = analysisResult!!,
                        onAskFollowUp = { question ->
                            viewModel.onScanQuestionChange(question)
                            viewModel.analyzeCurrentScan()
                        },
                        onCopy = {
                            clipboardManager.setText(
                                AnnotatedString(
                                    "${analysisResult!!.objectName} (${analysisResult!!.category})\n\n" +
                                            "Answer: ${analysisResult!!.summaryAnswer}\n\n" +
                                            "Primary Use: ${analysisResult!!.primaryUse}\n\n" +
                                            "Useful Tips:\n${analysisResult!!.usefulTips.joinToString("\n• ", prefix = "• ")}"
                                )
                            )
                            Toast.makeText(context, "Copied analysis to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AiResultCard(
    result: VisualAnalysisResult,
    onAskFollowUp: (String) -> Unit,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .testTag("ai_result_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Category, Object Name, Confidence & Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    RealityCategoryChip(category = result.category)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.objectName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RealityPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${(result.confidence * 100).toInt()}% Match",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RealityPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Summary Answer Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = RealityPrimaryDark.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, RealityPrimary.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = RealityPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Answer",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = RealityPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.summaryAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 21.sp
                    )
                }
            }

            // Primary Use
            Column {
                Text(
                    text = "Primary Utility & Application",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.primaryUse,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
            }

            // Useful Tips
            if (result.usefulTips.isNotEmpty()) {
                Column {
                    Text(
                        text = "Useful Tips & Recommendations",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    result.usefulTips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                color = RealityPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Key Facts
            if (result.keyFacts.isNotEmpty()) {
                Column {
                    Text(
                        text = "Key Observations",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    result.keyFacts.forEach { fact ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "✓ ",
                                color = RealitySuccess,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = fact,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Follow-up Suggestions
            if (result.suggestedQuestions.isNotEmpty()) {
                Column {
                    Text(
                        text = "Suggested Questions",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.suggestedQuestions.forEach { question ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { onAskFollowUp(question) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = RealityPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = question,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
