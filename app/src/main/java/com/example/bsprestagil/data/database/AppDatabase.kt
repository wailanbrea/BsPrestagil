package com.example.bsprestagil.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bsprestagil.data.database.converters.Converters
import com.example.bsprestagil.data.database.dao.*
import com.example.bsprestagil.data.database.entities.*

@Database(
    entities = [
        ClienteEntity::class,
        PrestamoEntity::class,
        PagoEntity::class,
        CuotaEntity::class,
        GarantiaEntity::class,
        UsuarioEntity::class,
        ConfiguracionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun clienteDao(): ClienteDao
    abstract fun prestamoDao(): PrestamoDao
    abstract fun pagoDao(): PagoDao
    abstract fun cuotaDao(): CuotaDao
    abstract fun garantiaDao(): GarantiaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun configuracionDao(): ConfiguracionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bsprestagil_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

