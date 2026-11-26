package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CvProfile
import com.example.data.CvSettings
import com.example.data.CvTemplate
import com.example.data.PersonalInfo
import com.example.ui.theme.PaperDivider
import com.example.ui.theme.PaperInk
import com.example.ui.theme.PaperMutedInk
import com.example.ui.theme.PaperWhite

/**
 * Renders the crisp white A4 CV sheet (logical 595dp x >=842dp = 210mm x 297mm)
 * with exact print colors regardless of editor dark mode.
 */
@Composable
fun A4ResumeSheet(
    profile: CvProfile,
    settings: CvSettings,
    modifier: Modifier = Modifier
) {
    val accent = settings.accentColor.color
    val softAccent = settings.accentColor.softColor
    val fontFamily = settings.fontFamily.fontFamily
    val scale = settings.fontSize.scale

    Box(
        modifier = modifier
            .width(595.dp)
            .heightIn(min = 842.dp)
            .background(PaperWhite)
    ) {
        when (settings.template) {
            CvTemplate.MODERN_EXECUTIVE -> ModernExecutiveTemplate(
                profile = profile,
                accent = accent,
                fontFamily = fontFamily,
                scale = scale
            )

            CvTemplate.CLASSIC_MINIMALIST -> ClassicMinimalistTemplate(
                profile = profile,
                accent = accent,
                fontFamily = fontFamily,
                scale = scale
            )

            CvTemplate.CREATIVE_TECH -> CreativeTechTemplate(
                profile = profile,
                accent = accent,
                softAccent = softAccent,
                fontFamily = fontFamily,
                scale = scale
            )
        }
    }
}

@Composable
private fun ResumePhoto(
    info: PersonalInfo,
    size: Dp,
    borderColor: Color
) {
    if (!info.showPhoto) return
    val hasUserPhoto = !info.photoUri.isNullOrBlank()
    val hasSample = info.useSampleAvatar
    if (!hasUserPhoto && !hasSample) return

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(Color(0xFFE2E8F0))
    ) {
        if (hasUserPhoto) {
            AsyncImage(
                model = info.photoUri,
                contentDescription = "CV Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(R.drawable.img_sample_avatar),
                contentDescription = "CV Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// =========================================================================
// TEMPLATE 1: MODERN EXECUTIVE (Two-column with colored left sidebar)
// =========================================================================
@Composable
private fun ModernExecutiveTemplate(
    profile: CvProfile,
    accent: Color,
    fontFamily: FontFamily,
    scale: Float
) {
    val info = profile.personalInfo
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .heightIn(min = 842.dp)
    ) {
        // Colored Left Sidebar
        Column(
            modifier = Modifier
                .width(188.dp)
                .fillMaxHeight()
                .background(accent)
                .padding(horizontal = 18.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (info.showPhoto && (!info.photoUri.isNullOrBlank() || info.useSampleAvatar)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ResumePhoto(
                        info = info,
                        size = 76.dp,
                        borderColor = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            // Contact Info
            val contacts = listOfNotNull(
                info.email.takeIf { it.isNotBlank() },
                info.phone.takeIf { it.isNotBlank() },
                info.location.takeIf { it.isNotBlank() },
                info.linkedIn.takeIf { it.isNotBlank() },
                info.portfolio.takeIf { it.isNotBlank() }
            )
            if (contacts.isNotEmpty()) {
                SidebarSection("CONTACT", fontFamily, scale) {
                    contacts.forEach { item ->
                        Text(
                            text = item,
                            fontFamily = fontFamily,
                            fontSize = (8.5f * scale).sp,
                            lineHeight = (12f * scale).sp,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            // Skills with clean bars
            if (profile.skills.isNotEmpty()) {
                SidebarSection("SKILLS", fontFamily, scale) {
                    profile.skills.forEach { skill ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = skill.name,
                                fontFamily = fontFamily,
                                fontSize = (8.5f * scale).sp,
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(skill.level.ratio)
                                        .fillMaxHeight()
                                        .background(Color.White)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // Languages
            val langs = profile.languages.filter { it.name.isNotBlank() }
            if (langs.isNotEmpty()) {
                SidebarSection("LANGUAGES", fontFamily, scale) {
                    langs.forEach { lang ->
                        Text(
                            text = "${lang.name} — ${lang.fluency.label}",
                            fontFamily = fontFamily,
                            fontSize = (8.3f * scale).sp,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }
            }
        }

        // Right Main Body
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = info.fullName.ifBlank { "Your Name" },
                    fontFamily = fontFamily,
                    fontSize = (21f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = PaperInk
                )
                if (info.headline.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = info.headline,
                        fontFamily = fontFamily,
                        fontSize = (11f * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
            }

            MainResumeSections(
                profile = profile,
                accent = accent,
                fontFamily = fontFamily,
                scale = scale,
                includeSkills = false,
                includeLanguages = false,
                useTimelineDots = false
            )
        }
    }
}

@Composable
private fun SidebarSection(
    title: String,
    fontFamily: FontFamily,
    scale: Float,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontFamily = fontFamily,
            fontSize = (9.5f * scale).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Color.White
        )
        HorizontalDivider(color = Color.White.copy(alpha = 0.30f), thickness = 0.8.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            content()
        }
    }
}

// =========================================================================
// TEMPLATE 2: CLASSIC MINIMALIST (Single-column corporate layout)
// =========================================================================
@Composable
private fun ClassicMinimalistTemplate(
    profile: CvProfile,
    accent: Color,
    fontFamily: FontFamily,
    scale: Float
) {
    val info = profile.personalInfo
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResumePhoto(info = info, size = 58.dp, borderColor = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.fullName.ifBlank { "Your Name" },
                    fontFamily = fontFamily,
                    fontSize = (22f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = PaperInk
                )
                if (info.headline.isNotBlank()) {
                    Text(
                        text = info.headline,
                        fontFamily = fontFamily,
                        fontSize = (11f * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
                val contactLine = listOfNotNull(
                    info.email.takeIf { it.isNotBlank() },
                    info.phone.takeIf { it.isNotBlank() },
                    info.location.takeIf { it.isNotBlank() },
                    info.linkedIn.takeIf { it.isNotBlank() },
                    info.portfolio.takeIf { it.isNotBlank() }
                ).joinToString("  •  ")
                if (contactLine.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = contactLine,
                        fontFamily = fontFamily,
                        fontSize = (8.5f * scale).sp,
                        color = PaperMutedInk,
                        lineHeight = (12f * scale).sp
                    )
                }
            }
        }

        HorizontalDivider(color = accent, thickness = 1.6.dp)

        MainResumeSections(
            profile = profile,
            accent = accent,
            fontFamily = fontFamily,
            scale = scale,
            includeSkills = true,
            includeLanguages = true,
            useTimelineDots = false
        )
    }
}

// =========================================================================
// TEMPLATE 3: CREATIVE TECH (Banner header + timeline dots + grid)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreativeTechTemplate(
    profile: CvProfile,
    accent: Color,
    softAccent: Color,
    fontFamily: FontFamily,
    scale: Float
) {
    val info = profile.personalInfo
    Column(modifier = Modifier.fillMaxWidth()) {
        // Accent Banner Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accent)
                .padding(horizontal = 30.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResumePhoto(info = info, size = 64.dp, borderColor = Color.White)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.fullName.ifBlank { "Your Name" },
                    fontFamily = fontFamily,
                    fontSize = (20f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (info.headline.isNotBlank()) {
                    Text(
                        text = info.headline,
                        fontFamily = fontFamily,
                        fontSize = (10.5f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
                val contactLine = listOfNotNull(
                    info.email.takeIf { it.isNotBlank() },
                    info.phone.takeIf { it.isNotBlank() },
                    info.location.takeIf { it.isNotBlank() },
                    info.linkedIn.takeIf { it.isNotBlank() },
                    info.portfolio.takeIf { it.isNotBlank() }
                ).joinToString("  |  ")
                if (contactLine.isNotBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = contactLine,
                        fontFamily = fontFamily,
                        fontSize = (8.2f * scale).sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = (11.5f * scale).sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MainResumeSections(
                profile = profile,
                accent = accent,
                fontFamily = fontFamily,
                scale = scale,
                includeSkills = false,
                includeLanguages = false,
                useTimelineDots = true
            )

            // Two-column Grid for Skills & Languages/Certs in Creative Tech
            if (profile.skills.isNotEmpty() || profile.languages.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (profile.skills.isNotEmpty()) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionHeader("Tech & Skills", accent, fontFamily, scale)
                            Spacer(Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                profile.skills.forEach { skill ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(softAccent)
                                            .border(0.6.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = skill.name,
                                            fontFamily = fontFamily,
                                            fontSize = (8.2f * scale).sp,
                                            fontWeight = FontWeight.Medium,
                                            color = accent
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val langs = profile.languages.filter { it.name.isNotBlank() }
                    if (langs.isNotEmpty()) {
                        Column(modifier = Modifier.weight(0.75f)) {
                            SectionHeader("Languages", accent, fontFamily, scale)
                            Spacer(Modifier.height(6.dp))
                            langs.forEach { lang ->
                                Text(
                                    text = "${lang.name} (${lang.fluency.label})",
                                    fontFamily = fontFamily,
                                    fontSize = (8.6f * scale).sp,
                                    color = PaperInk
                                )
                                Spacer(Modifier.height(3.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// SHARED RESUME SECTIONS
// =========================================================================
@Composable
private fun SectionHeader(
    title: String,
    accent: Color,
    fontFamily: FontFamily,
    scale: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontFamily = fontFamily,
            fontSize = (10f * scale).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp,
            color = accent
        )
        Spacer(Modifier.height(3.dp))
        HorizontalDivider(color = accent.copy(alpha = 0.75f), thickness = 1.dp)
    }
}

@Composable
private fun MainResumeSections(
    profile: CvProfile,
    accent: Color,
    fontFamily: FontFamily,
    scale: Float,
    includeSkills: Boolean,
    includeLanguages: Boolean,
    useTimelineDots: Boolean
) {
    // Summary
    if (profile.summary.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            SectionHeader("Professional Summary", accent, fontFamily, scale)
            Text(
                text = profile.summary,
                fontFamily = fontFamily,
                fontSize = (9f * scale).sp,
                lineHeight = (13.2f * scale).sp,
                color = PaperInk
            )
        }
    }

    // Work Experience
    if (profile.experiences.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Work Experience", accent, fontFamily, scale)
            profile.experiences.forEach { exp ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (useTimelineDots) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 4.dp, end = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = exp.jobTitle.ifBlank { "Position Title" },
                                fontFamily = fontFamily,
                                fontSize = (9.8f * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = PaperInk,
                                modifier = Modifier.weight(1f)
                            )
                            val dateRange = buildString {
                                append(exp.startDate)
                                if (exp.startDate.isNotBlank() && (exp.isCurrent || exp.endDate.isNotBlank())) append(" – ")
                                append(if (exp.isCurrent) "Present" else exp.endDate)
                            }
                            if (dateRange.isNotBlank()) {
                                Text(
                                    text = dateRange,
                                    fontFamily = fontFamily,
                                    fontSize = (8.2f * scale).sp,
                                    color = PaperMutedInk
                                )
                            }
                        }
                        val companyLine = listOfNotNull(
                            exp.company.takeIf { it.isNotBlank() },
                            exp.location.takeIf { it.isNotBlank() }
                        ).joinToString(" • ")
                        if (companyLine.isNotBlank()) {
                            Text(
                                text = companyLine,
                                fontFamily = fontFamily,
                                fontSize = (8.8f * scale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accent
                            )
                        }
                        if (exp.description.isNotBlank()) {
                            Text(
                                text = exp.description,
                                fontFamily = fontFamily,
                                fontSize = (8.8f * scale).sp,
                                lineHeight = (12.8f * scale).sp,
                                color = PaperInk,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Education
    if (profile.education.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionHeader("Education", accent, fontFamily, scale)
            profile.education.forEach { edu ->
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = edu.degree.ifBlank { "Degree" },
                            fontFamily = fontFamily,
                            fontSize = (9.5f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = PaperInk,
                            modifier = Modifier.weight(1f)
                        )
                        if (edu.graduationYear.isNotBlank()) {
                            Text(
                                text = edu.graduationYear,
                                fontFamily = fontFamily,
                                fontSize = (8.2f * scale).sp,
                                color = PaperMutedInk
                            )
                        }
                    }
                    val sub = listOfNotNull(
                        edu.institution.takeIf { it.isNotBlank() },
                        edu.location.takeIf { it.isNotBlank() },
                        edu.grade.takeIf { it.isNotBlank() }?.let { "Grade: $it" }
                    ).joinToString(" • ")
                    if (sub.isNotBlank()) {
                        Text(
                            text = sub,
                            fontFamily = fontFamily,
                            fontSize = (8.6f * scale).sp,
                            color = PaperMutedInk
                        )
                    }
                }
            }
        }
    }

    // Skills (single column mode)
    if (includeSkills && profile.skills.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            SectionHeader("Core Skills", accent, fontFamily, scale)
            Text(
                text = profile.skills.joinToString("  •  ") { "${it.name} (${it.level.label})" },
                fontFamily = fontFamily,
                fontSize = (8.8f * scale).sp,
                lineHeight = (12.8f * scale).sp,
                color = PaperInk
            )
        }
    }

    // Projects
    if (profile.projects.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionHeader("Projects", accent, fontFamily, scale)
            profile.projects.forEach { proj ->
                Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                    Text(
                        text = proj.name.ifBlank { "Project" },
                        fontFamily = fontFamily,
                        fontSize = (9.4f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = PaperInk
                    )
                    val meta = listOfNotNull(
                        proj.techStack.takeIf { it.isNotBlank() },
                        proj.link.takeIf { it.isNotBlank() }
                    ).joinToString("  •  ")
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            fontFamily = fontFamily,
                            fontSize = (8.2f * scale).sp,
                            fontWeight = FontWeight.Medium,
                            color = accent
                        )
                    }
                    if (proj.description.isNotBlank()) {
                        Text(
                            text = proj.description,
                            fontFamily = fontFamily,
                            fontSize = (8.6f * scale).sp,
                            lineHeight = (12.4f * scale).sp,
                            color = PaperInk
                        )
                    }
                }
            }
        }
    }

    // Certifications
    if (profile.certifications.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionHeader("Certifications", accent, fontFamily, scale)
            profile.certifications.forEach { cert ->
                val line = buildString {
                    append(cert.name)
                    if (cert.issuer.isNotBlank()) append(" — ${cert.issuer}")
                    if (cert.year.isNotBlank()) append(" (${cert.year})")
                }
                Text(
                    text = line,
                    fontFamily = fontFamily,
                    fontSize = (8.7f * scale).sp,
                    color = PaperInk
                )
            }
        }
    }

    // Languages (single column mode)
    if (includeLanguages) {
        val langs = profile.languages.filter { it.name.isNotBlank() }
        if (langs.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionHeader("Languages", accent, fontFamily, scale)
                Text(
                    text = langs.joinToString("  •  ") { "${it.name} (${it.fluency.label})" },
                    fontFamily = fontFamily,
                    fontSize = (8.7f * scale).sp,
                    color = PaperInk
                )
            }
        }
    }
}
