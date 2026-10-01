package com.example.ui.writer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ShareExportDialog
import com.example.ui.theme.WriterBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriterScreen(
    viewModel: WriterViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    val document = uiState.document ?: return

    var activeBlockId by remember { mutableStateOf<String?>(null) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleText by remember(document.title) { mutableStateOf(document.title) }

    // Share & Export dialog
    if (uiState.showShareDialog) {
        ShareExportDialog(
            document = document,
            onDismiss = { viewModel.openShareDialog(false) }
        )
    }

    // Insert Table Dialog
    if (uiState.showInsertTableDialog) {
        var rows by remember { mutableStateOf("3") }
        var cols by remember { mutableStateOf("3") }
        AlertDialog(
            onDismissRequest = { viewModel.openInsertTableDialog(false) },
            title = { Text("Insérer un tableau") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = rows,
                        onValueChange = { rows = it },
                        label = { Text("Nombre de lignes") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cols,
                        onValueChange = { cols = it },
                        label = { Text("Nombre de colonnes") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val r = rows.toIntOrNull()?.coerceIn(1, 10) ?: 3
                    val c = cols.toIntOrNull()?.coerceIn(1, 6) ?: 3
                    viewModel.insertTable(r, c)
                }) {
                    Text("Insérer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.openInsertTableDialog(false) }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Paper background colors
    val paperBgColor = when (uiState.paperTheme) {
        "sepia" -> Color(0xFFFDFBF7)
        "dark" -> Color(0xFF1E293B)
        else -> Color.White
    }
    val paperTextColor = when (uiState.paperTheme) {
        "dark" -> Color(0xFFF8FAFC)
        else -> Color(0xFF0F172A)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("writer_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                title = {
                    if (isEditingTitle) {
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.updateTitle(titleText)
                                    isEditingTitle = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Valider")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isEditingTitle = true }
                        ) {
                            Text(
                                text = document.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = WriterBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = document.extension,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WriterBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Find & Replace toggle
                    IconButton(onClick = { viewModel.toggleFindReplace() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher et remplacer",
                            tint = if (uiState.showFindReplace) WriterBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Reading mode toggle
                    IconButton(onClick = { viewModel.toggleReadingMode() }) {
                        Icon(
                            imageVector = if (uiState.isReadingMode) Icons.Default.MenuBook else Icons.Default.EditNote,
                            contentDescription = "Mode lecture",
                            tint = if (uiState.isReadingMode) WriterBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Paper tone selector
                    var showToneMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showToneMenu = true }) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Teinte papier")
                    }
                    DropdownMenu(
                        expanded = showToneMenu,
                        onDismissRequest = { showToneMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Blanc Standard") },
                            onClick = {
                                viewModel.setPaperTheme("white")
                                showToneMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sépia Reposant") },
                            onClick = {
                                viewModel.setPaperTheme("sepia")
                                showToneMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sombre Nuit") },
                            onClick = {
                                viewModel.setPaperTheme("dark")
                                showToneMenu = false
                            }
                        )
                    }

                    // Export / Share
                    IconButton(onClick = { viewModel.openShareDialog(true) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Partager")
                    }
                }
            )
        },
        bottomBar = {
            // Document Stats & Autosave Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val content = document.writerContent ?: WriterContent()
                    Text(
                        text = "${content.getTotalWords()} mots • ${content.getTotalCharacters()} caractères • ~${content.getReadingTimeMinutes()} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (uiState.isSaved) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isSaved) "Enregistré" else "Sauvegarde...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Find & Replace Toolbar
            AnimatedVisibility(visible = uiState.showFindReplace) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Rechercher...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.replaceQuery,
                                onValueChange = { viewModel.setReplaceQuery(it) },
                                placeholder = { Text("Remplacer par...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.performReplaceAll() },
                                enabled = uiState.searchQuery.isNotBlank()
                            ) {
                                Text("Remplacer tout")
                            }
                        }
                    }
                }
            }

            // Formatting Contextual Toolbar (in Edit Mode)
            if (!uiState.isReadingMode) {
                WriterFormattingToolbar(
                    activeBlock = document.writerContent?.blocks?.find { it.id == activeBlockId },
                    onStyleChange = { type -> activeBlockId?.let { viewModel.updateBlockType(it, type) } },
                    onBoldClick = { activeBlockId?.let { viewModel.toggleBlockBold(it) } },
                    onItalicClick = { activeBlockId?.let { viewModel.toggleBlockItalic(it) } },
                    onUnderlineClick = { activeBlockId?.let { viewModel.toggleBlockUnderline(it) } },
                    onAlignClick = { align -> activeBlockId?.let { viewModel.setBlockAlignment(it, align) } },
                    onInsertTableClick = { viewModel.openInsertTableDialog(true) },
                    onAddBlock = { viewModel.addBlockAfter(activeBlockId) }
                )
            }

            // Document Canvas Page
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = if (uiState.isReadingMode) 8.dp else 12.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = paperBgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        contentPadding = PaddingValues(bottom = 60.dp)
                    ) {
                        val blocks = document.writerContent?.blocks ?: emptyList()

                        itemsIndexed(blocks, key = { _, b -> b.id }) { index, block ->
                            val isActive = activeBlockId == block.id

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeBlockId = block.id }
                                    .background(
                                        if (isActive && !uiState.isReadingMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                        else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                if (uiState.isReadingMode) {
                                    // Clean Reading Display
                                    RenderTextBlockReadMode(block, paperTextColor)
                                } else {
                                    // Interactive Editable Block
                                    RenderTextBlockEditMode(
                                        block = block,
                                        onTextChanged = { viewModel.updateBlockText(block.id, it) },
                                        onDelete = { viewModel.removeBlock(block.id) },
                                        onAddAfter = { viewModel.addBlockAfter(block.id) }
                                    )
                                }
                            }
                        }

                        // Table display if present
                        document.writerContent?.table?.let { table ->
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Tableau (${table.rows}x${table.cols}) :",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = paperTextColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                RenderWriterTable(
                                    table = table,
                                    isReadingMode = uiState.isReadingMode,
                                    onCellChanged = { r, c, txt -> viewModel.updateTableCell(r, c, txt) }
                                )
                            }
                        }

                        // Add block button at bottom
                        if (!uiState.isReadingMode) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { viewModel.addBlockAfter(null) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ajouter un paragraphe")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WriterFormattingToolbar(
    activeBlock: WriterBlock?,
    onStyleChange: (BlockType) -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onAlignClick: (BlockAlignment) -> Unit,
    onInsertTableClick: () -> Unit,
    onAddBlock: () -> Unit
) {
    val scrollState = rememberScrollState()
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Style Selector Dropdown
            var showStyleMenu by remember { mutableStateOf(false) }
            TextButton(onClick = { showStyleMenu = true }) {
                Text(
                    text = when (activeBlock?.type) {
                        BlockType.HEADING_1 -> "Titre 1"
                        BlockType.HEADING_2 -> "Titre 2"
                        BlockType.HEADING_3 -> "Titre 3"
                        BlockType.BULLET_ITEM -> "Puces"
                        BlockType.NUMBERED_ITEM -> "Numéros"
                        BlockType.QUOTE -> "Citation"
                        BlockType.DIVIDER -> "Séparateur"
                        else -> "Paragraphe"
                    },
                    fontWeight = FontWeight.Bold
                )
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = showStyleMenu,
                onDismissRequest = { showStyleMenu = false }
            ) {
                DropdownMenuItem(text = { Text("Titre 1 (Principal)") }, onClick = { onStyleChange(BlockType.HEADING_1); showStyleMenu = false })
                DropdownMenuItem(text = { Text("Titre 2 (Section)") }, onClick = { onStyleChange(BlockType.HEADING_2); showStyleMenu = false })
                DropdownMenuItem(text = { Text("Titre 3 (Sous-section)") }, onClick = { onStyleChange(BlockType.HEADING_3); showStyleMenu = false })
                DropdownMenuItem(text = { Text("Paragraphe standard") }, onClick = { onStyleChange(BlockType.PARAGRAPH); showStyleMenu = false })
                DropdownMenuItem(text = { Text("Liste à puces (•)") }, onClick = { onStyleChange(BlockType.BULLET_ITEM); showStyleMenu = false })
                DropdownMenuItem(text = { Text("Liste numérotée (1.)") }, onClick = { onStyleChange(BlockType.NUMBERED_ITEM); showStyleMenu = false })
                DropdownMenuItem(text = { Text("Citation") }, onClick = { onStyleChange(BlockType.QUOTE); showStyleMenu = false })
                DropdownMenuItem(text = { Text("Séparateur horizontal") }, onClick = { onStyleChange(BlockType.DIVIDER); showStyleMenu = false })
            }

            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

            // Bold
            IconButton(
                onClick = onBoldClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (activeBlock?.isBold == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(imageVector = Icons.Default.FormatBold, contentDescription = "Gras")
            }

            // Italic
            IconButton(
                onClick = onItalicClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (activeBlock?.isItalic == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(imageVector = Icons.Default.FormatItalic, contentDescription = "Italique")
            }

            // Underline
            IconButton(
                onClick = onUnderlineClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (activeBlock?.isUnderline == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(imageVector = Icons.Default.FormatUnderlined, contentDescription = "Souligné")
            }

            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

            // Alignment
            IconButton(onClick = { onAlignClick(BlockAlignment.LEFT) }) {
                Icon(imageVector = Icons.Default.FormatAlignLeft, contentDescription = "Aligner à gauche")
            }
            IconButton(onClick = { onAlignClick(BlockAlignment.CENTER) }) {
                Icon(imageVector = Icons.Default.FormatAlignCenter, contentDescription = "Centrer")
            }
            IconButton(onClick = { onAlignClick(BlockAlignment.RIGHT) }) {
                Icon(imageVector = Icons.Default.FormatAlignRight, contentDescription = "Aligner à droite")
            }

            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

            // Insert Table
            IconButton(onClick = onInsertTableClick) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = "Insérer un tableau")
            }

            // Add Block
            IconButton(onClick = onAddBlock) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nouveau bloc")
            }
        }
    }
}

@Composable
private fun RenderTextBlockEditMode(
    block: WriterBlock,
    onTextChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onAddAfter: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (block.type) {
            BlockType.BULLET_ITEM -> {
                Text("•", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            }
            BlockType.NUMBERED_ITEM -> {
                Text("1.", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            }
            BlockType.DIVIDER -> {
                HorizontalDivider(modifier = Modifier.weight(1f).padding(vertical = 12.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp))
                }
                return
            }
            else -> {}
        }

        OutlinedTextField(
            value = block.text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (block.isBold || block.type.name.startsWith("HEADING")) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (block.isItalic || block.type == BlockType.QUOTE) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (block.isUnderline) TextDecoration.Underline else TextDecoration.None,
                textAlign = when (block.alignment) {
                    BlockAlignment.CENTER -> TextAlign.Center
                    BlockAlignment.RIGHT -> TextAlign.End
                    BlockAlignment.JUSTIFY -> TextAlign.Justify
                    else -> TextAlign.Start
                },
                fontSize = when (block.type) {
                    BlockType.HEADING_1 -> 22.sp
                    BlockType.HEADING_2 -> 18.sp
                    BlockType.HEADING_3 -> 16.sp
                    else -> 14.sp
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
    }
}

@Composable
private fun RenderTextBlockReadMode(block: WriterBlock, textColor: Color) {
    when (block.type) {
        BlockType.DIVIDER -> {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        }
        BlockType.BULLET_ITEM -> {
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("• ", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                Text(
                    text = block.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        BlockType.NUMBERED_ITEM -> {
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("1. ", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                Text(
                    text = block.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        BlockType.QUOTE -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(WriterBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "« ${block.text} »",
                    fontStyle = FontStyle.Italic,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        else -> {
            Text(
                text = block.text,
                color = textColor,
                fontWeight = if (block.isBold || block.type.name.startsWith("HEADING")) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (block.isUnderline) TextDecoration.Underline else TextDecoration.None,
                fontSize = when (block.type) {
                    BlockType.HEADING_1 -> 24.sp
                    BlockType.HEADING_2 -> 20.sp
                    BlockType.HEADING_3 -> 17.sp
                    else -> 15.sp
                },
                textAlign = when (block.alignment) {
                    BlockAlignment.CENTER -> TextAlign.Center
                    BlockAlignment.RIGHT -> TextAlign.End
                    BlockAlignment.JUSTIFY -> TextAlign.Justify
                    else -> TextAlign.Start
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (block.type.name.startsWith("HEADING")) 8.dp else 3.dp)
            )
        }
    }
}

@Composable
private fun RenderWriterTable(
    table: WriterTable,
    isReadingMode: Boolean,
    onCellChanged: (row: Int, col: Int, text: String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            table.cells.forEachIndexed { r, rowCells ->
                val isHeader = r == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isHeader) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent
                        )
                ) {
                    rowCells.forEachIndexed { c, cellText ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                .padding(6.dp)
                        ) {
                            if (isReadingMode) {
                                Text(
                                    text = cellText,
                                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            } else {
                                var text by remember(cellText) { mutableStateOf(cellText) }
                                OutlinedTextField(
                                    value = text,
                                    onValueChange = {
                                        text = it
                                        onCellChanged(r, c, it)
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
