package cl.duoc.activaciones.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.activaciones.model.Contacto
import cl.duoc.activaciones.model.Organizacion
import cl.duoc.activaciones.model.ResultadoActivacion
import cl.duoc.activaciones.model.Seguimiento
import cl.duoc.activaciones.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT COUNT(*) FROM usuario") suspend fun contar(): Int
    @Insert suspend fun insertar(u: Usuario): Long

    @Query("SELECT * FROM usuario WHERE correo = :correo AND clave = :clave AND estadoCuenta = 'ACTIVA' LIMIT 1")
    suspend fun login(correo: String, clave: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :id") fun porId(id: Long): Flow<Usuario?>
    @Query("SELECT * FROM usuario WHERE id = :id") suspend fun porIdUnaVez(id: Long): Usuario?
    @Query("SELECT * FROM usuario ORDER BY nombre") fun todos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun porCorreo(correo: String): Usuario?
}

@Dao
interface OrganizacionDao {
    @Insert suspend fun insertar(o: Organizacion): Long
    @Insert suspend fun insertarContacto(c: Contacto): Long
    @Query("DELETE FROM organizacion WHERE id = :id") suspend fun eliminar(id: Long)
    @Query("DELETE FROM contacto WHERE id = :id") suspend fun eliminarContacto(id: Long)
    @Query("DELETE FROM contacto WHERE organizacionId = :orgId") suspend fun eliminarContactosDe(orgId: Long)
    @Query("SELECT * FROM organizacion ORDER BY nombre") fun todas(): Flow<List<Organizacion>>
    @Query("SELECT * FROM organizacion WHERE id = :id") fun porId(id: Long): Flow<Organizacion?>
    @Query("SELECT * FROM contacto WHERE organizacionId = :orgId ORDER BY nombre")
    fun contactos(orgId: Long): Flow<List<Contacto>>
}

@Dao
interface SeguimientoDao {
    @Insert suspend fun insertar(s: Seguimiento): Long
    @Query("DELETE FROM seguimiento WHERE id = :id") suspend fun eliminar(id: Long)
    @Query("DELETE FROM seguimiento WHERE oportunidadId = :opId") suspend fun eliminarDeOportunidad(opId: Long)
    @Query("SELECT * FROM seguimiento") fun todos(): Flow<List<Seguimiento>>
    @Query("SELECT * FROM seguimiento") suspend fun todosUnaVez(): List<Seguimiento>
    @Query("SELECT * FROM seguimiento WHERE oportunidadId = :opId ORDER BY fechaHora DESC, id DESC")
    fun historial(opId: Long): Flow<List<Seguimiento>>
}

@Dao
interface ResultadoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardar(r: ResultadoActivacion)
    @Query("DELETE FROM resultado_activacion WHERE oportunidadId = :opId") suspend fun eliminarDeOportunidad(opId: Long)
    @Query("SELECT * FROM resultado_activacion WHERE oportunidadId = :opId")
    fun porOportunidad(opId: Long): Flow<ResultadoActivacion?>
}