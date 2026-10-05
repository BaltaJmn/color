# Publicar desde GitHub Actions

Los workflows de publicación de las hermanas, copiados a `.github/workflows/`. La secuencia de Android
vive en `BaltaJmn/ci` (`android-play-release.yml`): aquí solo está la llamada con el paquete, el CN
de la firma, las tareas de Gradle y la carpeta de notas. `release-ios.yml` sale del de Purl, porque
el proyecto de iOS sale de la misma plantilla (`Config.xcconfig`), con tres cambios: el nombre del
archivo, `permissions: contents: read` y el certificado de distribución propio (abajo, en
TestFlight). La puerta que lo saltaba mientras no había secretos de Apple se quitó con la cuenta ya
creada: un fallo de iOS tiene que verse en rojo.

## Cómo se dispara

**Por etiqueta**, no en cada push a `main`. Cada subida quema un `versionCode` y les llega a los
testers.

1. Subir el `versionCode` en `androidApp/build.gradle.kts` (y `versionName` si toca).
2. Reescribir `store/whatsnew/whatsnew-<idioma>` si cambia algo visible (tope 500).
3. Mientras dure la prueba cerrada, su fila en `store/prueba-cerrada.md`.
4. Commit, y la etiqueta:

```bash
git tag v1.0 && git push origin v1.0
```

La misma etiqueta dispara `release.yml` (Play, canal `alpha`, la prueba cerrada) y `release-ios.yml`
(TestFlight). Si se olvida el paso 1, Play rechaza la subida con "Version code N has already been
used", después de construir.

Disparo manual, para otro canal:

```bash
gh workflow run release.yml --ref main -f track=internal
```

**Mientras la app no tenga ninguna versión publicada** en ningún canal, Play solo acepta versiones en
borrador: `-f status=draft`, y la versión se lanza después desde la consola. Así se hizo la primera
de Chroma (24-09-2026, `-f track=internal -f status=draft`, `versionCode` 2): Play acepta la primera
subida por API y aplica solo su firma de apps.

**`internal` y `alpha` no son el mismo sitio.** La prueba interna se activa en minutos; los 14 días
con 12 testers solo corren en la **cerrada** (`alpha`). Un `versionCode` gastado en un canal no vale
en otro.

## La firma

La clave de subida de Chroma dice `CN=BaltaJmn`, así que `release.yml` pasa `signer-cn`: el valor por
defecto del workflow compartido es `CN=Baltasar` y rechazaría un paquete bien firmado. Comprobado
con el mismo `jarsigner -verify -verbose:summary -certs` que usa el workflow.

Sin `keystore.properties` el build de release cae a la clave de debug en silencio. El workflow lo
detecta antes de subir: es esa comprobación del CN.

## Secretos del repositorio

| Secreto | Qué es | De dónde sale |
|---|---|---|
| `KEYSTORE_BASE64` | El `.jks` de subida, en base64 | `~/keys/chroma-upload.jks` |
| `KEYSTORE_PASSWORD` | La del almacén | `keystore.properties` |
| `KEY_ALIAS` | `upload` | |
| `KEY_PASSWORD` | La de la clave (la misma) | `keystore.properties` |
| `PLAY_SERVICE_ACCOUNT_JSON` | La cuenta de servicio **de publicar** | `~/keys/play-service-account.json`; al rotarla con `credenciales.sh play` se actualiza sola |

Se ponen sin que el valor pase por la pantalla:

```bash
base64 -i ~/keys/chroma-upload.jks | gh secret set KEYSTORE_BASE64 -R BaltaJmn/color
sed -n 's/^storePassword=//p' keystore.properties | tr -d '\n' | gh secret set KEYSTORE_PASSWORD -R BaltaJmn/color
sed -n 's/^keyPassword=//p' keystore.properties | tr -d '\n' | gh secret set KEY_PASSWORD -R BaltaJmn/color
printf upload | gh secret set KEY_ALIAS -R BaltaJmn/color
gh secret set PLAY_SERVICE_ACCOUNT_JSON -R BaltaJmn/color < ~/keys/play-service-account.json
```

### Dos cuentas de servicio, siempre

**La que publica y la de RevenueCat no se juntan nunca.** La de RevenueCat es de solo lectura
(datos financieros y pedidos): si se filtra su JSON, te leen los pedidos. La de publicar puede subir
un binario a producción. RevenueCat guarda su JSON en sus servidores, así que ahí va la de lectura.

| Cuenta | Dónde vive | Permisos en Play Console |
|---|---|---|
| `play-publisher@chroma-baltajmn` | `~/keys/play-service-account.json`, este secreto y `~/keys/play.sh` | Publicar en canales de prueba y en producción, gestionar canales y testers, gestionar presencia en la tienda |
| La de RevenueCat | `~/keys/revenuecat-play-service-account.json` y la app de Play en RevenueCat | Ver información de la app, ver datos financieros, gestionar pedidos |

## TestFlight

`release-ios.yml` archiva `Chroma.xcarchive` con el esquema `iosApp`, y lo exporta y sube en un solo
`xcodebuild` (`destination: upload`). La versión y el número de build son el `versionName` y el
`versionCode` de `androidApp/build.gradle.kts`, los mismos que sube Play con esa etiqueta: las dos
tiendas llevan siempre la misma versión, y lanzar el flujo a mano sin subir el `versionCode` lo
rechazan las dos por build repetido.

El repositorio es público para que Actions no cueste. Siendo privado, cada minuto de macOS contaba
como diez del cupo gratuito, y el 05-10-2026 GitHub dejó de arrancar todos los trabajos, tests
incluidos, con "recent account payments have failed or your spending limit needs to be increased".
Un trabajo que falla sin pasos ni registro es eso: se mira en *Settings > Billing & plans*.

| Secreto | Qué es |
|---|---|
| `APPSTORE_KEY_ID` | El Key ID de la clave de la App Store Connect API |
| `APPSTORE_ISSUER_ID` | El Issuer ID, el mismo para todas las claves de la cuenta |
| `APPSTORE_PRIVATE_KEY` | El contenido del `.p8`, entero, con sus líneas `BEGIN`/`END` |
| `APPLE_TEAM_ID` | El Team ID de la cuenta de desarrollador |
| `APPLE_DISTRIBUTION_P12` | El certificado Apple Distribution con su clave privada, `.p12` en base64 |
| `APPLE_DISTRIBUTION_P12_PASSWORD` | La contraseña de ese `.p12` |

Rol *App Manager* o superior, para que `-allowProvisioningUpdates` cree el certificado de desarrollo
y los perfiles (app, widget y App Group). La de las hermanas sirve: es de cuenta. Los cuatro
primeros secretos los pone `~/keys/credenciales.sh appstore` en todos los repos de iOS a la vez. Los
dos del `.p12` salen de `~/keys/apple-distribution.p12`, con la contraseña en el Llavero
(`dev.baltajmn.apple-distribution-p12`), y de momento solo están en este repositorio.

### Por qué un `.p12` en un secreto

Sin certificado de distribución en el llavero, Xcode firma el `.ipa` en la nube (*cloud signing*), y
en este equipo eso no sube: App Store Connect lo rechaza con ITMS-90035, "Code failed to satisfy
specified code requirement(s)". Al firmar en la nube Xcode le pasa a `codesign` el requisito
designado como texto, con el nombre del certificado dentro, y `Process` de Foundation descompone la
"é" de "Jiménez" por el camino (NFD, `e` + U+0301). El certificado la lleva compuesta (NFC, U+00E9),
así que la firma no cumple su propio requisito: `codesign --verify --strict` lo dice igual en local.
Con la clave privada en el llavero, `codesign` saca el requisito del certificado y coinciden.
Comprobado el 05-10-2026 (CI con Xcode 26.6, local con 27): el `--requirements` sale en
`IDEDistributionPipeline.log`, y `Process` con `"Jim\u{e9}nez"` entrega `Jime\u{301}nez` al programa
que lanza. El mismo archivo exportado con la identidad local ya no pasa `--requirements`, y la app y
el widget cumplen su requisito.

Afecta a toda app de la cuenta, porque el nombre es el del equipo: los otros repos de iOS necesitan
el mismo paso y los dos secretos antes de subir a TestFlight. Hasta que Apple lo arregle, el
certificado (`2A7JLW9543`) vive en `~/keys/apple-distribution.p12` y caduca el 05-10-2027. Se
renueva creando otro por la API (`POST /v1/certificates`, tipo `DISTRIBUTION`, con una CSR nueva) y
rehaciendo el `.p12` y los dos secretos.

Antes de la primera subida a TestFlight, la clave `appl_` de RevenueCat tiene que estar en
`Billing.ios.kt`, o Pro no se podrá comprar en iOS.
