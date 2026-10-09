package com.example.hex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hex.engine.HexSession
import java.io.File

@Composable
fun HexDebuggerScreen(
    session: HexSession,
    modifier: Modifier = Modifier
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var currentLine by remember { mutableIntStateOf(1) }
    var breakpoints by remember { mutableStateOf(setOf<Int>()) }
    var variablesMap by remember { mutableStateOf(mapOf("x" to "10", "MAX" to "50", "notes" to "[14, 18, 12, 16]")) }

    val nxFiles = remember(session.currentDir.value) {
        val list = mutableListOf<File>()
        fun scan(dir: File) {
            dir.listFiles()?.forEach { f ->
                if (f.isDirectory) scan(f)
                else if (f.name.endsWith(".nx")) list.add(f)
            }
        }
        scan(session.currentDir.value)
        list
    }

    if (selectedFile == null && nxFiles.isNotEmpty()) {
        selectedFile = nxFiles.first()
    }

    val lines = selectedFile?.readText()?.split('\n') ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
    ) {
        // Debugger Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181825))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🐛 Débogueur Pas-à-Pas : ${selectedFile?.name ?: "Aucun"}",
                color = Color(0xFFFAB387),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // Action controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF313244))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = {
                    if (lines.isNotEmpty()) {
                        currentLine = (currentLine % lines.size) + 1
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF89B4FA)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Pas (s)", fontSize = 12.sp, color = Color.Black)
            }

            Button(
                onClick = {
                    if (lines.isNotEmpty()) {
                        currentLine = (currentLine % lines.size) + 1
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6E3A1)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Suivant (n)", fontSize = 12.sp, color = Color.Black)
            }

            Button(
                onClick = {
                    val nextBp = breakpoints.filter { it > currentLine }.minOrNull() ?: currentLine
                    currentLine = nextBp
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAB387)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Continuer (c)", fontSize = 12.sp, color = Color.Black)
            }

            Button(
                onClick = {
                    breakpoints = if (breakpoints.contains(currentLine)) {
                        breakpoints - currentLine
                    } else {
                        breakpoints + currentLine
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF38BA8)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Point (${currentLine})", fontSize = 12.sp, color = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Code lines view
            LazyColumn(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E1E2E))
                    .padding(8.dp)
            ) {
                items(lines.indices.toList()) { idx ->
                    val lineNum = idx + 1
                    val isCurrent = lineNum == currentLine
                    val isBp = breakpoints.contains(lineNum)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when {
                                    isCurrent -> Color(0xFF45475A)
                                    isBp -> Color(0xFF585B70)
                                    else -> Color.Transparent
                                }
                            )
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBp) "🔴 " else if (isCurrent) "► " else "   ",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                        Text(
                            text = "$lineNum  ",
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Text(
                            text = lines[idx],
                            color = if (isCurrent) Color(0xFFA6E3A1) else Color(0xFFCDD6F4),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Variables & Stack panel
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight()
                    .background(Color(0xFF181825))
                    .padding(8.dp)
            ) {
                Text(
                    "📊 Variables locales",
                    color = Color(0xFF89B4FA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                variablesMap.forEach { (key, value) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF313244)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(key, color = Color(0xFFFAB387), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text(value, color = Color(0xFFA6E3A1), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "📚 Pile d'appels",
                    color = Color(0xFF89B4FA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(" #0 programme principal (ligne $currentLine)", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
