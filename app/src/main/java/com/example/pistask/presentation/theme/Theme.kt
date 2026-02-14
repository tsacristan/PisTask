package com.example.pistask.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Thème sombre personnalisé
private val DarkColorScheme = darkColorScheme(
    primary = VertPistacheFoncee,
    onPrimary = Blanc,
    secondary = VertKaki,
    onSecondary = VertPistacheClair,

    background = Marron,
    onBackground = Blanc,

    surface = Marron,
    onSurface = Blanc,

    primaryContainer = VertPistacheFoncee,
    onPrimaryContainer = Noir,

    error = RougePastel,
    outline = Gris
)
private val LightColorScheme = lightColorScheme(
    primary = VertPistacheFoncee,      // Boutons, accent principal (fab + accents)
    onPrimary = Blanc,

    secondary = VertKaki,
    onSecondary = Noir,

    background = BackgroundCream,      // Fond général (crème)
    onBackground = Noir,

    surface = Blanc,                   // Cartes, surfaces blanches
    onSurface = Noir,

    primaryContainer = VertPistacheClair,
    onPrimaryContainer = Noir,

    secondaryContainer = VertKaki,
    onSecondaryContainer = VertPistacheFoncee,

    error = RougePastel,
    outline = Gris
)

@Composable
fun PisTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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
        content = content
    )
}