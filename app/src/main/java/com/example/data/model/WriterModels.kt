package com.example.data.model

data class WriterSpan(
    val text: String,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val textColorHex: String? = null,
    val highlightColorHex: String? = null
)

enum class BlockType {
    PARAGRAPH,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    BULLET_ITEM,
    NUMBERED_ITEM,
    QUOTE,
    DIVIDER
}

enum class BlockAlignment {
    LEFT, CENTER, RIGHT, JUSTIFY
}

data class WriterBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: BlockType = BlockType.PARAGRAPH,
    val text: String = "",
    val alignment: BlockAlignment = BlockAlignment.LEFT,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textColorHex: String? = null,
    val highlightHex: String? = null
)

data class WriterTable(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rows: Int = 3,
    val cols: Int = 3,
    val cells: List<List<String>> = List(rows) { List(cols) { "" } }
)

data class WriterContent(
    val blocks: List<WriterBlock> = emptyList(),
    val table: WriterTable? = null,
    val author: String = "StarOffice User",
    val showTrackChanges: Boolean = false
) {
    fun getTotalWords(): Int {
        val blockWords = blocks.sumOf { block ->
            block.text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
        }
        val tableWords = table?.cells?.sumOf { row ->
            row.sumOf { it.trim().split("\\s+".toRegex()).count { word -> word.isNotBlank() } }
        } ?: 0
        return blockWords + tableWords
    }

    fun getTotalCharacters(): Int {
        val blockChars = blocks.sumOf { it.text.length }
        val tableChars = table?.cells?.sumOf { row -> row.sumOf { it.length } } ?: 0
        return blockChars + tableChars
    }

    fun getReadingTimeMinutes(): Int {
        val words = getTotalWords()
        return maxOf(1, (words / 200) + if (words % 200 > 0) 1 else 0)
    }
}
