package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.LevelData
import com.example.data.model.WinCondition

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelPropertiesDialog(
    level: LevelData,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        author: String,
        winCondition: WinCondition,
        parMoves: Int,
        moveLimit: Int,
        difficulty: Difficulty,
        hint: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(level.title) }
    var author by remember { mutableStateOf(level.author) }
    var winCondition by remember { mutableStateOf(level.winCondition) }
    var parMoves by remember { mutableIntStateOf(level.parMoves) }
    var hasMoveLimit by remember { mutableStateOf(level.moveLimit > 0) }
    var moveLimit by remember { mutableIntStateOf(if (level.moveLimit > 0) level.moveLimit else 25) }
    var difficulty by remember { mutableStateOf(level.difficulty) }
    var hint by remember { mutableStateOf(level.hint) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Puzzle Settings & Rules",
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
                // Title & Author
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Puzzle Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prop_title_input"),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author / Creator") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prop_author_input"),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                // Win Condition Selector
                Text(
                    text = "Win Condition",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WinCondition.entries.forEach { condition ->
                        val isSelected = winCondition == condition
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { winCondition = condition }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                } else {
                                    Color(0xFF1E293B)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (condition) {
                                        WinCondition.REACH_EXIT -> Icons.Default.Flag
                                        WinCondition.COLLECT_ALL_STARS -> Icons.Default.Stars
                                        WinCondition.PUSH_ALL_TARGETS -> Icons.Default.ViewInAr
                                        WinCondition.COLLECT_AND_EXIT -> Icons.Default.Stars
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = condition.displayName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray
                                    )
                                    Text(
                                        text = condition.shortDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Difficulty chips
                Text(
                    text = "Target Difficulty",
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

                // Par Moves Stepper
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Par Moves (Target for 3-Star Rating)",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$parMoves Moves",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (parMoves > 3) parMoves-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF334155), CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Par", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { parMoves++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF334155), CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Par", tint = Color.White)
                            }
                        }
                    }
                }

                // Move Limit Cap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enforce Move Limit (Hard Cap)",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Fail if player runs out of moves",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = hasMoveLimit,
                        onCheckedChange = { hasMoveLimit = it }
                    )
                }

                if (hasMoveLimit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Max Moves: $moveLimit", fontWeight = FontWeight.Bold)
                        Row {
                            IconButton(
                                onClick = { if (moveLimit > 5) moveLimit-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF334155), CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Max", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { moveLimit++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF334155), CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Max", tint = Color.White)
                            }
                        }
                    }
                }

                // Hint field
                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Puzzle Hint (Optional)") },
                    placeholder = { Text("e.g. Try sliding on the left ice patch first...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prop_hint_input"),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        author,
                        winCondition,
                        parMoves,
                        if (hasMoveLimit) moveLimit else 0,
                        difficulty,
                        hint
                    )
                },
                modifier = Modifier.testTag("save_properties_button")
            ) {
                Text("Apply Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
