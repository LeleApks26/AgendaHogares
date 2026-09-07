package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF3B82F6)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(colorInt or 0x00000000FF000000)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        defaultColor
    }
}

/**
 * Calculates optimal text color (#000000 or #FFFFFF) for contrast against the background color.
 */
fun getOptimalTextColorHex(bgColorHex: String): String {
    return try {
        val clean = bgColorHex.removePrefix("#")
        val colorInt = clean.toLong(16)
        val r = ((colorInt shr 16) and 0xFF) / 255.0
        val g = ((colorInt shr 8) and 0xFF) / 255.0
        val b = (colorInt and 0xFF) / 255.0
        val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
        if (luminance > 0.52) "#000000" else "#FFFFFF"
    } catch (_: Exception) {
        "#FFFFFF"
    }
}

@Composable
fun BadgeChip(
    text: String,
    backgroundColorHex: String,
    textColorHex: String = "#FFFFFF",
    forceWhiteText: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val bgColor = parseHexColor(backgroundColorHex, Color(0xFF3B82F6))
    val effectiveTextColorHex = if (forceWhiteText) {
        "#FFFFFF"
    } else if (textColorHex.isBlank() || (textColorHex == "#FFFFFF" && getOptimalTextColorHex(backgroundColorHex) == "#000000")) {
        getOptimalTextColorHex(backgroundColorHex)
    } else {
        textColorHex
    }
    val textColor = parseHexColor(effectiveTextColorHex, Color.White)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
