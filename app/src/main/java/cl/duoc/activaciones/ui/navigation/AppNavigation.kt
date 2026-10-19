package cl.duoc.activaciones.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.activaciones.ui.components.app
import cl.duoc.activaciones.ui.components.vmFactory
import cl.duoc.activaciones.ui.screen.AlertasScreen
import cl.duoc.activaciones.ui.screen.DetalleScreen
import cl.duoc.activaciones.ui.oportunidad.ListaOportunidadesScreen
import cl.duoc.activaciones.ui.screen.LoginScreen
import cl.duoc.activaciones.ui.screen.MenuScreen
import cl.duoc.activaciones.ui.oportunidad.OportunidadFormScreen
import cl.duoc.activaciones.ui.screen.OrganizacionDetalleScreen
import cl.duoc.activaciones.ui.screen.OrganizacionesScreen
import cl.duoc.activaciones.ui.screen.PerfilScreen
import cl.duoc.activaciones.ui.screen.QrScannerScreen
import cl.duoc.activaciones.ui.screen.RegistroScreen
import cl.duoc.activaciones.ui.screen.ResumenScreen
import cl.duoc.activaciones.viewmodel.UsuarioViewModel

object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val LISTA = "oportunidades"
    const val FORM = "oportunidad_form/{id}"
    const val DETALLE = "oportunidad_detalle/{id}"
    const val ALERTAS = "alertas"
    const val ORGANIZACIONES = "organizaciones"
    const val ORGANIZACION = "organizacion/{id}"
    const val PERFIL = "perfil"
    const val REGISTRO = "registro"
    const val RESUMEN = "resumen"
    const val QR = "qr_scanner"

    fun form(id: Long = 0) = "oportunidad_form/$id"
    fun detalle(id: Long) = "oportunidad_detalle/$id"
    fun organizacion(id: Long) = "organizacion/$id"
}

/** Flujo: Login → Menú → ListaOportunidades → OportunidadForm (y demás módulos). */
@Composable
fun AppNavigation() {
    val app = app()
    val nav = rememberNavController()

    // ViewModel compartido entre Registro y Resumen (Guía 11)
    val usuarioViewModel: UsuarioViewModel =
        viewModel(factory = vmFactory { UsuarioViewModel(app.repo, app.oportunidadRepo, app.sesion) })

    val inicio = remember { if (app.sesion.usuarioId.value != null) Rutas.MENU else Rutas.LOGIN }
    val idArg = listOf(navArgument("id") { type = NavType.LongType })

    NavHost(nav, startDestination = inicio) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                alEntrar = { nav.navigate(Rutas.MENU) { popUpTo(nav.graph.id) { inclusive = true } } },
                alRegistrar = { nav.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            RegistroScreen(
                viewModel = usuarioViewModel,
                alVolver = { nav.popBackStack() },
                alRegistrado = {
                    nav.navigate(Rutas.RESUMEN) { popUpTo(Rutas.REGISTRO) { inclusive = true } }
                }
            )
        }
        composable(Rutas.RESUMEN) {
            ResumenScreen(
                viewModel = usuarioViewModel,
                alIrALogin = { nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } } }
            )
        }
        composable(Rutas.MENU) {
            MenuScreen(
                alOportunidades = { nav.navigate(Rutas.LISTA) },
                alAlertas = { nav.navigate(Rutas.ALERTAS) },
                alOrganizaciones = { nav.navigate(Rutas.ORGANIZACIONES) },
                alPerfil = { nav.navigate(Rutas.PERFIL) }
            )
        }
        composable(Rutas.LISTA) {
            ListaOportunidadesScreen(
                alVolver = { nav.popBackStack() },
                alAbrir = { nav.navigate(Rutas.detalle(it)) },
                alEditar = { nav.navigate(Rutas.form(it)) },
                alCrear = { nav.navigate(Rutas.form()) }
            )
        }
        composable(Rutas.FORM, arguments = idArg) { e ->
            val qr by e.savedStateHandle.getStateFlow<String?>("qr", null).collectAsState()
            OportunidadFormScreen(
                id = e.arguments?.getLong("id") ?: 0L,
                alVolver = { nav.popBackStack() },
                alEscanearQr = { nav.navigate(Rutas.QR) },
                qrEscaneado = qr,
                alConsumirQr = { e.savedStateHandle["qr"] = null }
            )
        }
        composable(Rutas.QR) {
            QrScannerScreen(
                alVolver = { nav.popBackStack() },
                alEscanear = { texto ->
                    nav.previousBackStackEntry?.savedStateHandle?.set("qr", texto)
                    nav.popBackStack()
                }
            )
        }
        composable(Rutas.DETALLE, arguments = idArg) { e ->
            DetalleScreen(
                id = e.arguments?.getLong("id") ?: 0L,
                alVolver = { nav.popBackStack() },
                alEditar = { nav.navigate(Rutas.form(it)) }
            )
        }
        composable(Rutas.ALERTAS) {
            AlertasScreen(alVolver = { nav.popBackStack() }, alAbrir = { nav.navigate(Rutas.detalle(it)) })
        }
        composable(Rutas.ORGANIZACIONES) {
            OrganizacionesScreen(alVolver = { nav.popBackStack() }, alAbrir = { nav.navigate(Rutas.organizacion(it)) })
        }
        composable(Rutas.ORGANIZACION, arguments = idArg) { e ->
            OrganizacionDetalleScreen(
                id = e.arguments?.getLong("id") ?: 0L,
                alVolver = { nav.popBackStack() },
                alAbrirOportunidad = { nav.navigate(Rutas.detalle(it)) }
            )
        }
        composable(Rutas.PERFIL) {
            PerfilScreen(
                alVolver = { nav.popBackStack() },
                alSalir = { nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } } }
            )
        }
    }
}