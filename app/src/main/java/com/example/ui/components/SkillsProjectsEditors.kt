package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.CertificationItem
import com.example.data.LanguageFluency
import com.example.data.LanguageItem
import com.example.data.ProjectItem
import com.example.data.SampleCvData
import com.example.data.SkillItem
import com.example.data.SkillLevel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsEditor(
    skills: List<SkillItem>,
    onAddSkill: (String, SkillLevel) -> Unit,
    onUpdateLevel: (String, SkillLevel) -> Unit,
    onRemoveSkill: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draftSkill by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(SkillLevel.EXPERT) }

    fun submitSkill() {
        if (draftSkill.isNotBlank()) {
            onAddSkill(draftSkill, selectedLevel)
            draftSkill = ""
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draftSkill,
                onValueChange = { draftSkill = it },
                label = { Text("Add a skill (press Enter)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitSkill() }),
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                            submitSkill()
                            true
                        } else {
                            false
                        }
                    }
                    .testTag("skill_input")
            )
            OutlinedButton(
                onClick = { submitSkill() },
                modifier = Modifier.testTag("add_skill_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add skill")
            }
        }

        // Proficiency level selector for next skill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Level:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SkillLevel.entries.forEach { level ->
                FilterChip(
                    selected = selectedLevel == level,
                    onClick = { selectedLevel = level },
                    label = { Text(level.label) }
                )
            }
        }

        // Current skills tags (tap tag to cycle proficiency)
        if (skills.isEmpty()) {
            EmptySectionPlaceholder("No skills added yet. Type a skill or tap suggested skills below.")
        } else {
            Text(
                text = "Your Skills (tap tag to cycle level)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                skills.forEach { skill ->
                    InputChip(
                        selected = true,
                        onClick = {
                            val entries = SkillLevel.entries
                            val next = entries[(skill.level.ordinal + 1) % entries.size]
                            onUpdateLevel(skill.id, next)
                        },
                        label = { Text("${skill.name} • ${skill.level.label}") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove ${skill.name}",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onRemoveSkill(skill.id) }
                            )
                        }
                    )
                }
            }
        }

        // Quick-add suggested skills
        val remainingSuggested = SampleCvData.suggestedSkills.filter { suggestion ->
            skills.none { it.name.equals(suggestion, ignoreCase = true) }
        }
        if (remainingSuggested.isNotEmpty()) {
            Text(
                text = "Quick-Add Suggested Skills",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                remainingSuggested.forEach { suggestion ->
                    AssistChip(
                        onClick = { onAddSkill(suggestion, selectedLevel) },
                        label = { Text(suggestion) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectsEditor(
    projects: List<ProjectItem>,
    onAdd: () -> Unit,
    onUpdate: (String, (ProjectItem) -> ProjectItem) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (projects.isEmpty()) {
            EmptySectionPlaceholder("No projects added yet. Showcase notable builds or portfolio pieces.")
        }

        projects.forEachIndexed { index, proj ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = proj.name.ifBlank { "Project #${index + 1}" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onRemove(proj.id) }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete project",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    OutlinedTextField(
                        value = proj.name,
                        onValueChange = { v -> onUpdate(proj.id) { it.copy(name = v) } },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = proj.techStack,
                            onValueChange = { v -> onUpdate(proj.id) { it.copy(techStack = v) } },
                            label = { Text("Tech Stack / Role") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = proj.link,
                            onValueChange = { v -> onUpdate(proj.id) { it.copy(link = v) } },
                            label = { Text("Live Link / Repo") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = proj.description,
                        onValueChange = { v -> onUpdate(proj.id) { it.copy(description = v) } },
                        label = { Text("Description & Outcome") },
                        minLines = 2,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_project_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add More Project")
        }
    }
}

@Composable
fun CertificationsLanguagesEditor(
    certifications: List<CertificationItem>,
    languages: List<LanguageItem>,
    onAddCert: () -> Unit,
    onUpdateCert: (String, (CertificationItem) -> CertificationItem) -> Unit,
    onRemoveCert: (String) -> Unit,
    onAddLanguage: () -> Unit,
    onUpdateLanguage: (String, (LanguageItem) -> LanguageItem) -> Unit,
    onRemoveLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Certifications",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (certifications.isEmpty()) {
            EmptySectionPlaceholder("No certifications added yet.")
        }

        certifications.forEachIndexed { index, cert ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = cert.name.ifBlank { "Certificate #${index + 1}" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onRemoveCert(cert.id) }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete certification",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cert.name,
                        onValueChange = { v -> onUpdateCert(cert.id) { it.copy(name = v) } },
                        label = { Text("Certificate Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cert.issuer,
                            onValueChange = { v -> onUpdateCert(cert.id) { it.copy(issuer = v) } },
                            label = { Text("Issuer") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cert.year,
                            onValueChange = { v -> onUpdateCert(cert.id) { it.copy(year = v) } },
                            label = { Text("Year") },
                            singleLine = true,
                            modifier = Modifier.weight(0.55f)
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddCert,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_cert_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add More Certification")
        }

        Text(
            text = "Languages",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (languages.isEmpty()) {
            EmptySectionPlaceholder("No languages added yet.")
        }

        languages.forEach { lang ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = lang.name,
                            onValueChange = { v -> onUpdateLanguage(lang.id) { it.copy(name = v) } },
                            label = { Text("Language") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onRemoveLanguage(lang.id) }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete language",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageFluency.entries.forEach { fluency ->
                            FilterChip(
                                selected = lang.fluency == fluency,
                                onClick = {
                                    onUpdateLanguage(lang.id) { it.copy(fluency = fluency) }
                                },
                                label = { Text(fluency.label) }
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddLanguage,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_language_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add More Language")
        }
    }
}
