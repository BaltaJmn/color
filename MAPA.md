# Mapa del repositorio

Qué hay en cada sitio. Se actualiza en el mismo commit que añade o mueve algo.

## Raíz

| Ruta | Qué es |
|---|---|
| `SPEC.md` | Producto: el porqué |
| `CLAUDE.md`, `AGENTS.md` | Contexto permanente para Claude Code y Codex (el mismo texto) |
| `README.md` | Presentación corta |
| `MAPA.md` | Este fichero |

## Documentos para programar

| Ruta | Qué es |
|---|---|
| `docs/tecnico.md` | Contrato técnico |
| `docs/pantallas.md` | Interfaz |
| `docs/textos.md` | Tono y vocabulario |

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
| `.github/workflows/tests.yml` | Tests comunes en cada push, en JVM y en Kotlin/Native |
| `gradle/libs.versions.toml` | Versiones, las de Purl sin tocar |
