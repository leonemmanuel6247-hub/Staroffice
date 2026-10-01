package com.example.ui.impress

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ImpressContent
import com.example.data.model.SlideLayout
import com.example.ui.calc.parseHexColor
import kotlinx.coroutines.delay

@Composable
fun SlideshowScreen(
    impressContent: ImpressContent,
    initialSlideIndex: Int = 0,
    onExit: () -> Unit
) {
    BackHandler { onExit() }

    val slides = impressContent.slides
    var currentSlideIndex by remember { mutableStateOf(initialSlideIndex.coerceIn(0, (slides.size - 1).coerceAtLeast(0))) }
    val currentSlide = slides.getOrNull(currentSlideIndex) ?: return

    val theme = impressContent.theme
    val bg = parseHexColor(theme.bgHex)
    val cardBg = parseHexColor(theme.cardBgHex)
    val primaryText = parseHexColor(theme.primaryTextHex)
    val secondaryText = parseHexColor(theme.secondaryTextHex)
    val accent = parseHexColor(theme.accentHex)

    // Timer state
    var elapsedSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Laser pointer position
    var laserPos by remember { mutableStateOf<Offset?>(null) }
    var isLaserActive by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .pointerInput(isLaserActive) {
                if (isLaserActive) {
                    detectDragGestures(
                        onDragStart = { laserPos = it },
                        onDrag = { change, _ ->
                            change.consume()
                            laserPos = change.position
                        },
                        onDragEnd = { laserPos = null }
                    )
                }
            }
    ) {
        // Slide Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentSlide.layout) {
                SlideLayout.TITLE -> {
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                    if (currentSlide.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentSlide.subtitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = secondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .background(accent, RoundedCornerShape(2.dp))
                    )
                }
                SlideLayout.BIG_STAT -> {
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = currentSlide.statValue,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        color = accent,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentSlide.statLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = secondaryText,
                        textAlign = TextAlign.Center
                    )
                }
                SlideLayout.TWO_COLUMNS -> {
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = currentSlide.leftColumnTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentSlide.leftColumnBody,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = primaryText
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = currentSlide.rightColumnTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentSlide.rightColumnBody,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }
                else -> { // TITLE_CONTENT or BLANK
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                    if (currentSlide.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentSlide.subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = secondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        currentSlide.bullets.forEach { bullet ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "★",
                                    color = accent,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                                )
                                Text(
                                    text = bullet,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tap targets for Previous (left 30%) and Next (right 30%) when laser is not active
        if (!isLaserActive) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.35f)
                        .clickable {
                            if (currentSlideIndex > 0) currentSlideIndex--
                        }
                )
                Box(modifier = Modifier.fillMaxHeight().weight(0.3f))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.35f)
                        .clickable {
                            if (currentSlideIndex < slides.size - 1) currentSlideIndex++
                        }
                )
            }
        }

        // Laser pointer rendering
        laserPos?.let { pos ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color(0xFFFF2222), radius = 10f, center = pos)
                drawCircle(color = Color(0x66FF2222), radius = 22f, center = pos)
            }
        }

        // Top Control Overlay Bar
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slide Counter
                Text(
                    text = "${currentSlideIndex + 1} / ${slides.size}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Timer
                val min = elapsedSeconds / 60
                val sec = elapsedSeconds % 60
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%02d:%02d".format(min, sec),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Laser pointer toggle
                    IconButton(onClick = { isLaserActive = !isLaserActive }) {
                        Icon(
                            imageVector = Icons.Default.Highlight,
                            contentDescription = "Pointeur laser",
                            tint = if (isLaserActive) Color(0xFFFF4444) else Color.White
                        )
                    }

                    // Notes drawer toggle
                    IconButton(onClick = { showNotes = !showNotes }) {
                        Icon(
                            imageVector = Icons.Default.StickyNote2,
                            contentDescription = "Notes du présentateur",
                            tint = if (showNotes) accent else Color.White
                        )
                    }

                    // Close slideshow
                    IconButton(onClick = onExit) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Quitter", tint = Color.White)
                    }
                }
            }
        }

        // Bottom Navigation Pills
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentSlideIndex > 0) currentSlideIndex-- },
                enabled = currentSlideIndex > 0,
                colors = IconButtonDefaults.iconButtonColors(containerColor = cardBg)
            ) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Précédent", tint = primaryText)
            }

            Text(
                text = "${currentSlideIndex + 1} / ${slides.size}",
                color = secondaryText,
                style = MaterialTheme.typography.bodySmall
            )

            IconButton(
                onClick = { if (currentSlideIndex < slides.size - 1) currentSlideIndex++ },
                enabled = currentSlideIndex < slides.size - 1,
                colors = IconButtonDefaults.iconButtonColors(containerColor = cardBg)
            ) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Suivant", tint = primaryText)
            }
        }

        // Presenter Notes Bottom Sheet
        AnimatedVisibility(
            visible = showNotes,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notes du présentateur (Diapo ${currentSlideIndex + 1})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        IconButton(onClick = { showNotes = false }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = secondaryText)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentSlide.speakerNotes.ifBlank { "Aucune note pour cette diapositive." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryText
                    )
                }
            }
        }
    }
}
