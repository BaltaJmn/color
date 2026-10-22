# Publicar desde GitHub Actions

Igual en las cuatro apps de la familia: el proceso, los workflows y los scripts viven en
`BaltaJmn/ci` (`README.md` y `PUBLICAR.md`). Aquí, solo lo que es de Chroma.

| Qué | Valor |
|---|---|
| Paquete de Play y bundle id de Apple | `com.baltajmn.color` |
| Clave de subida | `~/keys/chroma-upload.jks`, alias `upload`, `CN=BaltaJmn` (el `signer-cn` de `release.yml`) |
| Versión de las dos tiendas | `versionName` y `versionCode` de `androidApp/build.gradle.kts` |
| Web: contacto de Play, soporte y privacidad de Apple | https://color.baltajmn.dev/ |
| Pro | `pro_lifetime` en Play, `com.baltajmn.color.pro_lifetime` en Apple; 1,99 EUR en las dos. Mensual: suscripcion `pro_monthly` (plan base `monthly`) y `com.baltajmn.color.pro_monthly`, 0,99 EUR; creadas con `ci/tienda/compra.py --suscripcion` |

La primera subida a Play fue un borrador por CI (24-09-2026, `-f stores=play -f track=internal -f status=draft`, `versionCode` 2): Play acepta la primera subida por API y aplica solo su firma de apps.

## Publicar una versión: el tren de los lunes

Desde el 26-10-2026 las versiones salen solas con el tren semanal (`.github/workflows/tren.yml`; el
proceso, en `BaltaJmn/ci` PUBLICAR.md sección 7):

- **Domingo 19:37** (hora de Madrid): si hay cambios en main desde la última versión (sin contar
  `.md` ni `.github`), sube la minor (`X.Y+1.0`) y el `versionCode`, deja el commit y la etiqueta,
  sube a Play `alpha` y a TestFlight y avisa por ntfy con el botón "Parar esta semana".
- **Lunes 9:07**: producción al 100 % en Play y revisión en App Store (sale sola al aprobarse), con
  la ficha de `store/app-store` y las capturas de iPhone si cambiaron. Una tienda con la versión
  anterior en revisión, rechazada o sin publicar se salta la semana, y el aviso lo dice.
- **Entre semana nadie toca la versión ni pone etiquetas.** Un arreglo urgente sube la patch
  (`X.Y.Z+1`) a mano y solo con el sí del usuario: commit con la versión, `git tag vX.Y.Z && git
  push origin vX.Y.Z` (alpha y TestFlight) y `gh workflow run tren.yml -f paso=lunes -f seco=false`.
- **Novedades**: quien cambia algo visible reescribe `store/whatsnew/whatsnew-<idioma>` en todos los
  idiomas en el mismo commit (tope 500). Valen para las dos tiendas: nada que solo exista en una, ni
  el nombre de la otra tienda (Apple rechaza que se nombre Google Play). Si nadie las toca, salen las
  genéricas de `store/whatsnew-generic` y el aviso del domingo lo dice.
- Prueba sin publicar nada: `gh workflow run tren.yml -f paso=domingo` (seco por defecto) y después
  `-f paso=lunes`.

Otro canal o una sola tienda, a mano: `gh workflow run release.yml -f stores=play -f track=internal`
(`-f status=draft` mientras la app no haya publicado nada en Play).

## Ficha

Textos, gráficos y capturas en `store/`, con la estructura del README de `ci`. Cada push a `store/`
la comprueba; para subirla, `gh workflow run listings.yml -f target=play` (o `app-store`, `both`).
La App Store solo acepta cambios con una versión en preparación.

## Ficha de temporada

La descripción corta y el principio de la larga de Play cambian en las semanas de "year in pixels" y de
propósitos (15-12 al 31-01) con `temporada.yml`, que solo toca el texto (no iconos ni capturas). Los
textos, las fechas y las órdenes están en `store/temporada/README.md`. Cada push a `store/` la
comprueba, y subirla o volver a la normal se elige a mano: `gh workflow run temporada.yml -f
ficha=invierno-2027 -f modo=subir` (o `modo=dry` para que Play valide sin cambiar nada).

## Secretos del repositorio

Los cinco de Play salen de la clave de esta app y de la cuenta de servicio que publica. Se ponen sin
que el valor pase por la pantalla:

```bash
base64 -i ~/keys/chroma-upload.jks | gh secret set KEYSTORE_BASE64 -R BaltaJmn/color
sed -n 's/^storePassword=//p' keystore.properties | tr -d '\n' | gh secret set KEYSTORE_PASSWORD -R BaltaJmn/color
sed -n 's/^keyPassword=//p' keystore.properties | tr -d '\n' | gh secret set KEY_PASSWORD -R BaltaJmn/color
printf upload | gh secret set KEY_ALIAS -R BaltaJmn/color
gh secret set PLAY_SERVICE_ACCOUNT_JSON -R BaltaJmn/color < ~/keys/play-service-account.json
```

Los ocho de Apple (`APPSTORE_*`, `APPLE_*`) son de cuenta, iguales en las cuatro apps: los pone
`~/keys/credenciales.sh sincronizar`, que también renueva `PLAY_SERVICE_ACCOUNT_JSON` al rotarla.
