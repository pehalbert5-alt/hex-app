package com.example.hex.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

@Composable
fun HexWebPreviewScreen(
    previewFile: File?,
    modifier: Modifier = Modifier
) {
    if (previewFile == null || !previewFile.exists()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E2E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Aucun site web ouvert en aperçu.\nLancez 'site construire' ou 'exécuter page.htmlfr'.",
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        return
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181825))
    ) {
        // Address Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF313244))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌐 Aperçu Web: ${previewFile.name}",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { webViewInstance?.reload() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF89B4FA)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text("🔄 Actualiser", fontSize = 11.sp, color = Color.Black)
            }
        }

        // WebView Content
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    webViewClient = WebViewClient()
                    loadUrl("file://${previewFile.absolutePath}")
                    webViewInstance = this
                }
            },
            update = { wv ->
                wv.loadUrl("file://${previewFile.absolutePath}")
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
