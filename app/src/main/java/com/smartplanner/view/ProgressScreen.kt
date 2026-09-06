package com.smartplanner.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplanner.controller.ProgressController
import com.smartplanner.model.ProgressPoint
import com.smartplanner.view.theme.PrimaryBlue

@Composable
fun ProgressScreen(
    progressController: ProgressController,
    modifier: Modifier = Modifier
) {
    val state by progressController.uiState.collectAsState()
    val isEmptyState by progressController.isEmptyState.collectAsState()

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Progress & Consistency",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Consistency trend over the last 14 days.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Demo State Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Demo Toggle: Empty State",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Simulate state with no data points yet.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = isEmptyState,
                    onCheckedChange = { progressController.toggleEmptyState() },
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                )
            }
        }

        if (state.isEmptyState || state.points.isEmpty()) {
            // Empty State Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "No Chart Data",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Not enough check-in data to display progress yet.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Complete your daily check-ins on the Check-In tab to generate your consistency graph.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Display Consistency Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Consistency History (%)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ConsistencyChart(points = state.points)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "14 Days Ago",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Today",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Summarized stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val avg = state.points.map { it.consistencyScore }.average().toFloat()
                        StatItem(label = "Avg Consistency", value = "${String.format("%.1f", avg)}%")
                        
                        val streak = calculateStreak(state.points)
                        StatItem(label = "Current Streak", value = "$streak days")
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 18.sp,
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConsistencyChart(points: List<ProgressPoint>) {
    val lineBrush = Brush.linearGradient(
        colors = listOf(PrimaryBlue, PrimaryBlue.copy(alpha = 0.5f))
    )
    val fillBrush = Brush.verticalGradient(
        colors = listOf(PrimaryBlue.copy(alpha = 0.25f), Color.Transparent)
    )
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Transparent)
    ) {
        val width = size.width
        val height = size.height
        val maxScore = 100f

        // Draw Y-axis grids
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.isEmpty()) return@Canvas

        val stepX = width / (points.size - 1)
        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, progressPoint ->
            // Normalize score from 0-100 to y coordinate (remember y is 0 at the top, height at the bottom)
            val normalizedY = height - (progressPoint.consistencyScore / maxScore) * height
            val x = index * stepX

            if (index == 0) {
                path.moveTo(x, normalizedY)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, normalizedY)
            } else {
                path.lineTo(x, normalizedY)
                fillPath.lineTo(x, normalizedY)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }

            // Draw points
            drawCircle(
                color = PrimaryBlue,
                radius = 3.dp.toPx(),
                center = Offset(x, normalizedY)
            )
        }

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = fillBrush
        )

        // Draw line stroke
        drawPath(
            path = path,
            brush = lineBrush,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private fun calculateStreak(points: List<ProgressPoint>): Int {
    var streak = 0
    // Check from the end (today) backwards
    for (i in points.indices.reversed()) {
        if (points[i].consistencyScore > 50f) {
            streak++
        } else {
            break
        }
    }
    return streak
}
