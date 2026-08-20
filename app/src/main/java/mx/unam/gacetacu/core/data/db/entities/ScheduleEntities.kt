package mx.unam.gacetacu.core.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facultyId: String,
    val facultyName: String,
    val name: String,          // p. ej. "Horario 2026-2"
    val createdAt: Long,
)

@Entity(
    tableName = "schedule_class",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = androidx.room.ForeignKey.CASCADE,
        )
    ]
)
data class ScheduleClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: Long,
    val subject: String,
    val day: Int,              // 1 = lunes ... 6 = sábado
    val startHour: Int,        // 0-23
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val room: String,
)
