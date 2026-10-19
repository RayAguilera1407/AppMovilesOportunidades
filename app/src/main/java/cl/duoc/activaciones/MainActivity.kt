package cl.duoc.activaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.duoc.activaciones.ui.navigation.AppNavigation
import cl.duoc.activaciones.ui.theme.ActivacionesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivacionesTheme {
                AppNavigation()
            }
        }
    }
}
