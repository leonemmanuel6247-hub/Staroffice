package com.example.ui.pdf

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.calc.parseHexColor
import com.example.ui.components.ShareExportDialog
import com.example.ui.theme.PdfRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfScreen(
    viewModel: PdfViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    val document = uiState.document ?: return
    val pdf = document.pdfContent ?: PdfContent()
    val currentPage = pdf.pages.getOrNull(uiState.activePageIndex) ?: pdf.pages.firstOrNull() ?: return

    var isEditingTitle by remember { mutableStateOf(false) }
    var titleText by remember(document.title) { mutableStateOf(document.title) }

    // Signature Pad Dialog
    if (uiState.showSignaturePad) {
        SignaturePadDialog(
            onDismiss = { viewModel.openSignaturePad(false) },
            onSignatureConfirmed = { name, points ->
                viewModel.applySignature(name, points)
            }
        )
    }

    // Share & Export dialog
    if (uiState.showShareDialog) {
        ShareExportDialog(
            document = document,
            onDismiss = { viewModel.openShareDialog(false) }
        )
    }

    // Add Note Dialog
    if (uiState.showAddNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.openAddNoteDialog(false) },
            title = { Text("Ajouter une note adhésive") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Commentaire / Observation") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            viewModel.addAnnotation(
                                PdfAnnotation(
                                    type = AnnotationType.NOTE,
                                    text = noteText.trim(),
                                    colorHex = uiState.selectedColorHex
                                )
                            )
                            viewModel.openAddNoteDialog(false)
                        }
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.openAddNoteDialog(false) }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pdf_back_button")) {
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
                                color = PdfRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = document.extension,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PdfRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Signature button
                    Button(
                        onClick = { viewModel.openSignaturePad(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = PdfRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (pdf.isSigned) "Signé ✓" else "Signer")
                    }

                    // Share
                    IconButton(onClick = { viewModel.openShareDialog(true) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Partager")
                    }
                }
            )
        },
        bottomBar = {
            // Page navigation strip
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (uiState.activePageIndex > 0) viewModel.setActivePage(uiState.activePageIndex - 1) },
                        enabled = uiState.activePageIndex > 0
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Page précédente")
                    }

                    Text(
                        text = "Page ${uiState.activePageIndex + 1} sur ${pdf.pages.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (uiState.activePageIndex < pdf.pages.size - 1) viewModel.setActivePage(uiState.activePageIndex + 1) },
                            enabled = uiState.activePageIndex < pdf.pages.size - 1
                        ) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Page suivante")
                        }
                        IconButton(onClick = { viewModel.addPage() }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter une page", tint = PdfRed)
                        }
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
            // Annotation Tools Strip
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tool: Surligneur (Highlight)
                    FilterChip(
                        selected = uiState.activeTool == ActivePdfTool.HIGHLIGHT,
                        onClick = {
                            viewModel.setActiveTool(ActivePdfTool.HIGHLIGHT)
                            // Add a sample highlight
                            viewModel.addAnnotation(
                                PdfAnnotation(
                                    type = AnnotationType.HIGHLIGHT,
                                    text = "Passage important surligné",
                                    colorHex = uiState.selectedColorHex
                                )
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.BorderColor, null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Surligner") }
                    )

                    // Tool: Note adhésive
                    FilterChip(
                        selected = uiState.activeTool == ActivePdfTool.NOTE,
                        onClick = { viewModel.openAddNoteDialog(true) },
                        leadingIcon = { Icon(Icons.Default.StickyNote2, null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Note") }
                    )

                    VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

                    // Color swatches for annotations
                    val annotColors = listOf("#FEF08A", "#BAE6FD", "#BBF7D0", "#FECDD3")
                    annotColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(parseHexColor(hex), CircleShape)
                                .border(
                                    if (uiState.selectedColorHex == hex) 2.dp else 1.dp,
                                    if (uiState.selectedColorHex == hex) Color.Black else Color.Gray,
                                    CircleShape
                                )
                                .clickable { viewModel.setSelectedColorHex(hex) }
                        )
                    }
                }
            }

            // PDF Document Page Viewer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE2E8F0))
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentPadding = PaddingValues(bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Document Page Header
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "STAROFFICE PDF DOCUMENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PdfRed
                                    )
                                    Text(
                                        text = "Page ${currentPage.pageNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    text = currentPage.headerTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        // Text Sections
                        items(currentPage.sections.size) { idx ->
                            val sectionText = currentPage.sections[idx]
                            Text(
                                text = sectionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF334155),
                                lineHeight = 22.sp
                            )
                        }

                        // Annotations displayed on this page
                        if (currentPage.annotations.isNotEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Annotations & Notes de relecture :",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    currentPage.annotations.forEach { annot ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = parseHexColor(annot.colorHex).copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (annot.type == AnnotationType.HIGHLIGHT) Icons.Default.Highlight else Icons.Default.StickyNote2,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = Color(0xFF0F172A)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = annot.text,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "Ajouté par ${annot.author}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF475569)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Form Fields (fillable by user)
                        if (currentPage.formFields.isNotEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Formulaire interactif à remplir :",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        currentPage.formFields.forEach { field ->
                                            OutlinedTextField(
                                                value = field.value,
                                                onValueChange = { newVal -> viewModel.updateFormField(field.id, newVal) },
                                                label = { Text(field.label) },
                                                placeholder = { Text(field.placeholder) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = PdfRed,
                                                    unfocusedBorderColor = Color(0xFF94A3B8)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Signature Area
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (pdf.isSigned) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (pdf.isSigned) Color(0xFF3B82F6) else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openSignaturePad(true) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Signature Électronique",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (pdf.isSigned) Color(0xFF1E3A8A) else Color(0xFF475569)
                                        )
                                        if (pdf.isSigned) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981)
                                            ) {
                                                Text(
                                                    text = "CERTIFIÉ CONFORME",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (pdf.isSigned) {
                                        // Render signature stroke or name
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(70.dp)
                                                .background(Color.White, RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            val stroke = pdf.signaturePath
                                            if (!stroke.isNullOrEmpty()) {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    val minX = stroke.minOf { it.x }
                                                    val maxX = stroke.maxOf { it.x }.coerceAtLeast(minX + 1f)
                                                    val minY = stroke.minOf { it.y }
                                                    val maxY = stroke.maxOf { it.y }.coerceAtLeast(minY + 1f)

                                                    val scaleX = (size.width * 0.9f) / (maxX - minX)
                                                    val scaleY = (size.height * 0.9f) / (maxY - minY)
                                                    val scale = minOf(scaleX, scaleY)

                                                    val path = Path()
                                                    stroke.forEachIndexed { i, pt ->
                                                        val x = (pt.x - minX) * scale + size.width * 0.05f
                                                        val y = (pt.y - minY) * scale + size.height * 0.05f
                                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                                    }
                                                    drawPath(
                                                        path = path,
                                                        color = Color(0xFF1E3A8A),
                                                        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = pdf.signerName ?: "Signé",
                                                    fontStyle = FontStyle.Italic,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = Color(0xFF1E3A8A),
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Signé par ${pdf.signerName ?: "Utilisateur StarOffice"} le ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp)
                                                .background(Color.White, RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Draw, contentDescription = null, tint = PdfRed)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Cliquez ici pour apposer votre signature",
                                                    color = Color.Gray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
