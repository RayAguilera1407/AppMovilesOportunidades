package cl.duoc.activaciones.model

// Estado del formulario de registro
data class UsuarioUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val direccion: String = "",
    val area: String? = null,
    val zona: String? = null,
    val aceptaTerminos: Boolean = false,
    val registrando: Boolean = false,
    val errores: UsuarioErrores = UsuarioErrores()
)