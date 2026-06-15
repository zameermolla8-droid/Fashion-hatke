package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CharcoalPrimary,
    secondary = SandAccent,
    tertiary = Charcoal80,
    background = DarkCharcoal,
    surface = CharcoalPrimary,
    onPrimary = WarmWhite,
    onSecondary = DarkCharcoal,
    onBackground = LightGreyBg,
    onSurface = LightGreyBg
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CharcoalPrimary,
    secondary = SandAccent,
    tertiary = SoftBone,
    background = LightGreyBg,
    surface = WarmWhite,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = DarkCharcoal,
    onBackground = DarkCharcoal,
    onSurface = DarkCharcoal,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors to guarantee our premium Editorial palette displays exactly as requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
