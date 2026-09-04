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

        // Si la URL pertenece a Telegram usamos sus selectores específicos.
        if (url.contains("t.me/")) {

            val posts = doc.select(".tgme_widget_message")

            val now = System.currentTimeMillis()

            return posts.takeLast(15).mapNotNull { post ->

                // Enlace directo de la publicación
                val link = post.selectFirst(".tgme_widget_message_date")
                val href = link?.absUrl("href")
                    ?.ifBlank { null }
                    ?: return@mapNotNull null

                // Texto de la publicación
                val text = post.selectFirst(".tgme_widget_message_text")
                    ?.text()
                    ?.trim()
                    ?.ifBlank { null }
                    ?: return@mapNotNull null

                // Telegram no tiene un "título" como una noticia normal.
                // Usamos una parte del texto como título.
                val title = if (text.length > 80) {
                    text.take(80) + "..."
                } else {
                    text
                }

                // Imagen de la publicación, si existe
                val image = post.selectFirst(".tgme_widget_message_photo_wrap")
                    ?.attr("style")
                    ?.let { style ->
                        Regex("""background-image:url\(['"]?(.*?)['"]?\)""")
                            .find(style)
                            ?.groupValues
                            ?.getOrNull(1)
                    }

                NewsEntity(
                    url = href,
                    title = title,
                    summary = text,
                    imageUrl = image,
                    facultyId = faculty.id,
                    facultyName = faculty.displayName,
                    fetchedAt = now,
                )
            }
        }

        // Para páginas normales mantenemos el scraper que ya tenía el proyecto.
        val nodes: List<Element> = doc.select("article").ifEmpty {
            doc.select("a:has(h2), a:has(h3)")
        }

        val now = System.currentTimeMillis()

        return nodes.take(15).mapNotNull { el ->

            val link = if (el.tagName() == "a") {
                el
            } else {
                el.selectFirst("a[href]")
            }

            val href = link?.absUrl("href")
                ?.ifBlank { null }
                ?: return@mapNotNull null

            val title = el.selectFirst("h2, h3")
                ?.text()
                ?.ifBlank { null }
                ?: link.text().ifBlank { null }
                ?: return@mapNotNull null

            val summary = el.selectFirst("p")
                ?.text()
                .orEmpty()

            val img = el.selectFirst("img[src]")
                ?.absUrl("src")
                ?.ifBlank { null }

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
