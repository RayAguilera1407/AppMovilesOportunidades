package cl.duoc.activaciones.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.activaciones.model.Contacto
import cl.duoc.activaciones.model.Organizacion
import cl.duoc.activaciones.model.ResultadoActivacion
import cl.duoc.activaciones.model.Seguimiento
import cl.duoc.activaciones.model.Usuario

/** v3 -> v4: se agrega la dirección del usuario sin borrar ningún dato. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE usuario ADD COLUMN direccion TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [Usuario::class, Organizacion::class, Contacto::class, OportunidadEntity::class,
        Seguimiento::class, ResultadoActivacion::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun resultadoDao(): ResultadoDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "activaciones.db")
                .addMigrations(MIGRATION_3_4)
                .build()
    }
}