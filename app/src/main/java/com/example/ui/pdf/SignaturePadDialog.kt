package com.example.ui.pdf

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DrawPoint

@Composable
fun SignaturePadDialog(
    onDismiss: () -> Unit,
    onSignatureConfirmed: (name: String, points: List<DrawPoint>) -> Unit
) {
    var points by remember { mutableStateOf(listOf<DrawPoint>()) }
    var signerName by remember { mutableStateOf("") }

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
                Text(
                    text = "Signature Manuscrite",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tracez votre signature au doigt dans le cadre ci-dessous.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = signerName,
                    onValueChange = { signerName = it },
                    label = { Text("Nom du signataire") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Pad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    points = points + DrawPoint(offset.x, offset.y)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    points = points + DrawPoint(change.position.x, change.position.y)
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Guide line
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(20f, size.height * 0.75f),
                            end = Offset(size.width - 20f, size.height * 0.75f),
                            strokeWidth = 2f
                        )

                        if (points.isNotEmpty()) {
                            val path = Path()
                            path.moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                path.lineTo(points[i].x, points[i].y)
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF1E3A8A),
                                style = Stroke(
                                    width = 4f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (points.isEmpty()) {
                        Text(
                            text = "Signez ici...",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { points = emptyList() },
                        enabled = points.isNotEmpty()
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Effacer")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Annuler")
                        }
                        Button(
                            onClick = {
                                onSignatureConfirmed(
                                    signerName.ifBlank { "Signataire" },
                                    points
                                )
                            },
                            enabled = points.isNotEmpty()
                        ) {
                            Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Appliquer")
                        }
                    }
                }
            }
        }
    }
}
