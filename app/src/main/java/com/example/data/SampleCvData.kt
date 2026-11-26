package com.example.data

data class SummaryTemplate(
    val roleLabel: String,
    val text: String
)

object SampleCvData {

    val summaryTemplates = listOf(
        SummaryTemplate(
            roleLabel = "Software Developer",
            text = "Results-driven Software Engineer with 5+ years of experience designing scalable web and mobile applications. Skilled in TypeScript, Kotlin, cloud architectures, and leading cross-functional teams to ship high-impact products with 99.9% reliability."
        ),
        SummaryTemplate(
            roleLabel = "Business & Admin",
            text = "Detail-oriented Operations & Business Specialist with proven expertise in process optimization, stakeholder coordination, and executive reporting. Track record of reducing operational overhead by 22% while improving cross-team delivery timelines."
        ),
        SummaryTemplate(
            roleLabel = "Fresh Graduate",
            text = "Motivated Computer Science graduate with strong foundations in software engineering, data structures, and full-stack product development. Built multiple academic and open-source projects, eager to contribute to fast-paced engineering teams."
        ),
        SummaryTemplate(
            roleLabel = "Product Designer",
            text = "Human-centered Product Designer specializing in design systems, interactive prototyping, and B2B SaaS workflows. Passionate about transforming complex technical requirements into intuitive, accessible user experiences."
        )
    )

    val suggestedSkills = listOf(
        "Kotlin", "Jetpack Compose", "TypeScript", "React", "Node.js",
        "Python", "SQL", "AWS", "Docker", "System Design",
        "Product Strategy", "Agile / Scrum", "UI/UX Design", "Git & CI/CD"
    )

    fun defaultProfile(): CvProfile = CvProfile(
        personalInfo = PersonalInfo(
            fullName = "Asadullah Khan",
            headline = "Senior Full-Stack & Mobile Engineer",
            email = "asad.khan@example.com",
            phone = "+1 (415) 890-4312",
            location = "San Francisco, CA",
            linkedIn = "linkedin.com/in/asadkhan-dev",
            portfolio = "github.com/asadkhan-dev",
            photoUri = null,
            showPhoto = true,
            useSampleAvatar = true
        ),
        summary = "Senior Full-Stack & Mobile Engineer with 6+ years of experience architecting responsive web platforms and native Android applications. Proven track record of cutting latency by 40%, scaling SaaS products to 250K+ active users, and mentoring engineering squads.",
        experiences = listOf(
            WorkExperience(
                id = "exp-1",
                jobTitle = "Lead Software Engineer",
                company = "NovaCloud Systems",
                location = "San Francisco, CA",
                startDate = "Mar 2023",
                endDate = "",
                isCurrent = true,
                description = "• Architected a unified design system and real-time analytics dashboard used by 1,200+ enterprise teams.\n• Reduced mobile and web bundle load time by 42% through modular code splitting and caching.\n• Mentored 5 engineers and introduced automated CI/CD quality gates."
            ),
            WorkExperience(
                id = "exp-2",
                jobTitle = "Mobile & Web Developer",
                company = "BrightPath Labs",
                location = "Austin, TX",
                startDate = "Jun 2020",
                endDate = "Feb 2023",
                isCurrent = false,
                description = "• Built offline-first Android and React workflows serving 180K monthly active users.\n• Collaborated with product and design leads to increase onboarding completion by 28%.\n• Integrated Stripe billing and automated PDF invoice generation."
            )
        ),
        education = listOf(
            EducationItem(
                id = "edu-1",
                degree = "B.S. in Computer Science",
                institution = "University of California, Davis",
                location = "Davis, CA",
                graduationYear = "2020",
                grade = "3.85 / 4.00 GPA"
            )
        ),
        skills = listOf(
            SkillItem(id = "sk-1", name = "Kotlin & Compose", level = SkillLevel.EXPERT),
            SkillItem(id = "sk-2", name = "TypeScript & React", level = SkillLevel.EXPERT),
            SkillItem(id = "sk-3", name = "Node.js & PostgreSQL", level = SkillLevel.EXPERT),
            SkillItem(id = "sk-4", name = "System Architecture", level = SkillLevel.INTERMEDIATE),
            SkillItem(id = "sk-5", name = "AWS & Docker", level = SkillLevel.INTERMEDIATE),
            SkillItem(id = "sk-6", name = "UI/UX & Accessibility", level = SkillLevel.EXPERT)
        ),
        projects = listOf(
            ProjectItem(
                id = "proj-1",
                name = "PulseMetrics Open Analytics",
                techStack = "Kotlin, TypeScript, ClickHouse",
                link = "github.com/asadkhan-dev/pulsemetrics",
                description = "Lightweight privacy-first event telemetry engine processing 50K events/sec with real-time funnel charts."
            ),
            ProjectItem(
                id = "proj-2",
                name = "DocuCraft A4 Engine",
                techStack = "Compose, Android PdfDocument",
                link = "github.com/asadkhan-dev/docucraft",
                description = "Vector-accurate A4 layout renderer producing ATS-compliant multi-template PDF resumes."
            )
        ),
        certifications = listOf(
            CertificationItem(
                id = "cert-1",
                name = "AWS Certified Solutions Architect",
                issuer = "Amazon Web Services",
                year = "2024"
            ),
            CertificationItem(
                id = "cert-2",
                name = "Google Associate Android Developer",
                issuer = "Google Developers",
                year = "2022"
            )
        ),
        languages = listOf(
            LanguageItem(id = "lang-1", name = "English", fluency = LanguageFluency.NATIVE),
            LanguageItem(id = "lang-2", name = "Urdu", fluency = LanguageFluency.NATIVE),
            LanguageItem(id = "lang-3", name = "Spanish", fluency = LanguageFluency.BASIC)
        )
    )

    fun emptyProfile(): CvProfile = CvProfile(
        personalInfo = PersonalInfo(showPhoto = true, useSampleAvatar = false)
    )
}
