# Chroma

Una foto al día, un color que eliges, y un año pintado en una rejilla. Amigos opcionales, sin
métricas. Android + iOS, Compose Multiplatform sobre Kotlin Multiplatform. Nombre bajo el icono
**Chroma**; ficha **Chroma: color del día**. Identificador en las dos tiendas: `com.baltajmn.color`.
Repositorio `BaltaJmn/color`, **público**.

Este fichero lo carga Claude Code solo en cualquier sesión abierta sobre este repositorio. Es el
contexto permanente del proyecto: si algo hay que saber siempre, va aquí, no en el chat.

Hermana de Quilt (`../HabitTracker`), MoodTraker (`../MoodTraker`) y Purl (`../line`): misma
arquitectura y pago único. Casi todos los ficheros de plataforma salen de Purl; `docs/tecnico.md` 3
dice de cuál sale cada uno.

## Antes de escribir código

| Documento | Qué manda |
|---|---|
| `SPEC.md` | El porqué de cada decisión de producto |
| `docs/tecnico.md` | El contrato: identificadores, versiones, árbol, formatos, algoritmos, servidor, tests y qué gobierna cada issue (sección 11) |
| `docs/pantallas.md` | La interfaz: tokens, medidas, pantallas, estados, widgets |
| `docs/textos.md` | Tono y vocabulario. Los textos en sí viven en `Strings.kt` |

Se trabaja por issues de GitHub, en el orden de `SPEC.md` 11. Cada issue se cierra con su commit.

Si el código necesita algo que los documentos no dicen, **se decide, se escribe en el documento que
toca en el mismo commit, y se sigue**.

## Contratos que no se rompen

- **Lo que no compartes no sale del teléfono.** Sin analítica ni informes de fallos. Sale lo de
  RevenueCat y, con cuenta, lo que el usuario comparte. Cualquier cambio a eso toca en el mismo
  commit la política (`web/index.html`), `store/formularios.md` y `PrivacyInfo.xcprivacy`.
- **Social sin métricas.** Ni likes, ni comentarios, ni contadores, ni "visto por", ni avisos de
  publicación, ni rachas. Una función que meta cualquiera de esas cosas no entra.
- **Las fotos compartidas viven 7 días en el servidor.** Los colores, mientras exista la cuenta.
- **El móvil manda.** `entries.json` es la verdad; el servidor es una copia de lo compartido.
- `entries.json` vive en almacenamiento privado, **nunca** en el App Group. Escritura atómica con
  `.bak`, y un fichero ilegible va a cuarentena, no se sobrescribe.
- `widget.json` es el único contrato con los widgets y **nunca lleva fotos, palabras ni nombres de
  amigos**.
- Solo se edita la entrada de hoy. El día lógico acaba a las **03:00** locales.
- La extracción de color es **determinista** y vive en común: misma foto, mismos colores en las dos
  plataformas.
- **Importar nunca borra**: fusiona, gana el teléfono, y lo importado llega como privado.
- Lo que nunca se cobra: la foto y el color, Mi año, compartir, la copia, el recordatorio, el widget de
  hoy y todo lo de Amigos.
- La seguridad del servidor vive en RLS y funciones `security definer`, no en la app.
- `Strings.kt` obliga a los cinco idiomas (en, es, pt, de, fr) por firma de función y es la fuente
  única de los textos.
- **El `versionCode` no se reutiliza nunca.**

## Seguridad, sin excepciones

El repositorio es público: todo lo que entra en su historia se queda.

- El `.jks`, `keystore.properties` y `local.properties` nunca se suben.
- Los secretos viven en GitHub repository secrets o en los secretos de Supabase.
- En el binario solo van claves públicas: las de RevenueCat (`goog_`, `appl_`) y la `anon` de
  Supabase. La `sk_` de RevenueCat, la `service_role` de Supabase y la de Resend nunca entran en el
  repositorio.

## Commits

El cuerpo explica **por qué**, no qué. Un commit por issue, que la cierra (`Closes #N`), y push al
terminar cada una. Trailer:

```
Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
```

## Estilo

- Documentación y commits en español; código, comentarios y nombres en inglés.
- Sin em dash y sin emoji en nada que escriba Claude.
- Comentarios: solo los que explican una decisión que el código no puede explicar solo.

## Comandos

```bash
./gradlew :shared:testAndroidHostTest          # tests comunes sobre JVM, el rápido
./gradlew :shared:iosSimulatorArm64Test        # tests comunes sobre Kotlin/Native
./gradlew :androidApp:assembleDebug
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

Iterar con el objetivo mínimo del módulo tocado; el build de todo, una vez al final.
