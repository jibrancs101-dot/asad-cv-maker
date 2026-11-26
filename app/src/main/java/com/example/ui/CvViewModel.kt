package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CertificationItem
import com.example.data.CvAccentColor
import com.example.data.CvFontFamily
import com.example.data.CvFontSize
import com.example.data.CvProfile
import com.example.data.CvRepository
import com.example.data.CvSettings
import com.example.data.CvTemplate
import com.example.data.CvZoomMode
import com.example.data.EducationItem
import com.example.data.LanguageFluency
import com.example.data.LanguageItem
import com.example.data.PersistedCvState
import com.example.data.PersonalInfo
import com.example.data.ProjectItem
import com.example.data.SampleCvData
import com.example.data.SkillItem
import com.example.data.SkillLevel
import com.example.data.WorkExperience
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class EditorSection(val label: String) {
    PERSONAL("Personal"),
    SUMMARY("Summary"),
    EXPERIENCE("Experience"),
    EDUCATION("Education"),
    SKILLS("Skills"),
    PROJECTS("Projects"),
    CERTS_LANG("Certs & Lang")
}

enum class MobileViewMode {
    EDIT_FORM,
    LIVE_PREVIEW
}

data class CvUiState(
    val profile: CvProfile = SampleCvData.defaultProfile(),
    val settings: CvSettings = CvSettings(),
    val activeSection: EditorSection = EditorSection.PERSONAL,
    val expandedSections: Set<EditorSection> = setOf(EditorSection.PERSONAL),
    val mobileViewMode: MobileViewMode = MobileViewMode.EDIT_FORM,
    val isLoaded: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null
)

class CvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CvRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(CvUiState())
    val uiState: StateFlow<CvUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            val loaded = repository.stateFlow.first()
            _uiState.update {
                it.copy(
                    profile = loaded.profile,
                    settings = loaded.settings,
                    isLoaded = true
                )
            }
        }
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(250)
            val current = _uiState.value
            repository.saveState(PersistedCvState(current.profile, current.settings))
        }
    }

    private fun updateProfile(transform: (CvProfile) -> CvProfile) {
        _uiState.update { state -> state.copy(profile = transform(state.profile)) }
        scheduleAutoSave()
    }

    private fun updateSettings(transform: (CvSettings) -> CvSettings) {
        _uiState.update { state -> state.copy(settings = transform(state.settings)) }
        scheduleAutoSave()
    }

    fun selectSection(section: EditorSection) {
        _uiState.update { state ->
            state.copy(
                activeSection = section,
                expandedSections = state.expandedSections + section
            )
        }
    }

    fun toggleSectionExpanded(section: EditorSection) {
        _uiState.update { state ->
            val next = if (section in state.expandedSections) {
                state.expandedSections - section
            } else {
                state.expandedSections + section
            }
            state.copy(expandedSections = next, activeSection = section)
        }
    }

    fun setMobileViewMode(mode: MobileViewMode) {
        _uiState.update { it.copy(mobileViewMode = mode) }
    }

    fun toggleMobileViewMode() {
        _uiState.update {
            it.copy(
                mobileViewMode = if (it.mobileViewMode == MobileViewMode.EDIT_FORM) {
                    MobileViewMode.LIVE_PREVIEW
                } else {
                    MobileViewMode.EDIT_FORM
                }
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun showStatusMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun setExporting(exporting: Boolean) {
        _uiState.update { it.copy(isExporting = exporting) }
    }

    // --- Settings Actions ---
    fun toggleDarkEditor() = updateSettings { it.copy(isDarkEditor = !it.isDarkEditor) }
    fun setTemplate(template: CvTemplate) = updateSettings { it.copy(template = template) }
    fun setAccentColor(color: CvAccentColor) = updateSettings { it.copy(accentColor = color) }
    fun setFontFamily(fontFamily: CvFontFamily) = updateSettings { it.copy(fontFamily = fontFamily) }
    fun setFontSize(fontSize: CvFontSize) = updateSettings { it.copy(fontSize = fontSize) }
    fun setZoomMode(zoomMode: CvZoomMode) = updateSettings { it.copy(zoomMode = zoomMode) }

    // --- Quick Reset / Sample Actions ---
    fun loadSampleCv() {
        _uiState.update {
            it.copy(
                profile = SampleCvData.defaultProfile(),
                statusMessage = "Sample CV loaded"
            )
        }
        scheduleAutoSave()
    }

    fun clearAll() {
        _uiState.update {
            it.copy(
                profile = SampleCvData.emptyProfile(),
                statusMessage = "All fields cleared"
            )
        }
        scheduleAutoSave()
    }

    // --- Personal Info & Photo ---
    fun updatePersonalInfo(transform: (PersonalInfo) -> PersonalInfo) {
        updateProfile { profile ->
            profile.copy(personalInfo = transform(profile.personalInfo))
        }
    }

    fun importPickedPhoto(uri: Uri) {
        viewModelScope.launch {
            val savedPath = withContext(Dispatchers.IO) {
                runCatching {
                    val context = getApplication<Application>().applicationContext
                    val photoFile = File(context.filesDir, "profile_photo_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(photoFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Clean up older local copies
                    context.filesDir.listFiles()?.filter {
                        it.name.startsWith("profile_photo_") && it.absolutePath != photoFile.absolutePath
                    }?.forEach { it.delete() }
                    Uri.fromFile(photoFile).toString()
                }.getOrNull()
            }
            if (savedPath != null) {
                updatePersonalInfo {
                    it.copy(photoUri = savedPath, showPhoto = true, useSampleAvatar = false)
                }
                showStatusMessage("Profile photo updated")
            } else {
                showStatusMessage("Could not load selected image")
            }
        }
    }

    fun removePhoto() {
        updatePersonalInfo {
            it.copy(photoUri = null, useSampleAvatar = false)
        }
    }

    // --- Summary ---
    fun updateSummary(summary: String) {
        updateProfile { it.copy(summary = summary) }
    }

    // --- Work Experience ---
    fun addExperience() {
        updateProfile { profile ->
            profile.copy(experiences = profile.experiences + WorkExperience())
        }
    }

    fun updateExperience(id: String, transform: (WorkExperience) -> WorkExperience) {
        updateProfile { profile ->
            profile.copy(
                experiences = profile.experiences.map { if (it.id == id) transform(it) else it }
            )
        }
    }

    fun removeExperience(id: String) {
        updateProfile { profile ->
            profile.copy(experiences = profile.experiences.filterNot { it.id == id })
        }
    }

    fun moveExperience(id: String, delta: Int) {
        updateProfile { profile ->
            val list = profile.experiences.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            val target = idx + delta
            if (idx in list.indices && target in list.indices) {
                val item = list.removeAt(idx)
                list.add(target, item)
            }
            profile.copy(experiences = list)
        }
    }

    // --- Education ---
    fun addEducation() {
        updateProfile { profile ->
            profile.copy(education = profile.education + EducationItem())
        }
    }

    fun updateEducation(id: String, transform: (EducationItem) -> EducationItem) {
        updateProfile { profile ->
            profile.copy(
                education = profile.education.map { if (it.id == id) transform(it) else it }
            )
        }
    }

    fun removeEducation(id: String) {
        updateProfile { profile ->
            profile.copy(education = profile.education.filterNot { it.id == id })
        }
    }

    // --- Skills ---
    fun addSkill(name: String, level: SkillLevel = SkillLevel.EXPERT) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        updateProfile { profile ->
            if (profile.skills.any { it.name.equals(trimmed, ignoreCase = true) }) {
                profile
            } else {
                profile.copy(skills = profile.skills + SkillItem(name = trimmed, level = level))
            }
        }
    }

    fun updateSkillLevel(id: String, level: SkillLevel) {
        updateProfile { profile ->
            profile.copy(
                skills = profile.skills.map { if (it.id == id) it.copy(level = level) else it }
            )
        }
    }

    fun removeSkill(id: String) {
        updateProfile { profile ->
            profile.copy(skills = profile.skills.filterNot { it.id == id })
        }
    }

    // --- Projects ---
    fun addProject() {
        updateProfile { profile ->
            profile.copy(projects = profile.projects + ProjectItem())
        }
    }

    fun updateProject(id: String, transform: (ProjectItem) -> ProjectItem) {
        updateProfile { profile ->
            profile.copy(
                projects = profile.projects.map { if (it.id == id) transform(it) else it }
            )
        }
    }

    fun removeProject(id: String) {
        updateProfile { profile ->
            profile.copy(projects = profile.projects.filterNot { it.id == id })
        }
    }

    // --- Certifications ---
    fun addCertification() {
        updateProfile { profile ->
            profile.copy(certifications = profile.certifications + CertificationItem())
        }
    }

    fun updateCertification(id: String, transform: (CertificationItem) -> CertificationItem) {
        updateProfile { profile ->
            profile.copy(
                certifications = profile.certifications.map { if (it.id == id) transform(it) else it }
            )
        }
    }

    fun removeCertification(id: String) {
        updateProfile { profile ->
            profile.copy(certifications = profile.certifications.filterNot { it.id == id })
        }
    }

    // --- Languages ---
    fun addLanguage() {
        updateProfile { profile ->
            profile.copy(languages = profile.languages + LanguageItem(name = "", fluency = LanguageFluency.FLUENT))
        }
    }

    fun updateLanguage(id: String, transform: (LanguageItem) -> LanguageItem) {
        updateProfile { profile ->
            profile.copy(
                languages = profile.languages.map { if (it.id == id) transform(it) else it }
            )
        }
    }

    fun removeLanguage(id: String) {
        updateProfile { profile ->
            profile.copy(languages = profile.languages.filterNot { it.id == id })
        }
    }
}
