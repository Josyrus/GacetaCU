package mx.unam.gacetacu.feature.schedule

import mx.unam.gacetacu.core.data.db.dao.ScheduleDao
import mx.unam.gacetacu.core.data.db.entities.ScheduleClassEntity
import mx.unam.gacetacu.core.data.db.entities.ScheduleEntity

class ScheduleRepository(private val dao: ScheduleDao) {

    fun observeSchedules() = dao.observeSchedules()
    fun observeClasses(scheduleId: Long) = dao.observeClasses(scheduleId)

    suspend fun createSchedule(facultyId: String, facultyName: String, name: String): Long =
        dao.insertSchedule(
            ScheduleEntity(
                facultyId = facultyId,
                facultyName = facultyName,
                name = name,
                createdAt = System.currentTimeMillis(),
            )
        )

    suspend fun deleteSchedule(schedule: ScheduleEntity) = dao.deleteSchedule(schedule)

    suspend fun addClass(
        scheduleId: Long,
        subject: String,
        day: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        room: String,
    ) = dao.insertClass(
        ScheduleClassEntity(
            scheduleId = scheduleId,
            subject = subject,
            day = day,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            room = room,
        )
    )

    suspend fun deleteClass(classEntity: ScheduleClassEntity) = dao.deleteClass(classEntity)
}
