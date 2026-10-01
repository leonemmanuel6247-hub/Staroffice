package com.example.ui.calc

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CalcChart
import com.example.data.model.ChartType
import kotlin.math.cos
import kotlin.math.sin

data class ChartDataPoint(val label: String, val value: Double)

@Composable
fun CalcChartDialog(
    chart: CalcChart,
    dataPoints: List<ChartDataPoint>,
    onDismiss: () -> Unit
) {
    val chartPalette = listOf(
        Color(0xFF2563EB), // Blue
        Color(0xFF059669), // Green
        Color(0xFFD97706), // Amber
        Color(0xFFDC2626), // Red
        Color(0xFF7C3AED), // Purple
        Color(0xFF0891B2), // Cyan
        Color(0xFFE11D48)  // Rose
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chart.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Type : ${when (chart.type) {
                                ChartType.BAR -> "Histogramme"
                                ChartType.LINE -> "Courbe"
                                ChartType.PIE -> "Camembert"
                            }} • Plage ${chart.labelColumn}${chart.startRow}:${chart.valueColumn}${chart.endRow}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    if (dataPoints.isEmpty() || dataPoints.all { it.value == 0.0 }) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aucune donnée chiffrée valide dans la sélection", color = Color.Gray)
                        }
                    } else {
                        when (chart.type) {
                            ChartType.BAR -> {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val maxVal = dataPoints.maxOf { it.value }.coerceAtLeast(1.0)
                                    val barWidth = (size.width / (dataPoints.size * 2f)).coerceIn(12f, 48f)
                                    val spacing = size.width / dataPoints.size

                                    // Baseline
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.5f),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 2f
                                    )

                                    dataPoints.forEachIndexed { i, pt ->
                                        val barHeight = ((pt.value / maxVal) * (size.height * 0.85f)).toFloat()
                                        val x = i * spacing + (spacing - barWidth) / 2
                                        val y = size.height - barHeight
                                        val color = chartPalette[i % chartPalette.size]

                                        drawRoundRect(
                                            color = color,
                                            topLeft = Offset(x, y),
                                            size = Size(barWidth, barHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                        )
                                    }
                                }
                            }
                            ChartType.LINE -> {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val maxVal = dataPoints.maxOf { it.value }.coerceAtLeast(1.0)
                                    val minVal = dataPoints.minOf { it.value }.coerceAtMost(0.0)
                                    val range = (maxVal - minVal).coerceAtLeast(1.0)
                                    val stepX = if (dataPoints.size > 1) size.width / (dataPoints.size - 1) else size.width

                                    val path = Path()
                                    val points = mutableListOf<Offset>()

                                    dataPoints.forEachIndexed { i, pt ->
                                        val x = i * stepX
                                        val normalized = ((pt.value - minVal) / range).toFloat()
                                        val y = size.height - (normalized * (size.height * 0.8f) + size.height * 0.1f)
                                        val offset = Offset(x, y)
                                        points.add(offset)
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }

                                    // Line
                                    drawPath(
                                        path = path,
                                        color = Color(0xFF2563EB),
                                        style = Stroke(width = 4f)
                                    )

                                    // Dots
                                    points.forEach { pt ->
                                        drawCircle(color = Color.White, radius = 6f, center = pt)
                                        drawCircle(color = Color(0xFF2563EB), radius = 4f, center = pt)
                                    }
                                }
                            }
                            ChartType.PIE -> {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val total = dataPoints.sumOf { it.value.coerceAtLeast(0.0) }.coerceAtLeast(1.0)
                                    val diameter = minOf(size.width, size.height) * 0.9f
                                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                                    var currentAngle = -90f

                                    dataPoints.forEachIndexed { i, pt ->
                                        val sweep = ((pt.value.coerceAtLeast(0.0) / total) * 360f).toFloat()
                                        if (sweep > 0f) {
                                            drawArc(
                                                color = chartPalette[i % chartPalette.size],
                                                startAngle = currentAngle,
                                                sweepAngle = sweep,
                                                useCenter = true,
                                                topLeft = topLeft,
                                                size = Size(diameter, diameter)
                                            )
                                            currentAngle += sweep
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legends
                Text(
                    text = "Légende & Valeurs :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dataPoints.take(6).forEachIndexed { i, pt ->
                        val color = chartPalette[i % chartPalette.size]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pt.label.ifBlank { "Élément ${i + 1}" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "%.2f".format(pt.value),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}
