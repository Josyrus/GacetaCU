package mx.unam.gacetacu.feature.map

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import mx.unam.gacetacu.R
import mx.unam.gacetacu.core.model.Faculty
import mx.unam.gacetacu.core.model.FacultyCatalog
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val CU_CENTER = GeoPoint(19.3300, -99.1840)

@Composable
fun CUMapScreen() {
    val context = LocalContext.current
    var selectedFaculty by remember { mutableStateOf<Faculty?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        // Usa el caché interno de la app para evitar pedir permisos de almacenamiento externo.
        Configuration.getInstance().osmdroidTileCache = context.cacheDir.resolve("osmdroid/tiles")
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.map_title)) })

        Box(Modifier.padding(12.dp)) {
            OutlinedButton(onClick = { menuExpanded = true }) {
                Text(selectedFaculty?.displayName ?: stringResource(R.string.map_show_all))
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.map_show_all)) },
                    onClick = { selectedFaculty = null; menuExpanded = false },
                )
                FacultyCatalog.ALL.forEach { faculty ->
                    DropdownMenuItem(
                        text = { Text(faculty.displayName) },
                        onClick = { selectedFaculty = faculty; menuExpanded = false },
                    )
                }
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(CU_CENTER)
                    addFacultyMarkers(this, FacultyCatalog.ALL)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                val faculties = selectedFaculty?.let { listOf(it) } ?: FacultyCatalog.ALL
                addFacultyMarkers(mapView, faculties)
                selectedFaculty?.let { faculty ->
                    mapView.controller.animateTo(GeoPoint(faculty.lat, faculty.lng))
                    mapView.controller.setZoom(18.0)
                }
                mapView.invalidate()
            },
        )
    }
}

private fun addFacultyMarkers(mapView: MapView, faculties: List<Faculty>) {
    faculties.forEach { faculty ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(faculty.lat, faculty.lng)
            title = faculty.displayName
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
    }
}
