package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.Emerald600

@Composable
fun BrandHeader(
    isCompact: Boolean,
    isDarkEditor: Boolean,
    isExporting: Boolean,
    onToggleDarkEditor: () -> Unit,
    onLoadSample: () -> Unit,
    onClearAll: () -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Mark + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsadCvLogoMark(modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Asad CV Maker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!isCompact) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Emerald600.copy(alpha = 0.14f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Auto-Saved",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Emerald600
                                    )
                                }
                            }
                        }
                        Text(
                            text = "ATS-Ready Resume Studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isCompact) {
                        OutlinedButton(
                            onClick = onLoadSample,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("load_sample_button")
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Load Sample CV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onClearAll,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("clear_all_button")
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Clear All", fontSize = 12.sp)
                        }
                    }

                    IconButton(
                        onClick = onToggleDarkEditor,
                        modifier = Modifier.testTag("toggle_dark_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkEditor) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkEditor) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isCompact) {
                        IconButton(
                            onClick = onSharePdf,
                            enabled = !isExporting,
                            modifier = Modifier.testTag("share_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share PDF",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = onDownloadPdf,
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = if (isCompact) 12.dp else 16.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("download_pdf_button")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download PDF",
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isCompact) "PDF" else "Download PDF",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isCompact) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.testTag("header_more_button")
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More actions"
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Share PDF") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onSharePdf()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Load Sample CV") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onLoadSample()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear All") },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onClearAll()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun AsadCvLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Rounded Navy square base
        drawRoundRect(
            color = DeepNavy,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
        )
        // White document sheet
        val docLeft = w * 0.24f
        val docTop = h * 0.18f
        val docW = w * 0.52f
        val docH = h * 0.64f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(docLeft, docTop),
            size = Size(docW, docH),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        // Header bar on document
        drawRoundRect(
            color = Emerald600,
            topLeft = Offset(docLeft + docW * 0.16f, docTop + docH * 0.18f),
            size = Size(docW * 0.52f, docH * 0.10f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Content lines
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(docLeft + docW * 0.16f, docTop + docH * 0.38f),
            size = Size(docW * 0.68f, docH * 0.08f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(docLeft + docW * 0.16f, docTop + docH * 0.54f),
            size = Size(docW * 0.58f, docH * 0.08f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Emerald checkmark badge bottom-right
        val badgeCx = w * 0.72f
        val badgeCy = h * 0.72f
        drawCircle(
            color = Emerald600,
            radius = w * 0.17f,
            center = Offset(badgeCx, badgeCy)
        )
        val checkPath = Path().apply {
            moveTo(badgeCx - w * 0.07f, badgeCy)
            lineTo(badgeCx - w * 0.01f, badgeCy + h * 0.06f)
            lineTo(badgeCx + w * 0.08f, badgeCy - h * 0.05f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = w * 0.055f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}
