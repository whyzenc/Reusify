package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
  lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = OnCrimson,
    primaryContainer = CrimsonContainer,
    onPrimaryContainer = OnCrimsonContainer,
    secondary = OrangeDeep,
    onSecondary = Color.White,
    secondaryContainer = OrangeContainer,
    onSecondaryContainer = OnOrangeContainer,
    tertiary = OrangeAccent,
    onTertiary = Color.White,
    background = SurfaceIvory,
    onBackground = TextPrimary,
    surface = SurfacePureWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSoftWarm,
    onSurfaceVariant = TextSecondary,
    outline = OutlineWarm,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = ErrorRed,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = OrangeLight,
    onSecondary = Color(0xFF4A1D00),
    secondaryContainer = Color(0xFF5C2600),
    onSecondaryContainer = OrangeContainer,
    background = DarkBackground,
    onBackground = Color(0xFFFDF3F3),
    surface = DarkSurface,
    onSurface = Color(0xFFFDF3F3),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFE0C8C8),
    outline = Color(0xFF4E3838),
  )

val ReusifyShapes =
  Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun ReusifyTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = ReusifyTypography,
    shapes = ReusifyShapes,
    content = content,
  )
}
