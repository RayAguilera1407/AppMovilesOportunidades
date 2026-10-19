package cl.duoc.activaciones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.activaciones.data.repository.Repositorio
import cl.duoc.activaciones.util.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: Repositorio,
    private val sesion: Sesion
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun ingresar(correo: String, clave: String, alEntrar: () -> Unit) {
        if (correo.isBlank() || clave.isBlank()) {
            _error.value = "Ingresa correo y contraseña"
            return
        }
        viewModelScope.launch {
            val usuario = repo.login(correo, clave)
            if (usuario == null) {
                _error.value = "Correo o contraseña incorrectos"
            } else {
                _error.value = null
                sesion.iniciar(usuario.id)
                alEntrar()
            }
        }
    }
}