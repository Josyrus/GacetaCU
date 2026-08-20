package mx.unam.gacetacu.feature.transport

import mx.unam.gacetacu.R

/**
 * Rutas de PumaBus representadas como vector drawables (equivalentes a SVG en Android).
 * Los trazos de [drawableRes] son PLACEHOLDERS — reemplázalos importando el SVG real
 * de cada ruta (Android Studio: clic derecho en res/drawable > New > Vector Asset >
 * Local file (SVG)) y actualiza esta lista con los nuevos recursos.
 */
data class BusRoute(
    val id: String,
    val name: String,
    val drawableRes: Int?,
)

object PumaBusCatalog {
    val ROUTES = listOf(
        BusRoute("r1", "Ruta 1 — Metro CU ↔ Zona Cultural", R.drawable.route_1),
        BusRoute("r2", "Ruta 2 — Metro CU ↔ Facultad de Ingeniería", R.drawable.route_2),
        BusRoute("r3", "Ruta 3 — Circuito interior", R.drawable.route_3),
    )
}

/**
 * Módulos PumaAgua. Coordenadas aproximadas — verifica ubicaciones reales antes de
 * usarlas para navegación precisa.
 */
data class WaterStation(
    val name: String,
    val lat: Double,
    val lng: Double,
)

object PumaAguaCatalog {
    val STATIONS = listOf(
        WaterStation("Módulo Facultad de Ingeniería", 19.3316, -99.1791),
        WaterStation("Módulo Facultad de Ciencias", 19.3288, -99.1809),
        WaterStation("Módulo Central (Islas)", 19.3300, -99.1840),
        WaterStation("Módulo CCH / Explanada Rectoría", 19.3325, -99.1843),
    )
}
