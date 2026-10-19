package cl.duoc.activaciones.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Rosa = Color(0xFFD81B60)
val RosaOscuro = Color(0xFFAD1457)
val RosaClaro = Color(0xFFFCE4EC)
val Morado = Color(0xFF7B1FA2)
val MoradoOscuro = Color(0xFF4A148C)
val MoradoClaro = Color(0xFFF3E5F5)
private val Fondo = Color(0xFFFFF7FB)

val DegradadoRosaMorado = Brush.horizontalGradient(listOf(Rosa, Morado))

private val Colores = lightColorScheme(
    primary = Rosa,
    onPrimary = Color.White,
    primaryContainer = RosaClaro,
    onPrimaryContainer = Color(0xFF880E4F),
    secondary = Morado,
    onSecondary = Color.White,
    secondaryContainer = MoradoClaro,
    onSecondaryContainer = MoradoOscuro,
    tertiary = MoradoOscuro,
    background = Fondo,
    surface = Fondo,
    surfaceVariant = Color(0xFFF8E1EE),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF0F6),
    surfaceContainer = Color(0xFFFBE7F1),
    surfaceContainerHigh = Color(0xFFF8E1EE),
    surfaceContainerHighest = Color(0xFFF5E1F3),
    outline = Color(0xFFB07AA1)
)

@Composable
fun ActivacionesTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, content = contenido)
}

/** Barra superior rosa con texto blanco, usada en todas las pantallas. */
@Composable
fun barraColores(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
)
