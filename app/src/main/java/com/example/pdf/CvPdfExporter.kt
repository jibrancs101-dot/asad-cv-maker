package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.CvProfile
import com.example.data.CvSettings
import com.example.data.CvTemplate
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Vector A4 (595pt x 842pt = 210mm x 297mm) PDF generator producing crisp,
 * ATS-friendly selectable text and exact layout parity across all 3 templates.
 */
object CvPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 32f
    private const val BOTTOM_LIMIT = PAGE_HEIGHT - 30f

    fun exportToUri(
        context: Context,
        uri: Uri,
        profile: CvProfile,
        settings: CvSettings
    ): Result<Unit> = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            writePdf(context, out, profile, settings)
        } ?: error("Unable to open output stream")
    }

    fun exportToCacheFile(
        context: Context,
        profile: CvProfile,
        settings: CvSettings
    ): Result<File> = runCatching {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val safeName = profile.personalInfo.fullName
            .ifBlank { "Asad_CV" }
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(dir, "${safeName}_Resume.pdf")
        FileOutputStream(file).use { out ->
            writePdf(context, out, profile, settings)
        }
        file
    }

    private fun writePdf(
        context: Context,
        outputStream: OutputStream,
        profile: CvProfile,
        settings: CvSettings
    ) {
        val doc = PdfDocument()
        val baseTypeface = loadTypeface(context, settings.fontFamily.fontResId)
        val boldTypeface = Typeface.create(baseTypeface, Typeface.BOLD)
        val italicTypeface = Typeface.create(baseTypeface, Typeface.ITALIC)
        val scale = settings.fontSize.scale
        val accentInt = settings.accentColor.colorHex.toInt()
        val softAccentInt = settings.accentColor.softHex.toInt()
        val avatarBitmap = loadAvatarBitmap(context, profile)

        val renderer = PdfRenderer(
            doc = doc,
            profile = profile,
            baseTypeface = baseTypeface,
            boldTypeface = boldTypeface,
            italicTypeface = italicTypeface,
            scale = scale,
            accentColor = accentInt,
            softAccentColor = softAccentInt,
            avatarBitmap = avatarBitmap
        )

        when (settings.template) {
            CvTemplate.MODERN_EXECUTIVE -> renderer.renderModernExecutive()
            CvTemplate.CLASSIC_MINIMALIST -> renderer.renderClassicMinimalist()
            CvTemplate.CREATIVE_TECH -> renderer.renderCreativeTech()
        }

        doc.writeTo(outputStream)
        doc.close()
        avatarBitmap?.recycle()
    }

    private fun loadTypeface(context: Context, resId: Int): Typeface {
        return runCatching { ResourcesCompat.getFont(context, resId) }.getOrNull() ?: Typeface.SANS_SERIF
    }

    private fun loadAvatarBitmap(context: Context, profile: CvProfile): Bitmap? {
        if (!profile.personalInfo.showPhoto) return null
        val uriStr = profile.personalInfo.photoUri
        if (!uriStr.isNullOrBlank()) {
            return runCatching {
                val uri = Uri.parse(uriStr)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
        if (profile.personalInfo.useSampleAvatar) {
            return runCatching {
                BitmapFactory.decodeResource(context.resources, R.drawable.img_sample_avatar)
            }.getOrNull()
        }
        return null
    }

    private class PdfRenderer(
        private val doc: PdfDocument,
        private val profile: CvProfile,
        private val baseTypeface: Typeface,
        private val boldTypeface: Typeface,
        private val italicTypeface: Typeface,
        private val scale: Float,
        private val accentColor: Int,
        private val softAccentColor: Int,
        private val avatarBitmap: Bitmap?
    ) {
        private var pageNumber = 0
        private lateinit var currentPage: PdfDocument.Page
        private lateinit var canvas: Canvas

        private val inkDark = Color.parseColor("#111827")
        private val inkMuted = Color.parseColor("#4B5563")
        private val dividerGray = Color.parseColor("#E5E7EB")
        private val trackGray = Color.parseColor("#E2E8F0")

        private fun startPage(drawSidebarBg: Boolean = false, sidebarWidth: Float = 0f) {
            if (pageNumber > 0) {
                doc.finishPage(currentPage)
            }
            pageNumber++
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            currentPage = doc.startPage(pageInfo)
            canvas = currentPage.canvas
            canvas.drawColor(Color.WHITE)
            if (drawSidebarBg && sidebarWidth > 0f) {
                val paint = Paint().apply { color = accentColor; style = Paint.Style.FILL }
                canvas.drawRect(0f, 0f, sidebarWidth, PAGE_HEIGHT.toFloat(), paint)
            }
        }

        private fun finishDocument() {
            if (pageNumber > 0) {
                doc.finishPage(currentPage)
            }
        }

        private fun makeTextPaint(
            sizeSp: Float,
            color: Int,
            bold: Boolean = false,
            italic: Boolean = false
        ): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizeSp * scale
            this.color = color
            typeface = when {
                bold -> boldTypeface
                italic -> italicTypeface
                else -> baseTypeface
            }
        }

        private fun measureLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
            return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(10))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2.2f, 1.05f)
                .setIncludePad(false)
                .build()
        }

        private fun drawTextBlock(
            text: String,
            x: Float,
            y: Float,
            width: Float,
            paint: TextPaint
        ): Float {
            if (text.isBlank()) return y
            val layout = measureLayout(text, paint, width.toInt())
            canvas.save()
            canvas.translate(x, y)
            layout.draw(canvas)
            canvas.restore()
            return y + layout.height
        }

        private fun drawCircularBitmap(bitmap: Bitmap, cx: Float, cy: Float, radius: Float, borderColor: Int) {
            val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            val save = canvas.save()
            val path = Path().apply { addCircle(cx, cy, radius, Path.Direction.CW) }
            canvas.clipPath(path)

            val srcRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val dstRect = RectF(rect)
            if (srcRatio > 1f) {
                val extra = (dstRect.height() * srcRatio - dstRect.width()) / 2f
                dstRect.left -= extra
                dstRect.right += extra
            } else {
                val extra = (dstRect.width() / srcRatio - dstRect.height()) / 2f
                dstRect.top -= extra
                dstRect.bottom += extra
            }
            val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bitmap, null, dstRect, bmpPaint)
            canvas.restoreToCount(save)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawCircle(cx, cy, radius, borderPaint)
        }

        private fun drawSectionHeader(title: String, x: Float, y: Float, width: Float, underline: Boolean = true): Float {
            val paint = makeTextPaint(10.5f, accentColor, bold = true)
            val nextY = drawTextBlock(title.uppercase(), x, y, width, paint)
            if (underline) {
                val linePaint = Paint().apply { color = accentColor; strokeWidth = 1.2f }
                canvas.drawLine(x, nextY + 3f, x + width, nextY + 3f, linePaint)
                return nextY + 9f
            }
            return nextY + 5f
        }

        // =========================================================================
        // TEMPLATE 1: MODERN EXECUTIVE (Two-column with colored left sidebar)
        // =========================================================================
        fun renderModernExecutive() {
            val sidebarW = 188f
            val sidePad = 18f
            val sideContentW = sidebarW - sidePad * 2
            val mainX = sidebarW + 22f
            val mainW = PAGE_WIDTH - mainX - 24f

            startPage(drawSidebarBg = true, sidebarWidth = sidebarW)

            // Draw Sidebar Content on Page 1
            var sy = 26f
            if (avatarBitmap != null) {
                drawCircularBitmap(avatarBitmap, sidebarW / 2f, sy + 36f, 36f, Color.argb(120, 255, 255, 255))
                sy += 86f
            }

            val whiteTitle = makeTextPaint(9.5f, Color.WHITE, bold = true)
            val whiteBody = makeTextPaint(8.3f, Color.argb(235, 255, 255, 255))
            val whiteMuted = makeTextPaint(7.8f, Color.argb(190, 255, 255, 255))
            val sideDivider = Paint().apply { color = Color.argb(60, 255, 255, 255); strokeWidth = 0.8f }

            // Contact
            val contacts = listOfNotNull(
                profile.personalInfo.email.takeIf { it.isNotBlank() },
                profile.personalInfo.phone.takeIf { it.isNotBlank() },
                profile.personalInfo.location.takeIf { it.isNotBlank() },
                profile.personalInfo.linkedIn.takeIf { it.isNotBlank() },
                profile.personalInfo.portfolio.takeIf { it.isNotBlank() }
            )
            if (contacts.isNotEmpty()) {
                sy = drawTextBlock("CONTACT", sidePad, sy, sideContentW, whiteTitle)
                canvas.drawLine(sidePad, sy + 3f, sidePad + sideContentW, sy + 3f, sideDivider)
                sy += 8f
                contacts.forEach { c ->
                    sy = drawTextBlock(c, sidePad, sy, sideContentW, whiteBody) + 4f
                }
                sy += 10f
            }

            // Skills in sidebar
            if (profile.skills.isNotEmpty()) {
                sy = drawTextBlock("SKILLS", sidePad, sy, sideContentW, whiteTitle)
                canvas.drawLine(sidePad, sy + 3f, sidePad + sideContentW, sy + 3f, sideDivider)
                sy += 8f
                profile.skills.forEach { skill ->
                    if (sy < BOTTOM_LIMIT - 20f) {
                        sy = drawTextBlock(skill.name, sidePad, sy, sideContentW, whiteBody) + 3f
                        val bgBar = Paint().apply { color = Color.argb(55, 255, 255, 255) }
                        val fgBar = Paint().apply { color = Color.WHITE }
                        canvas.drawRoundRect(sidePad, sy, sidePad + sideContentW, sy + 3.5f, 2f, 2f, bgBar)
                        canvas.drawRoundRect(sidePad, sy, sidePad + sideContentW * skill.level.ratio, sy + 3.5f, 2f, 2f, fgBar)
                        sy += 9f
                    }
                }
                sy += 8f
            }

            // Languages in sidebar
            if (profile.languages.isNotEmpty()) {
                sy = drawTextBlock("LANGUAGES", sidePad, sy, sideContentW, whiteTitle)
                canvas.drawLine(sidePad, sy + 3f, sidePad + sideContentW, sy + 3f, sideDivider)
                sy += 8f
                profile.languages.forEach { lang ->
                    if (sy < BOTTOM_LIMIT - 16f && lang.name.isNotBlank()) {
                        sy = drawTextBlock("${lang.name} — ${lang.fluency.label}", sidePad, sy, sideContentW, whiteMuted) + 4f
                    }
                }
            }

            // Main Body Column
            var my = 28f
            val namePaint = makeTextPaint(20f, inkDark, bold = true)
            val headlinePaint = makeTextPaint(11f, accentColor, bold = true)
            my = drawTextBlock(profile.personalInfo.fullName.ifBlank { "Your Name" }, mainX, my, mainW, namePaint) + 2f
            if (profile.personalInfo.headline.isNotBlank()) {
                my = drawTextBlock(profile.personalInfo.headline, mainX, my, mainW, headlinePaint) + 12f
            } else {
                my += 8f
            }

            fun ensureMainSpace(neededHeight: Float) {
                if (my + neededHeight > BOTTOM_LIMIT) {
                    startPage(drawSidebarBg = true, sidebarWidth = sidebarW)
                    my = 28f
                }
            }

            my = renderMainSections(
                x = mainX,
                startY = my,
                width = mainW,
                includeSkills = false,
                includeLanguages = false,
                ensureSpace = { needed ->
                    if (my + needed > BOTTOM_LIMIT) {
                        startPage(drawSidebarBg = true, sidebarWidth = sidebarW)
                        my = 28f
                    }
                    my
                }
            )
            finishDocument()
        }

        // =========================================================================
        // TEMPLATE 2: CLASSIC MINIMALIST (Single column corporate)
        // =========================================================================
        fun renderClassicMinimalist() {
            val x = MARGIN
            val w = PAGE_WIDTH - MARGIN * 2
            startPage()
            var y = 30f

            val namePaint = makeTextPaint(21f, inkDark, bold = true)
            val headlinePaint = makeTextPaint(11f, accentColor, bold = true)
            val contactPaint = makeTextPaint(8.5f, inkMuted)

            if (avatarBitmap != null) {
                drawCircularBitmap(avatarBitmap, x + 28f, y + 28f, 28f, accentColor)
                val hx = x + 68f
                val hw = w - 68f
                var hy = drawTextBlock(profile.personalInfo.fullName.ifBlank { "Your Name" }, hx, y + 2f, hw, namePaint) + 2f
                if (profile.personalInfo.headline.isNotBlank()) {
                    hy = drawTextBlock(profile.personalInfo.headline, hx, hy, hw, headlinePaint) + 4f
                }
                val contactLine = listOfNotNull(
                    profile.personalInfo.email.takeIf { it.isNotBlank() },
                    profile.personalInfo.phone.takeIf { it.isNotBlank() },
                    profile.personalInfo.location.takeIf { it.isNotBlank() },
                    profile.personalInfo.linkedIn.takeIf { it.isNotBlank() },
                    profile.personalInfo.portfolio.takeIf { it.isNotBlank() }
                ).joinToString("  •  ")
                if (contactLine.isNotBlank()) {
                    hy = drawTextBlock(contactLine, hx, hy, hw, contactPaint)
                }
                y = maxOf(y + 62f, hy + 10f)
            } else {
                y = drawTextBlock(profile.personalInfo.fullName.ifBlank { "Your Name" }, x, y, w, namePaint) + 2f
                if (profile.personalInfo.headline.isNotBlank()) {
                    y = drawTextBlock(profile.personalInfo.headline, x, y, w, headlinePaint) + 4f
                }
                val contactLine = listOfNotNull(
                    profile.personalInfo.email.takeIf { it.isNotBlank() },
                    profile.personalInfo.phone.takeIf { it.isNotBlank() },
                    profile.personalInfo.location.takeIf { it.isNotBlank() },
                    profile.personalInfo.linkedIn.takeIf { it.isNotBlank() },
                    profile.personalInfo.portfolio.takeIf { it.isNotBlank() }
                ).joinToString("  •  ")
                if (contactLine.isNotBlank()) {
                    y = drawTextBlock(contactLine, x, y, w, contactPaint) + 8f
                }
            }

            val hrPaint = Paint().apply { color = accentColor; strokeWidth = 1.6f }
            canvas.drawLine(x, y, x + w, y, hrPaint)
            y += 14f

            renderMainSections(
                x = x,
                startY = y,
                width = w,
                includeSkills = true,
                includeLanguages = true,
                ensureSpace = { needed ->
                    if (y + needed > BOTTOM_LIMIT) {
                        startPage()
                        y = 30f
                    }
                    y
                }
            )
            finishDocument()
        }

        // =========================================================================
        // TEMPLATE 3: CREATIVE TECH (Banner header + timeline dots)
        // =========================================================================
        fun renderCreativeTech() {
            startPage()
            val bannerHeight = 104f
            val bannerPaint = Paint().apply { color = accentColor; style = Paint.Style.FILL }
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), bannerHeight, bannerPaint)

            val x = MARGIN
            val w = PAGE_WIDTH - MARGIN * 2
            var textX = x
            var textW = w
            if (avatarBitmap != null) {
                drawCircularBitmap(avatarBitmap, x + 32f, bannerHeight / 2f, 32f, Color.WHITE)
                textX = x + 78f
                textW = w - 78f
            }

            val namePaint = makeTextPaint(20f, Color.WHITE, bold = true)
            val headlinePaint = makeTextPaint(10.5f, Color.argb(230, 255, 255, 255), bold = true)
            val contactPaint = makeTextPaint(8.0f, Color.argb(215, 255, 255, 255))

            var by = 22f
            by = drawTextBlock(profile.personalInfo.fullName.ifBlank { "Your Name" }, textX, by, textW, namePaint) + 2f
            if (profile.personalInfo.headline.isNotBlank()) {
                by = drawTextBlock(profile.personalInfo.headline, textX, by, textW, headlinePaint) + 5f
            }
            val contactLine = listOfNotNull(
                profile.personalInfo.email.takeIf { it.isNotBlank() },
                profile.personalInfo.phone.takeIf { it.isNotBlank() },
                profile.personalInfo.location.takeIf { it.isNotBlank() },
                profile.personalInfo.linkedIn.takeIf { it.isNotBlank() },
                profile.personalInfo.portfolio.takeIf { it.isNotBlank() }
            ).joinToString("  |  ")
            if (contactLine.isNotBlank()) {
                drawTextBlock(contactLine, textX, by, textW, contactPaint)
            }

            var y = bannerHeight + 18f
            renderMainSections(
                x = x,
                startY = y,
                width = w,
                includeSkills = true,
                includeLanguages = true,
                useTimelineDots = true,
                ensureSpace = { needed ->
                    if (y + needed > BOTTOM_LIMIT) {
                        startPage()
                        y = 30f
                    }
                    y
                }
            )
            finishDocument()
        }

        private fun renderMainSections(
            x: Float,
            startY: Float,
            width: Float,
            includeSkills: Boolean,
            includeLanguages: Boolean,
            useTimelineDots: Boolean = false,
            ensureSpace: (Float) -> Float
        ): Float {
            var y = startY
            val bodyPaint = makeTextPaint(9.0f, inkDark)
            val mutedPaint = makeTextPaint(8.2f, inkMuted)
            val boldTitlePaint = makeTextPaint(9.8f, inkDark, bold = true)
            val accentSubPaint = makeTextPaint(8.8f, accentColor, bold = true)

            // Summary
            if (profile.summary.isNotBlank()) {
                y = ensureSpace(55f)
                y = drawSectionHeader("Professional Summary", x, y, width)
                y = drawTextBlock(profile.summary, x, y, width, bodyPaint) + 12f
            }

            // Work Experience
            if (profile.experiences.isNotEmpty()) {
                y = ensureSpace(60f)
                y = drawSectionHeader("Work Experience", x, y, width)
                profile.experiences.forEach { exp ->
                    val descH = if (exp.description.isNotBlank()) {
                        measureLayout(exp.description, bodyPaint, width.toInt()).height.toFloat()
                    } else 0f
                    y = ensureSpace(42f + descH)

                    val contentX = if (useTimelineDots) x + 14f else x
                    val contentW = if (useTimelineDots) width - 14f else width

                    if (useTimelineDots) {
                        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
                        canvas.drawCircle(x + 4f, y + 6f, 3.2f, dotPaint)
                    }

                    val titleText = exp.jobTitle.ifBlank { "Role Title" }
                    val dateRange = buildString {
                        append(exp.startDate)
                        if (exp.startDate.isNotBlank() && (exp.isCurrent || exp.endDate.isNotBlank())) append(" – ")
                        append(if (exp.isCurrent) "Present" else exp.endDate)
                    }
                    val titleEndY = drawTextBlock(titleText, contentX, y, contentW * 0.68f, boldTitlePaint)
                    if (dateRange.isNotBlank()) {
                        val datePaint = makeTextPaint(8.2f, inkMuted).apply { textAlign = Paint.Align.RIGHT }
                        canvas.drawText(dateRange, contentX + contentW, y + 9f * scale, datePaint)
                    }
                    y = titleEndY + 1f
                    val companyLine = listOfNotNull(
                        exp.company.takeIf { it.isNotBlank() },
                        exp.location.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    if (companyLine.isNotBlank()) {
                        y = drawTextBlock(companyLine, contentX, y, contentW, accentSubPaint) + 3f
                    }
                    if (exp.description.isNotBlank()) {
                        y = drawTextBlock(exp.description, contentX, y, contentW, bodyPaint)
                    }
                    y += 10f
                }
            }

            // Education
            if (profile.education.isNotEmpty()) {
                y = ensureSpace(50f)
                y = drawSectionHeader("Education", x, y, width)
                profile.education.forEach { edu ->
                    y = ensureSpace(36f)
                    val titleEndY = drawTextBlock(edu.degree.ifBlank { "Degree" }, x, y, width * 0.72f, boldTitlePaint)
                    if (edu.graduationYear.isNotBlank()) {
                        val yrPaint = makeTextPaint(8.2f, inkMuted).apply { textAlign = Paint.Align.RIGHT }
                        canvas.drawText(edu.graduationYear, x + width, y + 9f * scale, yrPaint)
                    }
                    y = titleEndY + 1f
                    val sub = listOfNotNull(
                        edu.institution.takeIf { it.isNotBlank() },
                        edu.location.takeIf { it.isNotBlank() },
                        edu.grade.takeIf { it.isNotBlank() }?.let { "Grade: $it" }
                    ).joinToString(" • ")
                    if (sub.isNotBlank()) {
                        y = drawTextBlock(sub, x, y, width, mutedPaint)
                    }
                    y += 8f
                }
            }

            // Skills (for single-column / Creative Tech)
            if (includeSkills && profile.skills.isNotEmpty()) {
                y = ensureSpace(50f)
                y = drawSectionHeader("Core Skills", x, y, width)
                val skillsText = profile.skills.joinToString("  •  ") { "${it.name} (${it.level.label})" }
                y = drawTextBlock(skillsText, x, y, width, bodyPaint) + 12f
            }

            // Projects
            if (profile.projects.isNotEmpty()) {
                y = ensureSpace(50f)
                y = drawSectionHeader("Projects", x, y, width)
                profile.projects.forEach { proj ->
                    val descH = if (proj.description.isNotBlank()) {
                        measureLayout(proj.description, bodyPaint, width.toInt()).height.toFloat()
                    } else 0f
                    y = ensureSpace(34f + descH)
                    y = drawTextBlock(proj.name.ifBlank { "Project" }, x, y, width, boldTitlePaint) + 1f
                    val meta = listOfNotNull(
                        proj.techStack.takeIf { it.isNotBlank() },
                        proj.link.takeIf { it.isNotBlank() }
                    ).joinToString("  •  ")
                    if (meta.isNotBlank()) {
                        y = drawTextBlock(meta, x, y, width, accentSubPaint) + 2f
                    }
                    if (proj.description.isNotBlank()) {
                        y = drawTextBlock(proj.description, x, y, width, bodyPaint)
                    }
                    y += 8f
                }
            }

            // Certifications
            if (profile.certifications.isNotEmpty()) {
                y = ensureSpace(45f)
                y = drawSectionHeader("Certifications", x, y, width)
                profile.certifications.forEach { cert ->
                    y = ensureSpace(24f)
                    val line = buildString {
                        append(cert.name)
                        if (cert.issuer.isNotBlank()) append(" — ${cert.issuer}")
                        if (cert.year.isNotBlank()) append(" (${cert.year})")
                    }
                    y = drawTextBlock(line, x, y, width, bodyPaint) + 5f
                }
                y += 6f
            }

            // Languages
            if (includeLanguages && profile.languages.isNotEmpty()) {
                y = ensureSpace(40f)
                y = drawSectionHeader("Languages", x, y, width)
                val langText = profile.languages
                    .filter { it.name.isNotBlank() }
                    .joinToString("  •  ") { "${it.name} (${it.fluency.label})" }
                y = drawTextBlock(langText, x, y, width, bodyPaint) + 8f
            }

            return y
        }
    }
}
