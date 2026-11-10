package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalTranslationLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var globalCopyToast: Toast? = null

/**
 * Reusable copy button for all search result types (Nouns, Verbs, Sentences, Adjectives, Lists).
 * Copies only the entered word / input text, without any conjugations or declension tables.
 */
@Composable
fun CopyResultButton(
    textToCopy: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    iconSize: Dp = 17.dp,
    testTag: String = "copy_result_button"
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val transLang = LocalTranslationLanguage.current.lowercase()
    var isCopied by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var resetJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val targetBgColor = if (isCopied) {
        if (isDark) Color(0xFF1B5E20) else Color(0xFFC8E6C9)
    } else {
        if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 150),
        label = "copy_btn_bg"
    )

    val iconTint = if (isCopied) {
        if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
    } else {
        if (isDark) Color(0xFFCCCCCC) else Color(0xFF757575)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(animatedBgColor)
            .clickable(
                role = Role.Button,
                onClick = {
                    val cleanText = textToCopy.trim()
                    if (cleanText.isNotBlank()) {
                        clipboardManager.setText(AnnotatedString(cleanText))
                        isCopied = true
                        val toastMsg = when (transLang) {
                            "ar" -> "تم نسخ: $cleanText"
                            "en" -> "Copied: $cleanText"
                            else -> "Kopiert: $cleanText"
                        }
                        // Cancel any previous active toast immediately to avoid queuing
                        globalCopyToast?.cancel()
                        val newToast = Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT)
                        globalCopyToast = newToast
                        newToast.show()

                        resetJob?.cancel()
                        resetJob = coroutineScope.launch {
                            // Display feedback clearly for 900ms then dismiss cleanly and quickly
                            delay(900)
                            newToast.cancel()
                            if (globalCopyToast === newToast) {
                                globalCopyToast = null
                            }
                            isCopied = false
                        }
                    }
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = if (isCopied) "Kopiert" else "Text kopieren",
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
