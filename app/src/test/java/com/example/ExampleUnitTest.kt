package com.example

import com.example.data.CvAccentColor
import com.example.data.CvFontFamily
import com.example.data.CvSettings
import com.example.data.CvTemplate
import com.example.data.PersistedCvState
import com.example.data.SampleCvData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun sampleProfile_hasCompleteInitialSections() {
        val sample = SampleCvData.defaultProfile()
        assertTrue(sample.personalInfo.fullName.isNotBlank())
        assertTrue(sample.summary.isNotBlank())
        assertTrue(sample.experiences.isNotEmpty())
        assertTrue(sample.education.isNotEmpty())
        assertTrue(sample.skills.isNotEmpty())
        assertTrue(sample.projects.isNotEmpty())
        assertTrue(sample.certifications.isNotEmpty())
        assertTrue(sample.languages.isNotEmpty())
    }

    @Test
    fun moshi_serializesAndDeserializesCvState() {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(PersistedCvState::class.java)
        val original = PersistedCvState(
            profile = SampleCvData.defaultProfile(),
            settings = CvSettings(
                template = CvTemplate.CREATIVE_TECH,
                accentColor = CvAccentColor.ROYAL_BLUE,
                fontFamily = CvFontFamily.GEORGIA,
                isDarkEditor = true
            )
        )
        val json = adapter.toJson(original)
        val decoded = adapter.fromJson(json)
        assertEquals(original, decoded)
    }
}
