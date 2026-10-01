package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DocumentType

@Composable
fun NewDocumentDialog(
    initialType: DocumentType = DocumentType.WRITER,
    onDismiss: () -> Unit,
    onCreate: (title: String, type: DocumentType, extension: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedExtension by remember(selectedType) { mutableStateOf(selectedType.defaultExtension) }
    var title by remember(selectedType) {
        mutableStateOf(
            when (selectedType) {
                DocumentType.WRITER -> "Nouveau Document"
                DocumentType.CALC -> "Nouveau Classeur"
                DocumentType.IMPRESS -> "Nouvelle Présentation"
                DocumentType.PDF -> "Nouveau PDF"
            }
        )
    }

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
                    Text(
                        text = "Créer un document",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Module Selector Grid
                Text(
                    text = "Choisir le module StarOffice :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DocumentType.values().forEach { docType ->
                        val isSelected = selectedType == docType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = docType },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) docType.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, docType.color) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = docType.iconEmoji, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = docType.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) docType.color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nom du fichier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Extension format selector
                Text(
                    text = "Format de fichier :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedType.supportedExtensions.forEach { ext ->
                        val isExtSelected = selectedExtension == ext
                        FilterChip(
                            selected = isExtSelected,
                            onClick = { selectedExtension = ext },
                            label = { Text(ext, fontWeight = if (isExtSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title.trim(), selectedType, selectedExtension)
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Créer")
                    }
                }
            }
        }
    }
}
