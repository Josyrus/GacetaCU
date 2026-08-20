package mx.unam.gacetacu.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.launch
import mx.unam.gacetacu.GacetaCUApp
import mx.unam.gacetacu.R

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as GacetaCUApp
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleLarge)

        listOf(
            "es" to stringResource(R.string.settings_language_es),
            "en" to stringResource(R.string.settings_language_en),
            "zh" to stringResource(R.string.settings_language_zh),
        ).forEach { (code, label) ->
            OutlinedButton(
                onClick = {
                    scope.launch {
                        app.container.userPreferences.setLanguage(code)
                        applyLocale(code)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(label) }
        }
    }
}

/**
 * Aplica el idioma vía AppCompatDelegate (funciona en API 26+ sin reiniciar la Activity
 * manualmente en la mayoría de los casos; en algunos dispositivos puede requerir recrear
 * la Activity — ver MainActivity).
 */
fun applyLocale(code: String) {
    val locales = LocaleListCompat.forLanguageTags(code)
    AppCompatDelegate.setApplicationLocales(locales)
}
