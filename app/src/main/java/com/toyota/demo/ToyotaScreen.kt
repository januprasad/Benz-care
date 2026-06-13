package com.toyota.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toyota.demo.ui.components.ConciergeBottomBar
import com.toyota.demo.ui.components.ConciergeTopBar
import com.toyota.demo.ui.screens.*
import com.toyota.demo.ui.settings.SettingsScreen
import com.toyota.demo.ui.settings.SettingsViewModel
import com.toyota.demo.ui.viewmodel.ScanViewModel
import com.toyota.demo.ui.viewmodel.ToyotaViewModel

@Composable
fun ToyotaCareScreen() {
    val viewModel: ToyotaViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val scanViewModel: ScanViewModel = hiltViewModel()

    val sessions by viewModel.allSessions.collectAsState()
    val scanHistory by scanViewModel.scanHistory.collectAsState(initial = emptyList())
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
                        scans = scanHistory,
                        onSessionSelected = { id -> viewModel.selectSession(id) },
                        onSessionDeleted = { id -> viewModel.deleteSession(id) },
                        onScanDeleted = { scan -> scanViewModel.deleteScan(scan) }
                    )

                    "testing" -> TestingTabContent()

                    "dashboard" -> DashboardTabContent()

                    "settings" -> SettingsScreen()
                }
            }
        }
    }
}
