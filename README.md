# GacetaCU

App social/informativa para la comunidad de Ciudad Universitaria (UNAM), **sin inicio de sesión**
(el "usuario" es 100% local). Construida en Kotlin + Jetpack Compose.

## Cómo abrirlo
1. Abre Android Studio (Koala o más reciente) → **Open** → selecciona la carpeta `GacetaCU/`.
2. Deja que Gradle sincronice (usa AGP 8.5.2 / Kotlin 1.9.24 / compileSdk 34).
3. Ejecuta en un emulador o dispositivo con **Android 8.0 (API 26) o superior**.
## Qué incluye

| Feature | Dónde | Notas |
|---|---|---|
| Noticias (Gaceta UNAM + facultades) | `feature/news` | Scraping con Jsoup, caché local en Room (máx. 15 registros), filtro por nombre y por facultad. **Los selectores CSS son un punto de partida** — verifica la estructura real de cada sitio con las devtools del navegador y ajusta `NewsRepository.scrapeFaculty()`. |
| Horario | `feature/schedule` | Menú desplegable para elegir facultad → crea un horario → agrega clases (día, hora, salón) → todo en Room. |
| Perfil local | `feature/profile` | Nombre de usuario + facultad, guardado en DataStore. Nunca sale del dispositivo. |
| PumaBus (rutas en SVG) | `feature/transport` | Las rutas se representan como **Vector Drawables** (el equivalente nativo a SVG en Android). Vienen 3 rutas de ejemplo con trazos placeholder — impórtalas reales desde Android Studio: clic derecho en `res/drawable` → *New* → *Vector Asset* → *Local file (SVG)*. |
| PumaAgua | `feature/transport` | Lista de módulos con coordenadas aproximadas — verifícalas. |
| Mapa de CU | `feature/map` | OpenStreetMap vía `osmdroid`, con marcadores de facultades y filtro por facultad. |
| Idiomas (ES/EN/ZH) | `feature/settings`, `res/values*` | `strings.xml` (es, por defecto), `values-en`, `values-zh`. Cambio de idioma con `AppCompatDelegate.setApplicationLocales`. |

## Cosas a verificar/completar antes de producción
- **URLs de gaceta por facultad**: `core/model/Faculty.kt` (`FacultyCatalog.ALL`) solo trae confirmada
  la de rectoría (`https://www.gaceta.unam.mx/`). El resto están con `gacetaUrl = null` —
  complétalas y el scraper las recogerá automáticamente.
- **Coordenadas de facultades y módulos PumaAgua**: son aproximadas, de memoria — confírmalas en el mapa.
- **Rutas PumaBus reales**: reemplaza los vector drawables placeholder (`route_1.xml`, `route_2.xml`, `route_3.xml`).
- El ícono de la app (`ic_launcher_foreground.xml`) es un placeholder — cámbialo por tu logo.

## Arquitectura
No usa Hilt/Koin a propósito (menos fricción de build): hay un contenedor manual de
dependencias en `core/AppContainer.kt`, expuesto vía `GacetaCUApp.container`, y un
`ViewModelFactory` genérico para inyectarlo en los ViewModels de Compose. Si el proyecto
crece, migrar a Hilt desde ahí es directo.
