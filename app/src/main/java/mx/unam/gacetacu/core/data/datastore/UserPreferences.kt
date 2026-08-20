package mx.unam.gacetacu.core.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gacetacu_prefs")

data class LocalUser(
    val username: String,
    val facultyId: String,
)

/**
 * Todo el "usuario" de la app vive solo en este dispositivo (DataStore),
 * nunca se envía a un servidor: no hay inicio de sesión ni backend propio.
 */
class UserPreferences(private val context: Context) {

    private object Keys {
        val USERNAME = stringPreferencesKey("username")
        val FACULTY_ID = stringPreferencesKey("faculty_id")
        val LANGUAGE = stringPreferencesKey("language") // "es" | "en" | "zh"
    }

    val user: Flow<LocalUser?> = context.dataStore.data.map { prefs ->
        val username = prefs[Keys.USERNAME]
        val facultyId = prefs[Keys.FACULTY_ID]
        if (username.isNullOrBlank() || facultyId.isNullOrBlank()) null
        else LocalUser(username, facultyId)
    }

    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "es" }

    suspend fun saveUser(username: String, facultyId: String) {
        context.dataStore.edit {
            it[Keys.USERNAME] = username
            it[Keys.FACULTY_ID] = facultyId
        }
    }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = code }
    }
}
