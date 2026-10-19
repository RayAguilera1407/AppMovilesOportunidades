package cl.duoc.activaciones.model

val AREAS_USUARIO = listOf("Comercial", "Marketing", "Comunicaciones", "Administración")

private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun correoValido(correo: String): Boolean = REGEX_CORREO.matches(correo.trim())

/** Campo numérico opcional: vacío es válido; si tiene texto debe ser un entero >= 0. */
fun errorEnteroPositivo(texto: String): String? = when {
    texto.isBlank() -> null
    texto.toIntOrNull()?.let { it >= 0 } != true -> "Debe ser un número entero positivo"
    else -> null
}

/** Función pura: se puede probar con tests unitarios sin Android. */
fun validarRegistro(s: UsuarioUiState): UsuarioErrores = UsuarioErrores(
    nombre = when {
        s.nombre.isBlank() -> "El nombre es obligatorio"
        s.nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        s.nombre.any { it.isDigit() } -> "El nombre no puede contener números"
        else -> null
    },
    correo = when {
        s.correo.isBlank() -> "El correo es obligatorio"
        !correoValido(s.correo) -> "Correo inválido (ej: nombre@dominio.cl)"
        else -> null
    },
    clave = when {
        s.clave.length < 6 -> "Debe tener al menos 6 caracteres"
        s.clave.none { it.isLetter() } || s.clave.none { it.isDigit() } -> "Debe incluir letras y números"
        else -> null
    },
    confirmarClave = if (s.confirmarClave != s.clave) "Las contraseñas no coinciden" else null,
    direccion = when {
        s.direccion.isBlank() -> "La dirección es obligatoria"
        s.direccion.trim().length < 5 -> "Debe tener al menos 5 caracteres"
        else -> null
    },
    area = if (s.area == null) "Selecciona tu área" else null,
    zona = if (s.zona == null) "Selecciona tu zona o sucursal" else null,
    terminos = if (!s.aceptaTerminos) "Debes aceptar los términos para continuar" else null
)