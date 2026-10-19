package cl.duoc.activaciones.model

// Errores individuales del formulario de registro
data class UsuarioErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val confirmarClave: String? = null,
    val direccion: String? = null,
    val area: String? = null,
    val zona: String? = null,
    val terminos: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, correo, clave, confirmarClave, direccion, area, zona, terminos).any { it != null }
}