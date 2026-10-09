package com.example.hex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun HexDocsScreen(
    session: HexSession,
    modifier: Modifier = Modifier
) {
    val docDir = File(session.workspace.rootDir, "documentation/fr")
    val docFiles = remember {
        docDir.listFiles()?.filter { it.extension == "md" }?.sortedBy { it.name } ?: emptyList()
    }

    var activeDocIndex by remember { mutableIntStateOf(0) }

    if (docFiles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E2E)),
            contentAlignment = Alignment.Center
        ) {
            Text("Aucune documentation trouvée.", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }

    val currentDoc = docFiles[activeDocIndex.coerceIn(0, docFiles.size - 1)]
    val text = currentDoc.readText()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
    ) {
        // Doc selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181825))
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            docFiles.forEachIndexed { idx, file ->
                val isSelected = idx == activeDocIndex
                Button(
                    onClick = { activeDocIndex = idx },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF89B4FA) else Color(0xFF313244)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = file.name,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Markdown Reader Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            text.split("\n").forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("# ") -> {
                        Text(
                            text = trimmed.removePrefix("# "),
                            color = Color(0xFF89B4FA),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    trimmed.startsWith("## ") -> {
                        Text(
                            text = trimmed.removePrefix("## "),
                            color = Color(0xFFA6E3A1),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                        Text(
                            text = "• " + trimmed.substring(2),
                            color = Color(0xFFCDD6F4),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                        )
                    }
                    trimmed.startsWith("|") -> {
                        Text(
                            text = trimmed,
                            color = Color(0xFFFAB387),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                    else -> {
                        if (trimmed.isNotEmpty()) {
                            Text(
                                text = line,
                                color = Color(0xFFCDD6F4),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
