package com.mubarak.mbcompass.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

object Const {
    const val APP_PAGE = "https://github.com/CompassMB/MBCompass"
    const val LICENSE_PAGE = "https://www.gnu.org/licenses/gpl-3.0.txt"
    const val SUPPORT_PAGE = "https://compassmb.github.io/MBCompass-site/donate.html"
    const val AUTHOR_EMAIL = "dev.mubarakbasha@proton.me"
}


@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}