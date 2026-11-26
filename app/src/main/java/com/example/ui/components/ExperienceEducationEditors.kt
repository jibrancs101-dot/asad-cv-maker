package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.EducationItem
import com.example.data.WorkExperience

@Composable
fun WorkExperienceEditor(
    experiences: List<WorkExperience>,
    onAdd: () -> Unit,
    onUpdate: (String, (WorkExperience) -> WorkExperience) -> Unit,
    onRemove: (String) -> Unit,
    onMove: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (experiences.isEmpty()) {
            EmptySectionPlaceholder("No work experience added yet. Tap below to add your first role.")
        }

        experiences.forEachIndexed { index, exp ->
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
                            text = exp.jobTitle.ifBlank { "Position #${index + 1}" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(
                                onClick = { onMove(exp.id, -1) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = { onMove(exp.id, 1) },
                                enabled = index < experiences.lastIndex
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move down")
                            }
                            IconButton(onClick = { onRemove(exp.id) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete experience",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = exp.jobTitle,
                        onValueChange = { v -> onUpdate(exp.id) { it.copy(jobTitle = v) } },
                        label = { Text("Job Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = exp.company,
                            onValueChange = { v -> onUpdate(exp.id) { it.copy(company = v) } },
                            label = { Text("Company") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = exp.location,
                            onValueChange = { v -> onUpdate(exp.id) { it.copy(location = v) } },
                            label = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = exp.startDate,
                            onValueChange = { v -> onUpdate(exp.id) { it.copy(startDate = v) } },
                            label = { Text("Start Date") },
                            placeholder = { Text("e.g. Jan 2022") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = if (exp.isCurrent) "Present" else exp.endDate,
                            onValueChange = { v -> onUpdate(exp.id) { it.copy(endDate = v) } },
                            enabled = !exp.isCurrent,
                            label = { Text("End Date") },
                            placeholder = { Text("e.g. Dec 2024") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = exp.isCurrent,
                            onCheckedChange = { checked ->
                                onUpdate(exp.id) { it.copy(isCurrent = checked) }
                            }
                        )
                        Text(
                            text = "Currently Working Here",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    OutlinedTextField(
                        value = exp.description,
                        onValueChange = { v -> onUpdate(exp.id) { it.copy(description = v) } },
                        label = { Text("Responsibilities & Achievements (Bullet points)") },
                        minLines = 3,
                        maxLines = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_experience_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add More Experience")
        }
    }
}

@Composable
fun EducationEditor(
    educationList: List<EducationItem>,
    onAdd: () -> Unit,
    onUpdate: (String, (EducationItem) -> EducationItem) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (educationList.isEmpty()) {
            EmptySectionPlaceholder("No education entries yet. Add your degrees or diplomas.")
        }

        educationList.forEachIndexed { index, edu ->
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
                            text = edu.degree.ifBlank { "Education #${index + 1}" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onRemove(edu.id) }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete education",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    OutlinedTextField(
                        value = edu.degree,
                        onValueChange = { v -> onUpdate(edu.id) { it.copy(degree = v) } },
                        label = { Text("Degree / Certificate") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = edu.institution,
                            onValueChange = { v -> onUpdate(edu.id) { it.copy(institution = v) } },
                            label = { Text("Institution / University") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = edu.location,
                            onValueChange = { v -> onUpdate(edu.id) { it.copy(location = v) } },
                            label = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = edu.graduationYear,
                            onValueChange = { v -> onUpdate(edu.id) { it.copy(graduationYear = v) } },
                            label = { Text("Graduation Year") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = edu.grade,
                            onValueChange = { v -> onUpdate(edu.id) { it.copy(grade = v) } },
                            label = { Text("Grade / CGPA") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_education_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add More Education")
        }
    }
}

@Composable
fun EmptySectionPlaceholder(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
