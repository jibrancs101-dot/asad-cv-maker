package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import com.example.ui.theme.GeorgiaFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LatoFontFamily
import com.example.ui.theme.RobotoFontFamily
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class PersonalInfo(
    val fullName: String = "",
    val headline: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val linkedIn: String = "",
    val portfolio: String = "",
    val photoUri: String? = null,
    val showPhoto: Boolean = true,
    val useSampleAvatar: Boolean = false
)

@JsonClass(generateAdapter = true)
data class WorkExperience(
    val id: String = UUID.randomUUID().toString(),
    val jobTitle: String = "",
    val company: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class EducationItem(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val institution: String = "",
    val location: String = "",
    val graduationYear: String = "",
    val grade: String = ""
)

enum class SkillLevel(val label: String, val ratio: Float) {
    BEGINNER("Beginner", 0.40f),
    INTERMEDIATE("Intermediate", 0.72f),
    EXPERT("Expert", 1.0f)
}

@JsonClass(generateAdapter = true)
data class SkillItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val level: SkillLevel = SkillLevel.EXPERT
)

@JsonClass(generateAdapter = true)
data class ProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val techStack: String = "",
    val link: String = "",
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class CertificationItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val issuer: String = "",
    val year: String = ""
)

enum class LanguageFluency(val label: String) {
    NATIVE("Native"),
    FLUENT("Fluent"),
    BASIC("Basic")
}

@JsonClass(generateAdapter = true)
data class LanguageItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val fluency: LanguageFluency = LanguageFluency.FLUENT
)

@JsonClass(generateAdapter = true)
data class CvProfile(
    val personalInfo: PersonalInfo = PersonalInfo(),
    val summary: String = "",
    val experiences: List<WorkExperience> = emptyList(),
    val education: List<EducationItem> = emptyList(),
    val skills: List<SkillItem> = emptyList(),
    val projects: List<ProjectItem> = emptyList(),
    val certifications: List<CertificationItem> = emptyList(),
    val languages: List<LanguageItem> = emptyList()
)

enum class CvTemplate(val label: String, val subtitle: String) {
    MODERN_EXECUTIVE("Modern Executive", "Two-column sidebar layout"),
    CLASSIC_MINIMALIST("Classic Minimalist", "Traditional single-column"),
    CREATIVE_TECH("Creative Tech", "Banner header & timeline")
}

enum class CvAccentColor(val label: String, val colorHex: Long, val softHex: Long) {
    NAVY("Navy", 0xFF0F172A, 0xFFF1F5F9),
    EMERALD("Emerald", 0xFF0D9488, 0xFFF0FDFA),
    CHARCOAL("Charcoal", 0xFF374151, 0xFFF3F4F6),
    ROYAL_BLUE("Royal Blue", 0xFF1D4ED8, 0xFFEFF6FF),
    BURGUNDY("Burgundy", 0xFF881337, 0xFFFFF1F2);

    val color: Color get() = Color(colorHex)
    val softColor: Color get() = Color(softHex)
}

enum class CvFontFamily(
    val label: String,
    val fontFamily: FontFamily,
    val fontResId: Int
) {
    INTER("Inter", InterFontFamily, R.font.inter),
    ROBOTO("Roboto", RobotoFontFamily, R.font.roboto),
    GEORGIA("Georgia", GeorgiaFontFamily, R.font.lora),
    LATO("Lato", LatoFontFamily, R.font.lato)
}

enum class CvFontSize(val label: String, val scale: Float) {
    SMALL("Small", 0.90f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.10f)
}

enum class CvZoomMode(val label: String, val multiplier: Float?) {
    ZOOM_75("75%", 0.75f),
    ZOOM_100("100%", 1.0f),
    FIT("Fit", null)
}

@JsonClass(generateAdapter = true)
data class CvSettings(
    val template: CvTemplate = CvTemplate.MODERN_EXECUTIVE,
    val accentColor: CvAccentColor = CvAccentColor.EMERALD,
    val fontFamily: CvFontFamily = CvFontFamily.INTER,
    val fontSize: CvFontSize = CvFontSize.MEDIUM,
    val zoomMode: CvZoomMode = CvZoomMode.FIT,
    val isDarkEditor: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PersistedCvState(
    val profile: CvProfile,
    val settings: CvSettings
)
