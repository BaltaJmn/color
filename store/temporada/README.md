# Fichas de temporada de Google Play

Plan de crecimiento (`plan-apps/0-plan.md`, apartado 4 puntos 16 y 17, y apartado 7). Enero es el pico
de búsqueda de "year in pixels" y de propósitos de año nuevo, así que durante esas semanas la ficha de
Play abre con eso. Solo cambia el **texto** de la ficha (la descripción corta y el principio de la
larga): el título, el icono, el gráfico y las capturas no se tocan.

| Carpeta | Cuándo | Qué dice |
|---|---|---|
| `invierno-2027/` | Del 15-12-2026 al 31-01-2027 | "Your 2027 year in pixels": empezar el año en una rejilla, un propósito que lleva un momento al día |
| `normal` (no es carpeta: es `store/listings/`) | Desde el 2-02-2027 | La ficha de siempre |

Cada idioma lleva dos ficheros:

- `short.txt`: la descripción corta, que sustituye a la normal. Tope de Play, 80 caracteres.
- `intro.txt`: un bloque con su titular en mayúsculas, que va **encima** de la descripción larga normal
  (`store/listings/<idioma>/full.txt`). La larga completa tiene un tope de 4000 caracteres.

La larga de la temporada se monta con la normal en el momento de subir, así que lo que se arregla en
`store/listings/` llega solo a la temporada y no hay dos copias que diverjan.

## Subirla y volver

Los cambios de ficha de cara al público los pide el usuario: la rutina de los lunes se lo recuerda la
semana anterior (lunes 7-12 y 25-01) y se suben con una orden, en martes (15-12 la de invierno y 2-02 la
vuelta): el lunes es del tren de versiones, y Play rechaza un cambio de ficha con la versión del tren
en revisión (`ci/PUBLICAR.md`).

```bash
# 1. Comprobar (también corre sola en cada push a store/): topes, idiomas y caracteres
python3 tools/store/temporada.py comprobar

# 2. Ensayo: Play valida la edición y la descarta, sin cambiar nada
gh workflow run temporada.yml -f ficha=invierno-2027 -f modo=dry

# 3. Subir, el martes 15-12: la ficha pasa a la de invierno
gh workflow run temporada.yml -f ficha=invierno-2027 -f modo=subir

# 4. Volver a la normal, el martes 2-02
gh workflow run temporada.yml -f ficha=normal -f modo=subir
```

Desde el Mac, `python3 tools/store/temporada.py subir invierno-2027 [--dry]` hace lo mismo con la
cuenta de servicio de `~/keys` (necesita el clon `../ci`).

## Solo afirmaciones verdaderas

Antes de subir, y cada vez que se cambie un texto:

- `intro.txt` dice solo lo que la app hace hoy en producción: una foto al día con su color, empezar el
  1 de enero o cualquier día después, sin rachas, un día saltado se queda vacío, sin cuenta (solo Amigos
  pide una), sin anuncios, sin analítica. Nada de "la mejor", puestos, premios, cifras de usuarios ni
  comparaciones con otras apps.
- No nombra nada que aún no esté en Play producción. El póster de historia 9:16 y el resto de la 1.2.3
  están ya en `store/listings/` ("cuatro estilos"): esa ficha, y con ella esta, solo se sube cuando
  esa versión esté publicada (`play.py estado com.baltajmn.color`).
- Los trece idiomas son traducciones del inglés y el español, revisadas por retrotraducción; un cambio
  en uno se hace en los trece.
- Sin raya larga ni comillas curvas: el comprobador las rechaza.

Una temporada nueva es una carpeta nueva con la misma forma (`short.txt` e `intro.txt` en cada uno de
los trece idiomas); `comprobar` la valida sin tocar el workflow.
