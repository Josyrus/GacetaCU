package mx.unam.gacetacu.feature.telegram

data class TelegramPost(
    val id: String,
    val text: String,
    val date: String?,
    val url: String,
    val imageUrl: String?
)
