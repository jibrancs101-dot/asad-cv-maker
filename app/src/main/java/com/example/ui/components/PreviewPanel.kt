package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CvAccentColor
import com.example.data.CvFontFamily
import com.example.data.CvFontSize
import com.example.data.CvTemplate
import com.example.data.CvZoomMode
import com.example.ui.CvUiState
import com.example.ui.CvViewModel
import com.example.ui.theme.PreviewStageDark
import com.example.ui.theme.PreviewStageLight
import kotlin.math.roundToInt

@Composable
fun PreviewPanel(
    uiState: CvUiState,
    viewModel: CvViewModel,
    bottomStagePadding: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val stageColor = if (settings.isDarkEditor) PreviewStageDark else PreviewStageLight

    Column(modifier = modifier.fillMaxSize()) {
        // Template Switcher + Typography / Color / Zoom Controls
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Template Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Template:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CvTemplate.entries.forEach { template ->
                        val selected = settings.template == template
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setTemplate(template) },
                            label = { Text(template.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.testTag("template_${template.name.lowercase()}")
                        )
                    }
                }

                // Row 2: Accent Color + Font Family + Font Size + Zoom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Accent Color Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CvAccentColor.entries.forEach { accent ->
                            val isSelected = settings.accentColor == accent
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(accent.color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else Color.White.copy(alpha = 0.6f),
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(accent) }
                                    .testTag("accent_${accent.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = accent.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Font Family Dropdown
                    var fontMenuOpen by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { fontMenuOpen = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("font_family_selector")
                        ) {
                            Text("Font: ${settings.fontFamily.label}", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = fontMenuOpen,
                            onDismissRequest = { fontMenuOpen = false }
                        ) {
                            CvFontFamily.entries.forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font.label) },
                                    onClick = {
                                        viewModel.setFontFamily(font)
                                        fontMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Font Size Toggle (S / M / L)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        CvFontSize.entries.forEach { sizeOption ->
                            val selected = settings.fontSize == sizeOption
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setFontSize(sizeOption) },
                                label = {
                                    Text(
                                        sizeOption.label.first().toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier.testTag("font_size_${sizeOption.name.lowercase()}")
                            )
                        }
                    }

                    // Zoom Controls (75%, 100%, Fit)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        CvZoomMode.entries.forEach { zoom ->
                            val selected = settings.zoomMode == zoom
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setZoomMode(zoom) },
                                label = { Text(zoom.label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("zoom_${zoom.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // A4 Preview Stage
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(stageColor)
        ) {
            val horizontalPad = 28.dp
            val containerMaxWidth = maxWidth
            val availableWidth = (containerMaxWidth - horizontalPad).coerceAtLeast(180.dp)
            val fitScale = (availableWidth / 595.dp).coerceIn(0.35f, 1.35f)
            val zoomScale = when (settings.zoomMode) {
                CvZoomMode.FIT -> fitScale
                CvZoomMode.ZOOM_75 -> 0.75f
                CvZoomMode.ZOOM_100 -> 1.0f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = 18.dp,
                        bottom = bottomStagePadding
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                // Center horizontally when narrower than viewport
                val minBoxWidth = (containerMaxWidth - 28.dp).coerceAtLeast(0.dp)
                Box(
                    modifier = Modifier.width(maxOf(minBoxWidth, 595.dp * zoomScale)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    A4ResumeSheet(
                        profile = uiState.profile,
                        settings = settings,
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(
                                    constraints.copy(minWidth = 0, maxWidth = Int.MAX_VALUE)
                                )
                                val scaledW = (placeable.width * zoomScale).roundToInt()
                                val scaledH = (placeable.height * zoomScale).roundToInt()
                                layout(scaledW, scaledH) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                            .shadow(10.dp, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}
