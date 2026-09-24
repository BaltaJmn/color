# Publicar desde GitHub Actions

Los workflows de publicación de las hermanas, copiados a `.github/workflows/`. La secuencia de Android
vive en `BaltaJmn/ci` (`android-play-release.yml`): aquí solo está la llamada con el paquete, el CN
de la firma, las tareas de Gradle y la carpeta de notas. `release-ios.yml` es el de Purl, porque el
proyecto de iOS sale de la misma plantilla (`Config.xcconfig`), con tres cambios: el nombre del
archivo, `permissions: contents: read` y el secreto de la puerta pasado por `env` en vez de
interpolado en el script.

## Cómo se dispara

**Por etiqueta**, no en cada push a `main`. Cada subida quema un `versionCode` y les llega a los
testers.

1. Subir el `versionCode` en `androidApp/build.gradle.kts` (y `versionName` si toca).
2. Reescribir `store/whatsnew/whatsnew-<idioma>` si cambia algo visible (tope 500).
3. Commit, y la etiqueta:

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
borrador: `-f status=draft`, y la versión se lanza después desde la consola. La primera subida de
Chroma se hace a mano en la consola de todas formas, porque ahí se acepta la firma de apps de Play.

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
| `PLAY_SERVICE_ACCOUNT_JSON` | La cuenta de servicio **de publicar** | `~/keys/play-service-account.json` |

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
`xcodebuild` (`destination: upload`). El número de build sale de `github.run_number`. Mientras no
exista `APPSTORE_KEY_ID`, el trabajo se salta solo y deja un aviso en vez de salir en rojo.

| Secreto | Qué es |
|---|---|
| `APPSTORE_KEY_ID` | El Key ID de la clave de la App Store Connect API |
| `APPSTORE_ISSUER_ID` | El Issuer ID, el mismo para todas las claves de la cuenta |
| `APPSTORE_PRIVATE_KEY` | El contenido del `.p8`, entero, con sus líneas `BEGIN`/`END` |
| `APPLE_TEAM_ID` | El Team ID de la cuenta de desarrollador |

Rol *App Manager* o superior, para que `-allowProvisioningUpdates` cree certificado y perfiles
(app, widget y App Group) sin meter un `.p12` en un secreto. La de las hermanas sirve: es de cuenta.
Antes de la primera subida a TestFlight, la clave `appl_` de RevenueCat tiene que estar en
`Billing.ios.kt`, o Pro no se podrá comprar en iOS.
