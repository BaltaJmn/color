# Mapa del repositorio

Qué hay en cada sitio. Se actualiza en el mismo commit que añade o mueve algo.

## Raíz

| Ruta | Qué es |
|---|---|
| `SPEC.md` | Producto: el porqué |
| `CLAUDE.md` | Contexto permanente para Claude Code |
| `README.md` | Presentación corta |
| `MAPA.md` | Este fichero |

## Documentos para programar

| Ruta | Qué es |
|---|---|
| `docs/tecnico.md` | Contrato técnico |
| `docs/pantallas.md` | Interfaz |
| `docs/textos.md` | Tono y vocabulario |

## Tienda y web

| Ruta | Qué es |
|---|---|
| `BaltaJmn/chroma-privacy` | La web, en color.baltajmn.dev por GitHub Pages: política (`index.html`), términos de Amigos (`terms.html`), borrado de cuenta (`delete.html`), invitación sin la app (`404.html`) y `.well-known` para que los enlaces abran la app. Repositorio público aparte, porque este es privado |
| `store/formularios.md` | Respuestas de los formularios de las dos consolas y cómo se publica la web |
| `store/listings/<idioma>/` | Ficha de Play: título, corta y larga, un párrafo por línea |
| `store/app-store/<idioma>/` | Ficha de App Store: nombre, subtítulo, palabras clave, promo y descripción |
| `store/whatsnew/` | Novedades de la versión, por idioma |
| `store/capturas.md` | Escenas, titulares y cómo se sacan las capturas |
| `store/servidor.md` | Cómo se monta el proyecto de Supabase, sus secretos y los inicios de sesión |

## Código

| Ruta | Qué es |
|---|---|
| `shared/src/commonMain` | Toda la interfaz y la lógica. El árbol está en `docs/tecnico.md` 3 |
| `shared/src/androidMain`, `shared/src/iosMain` | Solo los `actual` que el sistema obliga |
| `shared/src/commonTest` | Tests comunes, en JVM y en Kotlin/Native |
| `androidApp` | `MainActivity`, manifiesto, recursos, icono. Nada de lógica |
| `iosApp/iosApp` | `iOSApp.swift`, `Info.plist`, `PrivacyInfo.xcprivacy`, `<lang>.lproj` |
| `iosApp/ChromaWidget` | Widgets de WidgetKit. Leen `widget.json` y nada más |
| `iosApp/Configuration/Config.xcconfig` | Versión, identificador y Team ID de iOS |
| `supabase/migrations` | Esquema, funciones, RLS y la tarea de cada noche (v1.1) |
| `supabase/functions` | Edge Functions: `purge-photos`, `report-notify`, `delete-account` |
| `supabase/tests` | Tests pgTAP de las reglas de acceso (test 17) |
| `.github/workflows/tests.yml` | Tests comunes en cada push, en JVM y en Kotlin/Native |
| `tools/icon.py` | Genera todos los iconos desde una geometría |
| `tools/demo/generar.py` | El año de demostración de las capturas |
| `tools/store/capturas.py`, `tools/store/fichas.py` | Marco de las capturas; topes y subida de las fichas |
| `gradle/libs.versions.toml` | Versiones, las de Purl sin tocar |
