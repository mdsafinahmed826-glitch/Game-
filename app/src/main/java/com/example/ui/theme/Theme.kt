package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = DhakaEmeraldBright,
    onPrimary = Color(0xFF042219),
    primaryContainer = DhakaEmerald,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = MarigoldGold,
    onSecondary = Color(0xFF231800),
    secondaryContainer = Color(0xFF423000),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = RickshawCrimson,
    onTertiary = Color.White,
    background = NightAsphalt,
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF132026),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1B2D35),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = SlateBorder,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DhakaEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7F3D0),
    onPrimaryContainer = Color(0xFF022C22),
    secondary = ChaiAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = RickshawCrimson,
    onTertiary = Color.White,
    background = SoftCream,
    onBackground = Color(0xFF0F172A),
    surface = CardGlassLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2EDE8),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFB6CCC3),
  )

val DeshiShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
  )

@Composable
fun DeshiChaosTheme(
  darkTheme: Boolean = true,
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

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = DeshiShapes,
    content = content,
  )
}
