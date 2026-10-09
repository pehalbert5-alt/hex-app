package com.example.hex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hex.engine.HexSession
import com.example.hex.engine.LineType
import kotlinx.coroutines.launch

@Composable
fun HexTerminalScreen(
    session: HexSession,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var historyIndex by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val quickKeys = listOf("hex", "exécuter", "créer", "|", ">", "<", "\"", ":", ";", "=", "(", ")", "{", "}", "[", "]", "_", "+", "-", "/")

    fun sendCommand(cmd: String) {
        if (cmd.isNotBlank()) {
            session.execute(cmd)
            inputText = ""
            historyIndex = -1
            coroutineScope.launch {
                listState.animateScrollToItem((session.outputLines.size - 1).coerceAtLeast(0))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181825))
    ) {
        // Output Log area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(session.outputLines) { line ->
                val textColor = when (line.type) {
                    LineType.PROMPT -> Color(0xFF89B4FA)
                    LineType.SUCCESS -> Color(0xFFA6E3A1)
                    LineType.ERROR -> Color(0xFFF38BA8)
                    LineType.INFO -> Color(0xFFFAB387)
                    LineType.OUTPUT -> Color(0xFFCDD6F4)
                }

                Text(
                    text = line.text,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (line.type == LineType.PROMPT) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Action chips bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = { sendCommand("aide") },
                label = { Text("Aide", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF313244), labelColor = Color.White)
            )
            AssistChip(
                onClick = { sendCommand("exécuter exemples/complet.nx") },
                label = { Text("▶ Exécuter complet.nx", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF45475A), labelColor = Color(0xFFA6E3A1))
            )
            AssistChip(
                onClick = { sendCommand("exécuter exemples/moyenne.pyfr") },
                label = { Text("▶ Python fr", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF45475A), labelColor = Color(0xFFFAB387))
            )
            AssistChip(
                onClick = { sendCommand("créer projet mon_app") },
                label = { Text("+ Créer Projet", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF313244), labelColor = Color.White)
            )
            AssistChip(
                onClick = { sendCommand("traduire exemples/moyenne.pyfr") },
                label = { Text("Traduire", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF313244), labelColor = Color.White)
            )
            AssistChip(
                onClick = { session.clearScreen() },
                label = { Text("Effacer", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF313244), labelColor = Color(0xFFF38BA8))
            )
        }

        // Quick Symbol Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF11111B))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickKeys.forEach { key ->
                Button(
                    onClick = { inputText += if (key.length > 2) "$key " else key },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313244))
                ) {
                    Text(key, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Input Field & Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E2E))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (session.history.isNotEmpty()) {
                        if (historyIndex < session.history.size - 1) {
                            historyIndex++
                            inputText = session.history[session.history.size - 1 - historyIndex]
                        }
                    }
                }
            ) {
                Text("▲", color = Color.LightGray, fontSize = 16.sp)
            }

            IconButton(
                onClick = {
                    if (historyIndex > 0) {
                        historyIndex--
                        inputText = session.history[session.history.size - 1 - historyIndex]
                    } else if (historyIndex == 0) {
                        historyIndex = -1
                        inputText = ""
                    }
                }
            ) {
                Text("▼", color = Color.LightGray, fontSize = 16.sp)
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                placeholder = { Text("Tapez une commande...", color = Color.Gray, fontSize = 13.sp) },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendCommand(inputText) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF89B4FA),
                    unfocusedBorderColor = Color(0xFF45475A),
                    cursorColor = Color(0xFF89B4FA)
                )
            )

            IconButton(
                onClick = { sendCommand(inputText) }
            ) {
                Text("➔", color = Color(0xFF89B4FA), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
