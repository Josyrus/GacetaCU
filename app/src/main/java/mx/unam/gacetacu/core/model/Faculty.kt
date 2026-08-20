package mx.unam.gacetacu.core.model

/**
 * Catálogo de facultades/escuelas de CU.
 *
 * IMPORTANTE: las coordenadas son aproximadas (de memoria) y las URLs de gaceta
 * de cada facultad son un punto de partida: verifícalas y complétalas antes de
 * confiar en el scraper para producción. La única URL confirmada es la de
 * Gaceta UNAM (rectoría): https://www.gaceta.unam.mx/
 */
data class Faculty(
    val id: String,
    val displayName: String,
    val gacetaUrl: String?,       // null = todavía no verificada
    val lat: Double,
    val lng: Double,
)

object FacultyCatalog {

    val RECTORIA = Faculty(
        id = "rectoria",
        displayName = "Rectoría (Gaceta UNAM)",
        gacetaUrl = "https://www.gaceta.unam.mx/",
        lat = 19.3325,
        lng = -99.1843,
    )

    // Lista base. Agrega/ajusta según necesites; deja gacetaUrl = null si no la
    // has verificado y el repositorio simplemente la omitirá al hacer scraping.
    val ALL: List<Faculty> = listOf(
        RECTORIA,
        Faculty("ingenieria", "Facultad de Ingeniería", null, 19.3316, -99.1791),
        Faculty("ciencias", "Facultad de Ciencias", null, 19.3288, -99.1809),
        Faculty("medicina", "Facultad de Medicina", null, 19.3287, -99.1858),
        Faculty("derecho", "Facultad de Derecho", null, 19.3315, -99.1863),
        Faculty("economia", "Facultad de Economía", null, 19.3320, -99.1875),
        Faculty("filosofia", "Facultad de Filosofía y Letras", null, 19.3305, -99.1855),
        Faculty("cpys", "Facultad de Ciencias Políticas y Sociales", null, 19.3327, -99.1878),
        Faculty("psicologia", "Facultad de Psicología", null, 19.3273, -99.1826),
        Faculty("contaduria", "Facultad de Contaduría y Administración", null, 19.3339, -99.1852),
        Faculty("quimica", "Facultad de Química", null, 19.3305, -99.1789),
        Faculty("arquitectura", "Facultad de Arquitectura", null, 19.3283, -99.1866),
        Faculty("odontologia", "Facultad de Odontología", null, 19.3283, -99.1889),
        Faculty("veterinaria", "Facultad de Medicina Veterinaria y Zootecnia", null, 19.3220, -99.1830),
    )

    fun byId(id: String): Faculty? = ALL.find { it.id == id }
}
