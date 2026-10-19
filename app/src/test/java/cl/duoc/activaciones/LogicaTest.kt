package cl.duoc.activaciones

import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.model.Potencial
import cl.duoc.activaciones.model.Perfil
import cl.duoc.activaciones.model.Prioridad
import cl.duoc.activaciones.model.Seguimiento
import cl.duoc.activaciones.model.TipoAccion
import cl.duoc.activaciones.model.TipoEvento
import cl.duoc.activaciones.model.Usuario
import cl.duoc.activaciones.model.DIA_MS
import cl.duoc.activaciones.model.Filtro
import cl.duoc.activaciones.model.calcularAlertas
import cl.duoc.activaciones.model.filtrar
import cl.duoc.activaciones.model.visibles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicaTest {
    private val ahora = 1_000L * DIA_MS

    private fun op(
        id: Long, estado: EstadoOportunidad, enDias: Int = 30, diasEstado: Int = 0,
        responsable: Long = 1, region: String = "Metropolitana", prioridad: Prioridad = Prioridad.MEDIA
    ) = OportunidadEntity(
        id = id, nombre = "Op $id", tipo = TipoEvento.FERIA, descripcion = "desc", fuente = "web", fechaDeteccion = ahora,
        prioridad = prioridad, potencial = Potencial.MEDIO, estado = estado, fechaCambioEstado = ahora - diasEstado * DIA_MS, organizacionId = 1,
        responsableId = responsable, fechaInicio = ahora + enDias * DIA_MS, fechaTermino = ahora + enDias * DIA_MS,
        horario = "", comuna = "Peñalolén", region = region, publicoEsperado = 0, condiciones = "", costo = 0
    )

    @Test
    fun `evento dentro de 14 dias genera alerta de proximidad`() {
        val alertas = calcularAlertas(
            listOf(op(1, EstadoOportunidad.CONTACTO_REALIZADO, enDias = 5), op(2, EstadoOportunidad.CONTACTO_REALIZADO, enDias = 20)),
            emptyList(), ahora
        )
        assertEquals(listOf(1L), alertas.eventosProximos.map { it.id })
    }

    @Test
    fun `seguimiento vencido se marca como atrasado`() {
        val seg = Seguimiento(1, 1, TipoAccion.LLAMADA, ahora - 5 * DIA_MS, "ok", "llamar", ahora - DIA_MS)
        val alertas = calcularAlertas(listOf(op(1, EstadoOportunidad.CONTACTO_REALIZADO)), listOf(seg), ahora)
        assertEquals(1, alertas.seguimientosAtrasados.size)
    }

    @Test
    fun `oportunidad nueva sin gestiones aparece sin contacto`() {
        val registro = Seguimiento(1, 1, TipoAccion.CAMBIO_ESTADO, ahora, "registrada")
        val alertas = calcularAlertas(listOf(op(1, EstadoOportunidad.EVENTO_IDENTIFICADO)), listOf(registro), ahora)
        assertEquals(1, alertas.sinContacto.size)
    }

    @Test
    fun `propuesta con mas de 7 dias queda sin respuesta`() {
        val alertas = calcularAlertas(
            listOf(op(1, EstadoOportunidad.PROPUESTA_ENVIADA, diasEstado = 8), op(2, EstadoOportunidad.PROPUESTA_ENVIADA, diasEstado = 2)),
            emptyList(), ahora
        )
        assertEquals(listOf(1L), alertas.propuestasSinRespuesta.map { it.id })
    }

    @Test
    fun `oportunidad sin fecha de evento no genera alerta de proximidad`() {
        val sinFecha = op(1, EstadoOportunidad.CONTACTO_REALIZADO).copy(fechaInicio = null, fechaTermino = null)
        assertTrue(calcularAlertas(listOf(sinFecha), emptyList(), ahora).eventosProximos.isEmpty())
    }

    @Test
    fun `filtro combina estado region y prioridad`() {
        val ops = listOf(
            op(1, EstadoOportunidad.DESCARTADA, region = "Biobío"),
            op(2, EstadoOportunidad.DESCARTADA, prioridad = Prioridad.ALTA),
            op(3, EstadoOportunidad.CONTACTO_PENDIENTE, prioridad = Prioridad.ALTA)
        )
        val resultado = filtrar(ops, Filtro(estado = EstadoOportunidad.DESCARTADA, region = "Metropolitana", prioridad = Prioridad.ALTA))
        assertEquals(listOf(2L), resultado.map { it.id })
    }

    @Test
    fun `ejecutivo solo ve sus oportunidades y supervisor ve todas`() {
        val ops = listOf(op(1, EstadoOportunidad.DESCARTADA, responsable = 1), op(2, EstadoOportunidad.DESCARTADA, responsable = 2))
        val ejecutivo = Usuario(1, "E", "e@demo.cl", "x", Perfil.EJECUTIVO, "RM")
        val supervisor = Usuario(3, "S", "s@demo.cl", "x", Perfil.SUPERVISOR, "RM")
        assertEquals(1, visibles(ops, ejecutivo).size)
        assertEquals(2, visibles(ops, supervisor).size)
        assertTrue(visibles(ops, null).isEmpty())
    }
}
