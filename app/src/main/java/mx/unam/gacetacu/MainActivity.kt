package mx.unam.gacetacu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.unam.gacetacu.core.theme.GacetaCUTheme
import mx.unam.gacetacu.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GacetaCUTheme {
                AppNavHost()
            }
        }
    }
}
