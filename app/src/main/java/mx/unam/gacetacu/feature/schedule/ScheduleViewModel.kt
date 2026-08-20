package mx.unam.gacetacu.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.unam.gacetacu.core.data.db.entities.ScheduleClassEntity
import mx.unam.gacetacu.core.data.db.entities.ScheduleEntity

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {

    val schedules: StateFlow<List<ScheduleEntity>> =
        repository.observeSchedules().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedScheduleId = MutableStateFlow<Long?>(null)
    val selectedId: StateFlow<Long?> = selectedScheduleId

    val classes: StateFlow<List<ScheduleClassEntity>> = selectedScheduleId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.observeClasses(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun select(scheduleId: Long) {
        selectedScheduleId.value = scheduleId
    }

    fun createSchedule(facultyId: String, facultyName: String, name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createSchedule(facultyId, facultyName, name)
            selectedScheduleId.value = id
            onCreated(id)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            if (selectedScheduleId.value == schedule.id) selectedScheduleId.value = null
        }
    }

    fun addClass(
        subject: String,
        day: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        room: String,
    ) {
        val scheduleId = selectedScheduleId.value ?: return
        viewModelScope.launch {
            repository.addClass(scheduleId, subject, day, startHour, startMinute, endHour, endMinute, room)
        }
    }

    fun deleteClass(classEntity: ScheduleClassEntity) {
        viewModelScope.launch { repository.deleteClass(classEntity) }
    }
}
