package mx.unam.gacetacu.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.unam.gacetacu.core.data.datastore.LocalUser
import mx.unam.gacetacu.core.data.datastore.UserPreferences

class ProfileViewModel(private val prefs: UserPreferences) : ViewModel() {

    val user: StateFlow<LocalUser?> =
        prefs.user.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(username: String, facultyId: String) {
        viewModelScope.launch { prefs.saveUser(username, facultyId) }
    }
}
