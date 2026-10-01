package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument
import com.example.ui.calc.CalcScreen
import com.example.ui.calc.CalcViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.impress.ImpressScreen
import com.example.ui.impress.ImpressViewModel
import com.example.ui.pdf.PdfScreen
import com.example.ui.pdf.PdfViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.StarOfficeTheme
import com.example.ui.writer.WriterScreen
import com.example.ui.writer.WriterViewModel

sealed interface AppScreen {
    data object Home : AppScreen
    data class Writer(val documentId: String) : AppScreen
    data class Calc(val documentId: String) : AppScreen
    data class Impress(val documentId: String) : AppScreen
    data class Pdf(val documentId: String) : AppScreen
    data object Settings : AppScreen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StarOfficeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StarOfficeApp()
                }
            }
        }
    }
}

@Composable
fun StarOfficeApp() {
    val homeViewModel: HomeViewModel = viewModel()
    val writerViewModel: WriterViewModel = viewModel()
    val calcViewModel: CalcViewModel = viewModel()
    val impressViewModel: ImpressViewModel = viewModel()
    val pdfViewModel: PdfViewModel = viewModel()

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    when (val screen = currentScreen) {
        is AppScreen.Home -> {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenDocument = { doc: OfficeDocument ->
                    when (doc.type) {
                        DocumentType.WRITER -> {
                            writerViewModel.setDocument(doc)
                            currentScreen = AppScreen.Writer(doc.id)
                        }
                        DocumentType.CALC -> {
                            calcViewModel.setDocument(doc)
                            currentScreen = AppScreen.Calc(doc.id)
                        }
                        DocumentType.IMPRESS -> {
                            impressViewModel.setDocument(doc)
                            currentScreen = AppScreen.Impress(doc.id)
                        }
                        DocumentType.PDF -> {
                            pdfViewModel.setDocument(doc)
                            currentScreen = AppScreen.Pdf(doc.id)
                        }
                    }
                },
                onOpenSettings = {
                    currentScreen = AppScreen.Settings
                }
            )
        }
        is AppScreen.Writer -> {
            WriterScreen(
                viewModel = writerViewModel,
                onBack = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.Calc -> {
            CalcScreen(
                viewModel = calcViewModel,
                onBack = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.Impress -> {
            ImpressScreen(
                viewModel = impressViewModel,
                onBack = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.Pdf -> {
            PdfScreen(
                viewModel = pdfViewModel,
                onBack = { currentScreen = AppScreen.Home }
            )
        }
        is AppScreen.Settings -> {
            SettingsScreen(
                homeViewModel = homeViewModel,
                onBack = { currentScreen = AppScreen.Home }
            )
        }
    }
}
