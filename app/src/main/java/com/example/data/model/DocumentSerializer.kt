package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object DocumentSerializer {

    // --- WRITER ---
    fun serializeWriter(content: WriterContent): String {
        val root = JSONObject()
        root.put("author", content.author)
        root.put("showTrackChanges", content.showTrackChanges)

        val blocksArray = JSONArray()
        content.blocks.forEach { b ->
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("type", b.type.name)
            obj.put("text", b.text)
            obj.put("alignment", b.alignment.name)
            obj.put("isBold", b.isBold)
            obj.put("isItalic", b.isItalic)
            obj.put("isUnderline", b.isUnderline)
            obj.put("textColorHex", b.textColorHex ?: "")
            obj.put("highlightHex", b.highlightHex ?: "")
            blocksArray.put(obj)
        }
        root.put("blocks", blocksArray)

        content.table?.let { t ->
            val tableObj = JSONObject()
            tableObj.put("id", t.id)
            tableObj.put("rows", t.rows)
            tableObj.put("cols", t.cols)
            val rowsArray = JSONArray()
            t.cells.forEach { row ->
                val rArr = JSONArray()
                row.forEach { cellText -> rArr.put(cellText) }
                rowsArray.put(rArr)
            }
            tableObj.put("cells", rowsArray)
            root.put("table", tableObj)
        }

        return root.toString()
    }

    fun deserializeWriter(jsonStr: String?): WriterContent {
        if (jsonStr.isNullOrBlank()) return WriterContent()
        return try {
            val root = JSONObject(jsonStr)
            val author = root.optString("author", "StarOffice User")
            val showTrackChanges = root.optBoolean("showTrackChanges", false)

            val blocksList = mutableListOf<WriterBlock>()
            val blocksArray = root.optJSONArray("blocks")
            if (blocksArray != null) {
                for (i in 0 until blocksArray.length()) {
                    val bObj = blocksArray.getJSONObject(i)
                    val type = try {
                        BlockType.valueOf(bObj.optString("type", BlockType.PARAGRAPH.name))
                    } catch (e: Exception) {
                        BlockType.PARAGRAPH
                    }
                    val align = try {
                        BlockAlignment.valueOf(bObj.optString("alignment", BlockAlignment.LEFT.name))
                    } catch (e: Exception) {
                        BlockAlignment.LEFT
                    }
                    blocksList.add(
                        WriterBlock(
                            id = bObj.optString("id", java.util.UUID.randomUUID().toString()),
                            type = type,
                            text = bObj.optString("text", ""),
                            alignment = align,
                            isBold = bObj.optBoolean("isBold", false),
                            isItalic = bObj.optBoolean("isItalic", false),
                            isUnderline = bObj.optBoolean("isUnderline", false),
                            textColorHex = bObj.optString("textColorHex").takeIf { it.isNotEmpty() },
                            highlightHex = bObj.optString("highlightHex").takeIf { it.isNotEmpty() }
                        )
                    )
                }
            }

            var table: WriterTable? = null
            val tableObj = root.optJSONObject("table")
            if (tableObj != null) {
                val rows = tableObj.optInt("rows", 3)
                val cols = tableObj.optInt("cols", 3)
                val rowsArray = tableObj.optJSONArray("cells")
                val cells = mutableListOf<List<String>>()
                if (rowsArray != null) {
                    for (r in 0 until rowsArray.length()) {
                        val cArr = rowsArray.getJSONArray(r)
                        val rowCells = mutableListOf<String>()
                        for (c in 0 until cArr.length()) {
                            rowCells.add(cArr.getString(c))
                        }
                        cells.add(rowCells)
                    }
                }
                table = WriterTable(
                    id = tableObj.optString("id", java.util.UUID.randomUUID().toString()),
                    rows = rows,
                    cols = cols,
                    cells = cells
                )
            }

            WriterContent(blocks = blocksList, table = table, author = author, showTrackChanges = showTrackChanges)
        } catch (e: Exception) {
            WriterContent()
        }
    }

    // --- CALC ---
    fun serializeCalc(content: CalcContent): String {
        val root = JSONObject()
        root.put("activeSheetIndex", content.activeSheetIndex)

        val sheetsArray = JSONArray()
        content.sheets.forEach { s ->
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("name", s.name)
            sObj.put("maxRows", s.maxRows)
            sObj.put("maxCols", s.maxCols)

            val cellsObj = JSONObject()
            s.cells.forEach { (coord, cell) ->
                val cObj = JSONObject()
                cObj.put("rawValue", cell.rawValue)
                cObj.put("format", cell.format.name)
                cObj.put("isBold", cell.isBold)
                cObj.put("isItalic", cell.isItalic)
                cObj.put("alignment", cell.alignment.name)
                cObj.put("bgColorHex", cell.bgColorHex ?: "")
                cObj.put("textColorHex", cell.textColorHex ?: "")
                cellsObj.put(coord, cObj)
            }
            sObj.put("cells", cellsObj)

            val chartsArray = JSONArray()
            s.charts.forEach { ch ->
                val chObj = JSONObject()
                chObj.put("id", ch.id)
                chObj.put("title", ch.title)
                chObj.put("type", ch.type.name)
                chObj.put("labelColumn", ch.labelColumn)
                chObj.put("valueColumn", ch.valueColumn)
                chObj.put("startRow", ch.startRow)
                chObj.put("endRow", ch.endRow)
                chartsArray.put(chObj)
            }
            sObj.put("charts", chartsArray)

            sheetsArray.put(sObj)
        }
        root.put("sheets", sheetsArray)
        return root.toString()
    }

    fun deserializeCalc(jsonStr: String?): CalcContent {
        if (jsonStr.isNullOrBlank()) return CalcContent()
        return try {
            val root = JSONObject(jsonStr)
            val activeIndex = root.optInt("activeSheetIndex", 0)
            val sheetsArray = root.optJSONArray("sheets")
            val sheetsList = mutableListOf<CalcSheet>()

            if (sheetsArray != null) {
                for (i in 0 until sheetsArray.length()) {
                    val sObj = sheetsArray.getJSONObject(i)
                    val id = sObj.optString("id", java.util.UUID.randomUUID().toString())
                    val name = sObj.optString("name", "Feuille ${i + 1}")
                    val maxRows = sObj.optInt("maxRows", 30)
                    val maxCols = sObj.optInt("maxCols", 10)

                    val cellsMap = mutableMapOf<String, CalcCell>()
                    val cellsObj = sObj.optJSONObject("cells")
                    if (cellsObj != null) {
                        val keys = cellsObj.keys()
                        while (keys.hasNext()) {
                            val coord = keys.next()
                            val cObj = cellsObj.getJSONObject(coord)
                            val format = try {
                                CellFormat.valueOf(cObj.optString("format", CellFormat.GENERAL.name))
                            } catch (e: Exception) {
                                CellFormat.GENERAL
                            }
                            val align = try {
                                BlockAlignment.valueOf(cObj.optString("alignment", BlockAlignment.LEFT.name))
                            } catch (e: Exception) {
                                BlockAlignment.LEFT
                            }
                            cellsMap[coord] = CalcCell(
                                rawValue = cObj.optString("rawValue", ""),
                                format = format,
                                isBold = cObj.optBoolean("isBold", false),
                                isItalic = cObj.optBoolean("isItalic", false),
                                alignment = align,
                                bgColorHex = cObj.optString("bgColorHex").takeIf { it.isNotEmpty() },
                                textColorHex = cObj.optString("textColorHex").takeIf { it.isNotEmpty() }
                            )
                        }
                    }

                    val chartsList = mutableListOf<CalcChart>()
                    val chartsArray = sObj.optJSONArray("charts")
                    if (chartsArray != null) {
                        for (chIdx in 0 until chartsArray.length()) {
                            val chObj = chartsArray.getJSONObject(chIdx)
                            val chType = try {
                                ChartType.valueOf(chObj.optString("type", ChartType.BAR.name))
                            } catch (e: Exception) {
                                ChartType.BAR
                            }
                            chartsList.add(
                                CalcChart(
                                    id = chObj.optString("id", java.util.UUID.randomUUID().toString()),
                                    title = chObj.optString("title", "Graphique"),
                                    type = chType,
                                    labelColumn = chObj.optString("labelColumn", "A"),
                                    valueColumn = chObj.optString("valueColumn", "B"),
                                    startRow = chObj.optInt("startRow", 2),
                                    endRow = chObj.optInt("endRow", 6)
                                )
                            )
                        }
                    }

                    sheetsList.add(CalcSheet(id = id, name = name, cells = cellsMap, maxRows = maxRows, maxCols = maxCols, charts = chartsList))
                }
            }

            if (sheetsList.isEmpty()) sheetsList.add(CalcSheet())
            CalcContent(sheets = sheetsList, activeSheetIndex = activeIndex)
        } catch (e: Exception) {
            CalcContent()
        }
    }

    // --- IMPRESS ---
    fun serializeImpress(content: ImpressContent): String {
        val root = JSONObject()
        root.put("activeSlideIndex", content.activeSlideIndex)
        root.put("theme", content.theme.name)

        val slidesArray = JSONArray()
        content.slides.forEach { s ->
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("layout", s.layout.name)
            sObj.put("title", s.title)
            sObj.put("subtitle", s.subtitle)

            val bulletsArr = JSONArray()
            s.bullets.forEach { b -> bulletsArr.put(b) }
            sObj.put("bullets", bulletsArr)

            sObj.put("speakerNotes", s.speakerNotes)
            sObj.put("accentShape", s.accentShape ?: "")
            sObj.put("statValue", s.statValue)
            sObj.put("statLabel", s.statLabel)
            sObj.put("leftColumnTitle", s.leftColumnTitle)
            sObj.put("leftColumnBody", s.leftColumnBody)
            sObj.put("rightColumnTitle", s.rightColumnTitle)
            sObj.put("rightColumnBody", s.rightColumnBody)
            slidesArray.put(sObj)
        }
        root.put("slides", slidesArray)
        return root.toString()
    }

    fun deserializeImpress(jsonStr: String?): ImpressContent {
        if (jsonStr.isNullOrBlank()) return ImpressContent()
        return try {
            val root = JSONObject(jsonStr)
            val activeSlideIndex = root.optInt("activeSlideIndex", 0)
            val theme = try {
                PresentationTheme.valueOf(root.optString("theme", PresentationTheme.CORPORATE_BLUE.name))
            } catch (e: Exception) {
                PresentationTheme.CORPORATE_BLUE
            }

            val slidesArray = root.optJSONArray("slides")
            val slidesList = mutableListOf<ImpressSlide>()
            if (slidesArray != null) {
                for (i in 0 until slidesArray.length()) {
                    val sObj = slidesArray.getJSONObject(i)
                    val layout = try {
                        SlideLayout.valueOf(sObj.optString("layout", SlideLayout.TITLE_CONTENT.name))
                    } catch (e: Exception) {
                        SlideLayout.TITLE_CONTENT
                    }

                    val bulletsList = mutableListOf<String>()
                    val bArr = sObj.optJSONArray("bullets")
                    if (bArr != null) {
                        for (b in 0 until bArr.length()) {
                            bulletsList.add(bArr.getString(b))
                        }
                    }

                    slidesList.add(
                        ImpressSlide(
                            id = sObj.optString("id", java.util.UUID.randomUUID().toString()),
                            layout = layout,
                            title = sObj.optString("title", ""),
                            subtitle = sObj.optString("subtitle", ""),
                            bullets = bulletsList,
                            speakerNotes = sObj.optString("speakerNotes", ""),
                            accentShape = sObj.optString("accentShape").takeIf { it.isNotEmpty() },
                            statValue = sObj.optString("statValue", ""),
                            statLabel = sObj.optString("statLabel", ""),
                            leftColumnTitle = sObj.optString("leftColumnTitle", ""),
                            leftColumnBody = sObj.optString("leftColumnBody", ""),
                            rightColumnTitle = sObj.optString("rightColumnTitle", ""),
                            rightColumnBody = sObj.optString("rightColumnBody", "")
                        )
                    )
                }
            }
            if (slidesList.isEmpty()) {
                slidesList.add(ImpressSlide(layout = SlideLayout.TITLE, title = "Présentation"))
            }
            ImpressContent(slides = slidesList, activeSlideIndex = activeSlideIndex, theme = theme)
        } catch (e: Exception) {
            ImpressContent()
        }
    }

    // --- PDF ---
    fun serializePdf(content: PdfContent): String {
        val root = JSONObject()
        root.put("activePageIndex", content.activePageIndex)
        root.put("isSigned", content.isSigned)
        root.put("signerName", content.signerName ?: "")

        val pagesArray = JSONArray()
        content.pages.forEach { p ->
            val pObj = JSONObject()
            pObj.put("pageNumber", p.pageNumber)
            pObj.put("headerTitle", p.headerTitle)

            val secArr = JSONArray()
            p.sections.forEach { sec -> secArr.put(sec) }
            pObj.put("sections", secArr)

            val fieldsArr = JSONArray()
            p.formFields.forEach { f ->
                val fObj = JSONObject()
                fObj.put("id", f.id)
                fObj.put("label", f.label)
                fObj.put("value", f.value)
                fObj.put("placeholder", f.placeholder)
                fObj.put("isRequired", f.isRequired)
                fieldsArr.put(fObj)
            }
            pObj.put("formFields", fieldsArr)

            val annotArr = JSONArray()
            p.annotations.forEach { a ->
                val aObj = JSONObject()
                aObj.put("id", a.id)
                aObj.put("type", a.type.name)
                aObj.put("text", a.text)
                aObj.put("colorHex", a.colorHex)
                aObj.put("author", a.author)
                aObj.put("date", a.date)
                aObj.put("xRatio", a.xRatio)
                aObj.put("yRatio", a.yRatio)
                annotArr.put(aObj)
            }
            pObj.put("annotations", annotArr)

            pagesArray.put(pObj)
        }
        root.put("pages", pagesArray)
        return root.toString()
    }

    fun deserializePdf(jsonStr: String?): PdfContent {
        if (jsonStr.isNullOrBlank()) return PdfContent()
        return try {
            val root = JSONObject(jsonStr)
            val activePage = root.optInt("activePageIndex", 0)
            val isSigned = root.optBoolean("isSigned", false)
            val signerName = root.optString("signerName").takeIf { it.isNotEmpty() }

            val pagesArray = root.optJSONArray("pages")
            val pagesList = mutableListOf<PdfPageData>()
            if (pagesArray != null) {
                for (i in 0 until pagesArray.length()) {
                    val pObj = pagesArray.getJSONObject(i)
                    val pNum = pObj.optInt("pageNumber", i + 1)
                    val header = pObj.optString("headerTitle", "Page $pNum")

                    val sections = mutableListOf<String>()
                    val secArr = pObj.optJSONArray("sections")
                    if (secArr != null) {
                        for (s in 0 until secArr.length()) {
                            sections.add(secArr.getString(s))
                        }
                    }

                    val fields = mutableListOf<PdfFormField>()
                    val fArr = pObj.optJSONArray("formFields")
                    if (fArr != null) {
                        for (f in 0 until fArr.length()) {
                            val fObj = fArr.getJSONObject(f)
                            fields.add(
                                PdfFormField(
                                    id = fObj.optString("id", java.util.UUID.randomUUID().toString()),
                                    label = fObj.optString("label", ""),
                                    value = fObj.optString("value", ""),
                                    placeholder = fObj.optString("placeholder", ""),
                                    isRequired = fObj.optBoolean("isRequired", false)
                                )
                            )
                        }
                    }

                    val annots = mutableListOf<PdfAnnotation>()
                    val aArr = pObj.optJSONArray("annotations")
                    if (aArr != null) {
                        for (a in 0 until aArr.length()) {
                            val aObj = aArr.getJSONObject(a)
                            val type = try {
                                AnnotationType.valueOf(aObj.optString("type", AnnotationType.HIGHLIGHT.name))
                            } catch (e: Exception) {
                                AnnotationType.HIGHLIGHT
                            }
                            annots.add(
                                PdfAnnotation(
                                    id = aObj.optString("id", java.util.UUID.randomUUID().toString()),
                                    type = type,
                                    text = aObj.optString("text", ""),
                                    colorHex = aObj.optString("colorHex", "#FEF08A"),
                                    author = aObj.optString("author", "StarOffice"),
                                    date = aObj.optLong("date", System.currentTimeMillis()),
                                    xRatio = aObj.optDouble("xRatio", 0.5).toFloat(),
                                    yRatio = aObj.optDouble("yRatio", 0.5).toFloat()
                                )
                            )
                        }
                    }

                    pagesList.add(PdfPageData(pageNumber = pNum, headerTitle = header, sections = sections, formFields = fields, annotations = annots))
                }
            }
            if (pagesList.isEmpty()) pagesList.add(PdfPageData(pageNumber = 1))
            PdfContent(pages = pagesList, activePageIndex = activePage, isSigned = isSigned, signerName = signerName)
        } catch (e: Exception) {
            PdfContent()
        }
    }
}
