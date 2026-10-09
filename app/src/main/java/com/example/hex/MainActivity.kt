package com.example.hex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hex.engine.HexSession
import com.example.hex.ui.*
import com.example.hex.ui.theme.HexTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HexTheme {
                HexAppMain()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexAppMain() {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(HexTab.TERMINAL) }
    val openFiles = remember { mutableStateListOf<File>() }
    val activeFileIndex = remember { mutableIntStateOf(0) }
    var previewFile by remember { mutableStateOf<File?>(null) }

    var inputPromptText by remember { mutableStateOf<String?>(null) }
    var inputCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
    var userInputValue by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val session = remember {
        HexSession(
            context = context,
            onOpenFileInEditor = { file ->
                if (!openFiles.contains(file)) {
                    openFiles.add(file)
                }
                activeFileIndex.intValue = openFiles.indexOf(file)
                currentTab = HexTab.EDITOR
            },
            onOpenWebPreview = { file ->
                previewFile = file
                currentTab = HexTab.WEB
            },
            onRequestInput = { prompt, callback ->
                inputPromptText = prompt.ifEmpty { "Entrez une valeur :" }
                inputCallback = callback
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF181825)
            ) {
                HexDrawerContent(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        coroutineScope.launch { drawerState.close() }
                    },
                    session = session,
                    onOpenFile = { file ->
                        if (!openFiles.contains(file)) {
                            openFiles.add(file)
                        }
                        activeFileIndex.intValue = openFiles.indexOf(file)
                        currentTab = HexTab.EDITOR
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "HEX Terminal — ${currentTab.title}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF89B4FA)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        ) {
                            Text("☰", color = Color.White, fontSize = 18.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF11111B)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF11111B)
                ) {
                    HexTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = { Text(tab.icon, fontSize = 18.sp) },
                            label = {
                                Text(
                                    tab.title,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Color(0xFF89B4FA) else Color.Gray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF313244)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF1E1E2E))
            ) {
                when (currentTab) {
                    HexTab.TERMINAL -> HexTerminalScreen(session = session)
                    HexTab.EDITOR -> HexEditorScreen(
                        openFiles = openFiles,
                        activeFileIndex = activeFileIndex,
                        session = session
                    )
                    HexTab.WEB -> HexWebPreviewScreen(previewFile = previewFile)
                    HexTab.DEBUGGER -> HexDebuggerScreen(session = session)
                    HexTab.DOCS -> HexDocsScreen(session = session)
                }
            }
        }
    }

    // Input Dialog for saisir() / input()
    if (inputPromptText != null) {
        AlertDialog(
            onDismissRequest = {
                inputCallback?.invoke("")
                inputPromptText = null
                inputCallback = null
            },
            title = {
                Text(
                    text = inputPromptText ?: "Saisir une valeur",
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = userInputValue,
                    onValueChange = { userInputValue = it },
                    placeholder = { Text("Entrez votre réponse...") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        inputCallback?.invoke(userInputValue)
                        inputPromptText = null
                        inputCallback = null
                        userInputValue = ""
                    }
                ) {
                    Text("Valider")
                }
            }
        )
    }
}
