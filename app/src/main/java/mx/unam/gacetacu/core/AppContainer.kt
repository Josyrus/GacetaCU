package mx.unam.gacetacu.core

import android.content.Context
import mx.unam.gacetacu.core.data.datastore.UserPreferences
import mx.unam.gacetacu.core.data.db.AppDatabase
import mx.unam.gacetacu.feature.news.NewsRepository
import mx.unam.gacetacu.feature.schedule.ScheduleRepository

/**
 * Contenedor manual de dependencias. Se mantiene simple a propósito (sin Hilt/Koin)
 * para que el proyecto compile sin configuración extra de DI. Si el proyecto crece,
 * migrar a Hilt es directo desde aquí.
 */
class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)

    val userPreferences = UserPreferences(context)
    val newsRepository = NewsRepository(db.newsDao())
    val scheduleRepository = ScheduleRepository(db.scheduleDao())
}
