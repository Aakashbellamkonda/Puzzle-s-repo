package com.example.ui.jigsaw

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.jigsaw.JigsawCodec
import com.example.data.jigsaw.JigsawLevel

@Composable
fun JigsawImportDialog(
    onDismiss: () -> Unit,
    onImportSuccess: (JigsawLevel, playNow: Boolean) -> Unit
) {
    val context = LocalContext.current
    var inputCode by remember { mutableStateOf("") }

    val decodedLevel by remember(inputCode) {
        derivedStateOf {
            if (inputCode.isNotBlank()) JigsawCodec.decode(inputCode).getOrNull() else null
        }
    }

    val isInvalid by remember(inputCode, decodedLevel) {
        derivedStateOf { inputCode.isNotBlank() && decodedLevel == null }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Import Jigsaw Puzzle", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Paste a shared JIGSAW code below to assemble:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it },
                        placeholder = { Text("Paste JIGSAW:1: code here...") },
                        modifier = Modifier.weight(1f).testTag("import_jigsaw_code_input"),
                        maxLines = 2,
                        isError = isInvalid
                    )

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipItem = clipboard.primaryClip?.getItemAt(0)
                            val text = clipItem?.text?.toString() ?: ""
                            if (text.isNotBlank()) inputCode = text
                            else Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("paste_jigsaw_button")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                    }
                }

                if (isInvalid) {
                    Text("Invalid puzzle code format.", color = Color(0xFFEF4444), fontSize = 12.sp)
                }

                decodedLevel?.let { level ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(level.title, fontWeight = FontWeight.Bold, color = Color.White)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(level.difficulty.colorHex).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        level.difficulty.label,
                                        color = Color(level.difficulty.colorHex),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "By ${level.author} • ${level.totalPieces} pieces (${level.rows}x${level.cols})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "Rule: ${level.winCondition.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Preview board
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                                JigsawBoard(
                                    level = level,
                                    placedPieces = emptyList(),
                                    showGhostImage = true,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (decodedLevel != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onImportSuccess(decodedLevel!!, false) },
                        modifier = Modifier.testTag("save_imported_jigsaw")
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = { onImportSuccess(decodedLevel!!, true) },
                        modifier = Modifier.testTag("play_imported_jigsaw")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assemble Now")
                    }
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        dismissButton = {
            if (decodedLevel != null) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
