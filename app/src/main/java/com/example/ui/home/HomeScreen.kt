package com.example.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument
import com.example.ui.components.NewDocumentDialog
import com.example.ui.components.SecurityLockScreen
import com.example.ui.components.ShareExportDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDocument: (OfficeDocument) -> Unit,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    // PIN Lock Gate
    if (uiState.isPinLocked && uiState.isPinProtectionEnabled) {
        SecurityLockScreen(
            correctPin = uiState.userPin,
            onUnlockSuccess = { viewModel.unlockApp() }
        )
    }

    // New Document Dialog
    if (uiState.showNewDocDialog) {
        NewDocumentDialog(
            initialType = when (uiState.filter) {
                HomeFilter.WRITER -> DocumentType.WRITER
                HomeFilter.CALC -> DocumentType.CALC
                HomeFilter.IMPRESS -> DocumentType.IMPRESS
                HomeFilter.PDF -> DocumentType.PDF
                else -> DocumentType.WRITER
            },
            onDismiss = { viewModel.openNewDocDialog(false) },
            onCreate = { title, type, ext ->
                viewModel.createDocument(title, type, ext) { createdDoc ->
                    onOpenDocument(createdDoc)
                }
            }
        )
    }

    // Share & Export Dialog
    uiState.showShareDialogForDoc?.let { doc ->
        ShareExportDialog(
            document = doc,
            onDismiss = { viewModel.openShareDialogForDoc(null) }
        )
    }

    // Rename Dialog
    uiState.selectedDocForRename?.let { doc ->
        var newTitle by remember { mutableStateOf(doc.title) }
        AlertDialog(
            onDismissRequest = { viewModel.openRenameDialog(null) },
            title = { Text("Renommer le document") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.renameDocument(doc, newTitle) },
                    enabled = newTitle.isNotBlank()
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.openRenameDialog(null) }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Actions Bottom Sheet / Dialog
    uiState.selectedDocForActions?.let { doc ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.openActionsForDoc(null) },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(doc.type.containerColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = doc.type.iconEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${doc.title}${doc.extension}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${doc.type.displayNameFr} • ${formatFileSize(doc.sizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                ActionMenuItem(
                    icon = Icons.Default.Edit,
                    label = "Ouvrir & Éditer",
                    onClick = {
                        viewModel.openActionsForDoc(null)
                        onOpenDocument(doc)
                    }
                )

                ActionMenuItem(
                    icon = if (doc.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                    label = if (doc.isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                    onClick = {
                        viewModel.toggleFavorite(doc.id)
                        viewModel.openActionsForDoc(null)
                    }
                )

                ActionMenuItem(
                    icon = Icons.Default.DriveFileRenameOutline,
                    label = "Renommer le fichier",
                    onClick = {
                        viewModel.openActionsForDoc(null)
                        viewModel.openRenameDialog(doc)
                    }
                )

                ActionMenuItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Dupliquer",
                    onClick = {
                        viewModel.duplicateDocument(doc.id) {
                            onOpenDocument(it)
                        }
                    }
                )

                ActionMenuItem(
                    icon = Icons.Default.Share,
                    label = "Partager & Exporter (PDF / Office)",
                    onClick = {
                        viewModel.openActionsForDoc(null)
                        viewModel.openShareDialogForDoc(doc)
                    }
                )

                if (doc.isTrash) {
                    ActionMenuItem(
                        icon = Icons.Default.Restore,
                        label = "Restaurer le document",
                        onClick = {
                            viewModel.restoreFromTrash(doc.id)
                            viewModel.openActionsForDoc(null)
                        }
                    )
                    ActionMenuItem(
                        icon = Icons.Default.DeleteForever,
                        label = "Supprimer définitivement",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.deletePermanently(doc.id) }
                    )
                } else {
                    ActionMenuItem(
                        icon = Icons.Default.Delete,
                        label = "Déplacer dans la corbeille",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.moveToTrash(doc.id) }
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("★", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "StarOffice",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Suite Bureautique Libre",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Sort order button
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(imageVector = Icons.Default.Sort, contentDescription = "Trier les documents")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Plus récents d'abord") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.DATE_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (uiState.sortOrder == SortOrder.DATE_DESC) Icon(Icons.Default.Check, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Par ordre alphabétique (A-Z)") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NAME_ASC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (uiState.sortOrder == SortOrder.NAME_ASC) Icon(Icons.Default.Check, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Par taille de fichier") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.SIZE_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (uiState.sortOrder == SortOrder.SIZE_DESC) Icon(Icons.Default.Check, null)
                            }
                        )
                    }

                    // Settings button
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("settings_button")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (uiState.filter != HomeFilter.TRASH) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openNewDocDialog(true) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nouveau") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("new_doc_fab")
                )
            } else if (uiState.documents.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.emptyTrash() },
                    icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                    text = { Text("Vider la corbeille") },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Search Bar Item
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Rechercher un document, tableau, slide...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_input")
                )
            }

            // Hero Banner (visible when no search query and on ALL tab)
            if (uiState.searchQuery.isBlank() && uiState.filter == HomeFilter.ALL) {
                item {
                    HeroSection(
                        totalDocs = uiState.documents.size,
                        onCreateType = { type ->
                            viewModel.createDocument("Nouveau ${type.displayNameFr}", type, type.defaultExtension) {
                                onOpenDocument(it)
                            }
                        }
                    )
                }

                // Templates Gallery
                item {
                    TemplatesGallerySection(
                        onSelectTemplate = { templateKey ->
                            viewModel.createFromTemplate(templateKey) {
                                onOpenDocument(it)
                            }
                        }
                    )
                }
            }

            // Filter Tabs Row
            item {
                FilterTabsRow(
                    selectedFilter = uiState.filter,
                    onSelect = { viewModel.setFilter(it) }
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (uiState.filter) {
                            HomeFilter.ALL -> "Tous les documents (${uiState.documents.size})"
                            HomeFilter.WRITER -> "Documents Writer (${uiState.documents.size})"
                            HomeFilter.CALC -> "Classeurs Calc (${uiState.documents.size})"
                            HomeFilter.IMPRESS -> "Présentations Impress (${uiState.documents.size})"
                            HomeFilter.PDF -> "Documents PDF (${uiState.documents.size})"
                            HomeFilter.FAVORITES -> "Favoris (${uiState.documents.size})"
                            HomeFilter.TRASH -> "Corbeille (${uiState.documents.size})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Document List or Empty State
            if (uiState.documents.isEmpty()) {
                item {
                    EmptyDocumentsView(filter = uiState.filter)
                }
            } else {
                items(uiState.documents, key = { it.id }) { doc ->
                    DocumentCard(
                        document = doc,
                        onClick = { onOpenDocument(doc) },
                        onFavoriteClick = { viewModel.toggleFavorite(doc.id) },
                        onMoreClick = { viewModel.openActionsForDoc(doc) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    totalDocs: Int,
    onCreateType: (DocumentType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Optional background image overlay
            Image(
                painter = painterResource(id = R.drawable.staroffice_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.22f,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "100% Hors-Ligne & Souverain",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "$totalDocs documents",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Votre bureau mobile tout-en-un",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Créez et éditez librement vos textes, feuilles de calcul, diaporamas et PDF.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Launch Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickModuleButton(
                        type = DocumentType.WRITER,
                        modifier = Modifier.weight(1f),
                        onClick = { onCreateType(DocumentType.WRITER) }
                    )
                    QuickModuleButton(
                        type = DocumentType.CALC,
                        modifier = Modifier.weight(1f),
                        onClick = { onCreateType(DocumentType.CALC) }
                    )
                    QuickModuleButton(
                        type = DocumentType.IMPRESS,
                        modifier = Modifier.weight(1f),
                        onClick = { onCreateType(DocumentType.IMPRESS) }
                    )
                    QuickModuleButton(
                        type = DocumentType.PDF,
                        modifier = Modifier.weight(1f),
                        onClick = { onCreateType(DocumentType.PDF) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickModuleButton(
    type: DocumentType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = type.iconEmoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = type.color
            )
        }
    }
}

@Composable
private fun TemplatesGallerySection(onSelectTemplate: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Modèles Professionnels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "1-Clic pour dupliquer",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val templates = listOf(
                TemplateItemData("budget", "Budget Personnel", "Tableur Calc", "📊", CalcGreen, ".xlsx"),
                TemplateItemData("report", "Rapport Stratégique", "Document Writer", "📄", WriterBlue, ".docx"),
                TemplateItemData("pitch", "Pitch Investisseur", "Présentation Impress", "📽️", ImpressOrange, ".pptx"),
                TemplateItemData("invoice", "Facture Commerciale", "Tableur Calc", "📊", CalcGreen, ".xlsx"),
                TemplateItemData("contract", "Contrat Prestation", "Document PDF", "📑", PdfRed, ".pdf"),
                TemplateItemData("cv", "CV Moderne", "Document Writer", "📄", WriterBlue, ".docx")
            )

            items(templates) { item ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { onSelectTemplate(item.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = item.emoji, fontSize = 24.sp)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = item.color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = item.extension,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = item.color,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class TemplateItemData(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val extension: String
)

@Composable
private fun FilterTabsRow(
    selectedFilter: HomeFilter,
    onSelect: (HomeFilter) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == HomeFilter.ALL,
            onClick = { onSelect(HomeFilter.ALL) },
            label = { Text("Tous") }
        )
        FilterChip(
            selected = selectedFilter == HomeFilter.WRITER,
            onClick = { onSelect(HomeFilter.WRITER) },
            label = { Text("📄 Writer") }
        )
        FilterChip(
            selected = selectedFilter == HomeFilter.CALC,
            onClick = { onSelect(HomeFilter.CALC) },
            label = { Text("📊 Calc") }
        )
        FilterChip(
            selected = selectedFilter == HomeFilter.IMPRESS,
            onClick = { onSelect(HomeFilter.IMPRESS) },
            label = { Text("📽️ Impress") }
        )
        FilterChip(
            selected = selectedFilter == HomeFilter.PDF,
            onClick = { onSelect(HomeFilter.PDF) },
            label = { Text("📑 PDF") }
        )
        FilterChip(
            selected = selectedFilter == HomeFilter.FAVORITES,
            onClick = { onSelect(HomeFilter.FAVORITES) },
            label = { Text("⭐ Favoris") }
        )
        FilterChip(
            selected = selectedFilter == HomeFilter.TRASH,
            onClick = { onSelect(HomeFilter.TRASH) },
            label = { Text("🗑️ Corbeille") }
        )
    }
}

@Composable
fun DocumentCard(
    document: OfficeDocument,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("document_card_${document.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(document.type.containerColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = document.type.iconEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = document.type.color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = document.extension,
                            style = MaterialTheme.typography.labelSmall,
                            color = document.type.color,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDate(document.updatedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatFileSize(document.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Favorite button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Favori",
                    tint = if (document.isFavorite) StarGoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // More options button
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Plus d'options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyDocumentsView(filter: HomeFilter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = when (filter) {
                HomeFilter.FAVORITES -> Icons.Outlined.StarOutline
                HomeFilter.TRASH -> Icons.Outlined.Delete
                else -> Icons.Outlined.FolderOpen
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (filter) {
                HomeFilter.FAVORITES -> "Aucun document favori"
                HomeFilter.TRASH -> "La corbeille est vide"
                else -> "Aucun document trouvé"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (filter) {
                HomeFilter.FAVORITES -> "Ajoutez des étoiles à vos documents réguliers pour les retrouver ici."
                HomeFilter.TRASH -> "Les éléments supprimés apparaîtront ici."
                else -> "Créez votre premier document ou dupliquez un modèle prêt à l'emploi."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
        }
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.1f Mo".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "${bytes / 1024} Ko"
        else -> "$bytes octets"
    }
}

fun formatDate(timeMillis: Long): String {
    val diff = System.currentTimeMillis() - timeMillis
    return when {
        diff < 60000 -> "À l'instant"
        diff < 3600000 -> "Il y a ${diff / 60000} min"
        diff < 86400000 -> "Il y a ${diff / 3600000} h"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.FRANCE).format(Date(timeMillis))
    }
}
