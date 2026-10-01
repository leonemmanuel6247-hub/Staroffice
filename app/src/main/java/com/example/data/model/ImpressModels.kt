package com.example.data.model

enum class SlideLayout {
    TITLE,
    TITLE_CONTENT,
    TWO_COLUMNS,
    BIG_STAT,
    BLANK
}

data class ImpressSlide(
    val id: String = java.util.UUID.randomUUID().toString(),
    val layout: SlideLayout = SlideLayout.TITLE_CONTENT,
    val title: String = "",
    val subtitle: String = "",
    val bullets: List<String> = emptyList(),
    val speakerNotes: String = "",
    val accentShape: String? = null, // "circle", "star", "arrow", "badge"
    val statValue: String = "",
    val statLabel: String = "",
    val leftColumnTitle: String = "",
    val leftColumnBody: String = "",
    val rightColumnTitle: String = "",
    val rightColumnBody: String = ""
)

enum class PresentationTheme(
    val title: String,
    val bgHex: String,
    val cardBgHex: String,
    val primaryTextHex: String,
    val secondaryTextHex: String,
    val accentHex: String
) {
    CORPORATE_BLUE(
        title = "Bleu Corporate",
        bgHex = "#0F172A",
        cardBgHex = "#1E293B",
        primaryTextHex = "#FFFFFF",
        secondaryTextHex = "#94A3B8",
        accentHex = "#38BDF8"
    ),
    MIDNIGHT_DARK(
        title = "Midnight Élégant",
        bgHex = "#18181B",
        cardBgHex = "#27272A",
        primaryTextHex = "#FAFAFA",
        secondaryTextHex = "#A1A1AA",
        accentHex = "#F59E0B"
    ),
    SUNSET_WARM(
        title = "Sunset Moderne",
        bgHex = "#431407",
        cardBgHex = "#7C2D12",
        primaryTextHex = "#FFF7ED",
        secondaryTextHex = "#FDBA74",
        accentHex = "#FB923C"
    ),
    EMERALD_NATURE(
        title = "Émeraude Pro",
        bgHex = "#064E3B",
        cardBgHex = "#065F46",
        primaryTextHex = "#ECFDF5",
        secondaryTextHex = "#A7F3D0",
        accentHex = "#34D399"
    ),
    CLEAN_LIGHT(
        title = "Minimaliste Blanc",
        bgHex = "#F8FAFC",
        cardBgHex = "#FFFFFF",
        primaryTextHex = "#0F172A",
        secondaryTextHex = "#64748B",
        accentHex = "#2563EB"
    )
}

data class ImpressContent(
    val slides: List<ImpressSlide> = listOf(
        ImpressSlide(
            layout = SlideLayout.TITLE,
            title = "Nouvelle Présentation",
            subtitle = "Créée avec StarOffice Impress"
        )
    ),
    val activeSlideIndex: Int = 0,
    val theme: PresentationTheme = PresentationTheme.CORPORATE_BLUE
)
