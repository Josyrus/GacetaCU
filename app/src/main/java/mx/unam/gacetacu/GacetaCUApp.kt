package mx.unam.gacetacu

import android.app.Application
import mx.unam.gacetacu.core.AppContainer

class GacetaCUApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
