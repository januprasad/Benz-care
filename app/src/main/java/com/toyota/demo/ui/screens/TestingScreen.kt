package com.toyota.demo.ui.screens

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.toyota.demo.camera.CameraController
import com.toyota.demo.ui.viewmodel.ScanViewModel
import com.toyota.demo.ui.components.*
import com.toyota.demo.analysis.*

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
    val result by scanViewModel.analysisResult.collectAsState()
    val isAnalyzing by scanViewModel.isAnalyzing.collectAsState()

    val cameraController = remember { CameraController(context) }
    val lastBitmapState = remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (explanation == null && capturedBitmap == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also {
                            cameraController.startCamera(lifecycleOwner, it) { bitmap ->
                                lastBitmapState.value = bitmap
                                scanViewModel.onFrameAnalyzed(bitmap)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

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
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Manual Capture Fallback
                    Button(
                        onClick = { 
                            lastBitmapState.value?.let { scanViewModel.forceCapture(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Capture Manually", color = Color.White)
                    }
                }
            }
        } else if (explanation == null && capturedBitmap != null && !isAnalyzing) {
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
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Re-capture")
                    }

                    Button(
                        onClick = { scanViewModel.confirmAnalysis() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Analyze")
                    }
                }
            }
        } else {
            // Result Screen with LazyColumn and Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Dashboard Analysis",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    capturedBitmap?.let { bitmap ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured Dashboard Scan",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                if (isAnalyzing) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Analyzing with Gemma...")
                        }
                    }
                } else {
                    result?.let { analysis ->
                        item {
                            AnalysisGroupCard(
                                title = "Overview",
                                icon = Icons.Default.Info
                            ) {
                                Column {
                                    Text(
                                        text = analysis.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Badge(
                                            containerColor = when (analysis.severity) {
                                                Severity.HIGH -> Color(0xFFD32F2F)
                                                Severity.MEDIUM -> Color(0xFFF57C00)
                                                else -> Color(0xFF388E3C)
                                            }
                                        ) {
                                            Text(
                                                "SEVERITY: ${analysis.severity}",
                                                modifier = Modifier.padding(4.dp),
                                                color = Color.White
                                            )
                                        }
                                        
                                        val safeColor = when (analysis.safeToDriver) {
                                            SafeToDriver.YES -> Color(0xFF388E3C)
                                            SafeToDriver.NO -> Color(0xFFD32F2F)
                                            SafeToDriver.CONDITIONALLY -> Color(0xFFF57C00)
                                        }
                                        
                                        Badge(containerColor = safeColor) {
                                            Text(
                                                text = when (analysis.safeToDriver) {
                                                    SafeToDriver.YES -> "SAFE TO DRIVE"
                                                    SafeToDriver.NO -> "UNSAFE - STOP"
                                                    SafeToDriver.CONDITIONALLY -> "SHORT TRIPS ONLY"
                                                },
                                                modifier = Modifier.padding(4.dp),
                                                color = Color.White
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "System: ${analysis.system}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            AnalysisGroupCard(title = "Explanation", icon = Icons.AutoMirrored.Filled.Message) {
                                Text(
                                    text = analysis.plainExplanation,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        if (analysis.detectedLights.isNotEmpty()) {
                            item {
                                AnalysisGroupCard(
                                    title = "Detected Indicators (${analysis.detectedLights.size})",
                                    icon = Icons.Default.Visibility
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        analysis.detectedLights.forEach { light ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = light.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Badge(
                                                    containerColor = when (light.color.uppercase()) {
                                                        "RED" -> Color(0xFFD32F2F)
                                                        "AMBER", "ORANGE" -> Color(0xFFF57C00)
                                                        "YELLOW" -> Color(0xFFEF9F27)
                                                        "GREEN" -> Color(0xFF388E3C)
                                                        "BLUE" -> Color(0xFF1976D2)
                                                        else -> MaterialTheme.colorScheme.secondary
                                                    }
                                                ) {
                                                    Text(
                                                        text = light.color.uppercase(),
                                                        modifier = Modifier.padding(horizontal = 4.dp),
                                                        color = Color.White,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            AnalysisGroupCard(
                                title = "Likely Causes",
                                icon = Icons.Default.BugReport
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    analysis.likelyCauses.forEach { cause ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("•", fontWeight = FontWeight.Bold)
                                            Text(text = cause)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            AnalysisGroupCard(
                                title = "Recommended Action",
                                icon = Icons.Default.DirectionsCar
                            ) {
                                Text(
                                    text = analysis.driverAction,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        item {
                            AnalysisGroupCard(
                                title = "Estimated Cost",
                                icon = Icons.Default.AccountBalanceWallet
                            ) {
                                Text(
                                    text = analysis.estimatedCostRange,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }
                    } ?: item {
                        // Fallback for non-structured explanation (e.g. error messages)
                        explanation?.let {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { scanViewModel.resetScan() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Scan Another Dashboard")
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
