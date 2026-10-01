package com.example.data.repository

import com.example.data.model.*

object DefaultTemplates {

    fun getAllDefaultDocuments(): List<OfficeDocument> {
        val now = System.currentTimeMillis()
        return listOf(
            createStrategicReport(now - 3600000 * 2),
            createPersonalBudget(now - 3600000 * 5),
            createStartupPitch(now - 3600000 * 8),
            createServiceContractPdf(now - 3600000 * 12),
            createModernResume(now - 3600000 * 24),
            createCommercialInvoice(now - 3600000 * 30),
            createMeetingMinutes(now - 3600000 * 48)
        )
    }

    // --- WRITER 1: Rapport Stratégique ---
    fun createStrategicReport(time: Long = System.currentTimeMillis()): OfficeDocument {
        val blocks = listOf(
            WriterBlock(
                type = BlockType.HEADING_1,
                text = "Rapport d'Activité et Stratégie 2026",
                isBold = true,
                alignment = BlockAlignment.CENTER,
                textColorHex = "#1E3A8A"
            ),
            WriterBlock(
                type = BlockType.PARAGRAPH,
                text = "Direction Générale — Document Officiel StarOffice",
                alignment = BlockAlignment.CENTER,
                isItalic = true,
                textColorHex = "#64748B"
            ),
            WriterBlock(type = BlockType.DIVIDER, text = "---"),
            WriterBlock(
                type = BlockType.HEADING_2,
                text = "1. Résumé Exécutif",
                isBold = true,
                textColorHex = "#2563EB"
            ),
            WriterBlock(
                type = BlockType.PARAGRAPH,
                text = "L'exercice actuel démontre une accélération marquée de l'adoption des solutions bureautiques mobiles ouvertes et respectueuses de la vie privée. Nos indicateurs de performance clés (KPI) enregistrent une progression de +34% sur l'ensemble des segments opérationnels."
            ),
            WriterBlock(
                type = BlockType.HEADING_2,
                text = "2. Priorités Stratégiques",
                isBold = true,
                textColorHex = "#2563EB"
            ),
            WriterBlock(
                type = BlockType.BULLET_ITEM,
                text = "Confidentialité et Souveraineté Numérique : aucun traqueur, stockage 100% local."
            ),
            WriterBlock(
                type = BlockType.BULLET_ITEM,
                text = "Interopérabilité maximale avec les formats standards Microsoft Office (.docx, .xlsx, .pptx) et OpenDocument (.odt, .ods, .odp)."
            ),
            WriterBlock(
                type = BlockType.BULLET_ITEM,
                text = "Expérience tactile enrichie pour smartphones et tablettes Android."
            ),
            WriterBlock(
                type = BlockType.HEADING_2,
                text = "3. Conclusion & Perspectives",
                isBold = true,
                textColorHex = "#2563EB"
            ),
            WriterBlock(
                type = BlockType.PARAGRAPH,
                text = "Les jalons fixés pour le semestre à venir confirment la robustesse de notre modèle pérenne et autonome."
            )
        )

        val table = WriterTable(
            rows = 4,
            cols = 3,
            cells = listOf(
                listOf("Trimestre", "Objectif (€)", "Réalisé (€)"),
                listOf("T1 2026", "150 000", "168 500"),
                listOf("T2 2026", "180 000", "195 200"),
                listOf("T3 2026 (Est.)", "210 000", "232 000")
            )
        )

        return OfficeDocument(
            id = "template_writer_report",
            title = "Rapport Stratégique 2026",
            type = DocumentType.WRITER,
            extension = ".docx",
            createdAt = time,
            updatedAt = time,
            isFavorite = true,
            sizeBytes = 42500,
            tags = listOf("Stratégie", "Rapport", "Officiel"),
            writerContent = WriterContent(blocks = blocks, table = table, author = "Direction StarOffice")
        )
    }

    // --- WRITER 2: Curriculum Vitae ---
    fun createModernResume(time: Long = System.currentTimeMillis()): OfficeDocument {
        val blocks = listOf(
            WriterBlock(
                type = BlockType.HEADING_1,
                text = "Alexandre Dupont — Ingénieur Logiciel",
                isBold = true,
                alignment = BlockAlignment.CENTER,
                textColorHex = "#0F172A"
            ),
            WriterBlock(
                type = BlockType.PARAGRAPH,
                text = "Paris, France • contact@alexandredupont.fr • +33 6 12 34 56 78",
                alignment = BlockAlignment.CENTER,
                isItalic = true,
                textColorHex = "#475569"
            ),
            WriterBlock(type = BlockType.DIVIDER, text = "---"),
            WriterBlock(type = BlockType.HEADING_2, text = "Profil Professionnel", isBold = true, textColorHex = "#2563EB"),
            WriterBlock(
                type = BlockType.PARAGRAPH,
                text = "Ingénieur passionné par le développement d'applications Android performantes en Kotlin et Jetpack Compose. 6 ans d'expérience dans l'architecture logicielle, la sécurité des données et les suites bureautiques modernes."
            ),
            WriterBlock(type = BlockType.HEADING_2, text = "Compétences Clés", isBold = true, textColorHex = "#2563EB"),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Langages : Kotlin, Java, Python, C++, SQL"),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Frameworks : Jetpack Compose, Room, Coroutines, Flow, Retrofit"),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Outils : Git, CI/CD, Gradle, Architecture MVVM / Clean Architecture"),
            WriterBlock(type = BlockType.HEADING_2, text = "Expériences Récentes", isBold = true, textColorHex = "#2563EB"),
            WriterBlock(type = BlockType.PARAGRAPH, text = "Lead Développeur Mobile — OpenSoft Studio (2023 - Présent)", isBold = true),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Conception d'une suite applicative hors ligne utilisée par 150 000 utilisateurs actifs."),
            WriterBlock(type = BlockType.PARAGRAPH, text = "Développeur Android Senior — TechSolutions (2020 - 2023)", isBold = true),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Refonte complète vers Compose et optimisation des performances de rendu.")
        )

        return OfficeDocument(
            id = "template_writer_cv",
            title = "Curriculum Vitae Moderne",
            type = DocumentType.WRITER,
            extension = ".docx",
            createdAt = time,
            updatedAt = time,
            isFavorite = false,
            sizeBytes = 28900,
            tags = listOf("Emploi", "CV", "Carrière"),
            writerContent = WriterContent(blocks = blocks, author = "Alexandre Dupont")
        )
    }

    // --- WRITER 3: Compte Rendu ---
    fun createMeetingMinutes(time: Long = System.currentTimeMillis()): OfficeDocument {
        val blocks = listOf(
            WriterBlock(type = BlockType.HEADING_1, text = "Compte-Rendu de Réunion Hebdomadaire", isBold = true, textColorHex = "#1E3A8A"),
            WriterBlock(type = BlockType.PARAGRAPH, text = "Date : Octobre 2026 | Participants : Alice, Thomas, Marc, Sophie", isItalic = true, textColorHex = "#64748B"),
            WriterBlock(type = BlockType.DIVIDER, text = "---"),
            WriterBlock(type = BlockType.HEADING_2, text = "Ordre du Jour", isBold = true, textColorHex = "#2563EB"),
            WriterBlock(type = BlockType.NUMBERED_ITEM, text = "Point d'avancement sur la version 1.0 de StarOffice"),
            WriterBlock(type = BlockType.NUMBERED_ITEM, text = "Validation des modules Calc et Impress"),
            WriterBlock(type = BlockType.NUMBERED_ITEM, text = "Plan de tests et lancement beta"),
            WriterBlock(type = BlockType.HEADING_2, text = "Décisions & Actions", isBold = true, textColorHex = "#2563EB"),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Action Alice : Finaliser le moteur de calcul des formules du tableur."),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Action Thomas : Intégrer les graphiques interactifs (Barres, Camemberts)."),
            WriterBlock(type = BlockType.BULLET_ITEM, text = "Action Sophie : Revue de l'ergonomie tactile pour tablettes.")
        )

        return OfficeDocument(
            id = "template_writer_meeting",
            title = "Compte-Rendu Réunion Hebdo",
            type = DocumentType.WRITER,
            extension = ".odt",
            createdAt = time,
            updatedAt = time,
            isFavorite = false,
            sizeBytes = 18400,
            tags = listOf("Réunion", "Minutes", "Équipe"),
            writerContent = WriterContent(blocks = blocks, author = "Secrétariat de séance")
        )
    }

    // --- CALC 1: Budget Mensuel Personnel ---
    fun createPersonalBudget(time: Long = System.currentTimeMillis()): OfficeDocument {
        val cells = mutableMapOf<String, CalcCell>()
        // Headers
        cells["A1"] = CalcCell(rawValue = "Catégorie", isBold = true, bgColorHex = "#D1FAE5", textColorHex = "#065F46")
        cells["B1"] = CalcCell(rawValue = "Budget (€)", isBold = true, bgColorHex = "#D1FAE5", textColorHex = "#065F46", alignment = BlockAlignment.RIGHT)
        cells["C1"] = CalcCell(rawValue = "Réel (€)", isBold = true, bgColorHex = "#D1FAE5", textColorHex = "#065F46", alignment = BlockAlignment.RIGHT)
        cells["D1"] = CalcCell(rawValue = "Écart (€)", isBold = true, bgColorHex = "#D1FAE5", textColorHex = "#065F46", alignment = BlockAlignment.RIGHT)

        // Rows
        val data = listOf(
            Triple("Logement & Charges", "950", "950"),
            Triple("Alimentation & Courses", "450", "420"),
            Triple("Transports & Véhicule", "180", "165"),
            Triple("Loisirs & Sorties", "250", "280"),
            Triple("Épargne & Investissement", "600", "650")
        )

        data.forEachIndexed { idx, (cat, bud, reel) ->
            val row = idx + 2
            cells["A$row"] = CalcCell(rawValue = cat)
            cells["B$row"] = CalcCell(rawValue = bud, format = CellFormat.CURRENCY_EUR, alignment = BlockAlignment.RIGHT)
            cells["C$row"] = CalcCell(rawValue = reel, format = CellFormat.CURRENCY_EUR, alignment = BlockAlignment.RIGHT)
            cells["D$row"] = CalcCell(rawValue = "=B$row-C$row", format = CellFormat.CURRENCY_EUR, alignment = BlockAlignment.RIGHT)
        }

        // Totals
        val totalRow = data.size + 2
        cells["A$totalRow"] = CalcCell(rawValue = "TOTAL GÉNÉRAL", isBold = true, bgColorHex = "#A7F3D0", textColorHex = "#064E3B")
        cells["B$totalRow"] = CalcCell(rawValue = "=SUM(B2:B6)", isBold = true, format = CellFormat.CURRENCY_EUR, bgColorHex = "#A7F3D0", textColorHex = "#064E3B", alignment = BlockAlignment.RIGHT)
        cells["C$totalRow"] = CalcCell(rawValue = "=SUM(C2:C6)", isBold = true, format = CellFormat.CURRENCY_EUR, bgColorHex = "#A7F3D0", textColorHex = "#064E3B", alignment = BlockAlignment.RIGHT)
        cells["D$totalRow"] = CalcCell(rawValue = "=SUM(D2:D6)", isBold = true, format = CellFormat.CURRENCY_EUR, bgColorHex = "#A7F3D0", textColorHex = "#064E3B", alignment = BlockAlignment.RIGHT)

        val charts = listOf(
            CalcChart(
                title = "Répartition des Dépenses Réelles",
                type = ChartType.BAR,
                labelColumn = "A",
                valueColumn = "C",
                startRow = 2,
                endRow = 6
            )
        )

        val sheet = CalcSheet(
            name = "Budget Octobre",
            cells = cells,
            charts = charts
        )

        return OfficeDocument(
            id = "template_calc_budget",
            title = "Budget Mensuel Personnel",
            type = DocumentType.CALC,
            extension = ".xlsx",
            createdAt = time,
            updatedAt = time,
            isFavorite = true,
            sizeBytes = 35200,
            tags = listOf("Finance", "Budget", "Dépenses"),
            calcContent = CalcContent(sheets = listOf(sheet))
        )
    }

    // --- CALC 2: Facture Commerciale ---
    fun createCommercialInvoice(time: Long = System.currentTimeMillis()): OfficeDocument {
        val cells = mutableMapOf<String, CalcCell>()
        cells["A1"] = CalcCell(rawValue = "FACTURE N° FAC-2026-089", isBold = true, textColorHex = "#1E3A8A")
        cells["A2"] = CalcCell(rawValue = "Client : Société ABC Solutions", isItalic = true)

        cells["A4"] = CalcCell(rawValue = "Description", isBold = true, bgColorHex = "#E2E8F0")
        cells["B4"] = CalcCell(rawValue = "Qté", isBold = true, bgColorHex = "#E2E8F0", alignment = BlockAlignment.RIGHT)
        cells["C4"] = CalcCell(rawValue = "Prix Unit. (€)", isBold = true, bgColorHex = "#E2E8F0", alignment = BlockAlignment.RIGHT)
        cells["D4"] = CalcCell(rawValue = "Total HT (€)", isBold = true, bgColorHex = "#E2E8F0", alignment = BlockAlignment.RIGHT)

        val items = listOf(
            Triple("Développement module mobile", "5", "650"),
            Triple("Design UI/UX et maquettes Compose", "3", "500"),
            Triple("Audit de sécurité & confidentialité", "2", "800"),
            Triple("Documentation technique & formation", "1", "450")
        )

        items.forEachIndexed { idx, (desc, qte, pu) ->
            val row = idx + 5
            cells["A$row"] = CalcCell(rawValue = desc)
            cells["B$row"] = CalcCell(rawValue = qte, format = CellFormat.NUMBER, alignment = BlockAlignment.RIGHT)
            cells["C$row"] = CalcCell(rawValue = pu, format = CellFormat.CURRENCY_EUR, alignment = BlockAlignment.RIGHT)
            cells["D$row"] = CalcCell(rawValue = "=B$row*C$row", format = CellFormat.CURRENCY_EUR, alignment = BlockAlignment.RIGHT)
        }

        cells["C9"] = CalcCell(rawValue = "Sous-total HT", isBold = true, alignment = BlockAlignment.RIGHT)
        cells["D9"] = CalcCell(rawValue = "=SUM(D5:D8)", isBold = true, format = CellFormat.CURRENCY_EUR, alignment = BlockAlignment.RIGHT)

        cells["C10"] = CalcCell(rawValue = "TVA (20%)", isBold = true, alignment = BlockAlignment.RIGHT)
        cells["D10"] = CalcCell(rawValue = "=D9*0.2", format = CellFormat.CURRENCY_EUR, alignment = BlockAlignment.RIGHT)

        cells["C11"] = CalcCell(rawValue = "TOTAL TTC (€)", isBold = true, bgColorHex = "#FEF3C7", textColorHex = "#92400E", alignment = BlockAlignment.RIGHT)
        cells["D11"] = CalcCell(rawValue = "=D9+D10", isBold = true, format = CellFormat.CURRENCY_EUR, bgColorHex = "#FEF3C7", textColorHex = "#92400E", alignment = BlockAlignment.RIGHT)

        val sheet = CalcSheet(name = "Facture 2026-089", cells = cells)
        return OfficeDocument(
            id = "template_calc_invoice",
            title = "Facture Commerciale & Devis",
            type = DocumentType.CALC,
            extension = ".xlsx",
            createdAt = time,
            updatedAt = time,
            isFavorite = false,
            sizeBytes = 29500,
            tags = listOf("Facturation", "Vente", "Comptabilité"),
            calcContent = CalcContent(sheets = listOf(sheet))
        )
    }

    // --- IMPRESS 1: Pitch Deck ---
    fun createStartupPitch(time: Long = System.currentTimeMillis()): OfficeDocument {
        val slides = listOf(
            ImpressSlide(
                id = "slide_pitch_1",
                layout = SlideLayout.TITLE,
                title = "StarOffice Suite",
                subtitle = "La suite bureautique mobile libre, légère et souveraine",
                speakerNotes = "Introduction percutante : présenter la vision du projet et l'indépendance technologique."
            ),
            ImpressSlide(
                id = "slide_pitch_2",
                layout = SlideLayout.TITLE_CONTENT,
                title = "Le Problème du Marché",
                subtitle = "Dépendance aux abonnements et opacité des données",
                bullets = listOf(
                    "Suites payantes avec modèles d'abonnement coûteux (365, WPS)",
                    "Téléversement forcé des données confidentielles sur des serveurs distants",
                    "Applications mobiles lourdes et encombrées de publicités intempestives"
                ),
                speakerNotes = "Souligner la frustration des professionnels et particuliers face aux abonnements récurrents."
            ),
            ImpressSlide(
                id = "slide_pitch_3",
                layout = SlideLayout.BIG_STAT,
                title = "Impact & Croissance",
                subtitle = "Une adoption rapide et autonome",
                statValue = "100%",
                statLabel = "Hors-ligne & Zéro traceur publicitaire",
                speakerNotes = "Mettre en avant notre engagement absolu envers la vie privée des utilisateurs."
            ),
            ImpressSlide(
                id = "slide_pitch_4",
                layout = SlideLayout.TWO_COLUMNS,
                title = "Modules & Fonctionnalités",
                subtitle = "Une suite 4-en-1 tout-en-un",
                leftColumnTitle = "Création & Données",
                leftColumnBody = "• Writer : Traitement de texte enrichi avec export PDF et formats Word\n• Calc : Tableur avec formules, graphiques dynamiques et filtres",
                rightColumnTitle = "Présentation & Lecture",
                rightColumnBody = "• Impress : Diaporamas fluides avec mode présentateur\n• PDF : Visualiseur avec annotations et signature manuscrite",
                speakerNotes = "Démontrer la couverture complète des besoins quotidiens des utilisateurs."
            ),
            ImpressSlide(
                id = "slide_pitch_5",
                layout = SlideLayout.TITLE,
                title = "Merci pour votre attention !",
                subtitle = "StarOffice — L'outil de productivité moderne pour Android",
                speakerNotes = "Inviter aux questions et proposer une démonstration en direct sur l'appareil."
            )
        )

        return OfficeDocument(
            id = "template_impress_pitch",
            title = "Pitch Deck Investisseur 2026",
            type = DocumentType.IMPRESS,
            extension = ".pptx",
            createdAt = time,
            updatedAt = time,
            isFavorite = true,
            sizeBytes = 64800,
            tags = listOf("Pitch", "Présentation", "Start-up"),
            impressContent = ImpressContent(
                slides = slides,
                theme = PresentationTheme.CORPORATE_BLUE
            )
        )
    }

    // --- PDF 1: Contrat de Prestation ---
    fun createServiceContractPdf(time: Long = System.currentTimeMillis()): OfficeDocument {
        val formFields = listOf(
            PdfFormField(id = "field_client", label = "Nom du Client / Entreprise", value = "Société Horizon Digital", placeholder = "Nom complet"),
            PdfFormField(id = "field_provider", label = "Prestataire de services", value = "StarOffice Consulting SAS", placeholder = "Nom du prestataire"),
            PdfFormField(id = "field_amount", label = "Montant de la mission (€ HT)", value = "4 500 €", placeholder = "Montant en euros"),
            PdfFormField(id = "field_date", label = "Fait à (Lieu) et Date", value = "Paris, le 15 Octobre 2026", placeholder = "Ville et date")
        )

        val annotations = listOf(
            PdfAnnotation(
                id = "annot_1",
                type = AnnotationType.HIGHLIGHT,
                text = "Clause de confidentialité stricte",
                colorHex = "#FEF08A",
                author = "Juriste StarOffice",
                xRatio = 0.5f,
                yRatio = 0.42f
            ),
            PdfAnnotation(
                id = "annot_2",
                type = AnnotationType.NOTE,
                text = "Vérifier la conformité RGPD avant validation définitive.",
                colorHex = "#93C5FD",
                author = "Responsable Projet",
                xRatio = 0.75f,
                yRatio = 0.65f
            )
        )

        val pages = listOf(
            PdfPageData(
                pageNumber = 1,
                headerTitle = "CONTRAT DE PRESTATION DE SERVICES INFORMATIQUES",
                sections = listOf(
                    "ENTRE LES SOUSSIGNÉS :",
                    "D'une part, le Prestataire désigné ci-après, et d'autre part le Client. Il a été convenu et arrêté ce qui suit :",
                    "ARTICLE 1 — OBJET DE LA MISSION\nLe Prestataire s'engage à exécuter pour le compte du Client la mission de développement et d'intégration de solutions logicielles bureautiques conformes aux spécifications convenues.",
                    "ARTICLE 2 — PROPRIÉTÉ INTELLECTUELLE & CONFIDENTIALITÉ\nTous les documents, codes sources et données créés au cours de la prestation demeurent la propriété exclusive du Client dès complet paiement des sommes dues. Chaque partie s'engage à respecter la stricte confidentialité des informations échangées."
                ),
                formFields = formFields,
                annotations = annotations
            )
        )

        return OfficeDocument(
            id = "template_pdf_contract",
            title = "Contrat de Prestation de Service",
            type = DocumentType.PDF,
            extension = ".pdf",
            createdAt = time,
            updatedAt = time,
            isFavorite = true,
            sizeBytes = 51200,
            tags = listOf("Contrat", "Juridique", "Signature"),
            pdfContent = PdfContent(
                pages = pages,
                isSigned = true,
                signerName = "Jean Dupont (Direction)"
            )
        )
    }
}
