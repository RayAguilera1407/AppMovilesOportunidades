package cl.duoc.activaciones.data.repository

import androidx.room.withTransaction
import cl.duoc.activaciones.data.local.AppDatabase
import cl.duoc.activaciones.data.local.OportunidadEntity
import cl.duoc.activaciones.model.Alertas
import cl.duoc.activaciones.model.Contacto
import cl.duoc.activaciones.model.DIA_MS
import cl.duoc.activaciones.model.EstadoOportunidad
import cl.duoc.activaciones.model.Organizacion
import cl.duoc.activaciones.model.Perfil
import cl.duoc.activaciones.model.Potencial
import cl.duoc.activaciones.model.Prioridad
import cl.duoc.activaciones.model.Seguimiento
import cl.duoc.activaciones.model.TipoAccion
import cl.duoc.activaciones.model.TipoEvento
import cl.duoc.activaciones.model.Usuario
import cl.duoc.activaciones.model.calcularAlertas
import cl.duoc.activaciones.model.visibles

/** Usuarios, organizaciones, alertas y datos de prueba. Aquí se conectaría Retrofit con el backend. */
class Repositorio(private val db: AppDatabase) {
    private val usuarios = db.usuarioDao()
    private val organizaciones = db.organizacionDao()
    private val oportunidades = db.oportunidadDao()
    private val seguimientos = db.seguimientoDao()

    /** SHA-256 sin sal: suficiente para una demo académica (en producción: bcrypt/Argon2). */
    private fun hash(clave: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(clave.toByteArray()).joinToString("") { "%02x".format(it) }

    suspend fun login(correo: String, clave: String) =
        usuarios.login(correo.trim().lowercase(), hash(clave))

    fun usuario(id: Long) = usuarios.porId(id)
    suspend fun usuarioUnaVez(id: Long) = usuarios.porIdUnaVez(id)
    fun usuarios() = usuarios.todos()

    fun organizaciones() = organizaciones.todas()
    fun organizacion(id: Long) = organizaciones.porId(id)
    fun contactos(orgId: Long) = organizaciones.contactos(orgId)
    suspend fun guardarOrganizacion(o: Organizacion) = organizaciones.insertar(o)
    suspend fun guardarContacto(c: Contacto) = organizaciones.insertarContacto(c)
    suspend fun eliminarContacto(id: Long) = organizaciones.eliminarContacto(id)

    /** Elimina la organización y sus contactos; sus oportunidades se conservan sin organización. */
    suspend fun eliminarOrganizacion(id: Long) = db.withTransaction {
        oportunidades.desvincularOrganizacion(id)
        organizaciones.eliminarContactosDe(id)
        organizaciones.eliminar(id)
    }

    suspend fun alertasUnaVez(usuarioId: Long): Alertas {
        val usuario = usuarios.porIdUnaVez(usuarioId)
        return calcularAlertas(
            visibles(oportunidades.todasUnaVez(), usuario),
            seguimientos.todosUnaVez(),
            System.currentTimeMillis()
        )
    }

    /** Datos ficticios para poder probar la app apenas se instala. */
    suspend fun sembrarSiVacio() {
        if (usuarios.contar() > 0) return
        val hoy = System.currentTimeMillis()
        val claveDemo = hash("1234")
        db.withTransaction {
            val eje = usuarios.insertar(Usuario(nombre = "Camila Rojas", correo = "ejecutivo@demo.cl", clave = claveDemo, perfil = Perfil.EJECUTIVO, zona = "Metropolitana"))
            val eje2 = usuarios.insertar(Usuario(nombre = "Diego Fuentes", correo = "ejecutivo2@demo.cl", clave = claveDemo, perfil = Perfil.EJECUTIVO, zona = "Valparaíso"))
            usuarios.insertar(Usuario(nombre = "Marcela Soto", correo = "supervisor@demo.cl", clave = claveDemo, perfil = Perfil.SUPERVISOR, zona = "Nacional"))

            val o1 = organizaciones.insertar(Organizacion(nombre = "Minera Andes Ficticia S.A.", tipo = "Empresa privada", rubro = "Minería", comuna = "Las Condes", region = "Metropolitana"))
            val o2 = organizaciones.insertar(Organizacion(nombre = "Municipalidad de Ejemplo", tipo = "Institución pública", rubro = "Gobierno local", comuna = "Peñalolén", region = "Metropolitana"))
            val o3 = organizaciones.insertar(Organizacion(nombre = "Retail Pacífico Demo Ltda.", tipo = "Empresa privada", rubro = "Retail", comuna = "Viña del Mar", region = "Valparaíso"))
            val o4 = organizaciones.insertar(Organizacion(nombre = "Instituto Técnico Prueba", tipo = "Institución educacional", rubro = "Educación", comuna = "Concepción", region = "Biobío"))

            organizaciones.insertarContacto(Contacto(organizacionId = o1, nombre = "Paula Herrera", cargo = "Jefa de RR.HH.", area = "Personas", correo = "paula.herrera@ficticia.example", telefono = "+56 9 0000 0001"))
            organizaciones.insertarContacto(Contacto(organizacionId = o1, nombre = "Tomás Vidal", cargo = "Analista de bienestar", area = "Personas", correo = "tomas.vidal@ficticia.example", telefono = "+56 9 0000 0002"))
            organizaciones.insertarContacto(Contacto(organizacionId = o2, nombre = "Ana Muñoz", cargo = "Encargada de eventos", area = "DIDECO", correo = "ana.munoz@muni.example", telefono = "+56 9 0000 0003"))
            organizaciones.insertarContacto(Contacto(organizacionId = o3, nombre = "Jorge Pizarro", cargo = "Gerente de tienda", area = "Operaciones", correo = "jorge.pizarro@retail.example", telefono = "+56 9 0000 0004"))
            organizaciones.insertarContacto(Contacto(organizacionId = o4, nombre = "Lorena Castro", cargo = "Coordinadora de vinculación", area = "Vinculación con el medio", correo = "lorena.castro@instituto.example", telefono = "+56 9 0000 0005"))

            fun op(nombre: String, tipo: TipoEvento, fuente: String, pri: Prioridad, pot: Potencial, estado: EstadoOportunidad,
                   diasEstado: Int, org: Long, resp: Long, enDias: Int, comuna: String, region: String, publico: Int, costo: Int) =
                OportunidadEntity(
                    nombre = nombre, tipo = tipo, descripcion = "Participación con stand informativo en $nombre",
                    fuente = fuente, fechaDeteccion = hoy - 20 * DIA_MS, prioridad = pri,
                    potencial = pot, estado = estado, observaciones = "Dato de prueba", fechaCambioEstado = hoy - diasEstado * DIA_MS, organizacionId = org,
                    responsableId = resp, fechaInicio = hoy + enDias * DIA_MS, fechaTermino = hoy + (enDias + 1) * DIA_MS,
                    horario = "09:00 - 18:00", comuna = comuna, region = region, publicoEsperado = publico,
                    condiciones = "Stand 3x3, requiere acreditación previa", costo = costo
                )

            val a = oportunidades.insertar(op("Feria de beneficios Minera Andes", TipoEvento.FERIA, "Referido", Prioridad.ALTA, Potencial.ALTO, EstadoOportunidad.PROPUESTA_ENVIADA, 10, o1, eje, 5, "Las Condes", "Metropolitana", 800, 350000))
            val b = oportunidades.insertar(op("Feria laboral comunal", TipoEvento.FERIA_LABORAL, "Redes sociales", Prioridad.MEDIA, Potencial.MEDIO, EstadoOportunidad.EVENTO_IDENTIFICADO, 3, o2, eje, 25, "Peñalolén", "Metropolitana", 1500, 0))
            val c = oportunidades.insertar(op("Activación en tienda Viña", TipoEvento.ACTIVACION, "Visita en terreno", Prioridad.ALTA, Potencial.ALTO, EstadoOportunidad.CONTACTO_REALIZADO, 4, o3, eje2, 10, "Viña del Mar", "Valparaíso", 400, 120000))
            val d = oportunidades.insertar(op("Seminario de empleabilidad", TipoEvento.SEMINARIO, "Sitio web", Prioridad.BAJA, Potencial.BAJO, EstadoOportunidad.PARTICIPACION_CONFIRMADA, 2, o4, eje2, 18, "Concepción", "Biobío", 250, 80000))
            val e = oportunidades.insertar(op("Charla de bienestar financiero", TipoEvento.CHARLA, "Llamada entrante", Prioridad.MEDIA, Potencial.MEDIO, EstadoOportunidad.EN_ESPERA_RESPUESTA, 9, o1, eje, 40, "Las Condes", "Metropolitana", 120, 0))
            oportunidades.insertar(op("Feria navideña municipal", TipoEvento.FERIA, "Prensa local", Prioridad.BAJA, Potencial.BAJO, EstadoOportunidad.DESCARTADA, 6, o2, eje, 60, "Peñalolén", "Metropolitana", 2000, 500000))

            seguimientos.insertar(Seguimiento(oportunidadId = a, tipoAccion = TipoAccion.REUNION, fechaHora = hoy - 12 * DIA_MS, resultado = "Reunión con RR.HH., piden propuesta formal", proximaAccion = "Enviar propuesta", fechaProximoSeguimiento = hoy - 10 * DIA_MS))
            seguimientos.insertar(Seguimiento(oportunidadId = a, tipoAccion = TipoAccion.CORREO, fechaHora = hoy - 10 * DIA_MS, resultado = "Propuesta enviada por correo", proximaAccion = "Llamar para confirmar recepción", fechaProximoSeguimiento = hoy - 3 * DIA_MS))
            seguimientos.insertar(Seguimiento(oportunidadId = b, tipoAccion = TipoAccion.CAMBIO_ESTADO, fechaHora = hoy - 3 * DIA_MS, resultado = "Oportunidad registrada (Evento identificado)"))
            seguimientos.insertar(Seguimiento(oportunidadId = c, tipoAccion = TipoAccion.LLAMADA, fechaHora = hoy - 4 * DIA_MS, resultado = "Gerente interesado, solicita detalles", proximaAccion = "Visitar tienda", fechaProximoSeguimiento = hoy + 2 * DIA_MS))
            seguimientos.insertar(Seguimiento(oportunidadId = d, tipoAccion = TipoAccion.CORREO, fechaHora = hoy - 2 * DIA_MS, resultado = "Confirman cupo y stand"))
            seguimientos.insertar(Seguimiento(oportunidadId = e, tipoAccion = TipoAccion.CORREO, fechaHora = hoy - 9 * DIA_MS, resultado = "Se envía temario de la charla", proximaAccion = "Esperar respuesta", fechaProximoSeguimiento = hoy + 1 * DIA_MS))
        }
    }

    suspend fun registrar(u: Usuario): Boolean {
        val correo = u.correo.trim().lowercase()
        if (usuarios.porCorreo(correo) != null) return false
        usuarios.insertar(u.copy(correo = correo, clave = hash(u.clave)))
        return true
    }
}