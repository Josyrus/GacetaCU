package mx.unam.gacetacu.feature.news

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.unam.gacetacu.core.data.db.dao.NewsDao
import mx.unam.gacetacu.core.data.db.entities.NewsEntity
import mx.unam.gacetacu.core.model.Faculty
import mx.unam.gacetacu.core.model.FacultyCatalog
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

private const val MAX_LOCAL_ITEMS = 15
private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14) GacetaCU/0.1 (+https://github.com/josyrus)"

class NewsRepository(private val newsDao: NewsDao) {

    fun observeNews(facultyId: String?) =
        if (facultyId == null) newsDao.observeAll() else newsDao.observeByFaculty(facultyId)

    /**
     * Descarga y guarda noticias de una facultad (o de todas si [facultyId] es null).
     * Solo se conservan las [MAX_LOCAL_ITEMS] más recientes en total, como pidió el usuario.
     *
     * NOTA: los selectores CSS de [scrapeFaculty] son un punto de partida basado en la
     * estructura típica de un sitio de noticias en WordPress/Drupal. La estructura real de
     * gaceta.unam.mx y de cada facultad puede diferir — ajusta los selectores con las
     * herramientas de desarrollador del navegador si el scraping no trae resultados.
     */
    suspend fun refresh(facultyId: String? = null): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val targets: List<Faculty> = when {
                facultyId == null -> FacultyCatalog.ALL
                else -> listOfNotNull(FacultyCatalog.byId(facultyId))
            }

            val fetched = mutableListOf<NewsEntity>()
            for (faculty in targets) {
                val url = faculty.gacetaUrl ?: continue
                fetched += scrapeFaculty(faculty, url)
            }

            if (fetched.isNotEmpty()) {
                newsDao.insertAll(fetched)
                newsDao.trimTo(MAX_LOCAL_ITEMS)
            }
            Result.success(fetched.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scrapeFaculty(faculty: Faculty, url: String): List<NewsEntity> {
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(15_000)
            .get()

        // Selectores genéricos: intenta <article>, si no hay, cae a enlaces con h2/h3.
        val nodes: List<Element> = doc.select("article").ifEmpty {
            doc.select("a:has(h2), a:has(h3)")
        }

        val now = System.currentTimeMillis()
        return nodes.take(15).mapNotNull { el ->
            val link = if (el.tagName() == "a") el else el.selectFirst("a[href]")
            val href = link?.absUrl("href")?.ifBlank { null } ?: return@mapNotNull null
            val title = el.selectFirst("h2, h3")?.text()?.ifBlank { null }
                ?: link.text().ifBlank { null }
                ?: return@mapNotNull null
            val summary = el.selectFirst("p")?.text().orEmpty()
            val img = el.selectFirst("img[src]")?.absUrl("src")?.ifBlank { null }

            NewsEntity(
                url = href,
                title = title.trim(),
                summary = summary.trim(),
                imageUrl = img,
                facultyId = faculty.id,
                facultyName = faculty.displayName,
                fetchedAt = now,
            )
        }
    }
}
