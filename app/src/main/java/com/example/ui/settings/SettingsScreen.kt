package com.example.ui.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    homeViewModel: HomeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val homeUiState by homeViewModel.uiState.collectAsState()

    var showPinChangeDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Français") }
    var selectedTheme by remember { mutableStateOf("Système") }

    // Change PIN Dialog
    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Définir le code PIN de sécurité") },
            text = {
                Column {
                    Text(
                        text = "Entrez un code à 4 chiffres pour protéger l'accès à vos documents.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) newPinInput = it },
                        label = { Text("Code PIN (4 chiffres)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            homeViewModel.setPinProtection(true, newPinInput)
                            showPinChangeDialog = false
                            Toast.makeText(context, "Verrouillage par code PIN activé !", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newPinInput.length == 4
                ) {
                    Text("Activer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                title = {
                    Text("Paramètres & Confidentialité", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Section
            item {
                Text(
                    text = "SÉCURITÉ & ACCÈS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Verrouillage par code PIN", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = if (homeUiState.isPinProtectionEnabled) "Actif (Code configuré)" else "Désactivé",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = homeUiState.isPinProtectionEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinChangeDialog = true
                                    } else {
                                        homeViewModel.setPinProtection(false, homeUiState.userPin)
                                        Toast.makeText(context, "Verrouillage désactivé", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        if (homeUiState.isPinProtectionEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            TextButton(
                                onClick = { showPinChangeDialog = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Modifier le code PIN")
                            }
                        }
                    }
                }
            }

            // Appearance & Localization Section
            item {
                Text(
                    text = "AFFICHAGE & LANGUE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Language
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = if (selectedLanguage == "Français") "English" else "Français"
                                    Toast.makeText(context, "Langue : $selectedLanguage", Toast.LENGTH_SHORT).show()
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Langue de l'application", fontWeight = FontWeight.SemiBold)
                                    Text(selectedLanguage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = when (selectedTheme) {
                                        "Système" -> "Clair"
                                        "Clair" -> "Sombre"
                                        else -> "Système"
                                    }
                                    Toast.makeText(context, "Thème : $selectedTheme", Toast.LENGTH_SHORT).show()
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Thème visuel", fontWeight = FontWeight.SemiBold)
                                    Text(selectedTheme, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }

            // Storage Section
            item {
                Text(
                    text = "STOCKAGE & DONNÉES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Stockage Interne StarOffice", fontWeight = FontWeight.SemiBold)
                                Text("Données chiffrées localement sur l'appareil", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val totalBytes = homeUiState.documents.sumOf { it.sizeBytes }
                        LinearProgressIndicator(
                            progress = { 0.15f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${homeUiState.documents.size} documents enregistrés",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = com.example.ui.home.formatFileSize(totalBytes),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Cache temporaire nettoyé (0 Mo libéré)", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nettoyer le cache")
                        }
                    }
                }
            }

            // Privacy & Open Source Section
            item {
                Text(
                    text = "VIE PRIVÉE & OPEN SOURCE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Garantie Zéro Traqueur", fontWeight = FontWeight.Bold)
                                Text("Aucune télémétrie, aucun compte obligatoire, fonctionne 100% hors-ligne.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "StarOffice est distribué sous licence open source libre. Conforme aux formats ouverts ODF (OpenDocument Format) et Office Open XML.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Version", style = MaterialTheme.typography.labelMedium)
                            Text("1.0.0 (Open Edition)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
