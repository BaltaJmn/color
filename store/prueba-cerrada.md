# Prueba cerrada

Lo que cambia mientras dura la prueba cerrada (canal *Alpha*), apuntado según se sube. Sirve para
dos cosas: contestar a Play al pedir acceso a producción, que pregunta qué cambió a raíz de la prueba
(`lanzamiento-play.md` 8), y contar en producción qué se arregló.

Cada versión que sube a la prueba añade su fila aquí en el mismo commit que sube el `versionCode`.

## Estado

| | |
|---|---|
| Canal | Prueba cerrada - Alpha, grupo `chroma-testers` (`formularios.md` 3b) |
| Versión en el canal | 1.0.13 (16) subida el 07-10-2026 con `gh workflow run release.yml -f stores=play`. La etiqueta `v1.0.13` llegó después y su Release falló en Play (la 16 ya estaba) y se canceló antes de TestFlight; la 1.0.13 de iOS sube con `-f stores=app-store` cuando Apple apruebe la 1.0.12. Antes, 1.0.12 (15), del 05-10-2026 con la etiqueta `v1.0.12`, y 1.0.9 (12), que no llegó a salir de revisión |
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
| 15 | 1.0.12 | 05-10-2026 | Idiomas: de cinco a trece, con italiano, neerlandés, polaco, ruso, turco, indonesio, japonés y coreano, en la app, los widgets y las fichas. El fondo de la ventana y las vistas previas de los widgets de Android, y los widgets de iOS, seguían con los grises de antes de la 1.0.10. Para iOS, los enlaces que no abrían en iOS 18 y la hoja de compartir del iPad. Primera subida desde la 1.0.9: lleva también la 1.0.10 y la 1.0.11. | Decisión propia: salir en App Store con los idiomas de las fichas de Play; el resto, del repaso para App Store del 05-10-2026 |
| 16 | 1.0.13 | 07-10-2026 | Mi año cabe entero en la pantalla, sin desplazar: las celdas pasan a ser más planas que anchas, Póster y En palabras son botones y la Tira se elige con un icono de la cabecera. Al elegir color, la instrucción se lee a tamaño normal, los candidatos ya no se quedan fuera de la pantalla en móviles bajos y mantener pulsado uno dice su nombre sin elegirlo. Compartir, a la vista bajo la tarjeta y con la foto apagada de entrada. Las ofertas ya no salen encima del color recién elegido. Con lector de pantalla, los avisos y "Copiado" se oyen, la rejilla se recorre mes a mes y dice cuál es hoy. "Reducir movimiento" de iOS, conectado de verdad. La revelación ya no enseña el nombre junto a una esquina sin pintar. Tras comprar desde En palabras, se abre solo. Y la foto de galería que se perdía en Android 11, con la ficha en blanco que tapaba los botones. | Crítica de diseño propia del 07-10-2026; Android 11 y la ficha en blanco, un tester el 07-10-2026 |

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
- 03-10-2026, respuesta a las tres preguntas del grupo (emulador Android 16, 1080x2400): nada cortado
  ni ilegible. Los colores salen de la foto, pero solo de las zonas dominantes: un objeto pequeño y
  saturado sobre fondo liso puede quedarse sin muestra. El recordatorio solo se ofrece al guardar el
  primer color, así que quien abre la app y no anota nada nunca recibe aviso; propone ofrecerlo al
  abrirla o tras el primer día sin anotar. Pendiente de decidir las dos cosas.
- 04-10-2026, hilo del intercambio (emulador Android 11 x86): tras inscribirse, Play decía "Item not
  found" y la ficha web, "Not Found". El paquete lleva x86 y pide Android 8, así que no parece de
  compatibilidad; lo más probable es el retraso de Play tras la inscripción, como el del 29-09. Se le
  pidió que lo reintentara (06-10-2026) y el 07-10-2026 ya instaló la 1.0.12 sin borrar la caché de
  Play: era el retraso.
- 07-10-2026, mismo tester (emulador Android 11 x86, 1080x1920): en Hoy, la tarjeta vacía de la foto
  ocupa casi toda la primera pantalla y hay que desplazarse para descubrir "Hacer una foto" y "Elegir
  de las fotos de hoy"; propone que la galería se vea antes. "Elegir de las fotos de hoy" abrió el
  selector de documentos de Android (en Android 11 `PickVisualMedia` cae a él) y, con una imagen
  sintética, la selección no volvió a Chroma ni tras reabrir; él mismo no descarta que sea el
  selector de ese emulador. Arreglado el mismo día, para la versión siguiente: el selector de
  documentos tapa Chroma entera y Android puede matar el proceso detrás; la respuesta llegaba sin
  nadie esperándola y se tiraba (la cámara ya guardaba la suya en `shot.jpg`). Ahora se guarda y Hoy
  la recoge al volver a primer plano. Una foto que vuelve y no se lee avisa con `photoUnreadable` en
  vez de no hacer nada. Reproducido en el emulador con el selector de documentos y el proceso muerto
  detrás (`am kill`), y comprobado después con la foto de vuelta en Hoy y con un fichero roto. La
  ficha en blanco se acorta en pantallas bajas para que los dos botones quepan sin desplazarse
  (`pantallas.md` 3); comprobado a 1080x1920 con navegación por gestos y con tres botones.

## Solicitud de acceso a producción

Enviada el 13-10-2026 a las 16:06 desde *Panel de control > Solicitar acceso a producción*. Play
avisa por correo al propietario; suele tardar 7 días o menos. Al pedirla: 0 fallos y 0 ANR en Android
vitals (28 días) y un comentario privado en Play (09-10-2026, Galaxy A15 5G, 1.0.7, cinco estrellas:
"la aplicación cumple su función").

Respuestas enviadas, en inglés y con el tope de 300 caracteres de cada campo:

1. Prueba cerrada
   - Cómo se reclutó: "Test-for-test exchanges with other indie Android developers, run in Google
     Groups and by email, plus a few friends. Each tester joined our Google Group and accepted the
     test from the Play opt-in link; at least 12 stayed opted in for the full 14 days."
   - Facilidad: *Difícil*.
   - Interacciones: "Testers used it on real phones and emulators (Android 11 to 16, several screen
     sizes): taking or picking the day's photo, choosing its color, My Year in grid and strip view,
     the poster, the reminder and widgets. Use was steady but light, as the app is one photo a day by
     design."
   - Comentarios: "Collected from the test-exchange threads, email and Play private feedback (one
     5-star note). Points raised: PRO label wrapping on the poster, a one-day strip looking like a
     solid block, the gallery picker losing the photo on Android 11, buttons below the fold on short
     screens. All fixed."
2. La aplicación
   - Audiencia: "Teens (13+) and adults who want a light, private daily journal without writing:
     people into photography and color, and fans of 'year in pixels' and mood-tracker bullet
     journals. No sign-up is needed and everything stays on the phone."
   - Valor: "Once a day you take or pick a photo and the app offers colors taken from it; you choose
     the one that sums up your day. Over the year the colors fill a 365-day grid you can browse, turn
     into a poster or wallpaper, and back up. Ten seconds a day, no writing, no ads, no tracking."
   - Descargas el primer año: *Entre 0 y 10.000*.
3. Preparación
   - Cambios: "13 updates (1.0.1 to 1.0.13). From testers: poster buttons stack when they do not fit,
     short-history strip view, gallery photo kept on Android 11, compact Today on short screens. From
     our own testing: camera photo loss, app lock, reminders, backups, accessibility, 13 languages."
   - Por qué está lista: "Zero crashes and ANRs in Android vitals during the test. Every reported bug
     was reproduced on the emulator before fixing and checked again on the new build, and the shared
     test suite passes on each release. 1.0.13 has been in the test since Oct 7 with no new issues
     reported."
