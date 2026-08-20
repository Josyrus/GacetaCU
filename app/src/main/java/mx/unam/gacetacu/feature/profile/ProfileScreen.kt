package mx.unam.gacetacu.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.unam.gacetacu.GacetaCUApp
import mx.unam.gacetacu.R
import mx.unam.gacetacu.core.ViewModelFactory
import mx.unam.gacetacu.core.model.FacultyCatalog

@Composable
fun ProfileScreen() {
    val app = LocalContext.current.applicationContext as GacetaCUApp
    val viewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory { ProfileViewModel(app.container.userPreferences) }
    )
    val user by viewModel.user.collectAsState()

    var username by remember(user) { mutableStateOf(user?.username.orEmpty()) }
    var facultyId by remember(user) { mutableStateOf(user?.facultyId ?: FacultyCatalog.ALL.first { it.id != "rectoria" }.id) }
    var facultyMenuExpanded by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.profile_no_login), style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; saved = false },
            label = { Text(stringResource(R.string.profile_username)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Box {
            OutlinedButton(onClick = { facultyMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(FacultyCatalog.byId(facultyId)?.displayName ?: stringResource(R.string.profile_faculty))
            }
            DropdownMenu(expanded = facultyMenuExpanded, onDismissRequest = { facultyMenuExpanded = false }) {
                FacultyCatalog.ALL.filter { it.id != "rectoria" }.forEach { faculty ->
                    DropdownMenuItem(
                        text = { Text(faculty.displayName) },
                        onClick = { facultyId = faculty.id; facultyMenuExpanded = false; saved = false },
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.save(username, facultyId); saved = true },
            enabled = username.isNotBlank(),
        ) {
            Text(stringResource(if (user == null) R.string.profile_create else R.string.profile_edit))
        }

        if (saved) Text(stringResource(R.string.profile_saved), style = MaterialTheme.typography.labelMedium)
    }
}
