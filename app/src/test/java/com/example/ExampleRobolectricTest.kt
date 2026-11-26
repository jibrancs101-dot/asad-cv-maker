package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CvAccentColor
import com.example.data.CvFontFamily
import com.example.data.CvSettings
import com.example.data.CvTemplate
import com.example.data.PersistedCvState
import com.example.data.SampleCvData
import com.example.pdf.CvPdfExporter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Asad CV Maker", appName)
    }

    @Test
    fun `export pdf succeeds for all three templates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = SampleCvData.defaultProfile()

        CvTemplate.entries.forEach { template ->
            val settings = CvSettings(
                template = template,
                accentColor = CvAccentColor.EMERALD,
                fontFamily = CvFontFamily.INTER
            )
            val result = CvPdfExporter.exportToCacheFile(context, profile, settings)
            assertTrue("Expected PDF export success for $template", result.isSuccess)
            val file = result.getOrThrow()
            assertTrue("Expected non-empty PDF for $template", file.exists() && file.length() > 100L)
        }
    }

    @Test
    fun `moshi serializes and deserializes cv state cleanly`() {
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
