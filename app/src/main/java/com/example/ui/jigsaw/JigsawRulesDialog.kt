package com.example.ui.jigsaw

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.data.jigsaw.JigsawLevel
import com.example.data.jigsaw.JigsawWinCondition
import com.example.data.model.Difficulty

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JigsawRulesDialog(
    level: JigsawLevel,
    onDismiss: () -> Unit,
    onApply: (
        title: String,
        author: String,
        winCondition: JigsawWinCondition,
        timeLimit: Int,
        parTime: Int,
        maxMoves: Int,
        difficulty: Difficulty,
        hint: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(level.title) }
    var author by remember { mutableStateOf(level.author) }
    var winCondition by remember { mutableStateOf(level.winCondition) }
    var timeLimit by remember { mutableIntStateOf(level.timeLimitSeconds) }
    var parTime by remember { mutableIntStateOf(level.parTimeSeconds) }
    var maxMoves by remember { mutableIntStateOf(level.maxMoves) }
    var difficulty by remember { mutableStateOf(level.difficulty) }
    var hint by remember { mutableStateOf(level.hint) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Puzzle Rules & Objectives",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Puzzle Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("jigsaw_title_input"),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author / Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("jigsaw_author_input"),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                Text(
                    text = "Win Condition",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    JigsawWinCondition.entries.forEach { cond ->
                        val isSelected = winCondition == cond
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { winCondition = cond }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color(0xFF1E293B)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (cond) {
                                        JigsawWinCondition.STANDARD -> Icons.Default.Stars
                                        JigsawWinCondition.TIME_ATTACK -> Icons.Default.Timer
                                        JigsawWinCondition.MOVE_LIMIT -> Icons.Default.HourglassBottom
                                        JigsawWinCondition.EDGES_FIRST -> Icons.Default.PanTool
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = cond.displayName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray
                                    )
                                    Text(
                                        text = cond.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                if (winCondition == JigsawWinCondition.TIME_ATTACK) {
                    Column {
                        Text(
                            text = "Time Limit: ${timeLimit}s",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(60, 90, 120, 180, 240).forEach { sec ->
                                FilterChip(
                                    selected = timeLimit == sec,
                                    onClick = { timeLimit = sec },
                                    label = { Text("${sec}s") }
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Par Time (For 3-Star Rating): ${parTime}s",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 60, 90, 120, 150).forEach { sec ->
                            FilterChip(
                                selected = parTime == sec,
                                onClick = { parTime = sec },
                                label = { Text("${sec}s") }
                            )
                        }
                    }
                }

                // Difficulty chips
                Text(
                    text = "Difficulty Label",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(diff.colorHex).copy(alpha = 0.25f),
                                selectedLabelColor = Color(diff.colorHex)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Hint or Assembling Tip") },
                    placeholder = { Text("e.g. Look for the bright sun pieces first...") },
                    modifier = Modifier.fillMaxWidth().testTag("jigsaw_hint_input"),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(title, author, winCondition, timeLimit, parTime, maxMoves, difficulty, hint)
                },
                modifier = Modifier.testTag("jigsaw_apply_rules")
            ) {
                Text("Apply Rules")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
