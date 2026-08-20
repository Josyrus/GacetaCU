package mx.unam.gacetacu.feature.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mx.unam.gacetacu.GacetaCUApp
import mx.unam.gacetacu.R
import mx.unam.gacetacu.core.ViewModelFactory
import mx.unam.gacetacu.core.data.db.entities.NewsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(onOpenSettings: () -> Unit = {}) {
    val app = LocalContext.current.applicationContext as GacetaCUApp
    val viewModel: NewsViewModel = viewModel(
        factory = ViewModelFactory { NewsViewModel(app.container.newsRepository) }
    )
    val state by viewModel.uiState.collectAsState()
    var facultyMenuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.news_title)) },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.news_refresh))
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings))
                }
            }
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                label = { Text(stringResource(R.string.news_filter_name)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )

            Box {
                OutlinedButton(onClick = { facultyMenuExpanded = true }) {
                    Text(
                        viewModel.faculties.find { it.id == state.selectedFacultyId }?.displayName
                            ?: stringResource(R.string.news_all_faculties)
                    )
                }
                DropdownMenu(expanded = facultyMenuExpanded, onDismissRequest = { facultyMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.news_all_faculties)) },
                        onClick = { viewModel.selectFaculty(null); facultyMenuExpanded = false },
                    )
                    viewModel.faculties.forEach { faculty ->
                        DropdownMenuItem(
                            text = { Text(faculty.displayName) },
                            onClick = { viewModel.selectFaculty(faculty.id); facultyMenuExpanded = false },
                        )
                    }
                }
            }
        }

        Text(
            stringResource(R.string.news_saved_locally),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

        if (state.items.isEmpty() && !state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.news_empty))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.items, key = { it.url }) { news -> NewsCard(news) }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewsEntity) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            news.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(news.facultyName, style = MaterialTheme.typography.labelMedium)
            Text(news.title, style = MaterialTheme.typography.titleMedium)
            if (news.summary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(news.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { uriHandler.openUri(news.url) }) {
                Text(stringResource(R.string.news_read_more))
            }
        }
    }
}
