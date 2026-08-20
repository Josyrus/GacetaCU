package mx.unam.gacetacu.core.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.unam.gacetacu.core.data.db.entities.ScheduleClassEntity
import mx.unam.gacetacu.core.data.db.entities.ScheduleEntity

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule ORDER BY createdAt DESC")
    fun observeSchedules(): Flow<List<ScheduleEntity>>

    @Insert
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedule_class WHERE scheduleId = :scheduleId ORDER BY day, startHour, startMinute")
    fun observeClasses(scheduleId: Long): Flow<List<ScheduleClassEntity>>

    @Insert
    suspend fun insertClass(classEntity: ScheduleClassEntity): Long

    @Delete
    suspend fun deleteClass(classEntity: ScheduleClassEntity)
}
