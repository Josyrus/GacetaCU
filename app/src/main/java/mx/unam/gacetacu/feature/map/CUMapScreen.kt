package mx.unam.gacetacu.feature.map

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import mx.unam.gacetacu.R
import mx.unam.gacetacu.core.model.Faculty
import mx.unam.gacetacu.core.model.FacultyCatalog
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import java.nio.charset.Charset

// -----------------------------------------------------------------------
// Config del mapa de CU
// -----------------------------------------------------------------------

// URL de tu estilo personalizado (el JSON exportado de Maputnik, alojado en tu debianServer
// o en un raw de GitHub). Los tiles siguen viniendo de tiles.openfreemap.org; esto solo
// referencia el styling (colores, POIs ocultos, etc.)
private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"


private val CU_CENTER = LatLng(19.3300, -99.1840)

// Bounding box que delimita la exploración a Ciudad Universitaria.
// Ajusta estas esquinas al polígono real de CU (puedes sacarlas de un bbox en
// bboxfinder.com o del propio geojson del predio de CU en OSM).
private val CU_BOUNDS = LatLngBounds.from(
    /* north (lat max) */ 19.3420,
    /* east  (lng max) */ -99.1650,
    /* south (lat min) */ 19.3120,
    /* west  (lng min) */ -99.2010,
)

private const val CU_MIN_ZOOM = 14.5
private const val CU_MAX_ZOOM = 19.0

// Pitch (inclinación) usado para alternar 2D plano vs 3D con extrusión
private const val PITCH_FLAT = 0.0
private const val PITCH_3D = 55.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CUMapScreen() {
    val context = LocalContext.current
    var selectedFaculty by remember { mutableStateOf<Faculty?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var is3D by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var downloadDialogVisible by remember { mutableStateOf(false) }

    // MapLibre 11+ requiere inicialización síncrona antes de crear el MapView.
    // Usamos remember para asegurar que se llame antes de que AndroidView(factory=...) se ejecute.
    remember(context) {
        MapLibre.getInstance(context, null, WellKnownTileServer.MapLibre)
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.map_title)) },
            actions = {
                IconButton(onClick = {
                    is3D = !is3D
                    mapLibreMap?.let { map -> applyPitch(map, is3D) }
                }) {
                    Icon(Icons.Filled.Layers, contentDescription = stringResource(R.string.map_toggle_3d))
                }
                IconButton(onClick = { downloadDialogVisible = true }) {
                    Icon(Icons.Filled.Download, contentDescription = stringResource(R.string.map_download_offline))
                }
            },
        )

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

        MapLibreMapView(
            modifier = Modifier.fillMaxSize(),
            selectedFaculty = selectedFaculty,
            is3D = is3D,
            onMapReady = { map -> mapLibreMap = map },
        )
    }

    if (downloadDialogVisible) {
        OfflineDownloadDialog(
            onDismiss = { downloadDialogVisible = false },
            onConfirm = { regionName ->
                downloadCuRegionOffline(context, regionName)
                downloadDialogVisible = false
            },
        )
    }
}

// -----------------------------------------------------------------------
// AndroidView que envuelve el MapView clásico de MapLibre + ciclo de vida
// -----------------------------------------------------------------------

@Composable
private fun MapLibreMapView(
    modifier: Modifier = Modifier,
    selectedFaculty: Faculty?,
    is3D: Boolean,
    onMapReady: (MapLibreMap) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapViewState = remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                mapViewState.value = this
                onCreate(null)
                getMapAsync { map ->
                    map.setStyle(STYLE_URL) { style ->
                        configureCamera(map)
                        applyPitch(map, is3D)
                        addFacultyLayer(map, style, FacultyCatalog.ALL)
                        onMapReady(map)
                    }
                }
            }
        },
        update = { mapView ->
            mapView.getMapAsync { map ->
                val style = map.style ?: return@getMapAsync
                val faculties = selectedFaculty?.let { listOf(it) } ?: FacultyCatalog.ALL
                updateFacultyLayer(style, faculties)
                selectedFaculty?.let { faculty ->
                    map.easeCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(faculty.lat, faculty.lng), 18.0),
                    )
                }
            }
        },
    )

    // Reenvía el ciclo de vida de Compose al MapView (obligatorio: MapView no es
    // lifecycle-aware por sí solo, a diferencia de un composable nativo).
    DisposableEffect(lifecycleOwner) {
        val mapView = mapViewState.value
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView?.onStart()
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_STOP -> mapView?.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDestroy()
        }
    }
}

// -----------------------------------------------------------------------
// Cámara: centrar en CU y limitar la exploración a esa zona
// -----------------------------------------------------------------------

private fun configureCamera(map: MapLibreMap) {
    map.cameraPosition = CameraPosition.Builder()
        .target(CU_CENTER)
        .zoom(16.0)
        .build()

    // Esto es lo que realmente "encierra" al usuario dentro de CU: no puede
    // hacer pan fuera de este bounding box.
    map.setLatLngBoundsForCameraTarget(CU_BOUNDS)
    map.setMinZoomPreference(CU_MIN_ZOOM)
    map.setMaxZoomPreference(CU_MAX_ZOOM)
}

private fun applyPitch(map: MapLibreMap, is3D: Boolean) {
    val targetPitch = if (is3D) PITCH_3D else PITCH_FLAT
    val current = map.cameraPosition
    map.easeCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder(current)
                .tilt(targetPitch)
                .build(),
        ),
        400,
    )
}

// -----------------------------------------------------------------------
// Capa de facultades (equivalente a los Marker de osmdroid, vía GeoJSON)
// -----------------------------------------------------------------------

private fun addFacultyLayer(map: MapLibreMap, style: Style, faculties: List<Faculty>) {
    // Aquí añadirías tu GeoJsonSource + SymbolLayer para los pines de
    // facultades. Lo dejo señalado porque depende de cómo tengas modelado
    // Faculty (id, icono, etc.) — dime si quieres ese bloque completo también.
    updateFacultyLayer(style, faculties)
}

private fun updateFacultyLayer(style: Style, faculties: List<Faculty>) {
    // TODO: actualizar el GeoJsonSource con los features de `faculties`.
}

// -----------------------------------------------------------------------
// Descarga offline de la región de CU
// -----------------------------------------------------------------------

@Composable
private fun OfflineDownloadDialog(
    onDismiss: () -> Unit,
    onConfirm: (regionName: String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.map_download_offline)) },
        text = { Text(stringResource(R.string.map_download_offline_description)) },
        confirmButton = {
            TextButton(onClick = { onConfirm("ciudad_universitaria") }) {
                Text(stringResource(R.string.action_download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/**
 * Descarga los tiles + estilo de la región de CU para uso sin conexión.
 * El progreso se puede observar registrando un OfflineRegionObserver;
 * aquí se deja un callback mínimo con logging, ajusta a tu UI real
 * (ej. exponer el progreso vía StateFlow a la pantalla).
 */
fun downloadCuRegionOffline(context: Context, regionName: String) {
    val offlineManager = OfflineManager.getInstance(context)

    val definition = OfflineTilePyramidRegionDefinition(
        STYLE_URL,
        CU_BOUNDS,
        CU_MIN_ZOOM,
        CU_MAX_ZOOM,
        context.resources.displayMetrics.density,
        /* includeIdeographs = */ false,
    )

    val metadata = regionName.toByteArray(Charset.forName("UTF-8"))

    offlineManager.createOfflineRegion(
        definition,
        metadata,
        object : OfflineManager.CreateOfflineRegionCallback {
            override fun onCreate(offlineRegion: OfflineRegion) {
                offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                    override fun onStatusChanged(status: OfflineRegionStatus) {
                        val percentage = if (status.requiredResourceCount >= 0) {
                            (100.0 * status.completedResourceCount / status.requiredResourceCount)
                        } else {
                            0.0
                        }
                        // TODO: reportar `percentage` a tu UI (StateFlow/callback).
                        if (status.isComplete) {
                            // Descarga terminada; la región queda disponible sin red.
                        }
                    }

                    override fun onError(error: OfflineRegionError) {
                        // TODO: manejar error (sin espacio, sin red, etc.)
                    }

                    override fun mapboxTileCountLimitExceeded(limit: Long) {
                        // Límite de tiles alcanzado — reduce el bbox o el rango de zoom.
                    }
                })
                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
            }

            override fun onError(error: String) {
                // TODO: manejar error de creación de la región.
            }
        },
    )
}