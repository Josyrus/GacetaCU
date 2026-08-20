package mx.unam.gacetacu.core.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.unam.gacetacu.core.data.db.entities.NewsEntity

@Dao
interface NewsDao {

    @Query("SELECT * FROM news ORDER BY fetchedAt DESC")
    fun observeAll(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE facultyId = :facultyId ORDER BY fetchedAt DESC")
    fun observeByFaculty(facultyId: String): Flow<List<NewsEntity>>

    @Query("SELECT COUNT(*) FROM news")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NewsEntity>)

    @Query("DELETE FROM news WHERE facultyId = :facultyId")
    suspend fun deleteByFaculty(facultyId: String)

    // Mantiene solo las N más recientes en toda la tabla (histórico de 15 pedido por el usuario).
    @Query(
        """
        DELETE FROM news WHERE url NOT IN (
            SELECT url FROM news ORDER BY fetchedAt DESC LIMIT :limit
        )
        """
    )
    suspend fun trimTo(limit: Int)
}
