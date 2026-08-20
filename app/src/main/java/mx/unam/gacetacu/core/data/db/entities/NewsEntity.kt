package mx.unam.gacetacu.core.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val url: String,          // la URL de la nota funciona como id natural
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val facultyId: String,                // "rectoria" o el id de la facultad
    val facultyName: String,
    val fetchedAt: Long,                  // timestamp de cuándo se guardó localmente
)
