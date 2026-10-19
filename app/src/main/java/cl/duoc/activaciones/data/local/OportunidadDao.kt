package cl.duoc.activaciones.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    @Insert suspend fun insertar(o: OportunidadEntity): Long
    @Update suspend fun actualizar(o: OportunidadEntity)
    @Query("DELETE FROM oportunidad WHERE id = :id") suspend fun eliminar(id: Long)

    /** Al eliminar una organización, sus oportunidades quedan sin organización asignada. */
    @Query("UPDATE oportunidad SET organizacionId = NULL WHERE organizacionId = :orgId")
    suspend fun desvincularOrganizacion(orgId: Long)

    /** Flow: Room emite de nuevo cada vez que la tabla cambia, así la lista se actualiza sola. */
    @Query("SELECT * FROM oportunidad ORDER BY fechaDeteccion DESC, id DESC")
    fun todas(): Flow<List<OportunidadEntity>>

    @Query("SELECT * FROM oportunidad") suspend fun todasUnaVez(): List<OportunidadEntity>
    @Query("SELECT * FROM oportunidad WHERE id = :id") fun porId(id: Long): Flow<OportunidadEntity?>
    @Query("SELECT * FROM oportunidad WHERE id = :id") suspend fun porIdUnaVez(id: Long): OportunidadEntity?
    @Query("SELECT * FROM oportunidad WHERE organizacionId = :orgId ORDER BY fechaInicio")
    fun porOrganizacion(orgId: Long): Flow<List<OportunidadEntity>>
}
