package mx.unam.gacetacu.feature.telegram

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class TelegramRepository {

    suspend fun scrapeChannel(channel: String): List<TelegramPost> =
        withContext(Dispatchers.IO) {

            val url = "https://t.me/s/$channel"

            val document = Jsoup.connect(url)
                .userAgent(
                    "Mozilla/5.0 (X11; Linux x86_64) " +
                            "AppleWebKit/537.36 Chrome/120.0 Safari/537.36"
                )
                .timeout(15000)
                .get()

            document
                .select(".tgme_widget_message")
                .mapNotNull { message ->

                    val messageUrl = message
                        .selectFirst(".tgme_widget_message_date")
                        ?.attr("href")
                        ?: return@mapNotNull null

                    val id = messageUrl.substringAfterLast("/")

                    val text = message
                        .selectFirst(".tgme_widget_message_text")
                        ?.text()
                        ?: ""

                    val date = message
                        .selectFirst("time")
                        ?.attr("datetime")

                    val imageUrl = message
                        .selectFirst(".tgme_widget_message_photo_wrap")
                        ?.attr("style")
                        ?.substringAfter("background-image:url('")
                        ?.substringBefore("')")

                    TelegramPost(
                        id = id,
                        text = text,
                        date = date,
                        url = messageUrl,
                        imageUrl = imageUrl
                    )
                }
        }
}