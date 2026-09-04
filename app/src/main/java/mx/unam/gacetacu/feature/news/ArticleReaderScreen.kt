package mx.unam.gacetacu.feature.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mx.unam.gacetacu.core.data.db.entities.NewsEntity
import mx.unam.gacetacu.feature.news.ArticleReaderScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    news: NewsEntity,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        TopAppBar(
            title = {
                Text("Noticia")
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            news.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            Text(
                text = news.facultyName,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = news.title,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = news.summary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}