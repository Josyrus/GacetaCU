package mx.unam.gacetacu
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.unam.gacetacu.core.theme.GacetaCUTheme
import mx.unam.gacetacu.navigation.AppNavHost
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mx.unam.gacetacu.feature.telegram.TelegramRepository
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val repository = TelegramRepository()

                val posts = repository.scrapeChannel("FIUNAM_MX")

                Log.d("TELEGRAM_TEST", "Publicaciones encontradas: ${posts.size}")

                posts.forEach { post ->
                    Log.d("TELEGRAM_TEST", "--------------------")
                    Log.d("TELEGRAM_TEST", "ID: ${post.id}")
                    Log.d("TELEGRAM_TEST", "Fecha: ${post.date}")
                    Log.d("TELEGRAM_TEST", "Texto: ${post.text}")
                    Log.d("TELEGRAM_TEST", "URL: ${post.url}")
                    Log.d("TELEGRAM_TEST", "Imagen: ${post.imageUrl}")
                }

            } catch (e: Exception) {
                Log.e("TELEGRAM_TEST", "Error: ${e.message}")
                Log.e("TELEGRAM_TEST", Log.getStackTraceString(e))
            }
        }

        enableEdgeToEdge()
        setContent {
            GacetaCUTheme {
                AppNavHost()
            }
        }
    }
}
