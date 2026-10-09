package com.example.hex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hex.engine.FrenchTranspiler
import com.example.hex.engine.HexSession
import java.io.File

@Composable
fun HexEditorScreen(
    openFiles: MutableList<File>,
    activeFileIndex: MutableState<Int>,
    session: HexSession,
    modifier: Modifier = Modifier
) {
    if (openFiles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E2E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Aucun fichier ouvert dans l'éditeur.\nUtilisez 'ouvrir <fichier>' dans le terminal.",
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        return
    }

    val currentFileIndex = activeFileIndex.value.coerceIn(0, openFiles.size - 1)
    val currentFile = openFiles[currentFileIndex]
    var content by remember(currentFile) { mutableStateOf(currentFile.readText()) }
    var statusMessage by remember { mutableStateOf("") }

    val lines = content.split('\n')
    val lineNumbersString = (1..lines.size).joinToString("\n")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
    ) {
        // Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181825))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            openFiles.forEachIndexed { idx, file ->
                val isSelected = idx == currentFileIndex
                Surface(
                    onClick = { activeFileIndex.value = idx },
                    color = if (isSelected) Color(0xFF313244) else Color(0xFF11111B),
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = file.name,
                            color = if (isSelected) Color(0xFF89B4FA) else Color.LightGray,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✕",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        }

        // Toolbar Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF313244))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    currentFile.writeText(content)
                    statusMessage = "Enregistré : ${currentFile.name}"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF89B4FA)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("💾 Enregistrer", fontSize = 12.sp, color = Color.Black)
            }

            Button(
                onClick = {
                    currentFile.writeText(content)
                    session.executeFile(currentFile)
                    statusMessage = "Exécution de ${currentFile.name}"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6E3A1)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("▶ Exécuter", fontSize = 12.sp, color = Color.Black)
            }

            Button(
                onClick = {
                    val transpiled = FrenchTranspiler.transpileFile(content, currentFile.name, toReal = true)
                    statusMessage = "Traduit en vrai code."
                    session.appendLine("--- Code réel (${currentFile.name}) ---", com.example.hex.engine.LineType.INFO)
                    session.appendLine(transpiled.code, com.example.hex.engine.LineType.OUTPUT)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAB387)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("🔄 Traduire", fontSize = 12.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = Color.Green,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Code Editor Body (Line Numbers + TextField)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Line numbers column
            Text(
                text = lineNumbersString,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .background(Color(0xFF181825))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )

            // Text Area
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = Color(0xFFCDD6F4),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF89B4FA)
                )
            )
        }
    }
}
