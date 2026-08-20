package mx.unam.gacetacu.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.unam.gacetacu.core.data.db.dao.NewsDao
import mx.unam.gacetacu.core.data.db.dao.ScheduleDao
import mx.unam.gacetacu.core.data.db.entities.NewsEntity
import mx.unam.gacetacu.core.data.db.entities.ScheduleClassEntity
import mx.unam.gacetacu.core.data.db.entities.ScheduleEntity

@Database(
    entities = [NewsEntity::class, ScheduleEntity::class, ScheduleClassEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gacetacu.db",
                ).build().also { INSTANCE = it }
            }
    }
}
