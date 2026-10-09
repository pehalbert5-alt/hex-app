package com.example.hex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

enum class HexTab(val title: String, val icon: String) {
    TERMINAL("Terminal", "💻"),
    EDITOR("Éditeur", "📝"),
    WEB("Aperçu Web", "🌐"),
    DEBUGGER("Débogueur", "🐛"),
    DOCS("Documentation", "📚")
}

@Composable
fun HexDrawerContent(
    currentTab: HexTab,
    onTabSelected: (HexTab) -> Unit,
    session: HexSession,
    onOpenFile: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    var newFileName by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }

    fun getAllFiles(dir: File): List<File> {
        val list = mutableListOf<File>()
        dir.listFiles()?.sortedBy { if (it.isDirectory) "0_${it.name}" else "1_${it.name}" }?.forEach { f ->
            list.add(f)
            if (f.isDirectory) {
                list.addAll(getAllFiles(f))
            }
        }
        return list
    }

    val allFiles = remember(session.currentDir.value, showNewFileDialog) {
        getAllFiles(session.workspace.rootDir)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF181825))
            .padding(12.dp)
    ) {
        // Drawer Title Header
        Text(
            text = "HEX Terminal FR",
            color = Color(0xFF89B4FA),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        HorizontalDivider(color = Color(0xFF313244))
        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        Text("Navigation", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))

        HexTab.entries.forEach { tab ->
            val isSelected = tab == currentTab
            Surface(
                onClick = { onTabSelected(tab) },
                color = if (isSelected) Color(0xFF313244) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tab.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        tab.title,
                        color = if (isSelected) Color(0xFF89B4FA) else Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF313244))
        Spacer(modifier = Modifier.height(12.dp))

        // File Explorer Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Explorateur", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Button(
                onClick = { showNewFileDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF45475A)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("+ Fichier", fontSize = 10.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(allFiles) { file ->
                val relPath = file.path.removePrefix(session.workspace.rootDir.path)
                val depth = relPath.count { it == '/' } - 1
                val indent = "  ".repeat(depth.coerceAtLeast(0))
                val icon = if (file.isDirectory) "📁 " else "📄 "

                Text(
                    text = "$indent$icon${file.name}",
                    color = if (file.isDirectory) Color(0xFFFAB387) else Color(0xFFCDD6F4),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (file.isFile) {
                                onOpenFile(file)
                            }
                        }
                        .padding(vertical = 3.dp)
                )
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Nouveau fichier en français", fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("ex: test.nx ou script.pyfr") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            val f = File(session.currentDir.value, newFileName.trim())
                            f.parentFile?.mkdirs()
                            if (!f.exists()) f.writeText("")
                            onOpenFile(f)
                        }
                        showNewFileDialog = false
                        newFileName = ""
                    }
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
