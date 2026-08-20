package mx.unam.gacetacu.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.unam.gacetacu.core.data.db.entities.NewsEntity
import mx.unam.gacetacu.core.model.FacultyCatalog

data class NewsUiState(
    val items: List<NewsEntity> = emptyList(),
    val selectedFacultyId: String? = null, // null = todas
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(private val repository: NewsRepository) : ViewModel() {

    private val selectedFaculty = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val faculties = FacultyCatalog.ALL

    val uiState: StateFlow<NewsUiState> = combine(
        selectedFaculty.flatMapLatest { repository.observeNews(it) },
        selectedFaculty,
        query,
        loading,
        error,
    ) { items, faculty, q, isLoading, err ->
        val filtered = if (q.isBlank()) items else items.filter {
            it.title.contains(q, ignoreCase = true)
        }
        NewsUiState(filtered, faculty, q, isLoading, err)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NewsUiState())

    init {
        refresh()
    }

    fun selectFaculty(id: String?) {
        selectedFaculty.value = id
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            val result = repository.refresh(selectedFaculty.value)
            error.value = result.exceptionOrNull()?.message
            loading.value = false
        }
    }
}
