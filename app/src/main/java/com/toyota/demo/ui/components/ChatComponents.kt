package com.toyota.demo.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.toyota.demo.data.db.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConciergeChatMessageItem(message: ChatMessage) {
    val isUser = message.isUser
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    if (isUser) {
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
                    val imgUri = message.imageUri
                    if (imgUri != null) {
                        val bitmap = remember(imgUri) {
                            runCatching {
                                if (imgUri.startsWith("data:") || imgUri.length > 200) {
                                    val b64 = if (imgUri.contains(",")) imgUri.substringAfter(",") else imgUri
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

                    DamageReportText(text = message.messageText)

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
                                        val b64 = if (imgUri.contains(",")) imgUri.substringAfter(",") else imgUri
                                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } else null
                                }.getOrNull()
                            }

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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip(icon = Icons.Default.Memory, label = "Source: Vision_Gemma")
                            InfoChip(icon = Icons.Default.CheckCircle, label = "AI Analysis")
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val clipboard = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            IconButton(
                                onClick = {
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Damage Report", message.messageText))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Like", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Damage Report", message.messageText))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
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

@Composable
fun DamageReportText(text: String) {
    val lines = text.lines()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") || line.startsWith("## ") || line.startsWith("# ") -> {}
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("• ") -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val textColor = MaterialTheme.colorScheme.onSurface
                        Text("•", color = textColor.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(top = 1.dp))
                        Text(
                            text = parseBoldText(line.trimStart().removePrefix("- ").removePrefix("• "), MaterialTheme.colorScheme.onSurface),
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
                line.isBlank() -> Spacer(Modifier.height(2.dp))
                else -> {
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
