package haptikos.gestortareashogar_haptikos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = White,
    primaryContainer = LightYellow,
    secondary = LightOrange,
    secondaryContainer = YellowGreen,
    tertiary = Green,
    surfaceVariant = Gray,
    onSurfaceVariant = DarkGray,
    errorContainer = LightRed,
    error = Red,
    background = LightGray,
    onBackground = Black,
    surface = White,
    onSurface = Black
)

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = White,
    primaryContainer = LightYellow,
    secondary = LightOrange,
    secondaryContainer = YellowGreen,
    tertiary = Green,
    surfaceVariant = Gray,
    onSurfaceVariant = DarkGray,
    errorContainer = LightRed,
    error = Red,
    background = LightGray,
    onBackground = Black,
    surface = White,
    onSurface = Black
)

@Composable
fun GestorTareasHogar_HaptikosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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