package com.smartplanner.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplanner.model.AnchorConfig
import com.smartplanner.model.AnchorType
import com.smartplanner.model.Habit
import com.smartplanner.view.theme.DestructiveRed
import com.smartplanner.view.theme.PrimaryBlue
import com.smartplanner.view.theme.SuccessGreen

/**
 * Returns a display icon for an AnchorType.
 */
fun getAnchorIcon(type: AnchorType): ImageVector {
    return when (type) {
        AnchorType.CHARGING_STARTED -> Icons.Default.Bolt
        AnchorType.HEADPHONES_CONNECTED -> Icons.Default.Headphones
        AnchorType.ARRIVED_HOME -> Icons.Default.Home
        AnchorType.FIRST_UNLOCK -> Icons.Default.LockOpen
        AnchorType.CLOCK_TIME -> Icons.Default.Schedule
    }
}

/**
 * Chip/Badge rendering the active real anchor trigger for a habit.
 */
@Composable
fun AnchorBadge(
    anchorType: AnchorType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = PrimaryBlue.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .border(1.dp, PrimaryBlue.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = getAnchorIcon(anchorType),
                contentDescription = anchorType.displayName,
                tint = PrimaryBlue,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = anchorType.displayName,
                color = PrimaryBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Interactive Dialog for selecting and configuring context-aware Real Anchors.
 */
@Composable
fun AnchorConfigDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (type: AnchorType, clockTime: String, cooldownMinutes: Long, quietStart: Int, quietEnd: Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(habit.anchorType) }
    var clockTime by remember { mutableStateOf(habit.anchorConfig?.clockTimeString ?: "08:00") }
    var cooldownMinutes by remember { mutableLongStateOf(habit.anchorConfig?.cooldownMinutes ?: 120L) }
    var quietStartHour by remember { mutableIntStateOf(habit.anchorConfig?.quietHoursStartHour ?: 22) }
    var quietEndHour by remember { mutableIntStateOf(habit.anchorConfig?.quietHoursEndHour ?: 7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().testTag("anchor_config_dialog"),
        title = {
            Column {
                Text(
                    text = "Configure Real Anchor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = habit.title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Choose a context-aware device signal to anchor this habit:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                // Anchor Types Radio Cards
                AnchorType.values().forEach { type ->
                    val isSelected = selectedType == type
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .testTag("anchor_option_${type.name}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue)
                        else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = getAnchorIcon(type),
                                contentDescription = null,
                                tint = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = type.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Permission Note for Arrived Home
                if (selectedType == AnchorType.ARRIVED_HOME) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📍 Location Permission Notice",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "Arrived Home uses geofencing. If background location is not granted, this anchor automatically falls back to Clock Time (${clockTime}) with no data loss.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Clock time fallback / input
                if (selectedType == AnchorType.CLOCK_TIME || selectedType == AnchorType.ARRIVED_HOME) {
                    OutlinedTextField(
                        value = clockTime,
                        onValueChange = { clockTime = it },
                        label = { Text("Scheduled Time / Fallback (HH:mm)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("anchor_clock_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                    )
                }

                // Per-Habit Rules: Quiet Hours & Cooldown
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🛡️ Habit Rules & Protection",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Max 1 reminder per day per habit\n• Quiet Hours: ${quietStartHour}:00 to 0${quietEndHour}:00 (No late disturbance)\n• Cooldown: ${cooldownMinutes} minutes between repeat attempts",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedType, clockTime, cooldownMinutes, quietStartHour, quietEndHour)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("save_anchor_btn")
            ) {
                Text("Save Anchor", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    )
}

/**
 * Message Card showing trigger test feedback.
 */
@Composable
fun TriggerFeedbackBanner(
    message: String,
    isAllowed: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isAllowed) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
    val contentColor = if (isAllowed) SuccessGreen else Color(0xFFD97706)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isAllowed) Icons.Default.Check else Icons.Default.Bolt,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}
