package com.toyota.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.toyota.demo.data.db.ChatMessage
import com.toyota.demo.data.db.ChatSession
import com.toyota.demo.ui.settings.SettingsViewModel
import com.toyota.demo.ui.settings.SettingsScreen
import com.toyota.demo.ui.theme.MyApplicationTheme
import com.toyota.demo.ui.theme.SophisticatedEmeraldDot
import com.toyota.demo.ui.viewmodel.ToyotaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.DisposableEffect
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import com.toyota.demo.camera.CameraController
import com.toyota.demo.ui.viewmodel.ScanViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.lazy.itemsIndexed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.text.contains
import kotlin.text.startsWith
import kotlin.text.substringAfter


@Composable
fun ToyotaCareScreen() {
    val viewModel: ToyotaViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val sessions by viewModel.allSessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val isTestFeature1Enabled by settingsViewModel.isTestFeature1Enabled.collectAsState()

    var activeTab by remember { mutableStateOf("chat") }

    LaunchedEffect(activeSessionId) {
        if (activeSessionId != null) activeTab = "chat"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { ConciergeTopBar() },
        bottomBar = {
            ConciergeBottomBar(
                activeTab = activeTab,
                isTestFeature1Enabled = isTestFeature1Enabled,
                onTabSelected = { tab -> activeTab = tab }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .border(1.dp, MaterialTheme.colorScheme.error)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    "chat" -> ChatTabContent(
                        viewModel = viewModel,
                        messages = messages,
                        isGenerating = isGenerating,
                        selectedImageUri = selectedImageUri,
                        onNewChatRequested = { viewModel.selectSession(null) }
                    )

                    "history" -> HistoryTabContent(
                        sessions = sessions,
                        activeSessionId = activeSessionId,
                        onSessionSelected = { id -> viewModel.selectSession(id) },
                        onSessionDeleted = { id -> viewModel.deleteSession(id) }
                    )

                    "testing" -> TestingTabContent()

                    "dashboard" -> DashboardTabContent()

                    "settings" -> SettingsScreen()
                }
            }
        }
    }
}


// ── Top Bar ───────────────────────────────────────────────────────────────────
@Composable
fun ConciergeTopBar() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.concierge),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.gemma_powered),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

// ── Bottom Nav ────────────────────────────────────────────────────────────────
@Composable
fun ConciergeBottomBar(
    activeTab: String,
    isTestFeature1Enabled: Boolean,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        val chatTabText = stringResource(R.string.chat)
        NavigationBarItem(
            selected = activeTab == chatTabText,
            onClick = { onTabSelected(chatTabText) },
            icon = {
                Icon(
                    Icons.Default.Message,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    stringResource(R.string.repair_ai),
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = activeTab == "history",
            onClick = { onTabSelected("history") },
            icon = {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    stringResource(R.string.history),
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        if (isTestFeature1Enabled) {
            NavigationBarItem(
                selected = activeTab == "testing",
                onClick = { onTabSelected("testing") },
                icon = {
                    Icon(
                        Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "TESTING",
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        NavigationBarItem(
            selected = activeTab == "settings",
            onClick = { onTabSelected("settings") },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    "SETTINGS",
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

// ── Chat Tab ──────────────────────────────────────────────────────────────────
@Composable
fun ChatTabContent(
    viewModel: ToyotaViewModel,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    selectedImageUri: Uri?,
    onNewChatRequested: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleSelectedImageUri(context, it) }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) scrollState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Message list ──────────────────────────────────────────────────
        if (messages.isEmpty() && !isGenerating) {
            // Empty state (Home Screen)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Toyota Care AI",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Upload a photo for instant damage\nanalysis and repair estimation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(48.dp))

                // Select Damage Photo Button
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Select Damage Photo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))

                // OR Separator
                Row(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "O R",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Camera Button
                Surface(
                    onClick = { /* TODO: Launch Camera */ },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(110.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Camera",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

            }
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ConciergeChatMessageItem(msg)
                }
                if (isGenerating) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = SophisticatedEmeraldDot
                                    )
                                    Text(
                                        "Analyzing damage…",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Image thumbnail preview
                selectedImageUri?.let { uri ->
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            onClick = { viewModel.setImage(null, null) },
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Action Chips instead of Text Input
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            HomeActionChip("Repair Analysis") {
                                viewModel.sendMessage(
                                    "Provide a detailed repair analysis for this damage.",
                                    context
                                )
                            }
                            HomeActionChip("Accident Report") {
                                viewModel.sendMessage(
                                    "Generate a structured accident report based on these photos.",
                                    context
                                )
                            }
                            HomeActionChip("Nearby Shop") {
                                viewModel.sendMessage(
                                    "Find the nearest Toyota service center",
                                    context
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeActionChip(label: String, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Chat Message Item ─────────────────────────────────────────────────────────
@Composable
fun ConciergeChatMessageItem(message: ChatMessage) {
    val isUser = message.isUser
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    if (isUser) {
        // User bubble — right aligned, dark card with image + text
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column {
                    // Show image if present
                    val imgUri = message.imageUri
                    if (imgUri != null) {
                        val bitmap = remember(imgUri) {
                            runCatching {
                                if (imgUri.startsWith("data:") || imgUri.length > 200) {
                                    // base64
                                    val b64 =
                                        if (imgUri.contains(",")) imgUri.substringAfter(",") else imgUri
                                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } else null
                            }.getOrNull()
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Uploaded image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (message.messageText.isNotBlank()) {
                        Text(
                            text = message.messageText,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            Text(
                text = "SENT AT $timeStr",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
            )
        }
    } else {
        // AI response
        val hasReportHeaders = message.messageText.contains("### 1. Damage Summary") ||
                message.messageText.contains("### 2. Repair Estimate")

        val isDamageReport = hasReportHeaders || !message.estimateJson.isNullOrBlank()

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isDamageReport) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondaryContainer.copy(
                    alpha = 0.2f
                ),
                shadowElevation = if (isDamageReport) 3.dp else 0.dp,
                modifier = if (isDamageReport) Modifier.fillMaxWidth() else Modifier.widthIn(max = 320.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isDamageReport) {
                        // Section header only for actual reports
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Repair Report",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Render the AI response text
                    DamageReportText(text = message.messageText)

                    // Image Slider View
                    message.imageUri?.let { imgUri ->
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val bitmap = remember(imgUri) {
                                runCatching {
                                    if (imgUri.startsWith("data:") || imgUri.length > 200) {
                                        val b64 =
                                            if (imgUri.contains(",")) imgUri.substringAfter(",") else imgUri
                                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } else null
                                }.getOrNull()
                            }

                            // Current single image shown as first item in the "slider"
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Analysis image",
                                    modifier = Modifier
                                        .size(160.dp, 120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = imgUri,
                                    contentDescription = "Analysis image",
                                    modifier = Modifier
                                        .size(160.dp, 120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                        }
                    }

                    if (isDamageReport) {
                        Spacer(Modifier.height(12.dp))

                        // Source chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip(icon = Icons.Default.Memory, label = "Source: Vision_Gemma")
                            InfoChip(icon = Icons.Default.CheckCircle, label = "AI Analysis")
                        }

                        Spacer(Modifier.height(16.dp))

                        // Action row — copy/refresh/share
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val clipboard =
                                LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE)
                                        as ClipboardManager
                            IconButton(
                                onClick = {
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("Damage Report", message.messageText)
                                    )
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ThumbUp,
                                    contentDescription = "Like",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Regenerate",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("Damage Report", message.messageText)
                                    )
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// ── Damage Report Text (renders **bold** and bullet points) ───────────────────
@Composable
fun DamageReportText(text: String) {
    val lines = text.lines()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") || line.startsWith("## ") || line.startsWith("# ") -> {
                    // Skip markdown headers (already shown in the card header)
                }

                line.trimStart().startsWith("- ") || line.trimStart().startsWith("• ") -> {
                    // Bullet point
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val textColor = MaterialTheme.colorScheme.onSurface
                        Text(
                            "•",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = parseBoldText(
                                line.trimStart().removePrefix("- ").removePrefix("• "),
                                MaterialTheme.colorScheme.onSurface
                            ),
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                line.isBlank() -> Spacer(Modifier.height(2.dp))
                else -> {
                    // Regular paragraph — highlight SEVERE/MODERATE/LIGHT
                    Text(
                        text = parseSeverityText(line),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

fun parseBoldText(text: String, boldColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor))
                append(part)
                pop()
            } else {
                append(part)
            }
        }
    }
}

fun parseSeverityText(text: String): AnnotatedString {
    return buildAnnotatedString {
        val severeIdx = text.indexOf("SEVERE")
        val moderateIdx = text.indexOf("MODERATE")
        val lightIdx = text.indexOf("LIGHT")

        when {
            severeIdx >= 0 -> {
                append(text.substring(0, severeIdx))
                pushStyle(SpanStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold))
                append("SEVERE")
                pop()
                append(text.substring(severeIdx + 6))
            }

            moderateIdx >= 0 -> {
                append(text.substring(0, moderateIdx))
                pushStyle(SpanStyle(color = Color(0xFFF57C00), fontWeight = FontWeight.Bold))
                append("MODERATE")
                pop()
                append(text.substring(moderateIdx + 8))
            }

            lightIdx >= 0 -> {
                append(text.substring(0, lightIdx))
                pushStyle(SpanStyle(color = Color(0xFF388E3C), fontWeight = FontWeight.Bold))
                append("LIGHT")
                pop()
                append(text.substring(lightIdx + 5))
            }

            else -> append(text)
        }
    }
}

// ── Info Chip ─────────────────────────────────────────────────────────────────
@Composable
fun InfoChip(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Action Pill Button ────────────────────────────────────────────────────────
@Composable
fun ActionPillButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ── History Tab ───────────────────────────────────────────────────────────────
@Composable
fun HistoryTabContent(
    sessions: List<ChatSession>,
    activeSessionId: Long?,
    onSessionSelected: (Long) -> Unit,
    onSessionDeleted: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (sessions.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No analysis yet.\nUpload a photo to get started.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
        items(sessions) { session ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSessionSelected(session.id) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            session.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(
                                Date(
                                    session.timestamp
                                )
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(
                        onClick = { onSessionDeleted(session.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Testing Tab ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TestingTabContent() {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    when {
        cameraPermissionState.status.isGranted -> {
            CameraScannerScreen()
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Dashboard Light Analysis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "To identify dashboard warning lights and indicators, we need access to your camera.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable Camera")
                }
            }
        }
    }
}

@Composable
fun CameraScannerScreen() {
    val scanViewModel: ScanViewModel = hiltViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val capturedBitmap by scanViewModel.capturedBitmap.collectAsState()
    val explanation by scanViewModel.explanation.collectAsState()
    val isAnalyzing by scanViewModel.isAnalyzing.collectAsState()
    val scanHistory by scanViewModel.scanHistory.collectAsState(initial = emptyList())

    val cameraController = remember { CameraController(context) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        if (explanation == null && capturedBitmap == null) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also {
                            cameraController.startCamera(lifecycleOwner, it) { bitmap ->
                                scanViewModel.onFrameAnalyzed(bitmap)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay for scanning
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp, 200.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Position dashboard lights within frame",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    )
                }
            }
        } else if (explanation == null && capturedBitmap != null) {
            // Confirmation Screen
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Confirm Capture",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "Captured Dashboard",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { scanViewModel.resetScan() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Re-capture")
                    }
                    
                    Button(
                        onClick = { scanViewModel.confirmAnalysis() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Analyze")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "Dashboard Analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text(
                        "Analyzing with Gemma...",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )
                } else {
                    explanation?.let {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { scanViewModel.resetScan() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan Another Code")
                    }
                }
            }
        }

        // History Section
        if (scanHistory.isNotEmpty()) {
            HorizontalDivider()
            Text(
                "Recent Scans",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scanHistory) { scan ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(8.dp),
                        onClick = { /* Could show details */ }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                scan.errorCode,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { scanViewModel.deleteScan(scan) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraController.release()
        }
    }
}

// ── Dashboard Tab ──────────────────────────────────────────────────────────────
@Composable
fun DashboardTabContent() {
    val darkBg = Color(0xFF0F0F0F)
    val accentRed = Color(0xFFFF002E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
            Text(
                "Toyota Camry",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
        }

        // Car Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0x33FF0000)))
                drawRect(brush)
            }

            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(200.dp)
            )

            Text("Toyota Camry\nVisual", color = Color.Gray, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(20.dp))

        // Status Label
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "CAR STATUS",
                color = Color.White,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(accentRed))
        }

        Spacer(Modifier.height(24.dp))

        // Status Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(Modifier.weight(1f), Icons.Default.Thermostat, "22°C", "TEMP")
            StatusCard(Modifier.weight(1f), Icons.Default.DirectionsCar, "12,450", "MILEAGE")
            StatusCard(Modifier.weight(1f), Icons.Default.BatteryFull, "85%", "BATTERY")
        }

        Spacer(Modifier.height(32.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                onClick = { },
                modifier = Modifier
                    .weight(1.2f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = accentRed
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Climate", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun StatusCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Surface(
        modifier = modifier.height(110.dp),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(label, color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}
