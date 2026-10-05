# Prueba cerrada

Lo que cambia mientras dura la prueba cerrada (canal *Alpha*), apuntado según se sube. Sirve para
dos cosas: contestar a Play al pedir acceso a producción, que pregunta qué cambió a raíz de la prueba
(`lanzamiento-play.md` 8), y contar en producción qué se arregló.

Cada versión que sube a la prueba añade su fila aquí en el mismo commit que sube el `versionCode`.

## Estado

| | |
|---|---|
| Canal | Prueba cerrada - Alpha, grupo `chroma-testers` (`formularios.md` 3b) |
| Versión en el canal | 1.0.9 (12) subida el 05-10-2026, en revisión de Play; antes, 1.0.8 (11) |
| 12 testers con la prueba aceptada | Cumplido el 29-09-2026. A 03-10-2026 siguen siendo 12, justos: el grupo tiene 25 miembros, pero unirse al grupo no cuenta sin aceptar la prueba |
| 14 días seguidos | 4 de 14 a 03-10-2026; como pronto el 13-10-2026 |
| Fallos y ANR en Android vitals | 0 a 03-10-2026 (últimos 28 días) |
| Comentarios privados de testers en Play | 0 a 03-10-2026; todo lo recibido ha llegado por los hilos del intercambio |

## Versiones

| versionCode | Versión | Fecha | Qué cambió | De dónde salió |
|---|---|---|---|---|
| 3 | 1.0 | 24-09-2026 | Primera versión de la prueba cerrada: la misma 1.0 de la prueba interna. | - |
| 4 | 1.0.1 | 24-09-2026 | Pide una valoración una sola vez, al llegar a 7 días con color. | Decisión propia |
| 5 | 1.0.2 | 28-09-2026 | Los círculos de color se aplastaban en móviles estrechos. La foto de la cámara se perdía con el móvil justo de espacio (se escribía en la caché). El paywall prometía Amigos, que no llega hasta la v1.1. Compartir no enseñaba la vista previa de la tarjeta. | Probando la versión de la prueba el primer día |
| 6 | 1.0.3 | 28-09-2026 | Lavado de cara: fecha en grande, barra de navegación flotante, ajustes por grupos y widgets nuevos. La vista previa del widget del año no cargaba en el selector. | Decisión propia: la app se sentía sosa, y los testers tenían que valorar la de verdad |
| 7 | 1.0.4 | 29-09-2026 | La foto hecha con el móvil en horizontal se perdía al volver de la cámara. Girar el móvil devolvía a Hoy y volvía a pedir el desbloqueo. El widget de Hoy enseñaba "Can't show content" cuando Android arrancaba la app solo para refrescarlo. Un recordatorio o un cambio de hora con la app abierta podía llevarse el último cambio sin guardar y la foto recién hecha. | Revisión del código antes de subir versión; los tres primeros reproducidos en el emulador |
| 8 | 1.0.5 | 01-10-2026 | Bloqueo: la capa dejaba pasar los toques a lo de debajo (se podía borrar el día o apagar el bloqueo sin autenticar), TalkBack la atravesaba y los diálogos quedaban encima. En Android 8 a 10 encenderlo cerraba la app. El minuto del rebloqueo no contaba el tiempo con el móvil dormido. Sin bloqueo de pantalla en el teléfono, la capa no tenía salida. El visor de fotos dejaba pasar los toques. | Revisión del código del 30-09-2026 (#45); el visor, comprobado en el emulador |
| 9 | 1.0.6 | 01-10-2026 | Arranque: en Android 7 la app no abría (ahora pide Android 8). Las fotos con orientación EXIF en espejo (algunas cámaras frontales) se guardaban de lado. El idioma de la app no cambiaba hasta cerrarla. El modo oscuro, el tamaño de letra o un teclado devolvían a Hoy y perdían la foto recién hecha. Sin app de cámara que conteste, o con un `entries.json` o una foto que no se leen, la app se cerraba. | Revisión del código del 30-09-2026 (#46); modo oscuro e idioma, comprobados en el emulador |
| 10 | 1.0.7 | 01-10-2026 | Datos y fotos: un corte entre los dos renombres de la escritura devolvía la versión anterior. El aviso de fichero ilegible se perdía si lo encontraba un receiver. Con el disco lleno la foto quedaba a medias. Un móvil restaurado desde la copia de Android no recuperaba nunca las fotos desde el zip. Con el proceso muerto detrás de la cámara, la foto se perdía. Con la app abierta a las 03:00, tocar un color escribía en el día siguiente. Los días futuros de una copia abrían un año inexistente. | Revisión del código del 30-09-2026 (#47) |
| 11 | 1.0.8 | 01-10-2026 | Recordatorio y widgets: con las notificaciones (o solo el canal del aviso) apagadas, el recordatorio seguía encendido sin llegar y sin decirlo. El aviso seguía en la bandeja tras elegir el color o al día siguiente. Tocarlo no llevaba a Hoy. Los widgets no pasaban de día a las 03:00 ni tras un cambio de hora, y no seguían el modo oscuro. | Revisión del código del 30-09-2026 (#48) |
| 12 | 1.0.9 | 01-10-2026 | Pago: un pago pendiente parecía un error y Pro no llegaba hasta salir y volver a entrar. Un corte de red con el cobro hecho invitaba a pagar otra vez. Restaurar sin conexión decía "no hay nada que restaurar". La reseña gastaba su única petición al importar una copia o con la app parada. | Revisión del código del 30-09-2026 (#49) |
| 13 | 1.0.10 | 01-10-2026 | Interfaz, accesibilidad y color: días casi blancos o casi negros parecían vacíos en Mi año, el póster y el widget. Con letra grande se partían palabras en la tarjeta, la cabecera y los botones, y en el póster sin Pro la etiqueta PRO de "Guardar en Fotos" se partía en vertical. El selector de hora salía en lavanda. El lector de pantalla leía lo que había debajo de una capa. Las estadísticas daban por diferencia lo que era ruido y comparaban un año a medias contra uno entero. Los colores ofrecidos salían también de la parte recortada de la foto. Nuevo: zoom en la foto y copiar el hex. | Revisión del código del 30-09-2026 (#50, #51); la etiqueta PRO, un tester el 02-10-2026 |
| 14 | 1.0.11 | 03-10-2026 | Tira: con un solo día era un bloque de un color. Ahora, con menos de 30 días, cada día ocupa 1/30 del ancho y el resto queda vacío, en Mi año y en el póster. Entran también los arreglos de Amigos (#52), que sigue apagado hasta producción. | Un tester el 02-10-2026 |

## Comentarios de los testers

Llegan por los comentarios privados de Play (*Valoraciones y reseñas > Comentarios de pruebas*), por
`baltajmn@gmail.com` y por los hilos del intercambio de pruebas. Una línea por comentario: fecha, qué
dijo (sin nombre) y qué se hizo.

- 28-09-2026, hilo del intercambio: "se ve muy limpia". Nada que cambiar.
- 28-09-2026, hilo del intercambio: un tester con iPhone se inscribió y Play no le dejaba instalar.
  Chroma es solo Android; se le explicó. Nada en la app.
- 29-09-2026, hilo del intercambio: tras inscribirse, Chroma no le salía en Play ("Is India country
  enabled?"); a los 20 minutos ya estaba. Retraso de Play tras unirse al grupo, el canal está en todos
  los países. Nada en la app.
- 30-09-2026, hilo del intercambio (HMD Pulse): la tarjeta de hoy con el hex vacío `#------` "te dice
  para qué es el día sin ningún onboarding". Nada que cambiar; confirma que la pantalla de hoy se
  entiende sin tutorial.
- 02-10-2026, hilo del intercambio (1.0.6, pantalla 1080x2400): en el póster, la etiqueta PRO de
  "Guardar en Fotos" no cabe y se parte en vertical (P / R / O). Arreglado en 1.0.10 (13): los dos
  botones se repartían la fila a partes iguales y el texto se quedaba todo el ancho; ahora, si no caben
  los dos, se apilan, y la etiqueta conserva su ancho. Comprobado en el emulador a 411 dp de ancho
  (el de un 1080x2400) con la letra al 100, 130 y 200 %.
- 02-10-2026, hilo del intercambio: con un solo día anotado, la vista Tira es un bloque de un solo
  color; mostrar el resto del año vacío explicaría en qué se convierte. Decidido el 03-10-2026: con
  menos de 30 días, cada día ocupa 1/30 del ancho y el resto queda vacío, en Mi año y en el póster
  Tira (el fondo de pantalla no cambia). El año entero vacío se descartó: al principio no se ve nada y
  en diciembre los huecos parecen un reproche. Llega en la 1.0.11 (14).

## Para la solicitud de acceso a producción

Borrador de las respuestas, que se termina al pedir el acceso con lo que haya arriba.

- **Cómo se reclutó a los testers:** intercambio de pruebas con otros desarrolladores, en grupos de
  Google y por correo. Cada tester entra en el grupo `chroma-testers` y acepta la prueba desde el
  enlace de Play.
- **Qué cambió a raíz de la prueba:** las filas de la tabla desde el `versionCode` 4, en una frase cada
  una.
- **Por qué está lista:** los fallos y ANR de Android vitals durante la prueba (comprobarlos al pedir
  el acceso), los tests comunes en verde en cada versión, y cada fallo arreglado reproducido antes en
  el emulador y comprobado después con la versión nueva.
