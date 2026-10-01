package com.example.ui.impress

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
import com.example.ui.calc.parseHexColor
import com.example.ui.components.ShareExportDialog
import com.example.ui.theme.ImpressOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpressScreen(
    viewModel: ImpressViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    val document = uiState.document ?: return
    val impress = document.impressContent ?: ImpressContent()
    val currentSlide = impress.slides.getOrNull(uiState.activeSlideIndex) ?: impress.slides.firstOrNull() ?: return

    var isEditingTitle by remember { mutableStateOf(false) }
    var titleText by remember(document.title) { mutableStateOf(document.title) }

    // If fullscreen slideshow is active
    if (uiState.isSlideshowActive) {
        SlideshowScreen(
            impressContent = impress,
            initialSlideIndex = uiState.activeSlideIndex,
            onExit = { viewModel.setSlideshowActive(false) }
        )
        return
    }

    // Share & Export dialog
    if (uiState.showShareDialog) {
        ShareExportDialog(
            document = document,
            onDismiss = { viewModel.openShareDialog(false) }
        )
    }

    // Theme Picker Dialog
    if (uiState.showThemeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.openThemeDialog(false) },
            title = { Text("Choisir un thème de présentation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresentationTheme.values().forEach { themeOption ->
                        val isSelected = impress.theme == themeOption
                        Surface(
                            onClick = { viewModel.setTheme(themeOption) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(parseHexColor(themeOption.bgHex), CircleShape)
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = themeOption.title, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Fond ${themeOption.bgHex} • Accent ${themeOption.accentHex}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.openThemeDialog(false) }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Add Slide Dialog
    if (uiState.showAddSlideDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.openAddSlideDialog(false) },
            title = { Text("Ajouter une diapositive") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SlideLayout.values().forEach { layoutOption ->
                        Surface(
                            onClick = { viewModel.addSlide(layoutOption) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (layoutOption) {
                                        SlideLayout.TITLE -> "★"
                                        SlideLayout.TITLE_CONTENT -> "☰"
                                        SlideLayout.TWO_COLUMNS -> "▥"
                                        SlideLayout.BIG_STAT -> "%"
                                        SlideLayout.BLANK -> "▢"
                                    },
                                    fontSize = 20.sp,
                                    color = ImpressOrange
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = when (layoutOption) {
                                            SlideLayout.TITLE -> "Diapositive de Titre"
                                            SlideLayout.TITLE_CONTENT -> "Titre et Contenu à puces"
                                            SlideLayout.TWO_COLUMNS -> "Deux Colonnes de texte"
                                            SlideLayout.BIG_STAT -> "Chiffre Clé & Statistique"
                                            SlideLayout.BLANK -> "Diapositive Vierge"
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.openAddSlideDialog(false) }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("impress_back_button")) {
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
                                color = ImpressOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = document.extension,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ImpressOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Play Slideshow button
                    Button(
                        onClick = { viewModel.setSlideshowActive(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ImpressOrange),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Diaporama")
                    }

                    // Theme selector
                    IconButton(onClick = { viewModel.openThemeDialog(true) }) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Thème")
                    }

                    // Share
                    IconButton(onClick = { viewModel.openShareDialog(true) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Partager")
                    }
                }
            )
        },
        bottomBar = {
            // Slide Thumbnails Strip
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diapositives (${uiState.activeSlideIndex + 1} / ${impress.slides.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Move slide up
                            IconButton(
                                onClick = { viewModel.moveSlide(-1) },
                                enabled = uiState.activeSlideIndex > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Déplacer à gauche", modifier = Modifier.size(16.dp))
                            }
                            // Move slide down
                            IconButton(
                                onClick = { viewModel.moveSlide(1) },
                                enabled = uiState.activeSlideIndex < impress.slides.size - 1,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Déplacer à droite", modifier = Modifier.size(16.dp))
                            }
                            // Duplicate
                            IconButton(
                                onClick = { viewModel.duplicateActiveSlide() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Dupliquer", modifier = Modifier.size(16.dp))
                            }
                            // Delete
                            IconButton(
                                onClick = { viewModel.deleteActiveSlide() },
                                enabled = impress.slides.size > 1,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                            // Add slide
                            IconButton(
                                onClick = { viewModel.openAddSlideDialog(true) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter", modifier = Modifier.size(16.dp), tint = ImpressOrange)
                            }
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(impress.slides) { index, slide ->
                            val isSelected = index == uiState.activeSlideIndex
                            Card(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(60.dp)
                                    .clickable { viewModel.setActiveSlide(index) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) parseHexColor(impress.theme.cardBgHex) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ImpressOrange) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${index + 1}. ${slide.title.ifBlank { "Diapo" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) parseHexColor(impress.theme.primaryTextHex) else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = slide.layout.name.lowercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = if (isSelected) parseHexColor(impress.theme.secondaryTextHex) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Slide Canvas Card (16:9 aspect)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = parseHexColor(impress.theme.bgHex)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RenderSlidePreview(slide = currentSlide, theme = impress.theme)
                    }
                }
            }

            // Slide Layout Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SlideLayout.values().forEach { l ->
                        FilterChip(
                            selected = currentSlide.layout == l,
                            onClick = { viewModel.updateActiveSlide { it.copy(layout = l) } },
                            label = { Text(l.name.replace("_", " ")) }
                        )
                    }
                }
            }

            // Slide Editing Inputs Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Édition de la diapositive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Title Field
                        OutlinedTextField(
                            value = currentSlide.title,
                            onValueChange = { newTitle ->
                                viewModel.updateActiveSlide { it.copy(title = newTitle) }
                            },
                            label = { Text("Titre de la diapositive") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Subtitle Field (if relevant)
                        if (currentSlide.layout == SlideLayout.TITLE || currentSlide.layout == SlideLayout.TITLE_CONTENT) {
                            OutlinedTextField(
                                value = currentSlide.subtitle,
                                onValueChange = { newSub ->
                                    viewModel.updateActiveSlide { it.copy(subtitle = newSub) }
                                },
                                label = { Text("Sous-titre") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Bullets Field (if TITLE_CONTENT)
                        if (currentSlide.layout == SlideLayout.TITLE_CONTENT) {
                            Text("Points clés :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            currentSlide.bullets.forEachIndexed { bIdx, bulletText ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = bulletText,
                                        onValueChange = { newB ->
                                            val updatedB = currentSlide.bullets.toMutableList().apply { set(bIdx, newB) }
                                            viewModel.updateActiveSlide { it.copy(bullets = updatedB) }
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    IconButton(onClick = {
                                        val updatedB = currentSlide.bullets.toMutableList().apply { removeAt(bIdx) }
                                        viewModel.updateActiveSlide { it.copy(bullets = updatedB) }
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Supprimer le point")
                                    }
                                }
                            }
                            TextButton(onClick = {
                                val updatedB = currentSlide.bullets + "Nouveau point"
                                viewModel.updateActiveSlide { it.copy(bullets = updatedB) }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter un point")
                            }
                        }

                        // Big Stat Fields (if BIG_STAT)
                        if (currentSlide.layout == SlideLayout.BIG_STAT) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = currentSlide.statValue,
                                    onValueChange = { s -> viewModel.updateActiveSlide { it.copy(statValue = s) } },
                                    label = { Text("Chiffre (ex: 85%)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = currentSlide.statLabel,
                                    onValueChange = { l -> viewModel.updateActiveSlide { it.copy(statLabel = l) } },
                                    label = { Text("Description") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Two columns fields (if TWO_COLUMNS)
                        if (currentSlide.layout == SlideLayout.TWO_COLUMNS) {
                            OutlinedTextField(
                                value = currentSlide.leftColumnTitle,
                                onValueChange = { t -> viewModel.updateActiveSlide { it.copy(leftColumnTitle = t) } },
                                label = { Text("Titre Colonne Gauche") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = currentSlide.leftColumnBody,
                                onValueChange = { b -> viewModel.updateActiveSlide { it.copy(leftColumnBody = b) } },
                                label = { Text("Contenu Colonne Gauche") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                            OutlinedTextField(
                                value = currentSlide.rightColumnTitle,
                                onValueChange = { t -> viewModel.updateActiveSlide { it.copy(rightColumnTitle = t) } },
                                label = { Text("Titre Colonne Droite") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = currentSlide.rightColumnBody,
                                onValueChange = { b -> viewModel.updateActiveSlide { it.copy(rightColumnBody = b) } },
                                label = { Text("Contenu Colonne Droite") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }

                        // Speaker Notes
                        OutlinedTextField(
                            value = currentSlide.speakerNotes,
                            onValueChange = { notes ->
                                viewModel.updateActiveSlide { it.copy(speakerNotes = notes) }
                            },
                            label = { Text("Notes du présentateur (aide-mémoire)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderSlidePreview(slide: ImpressSlide, theme: PresentationTheme) {
    val primaryText = parseHexColor(theme.primaryTextHex)
    val secondaryText = parseHexColor(theme.secondaryTextHex)
    val accent = parseHexColor(theme.accentHex)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (slide.layout) {
            SlideLayout.TITLE -> {
                Text(
                    text = slide.title.ifBlank { "Titre de présentation" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                if (slide.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = slide.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(3.dp)
                        .background(accent, RoundedCornerShape(2.dp))
                )
            }
            SlideLayout.BIG_STAT -> {
                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = slide.statValue.ifBlank { "100%" },
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = slide.statLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                    textAlign = TextAlign.Center
                )
            }
            SlideLayout.TWO_COLUMNS -> {
                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = slide.leftColumnTitle, fontWeight = FontWeight.Bold, color = accent, fontSize = 11.sp)
                        Text(text = slide.leftColumnBody, color = primaryText, fontSize = 10.sp, maxLines = 3)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = slide.rightColumnTitle, fontWeight = FontWeight.Bold, color = accent, fontSize = 11.sp)
                        Text(text = slide.rightColumnBody, color = primaryText, fontSize = 10.sp, maxLines = 3)
                    }
                }
            }
            else -> { // TITLE_CONTENT
                Text(
                    text = slide.title.ifBlank { "Titre" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    slide.bullets.take(4).forEach { b ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("★ ", color = accent, fontSize = 10.sp)
                            Text(text = b, color = primaryText, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
