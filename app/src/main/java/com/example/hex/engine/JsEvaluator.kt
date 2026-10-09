package com.example.hex.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView

class JsEvaluator(private val context: Context, private val onOutput: (String) -> Unit) {

    private var webView: WebView? = null

    init {
        Handler(Looper.getMainLooper()).post {
            val wv = WebView(context)
            wv.settings.javaScriptEnabled = true
            wv.addJavascriptInterface(JsBridge(), "AndroidHex")
            webView = wv
        }
    }

    inner class JsBridge {
        @JavascriptInterface
        fun log(msg: String) {
            onOutput(msg)
        }

        @JavascriptInterface
        fun error(msg: String) {
            onOutput("Erreur JS: $msg")
        }
    }

    fun evaluate(jsCode: String) {
        Handler(Looper.getMainLooper()).post {
            val wrapped = """
                (function() {
                    console.log = function() {
                        var args = Array.prototype.slice.call(arguments);
                        AndroidHex.log(args.join(" "));
                    };
                    console.error = function() {
                        var args = Array.prototype.slice.call(arguments);
                        AndroidHex.error(args.join(" "));
                    };
                    window.alert = function(msg) {
                        AndroidHex.log("ALERTE: " + msg);
                    };
                    try {
                        $jsCode
                    } catch(e) {
                        AndroidHex.error(e.toString());
                    }
                })();
            """.trimIndent()

            webView?.evaluateJavascript(wrapped, null)
        }
    }
}
