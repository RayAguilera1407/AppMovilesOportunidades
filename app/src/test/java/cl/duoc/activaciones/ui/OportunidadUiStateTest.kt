package cl.duoc.activaciones.ui

import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.ui.oportunidad.OportunidadUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OportunidadUiStateTest {
    @Test
    fun `una oportunidad nueva parte en Evento identificado y sin errores`() {
        val estado = OportunidadUiState()
        assertEquals(EstadoOportunidad.EVENTO_IDENTIFICADO, estado.estado)
        assertFalse(estado.tieneErrores)
    }

    @Test
    fun `cualquier error de campo marca el formulario con errores`() {
        assertTrue(OportunidadUiState(nombreError = "El nombre es obligatorio").tieneErrores)
        assertTrue(OportunidadUiState(potencialError = "Selecciona el potencial").tieneErrores)
    }
}
