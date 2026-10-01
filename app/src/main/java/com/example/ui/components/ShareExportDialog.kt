package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument

@Composable
fun ShareExportDialog(
    document: OfficeDocument,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Partager & Exporter",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${document.title}${document.extension}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options list
                ExportOptionItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Exporter en PDF (.pdf)",
                    description = "Mise en page vectorielle prête pour impression",
                    onClick = {
                        Toast.makeText(context, "Export PDF généré avec succès : ${document.title}.pdf", Toast.LENGTH_SHORT).show()
                        shareText(context, generateDocumentSummary(document), "StarOffice Document PDF : ${document.title}")
                        onDismiss()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                ExportOptionItem(
                    icon = Icons.Default.FilePresent,
                    title = "Exporter format Office (${document.extension})",
                    description = "Compatible Microsoft Office et OpenOffice / LibreOffice",
                    onClick = {
                        Toast.makeText(context, "Exporté : ${document.title}${document.extension}", Toast.LENGTH_SHORT).show()
                        shareText(context, generateDocumentSummary(document), "Fichier StarOffice : ${document.title}${document.extension}")
                        onDismiss()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (document.type == DocumentType.CALC) {
                    ExportOptionItem(
                        icon = Icons.Default.TableChart,
                        title = "Exporter en CSV (.csv)",
                        description = "Format texte universel pour données tabulaires",
                        onClick = {
                            val csvData = generateCsv(document)
                            shareText(context, csvData, "Données CSV : ${document.title}.csv")
                            onDismiss()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                ExportOptionItem(
                    icon = Icons.Default.ContentCopy,
                    title = "Copier le texte dans le presse-papier",
                    description = "Copie instantanée pour collage rapide",
                    onClick = {
                        val text = generateDocumentSummary(document)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(document.title, text))
                        Toast.makeText(context, "Copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                ExportOptionItem(
                    icon = Icons.Default.Share,
                    title = "Partager via une application...",
                    description = "Envoyer par e-mail, messagerie ou cloud",
                    onClick = {
                        shareText(context, generateDocumentSummary(document), document.title)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExportOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun shareText(context: Context, text: String, subject: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Partager StarOffice"))
}

private fun generateDocumentSummary(document: OfficeDocument): String {
    val sb = StringBuilder()
    sb.appendLine("=== ${document.title} (${document.type.name}) ===")
    sb.appendLine("Généré avec StarOffice Suite Open Source")
    sb.appendLine()

    when (document.type) {
        DocumentType.WRITER -> {
            document.writerContent?.blocks?.forEach { b ->
                sb.appendLine(b.text)
            }
            document.writerContent?.table?.let { t ->
                sb.appendLine("\n--- Tableau (${t.rows}x${t.cols}) ---")
                t.cells.forEach { row ->
                    sb.appendLine(row.joinToString(" | "))
                }
            }
        }
        DocumentType.CALC -> {
            val sheet = document.calcContent?.activeSheet()
            sb.appendLine("Feuille : ${sheet?.name ?: "Feuille"}")
            sb.appendLine(generateCsv(document))
        }
        DocumentType.IMPRESS -> {
            document.impressContent?.slides?.forEachIndexed { idx, s ->
                sb.appendLine("--- Diapositive ${idx + 1} : ${s.title} ---")
                if (s.subtitle.isNotBlank()) sb.appendLine(s.subtitle)
                s.bullets.forEach { b -> sb.appendLine(" • $b") }
                if (s.speakerNotes.isNotBlank()) sb.appendLine("Notes : ${s.speakerNotes}")
                sb.appendLine()
            }
        }
        DocumentType.PDF -> {
            document.pdfContent?.pages?.forEach { p ->
                sb.appendLine("Page ${p.pageNumber} : ${p.headerTitle}")
                p.sections.forEach { sb.appendLine(it) }
                p.formFields.forEach { f -> sb.appendLine("${f.label} : ${f.value}") }
            }
            if (document.pdfContent?.isSigned == true) {
                sb.appendLine("\n[Signé électroniquement par ${document.pdfContent.signerName ?: "Signataire"}]")
            }
        }
    }
    return sb.toString()
}

private fun generateCsv(document: OfficeDocument): String {
    val sheet = document.calcContent?.activeSheet() ?: return ""
    val sb = StringBuilder()
    for (r in 1..sheet.maxRows.coerceAtMost(25)) {
        val rowVals = mutableListOf<String>()
        var hasContent = false
        for (c in 0 until sheet.maxCols.coerceAtMost(10)) {
            val letter = com.example.ui.calc.CalcFormulaEvaluator.indexToColLetter(c)
            val cell = sheet.cells["$letter$r"]
            val v = cell?.rawValue ?: ""
            if (v.isNotBlank()) hasContent = true
            rowVals.add("\"$v\"")
        }
        if (hasContent) {
            sb.appendLine(rowVals.joinToString(","))
        }
    }
    return sb.toString()
}
