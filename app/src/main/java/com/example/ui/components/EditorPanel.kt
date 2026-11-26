package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CvUiState
import com.example.ui.CvViewModel
import com.example.ui.EditorSection
import kotlinx.coroutines.launch

@Composable
fun EditorPanel(
    uiState: CvUiState,
    viewModel: CvViewModel,
    bottomContentPadding: androidx.compose.ui.unit.Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val profile = uiState.profile

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Step Tabs Strip
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EditorSection.entries) { section ->
                    val selected = uiState.activeSection == section
                    val count = sectionCountBadge(section, uiState)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            viewModel.selectSection(section)
                            scope.launch {
                                listState.animateScrollToItem(section.ordinal)
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = sectionIcon(section),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(section.label, fontSize = 12.5.sp)
                                if (count != null && count > 0) {
                                    Spacer(Modifier.width(5.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("tab_${section.name.lowercase()}")
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Collapsible Accordion Form Sections
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = 14.dp,
                bottom = bottomContentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(EditorSection.entries, key = { it.name }) { section ->
                val isExpanded = section in uiState.expandedSections
                CollapsibleSectionCard(
                    section = section,
                    stepNumber = section.ordinal + 1,
                    badgeCount = sectionCountBadge(section, uiState),
                    isExpanded = isExpanded,
                    onToggle = { viewModel.toggleSectionExpanded(section) }
                ) {
                    when (section) {
                        EditorSection.PERSONAL -> PersonalInfoEditor(
                            info = profile.personalInfo,
                            onUpdate = viewModel::updatePersonalInfo,
                            onPickPhotoUri = viewModel::importPickedPhoto,
                            onRemovePhoto = viewModel::removePhoto
                        )

                        EditorSection.SUMMARY -> SummaryEditor(
                            summary = profile.summary,
                            onSummaryChange = viewModel::updateSummary
                        )

                        EditorSection.EXPERIENCE -> WorkExperienceEditor(
                            experiences = profile.experiences,
                            onAdd = viewModel::addExperience,
                            onUpdate = viewModel::updateExperience,
                            onRemove = viewModel::removeExperience,
                            onMove = viewModel::moveExperience
                        )

                        EditorSection.EDUCATION -> EducationEditor(
                            educationList = profile.education,
                            onAdd = viewModel::addEducation,
                            onUpdate = viewModel::updateEducation,
                            onRemove = viewModel::removeEducation
                        )

                        EditorSection.SKILLS -> SkillsEditor(
                            skills = profile.skills,
                            onAddSkill = viewModel::addSkill,
                            onUpdateLevel = viewModel::updateSkillLevel,
                            onRemoveSkill = viewModel::removeSkill
                        )

                        EditorSection.PROJECTS -> ProjectsEditor(
                            projects = profile.projects,
                            onAdd = viewModel::addProject,
                            onUpdate = viewModel::updateProject,
                            onRemove = viewModel::removeProject
                        )

                        EditorSection.CERTS_LANG -> CertificationsLanguagesEditor(
                            certifications = profile.certifications,
                            languages = profile.languages,
                            onAddCert = viewModel::addCertification,
                            onUpdateCert = viewModel::updateCertification,
                            onRemoveCert = viewModel::removeCertification,
                            onAddLanguage = viewModel::addLanguage,
                            onUpdateLanguage = viewModel::updateLanguage,
                            onRemoveLanguage = viewModel::removeLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsibleSectionCard(
    section: EditorSection,
    stepNumber: Int,
    badgeCount: Int?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 13.dp)
                    .testTag("accordion_${section.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExpanded) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = sectionIcon(section),
                        contentDescription = null,
                        tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = sectionTitle(section),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeCount != null && badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$badgeCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse section" else "Expand section",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                        content()
                    }
                }
            }
        }
    }
}

private fun sectionIcon(section: EditorSection): ImageVector = when (section) {
    EditorSection.PERSONAL -> Icons.Default.Person
    EditorSection.SUMMARY -> Icons.Default.Description
    EditorSection.EXPERIENCE -> Icons.Default.Work
    EditorSection.EDUCATION -> Icons.Default.School
    EditorSection.SKILLS -> Icons.Default.Build
    EditorSection.PROJECTS -> Icons.Default.Code
    EditorSection.CERTS_LANG -> Icons.Default.Verified
}

private fun sectionTitle(section: EditorSection): String = when (section) {
    EditorSection.PERSONAL -> "Personal Info & Photo"
    EditorSection.SUMMARY -> "Professional Summary"
    EditorSection.EXPERIENCE -> "Work Experience"
    EditorSection.EDUCATION -> "Education"
    EditorSection.SKILLS -> "Skills & Proficiency"
    EditorSection.PROJECTS -> "Projects (Optional)"
    EditorSection.CERTS_LANG -> "Certifications & Languages"
}

private fun sectionCountBadge(section: EditorSection, state: CvUiState): Int? = when (section) {
    EditorSection.PERSONAL -> null
    EditorSection.SUMMARY -> null
    EditorSection.EXPERIENCE -> state.profile.experiences.size
    EditorSection.EDUCATION -> state.profile.education.size
    EditorSection.SKILLS -> state.profile.skills.size
    EditorSection.PROJECTS -> state.profile.projects.size
    EditorSection.CERTS_LANG -> state.profile.certifications.size + state.profile.languages.size
}
