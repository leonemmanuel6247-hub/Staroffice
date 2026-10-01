package com.example.ui.calc

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ShareExportDialog
import com.example.ui.theme.CalcGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcScreen(
    viewModel: CalcViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    val document = uiState.document ?: return
    val calc = document.calcContent ?: CalcContent()
    val activeSheet = calc.activeSheet()

    var isEditingTitle by remember { mutableStateOf(false) }
    var titleText by remember(document.title) { mutableStateOf(document.title) }

    // Share & Export dialog
    if (uiState.showShareDialog) {
        ShareExportDialog(
            document = document,
            onDismiss = { viewModel.openShareDialog(false) }
        )
    }

    // Chart Dialog
    if (uiState.showChartDialog && uiState.activeChart != null) {
        val points = viewModel.getChartDataPoints(uiState.activeChart!!)
        CalcChartDialog(
            chart = uiState.activeChart!!,
            dataPoints = points,
            onDismiss = { viewModel.openChartDialog(false) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calc_back_button")) {
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
                                color = CalcGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = document.extension,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CalcGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Open chart visualizer
                    IconButton(onClick = { viewModel.openChartDialog(true) }) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = "Graphiques", tint = CalcGreen)
                    }
                    // Export
                    IconButton(onClick = { viewModel.openShareDialog(true) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Partager")
                    }
                }
            )
        },
        bottomBar = {
            // Quick Formatting Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val currentCell = activeSheet.cells[uiState.selectedCellCoord]

                    // Format Currency €
                    FilterChip(
                        selected = currentCell?.format == CellFormat.CURRENCY_EUR,
                        onClick = { viewModel.setCellFormat(CellFormat.CURRENCY_EUR) },
                        label = { Text("€ Euro", fontWeight = FontWeight.Bold) }
                    )

                    // Format Percent %
                    FilterChip(
                        selected = currentCell?.format == CellFormat.PERCENT,
                        onClick = { viewModel.setCellFormat(CellFormat.PERCENT) },
                        label = { Text("% Pourcent", fontWeight = FontWeight.Bold) }
                    )

                    // Format Number
                    FilterChip(
                        selected = currentCell?.format == CellFormat.NUMBER,
                        onClick = { viewModel.setCellFormat(CellFormat.NUMBER) },
                        label = { Text("123 Nombre") }
                    )

                    VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

                    // Bold
                    IconButton(onClick = { viewModel.toggleCellBold() }) {
                        Icon(
                            imageVector = Icons.Default.FormatBold,
                            contentDescription = "Gras",
                            tint = if (currentCell?.isBold == true) CalcGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Italic
                    IconButton(onClick = { viewModel.toggleCellItalic() }) {
                        Icon(
                            imageVector = Icons.Default.FormatItalic,
                            contentDescription = "Italique",
                            tint = if (currentCell?.isItalic == true) CalcGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

                    // Alignments
                    IconButton(onClick = { viewModel.setCellAlignment(BlockAlignment.LEFT) }) {
                        Icon(imageVector = Icons.Default.FormatAlignLeft, contentDescription = "Gauche")
                    }
                    IconButton(onClick = { viewModel.setCellAlignment(BlockAlignment.CENTER) }) {
                        Icon(imageVector = Icons.Default.FormatAlignCenter, contentDescription = "Centre")
                    }
                    IconButton(onClick = { viewModel.setCellAlignment(BlockAlignment.RIGHT) }) {
                        Icon(imageVector = Icons.Default.FormatAlignRight, contentDescription = "Droite")
                    }

                    VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

                    // Quick Colors
                    val colors = listOf(null, "#D1FAE5", "#FEF3C7", "#DBEAFE", "#FEE2E2")
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (hex != null) parseHexColor(hex) else Color.White,
                                    CircleShape
                                )
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { viewModel.setCellBgColor(hex) }
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
            // Sheet Tabs Row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    calc.sheets.forEachIndexed { index, sheet ->
                        val isSelected = index == calc.activeSheetIndex
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CalcGreen) else null,
                            modifier = Modifier.clickable { viewModel.switchSheet(index) }
                        ) {
                            Text(
                                text = sheet.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CalcGreen else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Add Sheet button
                    IconButton(onClick = { viewModel.addSheet() }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter une feuille")
                    }
                }
            }

            // Formula Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = uiState.selectedCellCoord,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "fx",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = CalcGreen,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        OutlinedTextField(
                            value = uiState.formulaBarInput,
                            onValueChange = { viewModel.updateFormulaBarInput(it) },
                            singleLine = true,
                            placeholder = { Text("Valeur ou formule (ex: =SUM(B2:B5))") },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Quick formula snippets row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val snippets = listOf(
                            "=SUM(" to "SOMME",
                            "=AVERAGE(" to "MOYENNE",
                            "=COUNT(" to "NB",
                            "=MAX(" to "MAX",
                            "=MIN(" to "MIN",
                            "=IF(" to "SI",
                            "+" to "+",
                            "-" to "-",
                            "*" to "×",
                            "/" to "÷"
                        )
                        snippets.forEach { (snip, label) ->
                            AssistChip(
                                onClick = { viewModel.insertFormulaSnippet(snip) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }

            // Grid View Container (2D scrollable table)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9))
            ) {
                SpreadsheetGrid(
                    sheet = activeSheet,
                    selectedCoord = uiState.selectedCellCoord,
                    onCellSelected = { viewModel.selectCell(it) },
                    onCellEdited = { coord, valStr -> viewModel.updateCellRawValue(coord, valStr) }
                )
            }
        }
    }
}

@Composable
private fun SpreadsheetGrid(
    sheet: CalcSheet,
    selectedCoord: String,
    onCellSelected: (String) -> Unit,
    onCellEdited: (String, String) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val numRows = sheet.maxRows.coerceAtLeast(20)
    val numCols = sheet.maxCols.coerceAtLeast(8)

    val colWidth = 110.dp
    val rowHeaderWidth = 44.dp
    val rowHeight = 36.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
    ) {
        Column {
            // Column Headers Row (Corner + A, B, C, D...)
            Row(modifier = Modifier.background(Color(0xFFE2E8F0))) {
                // Top-left Corner Cell
                Box(
                    modifier = Modifier
                        .size(width = rowHeaderWidth, height = rowHeight)
                        .border(0.5.dp, Color(0xFFCBD5E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◢", fontSize = 10.sp, color = Color.Gray)
                }

                // Column letters
                for (c in 0 until numCols) {
                    val colLetter = CalcFormulaEvaluator.indexToColLetter(c)
                    Box(
                        modifier = Modifier
                            .size(width = colWidth, height = rowHeight)
                            .border(0.5.dp, Color(0xFFCBD5E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = colLetter,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Rows (1, 2, 3...)
            for (r in 1..numRows) {
                Row {
                    // Row Number Header
                    Box(
                        modifier = Modifier
                            .size(width = rowHeaderWidth, height = rowHeight)
                            .background(Color(0xFFE2E8F0))
                            .border(0.5.dp, Color(0xFFCBD5E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$r",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }

                    // Cells in this row
                    for (c in 0 until numCols) {
                        val colLetter = CalcFormulaEvaluator.indexToColLetter(c)
                        val coord = "$colLetter$r"
                        val cell = sheet.cells[coord]
                        val isSelected = coord == selectedCoord

                        val evaluatedDisplay = if (cell != null) {
                            CalcFormulaEvaluator.evaluate(coord, cell, sheet.cells)
                        } else ""

                        val cellBg = cell?.bgColorHex?.let { parseHexColor(it) } ?: Color.White

                        Box(
                            modifier = Modifier
                                .size(width = colWidth, height = rowHeight)
                                .background(cellBg)
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) CalcGreen else Color(0xFFE2E8F0)
                                )
                                .clickable { onCellSelected(coord) }
                                .padding(horizontal = 6.dp),
                            contentAlignment = when (cell?.alignment) {
                                BlockAlignment.CENTER -> Alignment.Center
                                BlockAlignment.RIGHT -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        ) {
                            Text(
                                text = evaluatedDisplay,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (cell?.isBold == true) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (cell?.isItalic == true) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                    textAlign = when (cell?.alignment) {
                                        BlockAlignment.CENTER -> TextAlign.Center
                                        BlockAlignment.RIGHT -> TextAlign.End
                                        else -> TextAlign.Start
                                    }
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = cell?.textColorHex?.let { parseHexColor(it) } ?: Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color.White
    }
}
