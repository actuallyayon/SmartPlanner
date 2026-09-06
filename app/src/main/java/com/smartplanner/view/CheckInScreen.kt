package com.smartplanner.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplanner.controller.CheckInController
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.view.theme.DestructiveRed
import com.smartplanner.view.theme.PrimaryBlue
import com.smartplanner.view.theme.SuccessGreen

@Composable
fun CheckInScreen(
    checkInController: CheckInController,
    modifier: Modifier = Modifier
) {
    val state by checkInController.uiState.collectAsState()

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
        Column {
            Text(
                text = "Today's Check-In",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Log today's habits to update your progress.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (state.showSuccess) {
            // Inline Confirmation Success Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓ Check-in saved!",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your habits and streak have been updated.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (state.habits.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits to track. Create a Habit Plan first!",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            state.habits.forEach { habit ->
                val selectedStatus = state.statuses[habit.id]
                val noteText = state.notes[habit.id] ?: ""

                CheckInHabitCard(
                    habit = habit,
                    selectedStatus = selectedStatus,
                    noteText = noteText,
                    onStatusSelected = { checkInController.onStatusSelected(habit.id, it) },
                    onNoteChanged = { checkInController.onNoteChanged(habit.id, it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { checkInController.saveCheckIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(
                    text = "Save Check-In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CheckInHabitCard(
    habit: Habit,
    selectedStatus: CheckInStatus?,
    noteText: String,
    onStatusSelected: (CheckInStatus) -> Unit,
    onNoteChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = habit.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Min version: ${habit.minVersion}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Status Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Done Button (SuccessGreen)
                val isDone = selectedStatus == CheckInStatus.DONE
                OutlinedButton(
                    onClick = { onStatusSelected(CheckInStatus.DONE) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDone) SuccessGreen.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isDone) SuccessGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.run {
                        if (isDone) androidx.compose.foundation.BorderStroke(2.dp, SuccessGreen)
                        else this
                    }
                ) {
                    Text(
                        text = "Done",
                        fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Partial Button (PrimaryBlue)
                val isPartial = selectedStatus == CheckInStatus.PARTIAL
                OutlinedButton(
                    onClick = { onStatusSelected(CheckInStatus.PARTIAL) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isPartial) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isPartial) PrimaryBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.run {
                        if (isPartial) androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue)
                        else this
                    }
                ) {
                    Text(
                        text = "Partial",
                        fontWeight = if (isPartial) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Skipped Button (DestructiveRed)
                val isSkipped = selectedStatus == CheckInStatus.SKIPPED
                OutlinedButton(
                    onClick = { onStatusSelected(CheckInStatus.SKIPPED) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSkipped) DestructiveRed.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isSkipped) DestructiveRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.run {
                        if (isSkipped) androidx.compose.foundation.BorderStroke(2.dp, DestructiveRed)
                        else this
                    }
                ) {
                    Text(
                        text = "Skipped",
                        fontWeight = if (isSkipped) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Note Text Field
            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteChanged,
                label = { Text("Add optional note...", fontSize = 14.sp) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
