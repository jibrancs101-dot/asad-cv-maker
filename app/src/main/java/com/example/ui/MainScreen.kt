package com.example.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pdf.CvPdfExporter
import com.example.ui.components.BrandHeader
import com.example.ui.components.EditorPanel
import com.example.ui.components.PreviewPanel
import com.example.ui.theme.AsadCvMakerTheme
import com.example.ui.theme.Emerald600
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AsadCvMakerApp(viewModel: CvViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                viewModel.setExporting(true)
                val result = withContext(Dispatchers.IO) {
                    CvPdfExporter.exportToUri(
                        context = context,
                        uri = uri,
                        profile = uiState.profile,
                        settings = uiState.settings
                    )
                }
                viewModel.setExporting(false)
                result.onSuccess {
                    viewModel.showStatusMessage("PDF exported successfully")
                }.onFailure { err ->
                    viewModel.showStatusMessage("PDF export failed: ${err.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun triggerDownloadPdf() {
        val safeName = uiState.profile.personalInfo.fullName
            .ifBlank { "Asad_CV" }
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
        createPdfLauncher.launch("${safeName}_Resume.pdf")
    }

    fun triggerSharePdf() {
        scope.launch {
            viewModel.setExporting(true)
            val result = withContext(Dispatchers.IO) {
                CvPdfExporter.exportToCacheFile(
                    context = context,
                    profile = uiState.profile,
                    settings = uiState.settings
                )
            }
            viewModel.setExporting(false)
            result.onSuccess { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share CV PDF"))
            }.onFailure { err ->
                viewModel.showStatusMessage("Could not share PDF: ${err.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    AsadCvMakerTheme(darkTheme = uiState.settings.isDarkEditor) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isSplitScreen = maxWidth >= 840.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = if (isSplitScreen) 8.dp else 72.dp)
                    )
                },
                topBar = {
                    BrandHeader(
                        isCompact = !isSplitScreen,
                        isDarkEditor = uiState.settings.isDarkEditor,
                        isExporting = uiState.isExporting,
                        onToggleDarkEditor = viewModel::toggleDarkEditor,
                        onLoadSample = viewModel::loadSampleCv,
                        onClearAll = viewModel::clearAll,
                        onDownloadPdf = ::triggerDownloadPdf,
                        onSharePdf = ::triggerSharePdf
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isSplitScreen) {
                        // Desktop / Tablet Split-Screen Workspace
                        Row(modifier = Modifier.fillMaxSize()) {
                            EditorPanel(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(0.46f)
                                    .fillMaxHeight()
                            )
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            PreviewPanel(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(0.54f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        // Mobile Responsive View + Floating Bottom Toggle Pill
                        if (uiState.mobileViewMode == MobileViewMode.EDIT_FORM) {
                            EditorPanel(
                                uiState = uiState,
                                viewModel = viewModel,
                                bottomContentPadding = 92.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            PreviewPanel(
                                uiState = uiState,
                                viewModel = viewModel,
                                bottomStagePadding = 92.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        ExtendedFloatingActionButton(
                            onClick = viewModel::toggleMobileViewMode,
                            containerColor = Emerald600,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(28.dp),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 16.dp)
                                .testTag("mobile_view_toggle_fab")
                        ) {
                            val isEditing = uiState.mobileViewMode == MobileViewMode.EDIT_FORM
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isEditing) "Live Preview" else "Edit Form",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
