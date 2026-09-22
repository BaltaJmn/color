package com.baltajmn.color.i18n

import com.baltajmn.color.color.colorLabel
import kotlinx.datetime.LocalDate

/** Two-letter code of the device language. */
expect fun systemLanguage(): String

/** The languages the app ships. Anything else falls back to English. */
internal val SUPPORTED = listOf("en", "es", "pt", "de", "fr")

internal fun normalizeLanguage(code: String): String =
    code.take(2).lowercase().takeIf { it in SUPPORTED } ?: "en"

/**
 * Every user-facing string, in one table. This file is the only copy (docs/textos.md).
 *
 * Not Compose Resources on purpose: part of these strings are drawn outside a `@Composable` (a
 * BroadcastReceiver, a Glance widget, the Canvas of the share card, a notification builder).
 *
 * ponytail: the language is read once at first access. Both systems restart the app when the
 * language changes, so this only matters if live switching is ever needed. The functions read
 * [lang] on every call, which lets the tests go through the five.
 */
object S {

    internal var lang = normalizeLanguage(systemLanguage())

    private fun t(en: String, es: String, pt: String, de: String, fr: String): String = when (lang) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        else -> en
    }

    /** The color name for a stored key, in the device language. */
    fun colorName(key: String): String = colorLabel(key, lang)

    // 1. Dates

    fun monthNames(): List<String> = t(
        "January, February, March, April, May, June, July, August, September, October, November, December",
        "enero, febrero, marzo, abril, mayo, junio, julio, agosto, septiembre, octubre, noviembre, diciembre",
        "janeiro, fevereiro, março, abril, maio, junho, julho, agosto, setembro, outubro, novembro, dezembro",
        "Januar, Februar, März, April, Mai, Juni, Juli, August, September, Oktober, November, Dezember",
        "janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre",
    ).split(", ")

    fun monthShort(): List<String> = t(
        "Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec",
        "ene, feb, mar, abr, may, jun, jul, ago, sept, oct, nov, dic",
        "jan, fev, mar, abr, mai, jun, jul, ago, set, out, nov, dez",
        "Jan., Feb., März, Apr., Mai, Juni, Juli, Aug., Sept., Okt., Nov., Dez.",
        "janv., févr., mars, avr., mai, juin, juil., août, sept., oct., nov., déc.",
    ).split(", ")

    fun monthInitials(): List<String> = t(
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "E, F, M, A, M, J, J, A, S, O, N, D",
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "J, F, M, A, M, J, J, A, S, O, N, D",
    ).split(", ")

    fun weekdayNames(): List<String> = t(
        "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday",
        "lunes, martes, miércoles, jueves, viernes, sábado, domingo",
        "segunda-feira, terça-feira, quarta-feira, quinta-feira, sexta-feira, sábado, domingo",
        "Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag, Sonntag",
        "lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche",
    ).split(", ")

    fun weekdayShort(): List<String> = t(
        "Mon, Tue, Wed, Thu, Fri, Sat, Sun",
        "lun, mar, mié, jue, vie, sáb, dom",
        "seg, ter, qua, qui, sex, sáb, dom",
        "Mo, Di, Mi, Do, Fr, Sa, So",
        "lun, mar, mer, jeu, ven, sam, dim",
    ).split(", ")

    // 2. Navigation

    val navToday get() = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    val navYear get() = t("My year", "Mi año", "Meu ano", "Mein Jahr", "Mon année")
    val navFriends get() = t("Friends", "Amigos", "Amigos", "Freunde", "Amis")

    // 3. Today

    val todayPrompt get() = t(
        "What color is today?",
        "¿De qué color es hoy?",
        "De que cor é hoje?",
        "Welche Farbe hat heute?",
        "De quelle couleur est aujourd'hui ?",
    )
    val firstHelp get() = t(
        "Take a photo of something that caught your eye. You'll pick a color from it.",
        "Haz una foto de algo que te llame la atención. De ella sacarás un color.",
        "Tire uma foto de algo que chamou sua atenção. Dela vai sair uma cor.",
        "Fotografiere etwas, das dir ins Auge fällt. Daraus wählst du eine Farbe.",
        "Prends en photo quelque chose qui t'attire l'œil. Tu en tireras une couleur.",
    )
    val takePhoto get() = t("Take a photo", "Hacer una foto", "Tirar uma foto", "Foto aufnehmen", "Prendre une photo")
    val fromGallery get() = t(
        "Choose from today's photos",
        "Elegir de las fotos de hoy",
        "Escolher das fotos de hoje",
        "Aus den Fotos von heute wählen",
        "Choisir parmi les photos du jour",
    )
    val pickColor get() = t("Pick your color", "Elige tu color", "Escolha sua cor", "Wähl deine Farbe", "Choisis ta couleur")
    val galleryNotToday get() = t(
        "That photo is from another day. Today's color comes from today.",
        "Esa foto es de otro día. El color de hoy sale de hoy.",
        "Essa foto é de outro dia. A cor de hoje vem de hoje.",
        "Dieses Foto ist von einem anderen Tag. Die Farbe von heute kommt von heute.",
        "Cette photo date d'un autre jour. La couleur du jour vient d'aujourd'hui.",
    )
    val photoUnreadable get() = t(
        "Couldn't read that photo.",
        "No se ha podido leer esa foto.",
        "Não foi possível ler essa foto.",
        "Dieses Foto konnte nicht gelesen werden.",
        "Impossible de lire cette photo.",
    )
    val addWord get() = t("Add a word", "Añadir una palabra", "Adicionar uma palavra", "Ein Wort hinzufügen", "Ajouter un mot")
    val wordPlaceholder get() = t("One word for today", "Una palabra para hoy", "Uma palavra para hoje", "Ein Wort für heute", "Un mot pour aujourd'hui")
    val retakePhoto get() = t("Another photo", "Otra foto", "Outra foto", "Anderes Foto", "Une autre photo")
    val deleteDay get() = t("Delete this day", "Borrar este día", "Excluir este dia", "Diesen Tag löschen", "Supprimer ce jour")
    val noticeSaveFailed get() = t(
        "Couldn't save. I'll try again with your next change.",
        "No se ha podido guardar. Lo intento otra vez con tu próximo cambio.",
        "Não foi possível salvar. Vou tentar de novo na sua próxima alteração.",
        "Konnte nicht gespeichert werden. Ich versuche es bei deiner nächsten Änderung erneut.",
        "Impossible d'enregistrer. Je réessaierai avec ta prochaine modification.",
    )
    val noticeCorrupt get() = t(
        "Couldn't read your colors. The files were saved separately and nothing was deleted.",
        "No se han podido leer tus colores. Los ficheros se han guardado aparte y no se ha borrado nada.",
        "Não foi possível ler suas cores. Os arquivos foram guardados à parte e nada foi apagado.",
        "Deine Farben konnten nicht gelesen werden. Die Dateien wurden separat gesichert, gelöscht wurde nichts.",
        "Impossible de lire tes couleurs. Les fichiers ont été sauvegardés à part, rien n'a été supprimé.",
    )
    val noticeBackup get() = t(
        "A month of colors. Save a copy off your phone?",
        "Un mes de colores. ¿Guardas una copia fuera del teléfono?",
        "Um mês de cores. Quer guardar uma cópia fora do telefone?",
        "Ein Monat voller Farben. Sicherst du eine Kopie außerhalb des Handys?",
        "Un mois de couleurs. Tu gardes une copie hors du téléphone ?",
    )
    val makeBackup get() = t("Make a backup", "Hacer copia", "Fazer cópia", "Kopie erstellen", "Faire une copie")

    // 4. My year

    val viewGrid get() = t("Grid", "Rejilla", "Grade", "Raster", "Grille")
    val viewStrip get() = t("Strip", "Tira", "Faixa", "Streifen", "Bande")
    val yearEmpty get() = t(
        "Your year will fill in, one color at a time.",
        "Tu año se irá llenando, color a color.",
        "Seu ano vai se preenchendo, cor por cor.",
        "Dein Jahr füllt sich, Farbe für Farbe.",
        "Ton année se remplira, couleur après couleur.",
    )
    val poster get() = t("Poster", "Póster", "Pôster", "Poster", "Affiche")

    // 5. Open day

    val deleteTitle get() = t("Delete this day?", "¿Borrar este día?", "Excluir este dia?", "Diesen Tag löschen?", "Supprimer ce jour ?")
    val deleteText get() = t(
        "The color and its photo will be deleted. This cannot be undone.",
        "Se borran el color y su foto. No se puede deshacer.",
        "A cor e a foto serão excluídas. Não é possível desfazer.",
        "Die Farbe und ihr Foto werden gelöscht. Das lässt sich nicht rückgängig machen.",
        "La couleur et sa photo seront supprimées. Cette action est irréversible.",
    )
    val delete get() = t("Delete", "Borrar", "Excluir", "Löschen", "Supprimer")

    // 6. Settings

    val settingsTitle get() = t("Settings", "Ajustes", "Ajustes", "Einstellungen", "Réglages")
    val sectionReminder get() = t("Reminder", "Recordatorio", "Lembrete", "Erinnerung", "Rappel")
    val sectionCard get() = t("Card", "Tarjeta", "Cartão", "Karte", "Carte")
    val sectionPrivacy get() = t("Privacy", "Privacidad", "Privacidade", "Datenschutz", "Confidentialité")
    val sectionBackup get() = t("Backup", "Copia", "Cópia", "Sicherung", "Sauvegarde")
    val sectionPro get() = t("Chroma Pro", "Chroma Pro", "Chroma Pro", "Chroma Pro", "Chroma Pro")
    val sectionMoreApps get() = t("More apps", "Más apps", "Mais apps", "Weitere Apps", "Plus d'apps")
    val sectionAbout get() = t("About", "Acerca de", "Sobre", "Über", "À propos")
    val reminderRow get() = t("Daily reminder", "Recordatorio diario", "Lembrete diário", "Tägliche Erinnerung", "Rappel quotidien")
    val reminderOff get() = t("Off", "Apagado", "Desativado", "Aus", "Désactivé")
    val openSystemSettings get() = t("Open settings", "Abrir ajustes", "Abrir ajustes", "Einstellungen öffnen", "Ouvrir les réglages")
    val watermarkRow get() = t(
        "\"Chroma\" on shared cards",
        "\"Chroma\" en las tarjetas compartidas",
        "\"Chroma\" nos cartões compartilhados",
        "\"Chroma\" auf geteilten Karten",
        "\"Chroma\" sur les cartes partagées",
    )
    val exportRow get() = t("Export backup", "Exportar copia", "Exportar cópia", "Kopie exportieren", "Exporter une copie")
    val lastBackupNever get() = t("No backup yet", "Todavía ninguna copia", "Ainda nenhuma cópia", "Noch keine Sicherung", "Encore aucune copie")
    val exportNothing get() = t(
        "There's nothing to back up yet.",
        "Aún no hay nada que copiar.",
        "Ainda não há nada para copiar.",
        "Es gibt noch nichts zu sichern.",
        "Il n'y a encore rien à copier.",
    )
    val importRow get() = t("Import backup", "Importar copia", "Importar cópia", "Kopie importieren", "Importer une copie")
    val importSubtitle get() = t(
        "Joins your colors, nothing gets deleted",
        "Se junta con tus colores, sin borrar nada",
        "Se junta às suas cores, sem apagar nada",
        "Wird mit deinen Farben zusammengeführt, nichts wird gelöscht",
        "Se joint à tes couleurs, rien n'est supprimé",
    )
    val proRow get() = t("Chroma Pro", "Chroma Pro", "Chroma Pro", "Chroma Pro", "Chroma Pro")
    val proSubtitle get() = t(
        "Year poster and year widget. One-time payment",
        "Póster y widget del año. Pago único",
        "Pôster e widget do ano. Pagamento único",
        "Jahresposter und Jahres-Widget. Einmalzahlung",
        "Affiche et widget de l'année. Paiement unique",
    )
    val proOwned get() = t("Purchased. Thank you.", "Comprado. Gracias.", "Comprado. Obrigado.", "Gekauft. Danke.", "Acheté. Merci.")
    val restoreRow get() = t("Restore purchase", "Restaurar compra", "Restaurar compra", "Kauf wiederherstellen", "Restaurer l'achat")
    val restoreDone get() = t("Purchase restored.", "Compra restaurada.", "Compra restaurada.", "Kauf wiederhergestellt.", "Achat restauré.")
    val restoreNothing get() = t(
        "There's no purchase to restore.",
        "No hay ninguna compra que restaurar.",
        "Não há nenhuma compra para restaurar.",
        "Es gibt keinen Kauf zum Wiederherstellen.",
        "Il n'y a aucun achat à restaurer.",
    )
    val siblingQuilt get() = t(
        "Your habits, a year at a glance",
        "Tus hábitos, un año a la vista",
        "Seus hábitos, um ano à vista",
        "Deine Gewohnheiten, ein Jahr im Blick",
        "Tes habitudes, une année en un coup d'œil",
    )
    val siblingMood get() = t(
        "How each day went, in colour",
        "Cómo te ha ido cada día, en color",
        "Como foi cada dia, em cores",
        "Wie jeder Tag war, in Farbe",
        "Comment chaque jour s'est passé, en couleur",
    )
    val siblingPurl get() = t(
        "One line a day, read years later",
        "Una línea al día, releída años después",
        "Uma linha por dia, relida anos depois",
        "Eine Zeile am Tag, Jahre später gelesen",
        "Une ligne par jour, relue des années après",
    )
    val privacyRow get() = t("Privacy policy", "Política de privacidad", "Política de privacidade", "Datenschutz", "Confidentialité")

    // 7. Dialogs and notices

    val ok get() = t("OK", "Vale", "OK", "OK", "OK")
    val cancel get() = t("Cancel", "Cancelar", "Cancelar", "Abbrechen", "Annuler")
    val yes get() = t("Yes", "Sí", "Sim", "Ja", "Oui")
    val notNow get() = t("Not now", "Ahora no", "Agora não", "Jetzt nicht", "Pas maintenant")
    val working get() = t("One moment...", "Un momento...", "Um momento...", "Einen Moment...", "Un instant...")
    val importTitle get() = t("Import backup", "Importar copia", "Importar cópia", "Kopie importieren", "Importer une copie")
    val importAction get() = t("Import", "Importar", "Importar", "Importieren", "Importer")
    val importFailedTitle get() = t(
        "Couldn't import",
        "No se ha podido importar",
        "Não foi possível importar",
        "Import fehlgeschlagen",
        "Échec de l'import",
    )
    val importNotBackup get() = t(
        "That file is not a backup from Chroma.",
        "Ese fichero no es una copia de Chroma.",
        "Esse arquivo não é uma cópia do Chroma.",
        "Diese Datei ist keine Sicherung von Chroma.",
        "Ce fichier n'est pas une copie de Chroma.",
    )
    val importDamaged get() = t(
        "The backup is incomplete or damaged. Your colors weren't touched.",
        "La copia está incompleta o dañada. Tus colores no se han tocado.",
        "A cópia está incompleta ou danificada. Suas cores não foram alteradas.",
        "Die Sicherung ist unvollständig oder beschädigt. Deine Farben wurden nicht verändert.",
        "La copie est incomplète ou endommagée. Tes couleurs n'ont pas été touchées.",
    )
    val importTooNew get() = t(
        "This backup is from a newer version of Chroma. Update the app and try again.",
        "Esta copia es de una versión más nueva de Chroma. Actualiza la app y vuelve a probar.",
        "Esta cópia é de uma versão mais nova do Chroma. Atualize o app e tente de novo.",
        "Diese Sicherung stammt aus einer neueren Version von Chroma. Aktualisiere die App und versuch es erneut.",
        "Cette copie vient d'une version plus récente de Chroma. Mets à jour l'app et réessaie.",
    )
    val importEmpty get() = t(
        "The backup has no colors.",
        "La copia no tiene ningún color.",
        "A cópia não tem nenhuma cor.",
        "Die Sicherung enthält keine Farbe.",
        "La copie ne contient aucune couleur.",
    )
    val exportFailed get() = t(
        "Couldn't save the backup.",
        "No se ha podido guardar la copia.",
        "Não foi possível salvar a cópia.",
        "Die Sicherung konnte nicht gespeichert werden.",
        "Impossible d'enregistrer la copie.",
    )

    // 8. Chroma Pro

    val proTitle get() = t("Chroma Pro", "Chroma Pro", "Chroma Pro", "Chroma Pro", "Chroma Pro")
    val proPoster get() = t(
        "Your year as a poster, in three styles",
        "Tu año en póster, en tres estilos",
        "Seu ano em pôster, em três estilos",
        "Dein Jahr als Poster, in drei Stilen",
        "Ton année en affiche, en trois styles",
    )
    val proYearWidget get() = t("The year widget", "El widget del año", "O widget do ano", "Das Jahres-Widget", "Le widget de l'année")
    val proOnce get() = t(
        "One-time payment, no subscription. Friends are always free.",
        "Pago único, sin suscripción. Los amigos siempre son gratis.",
        "Pagamento único, sem assinatura. Os amigos são sempre grátis.",
        "Einmalzahlung, kein Abo. Freunde sind immer kostenlos.",
        "Paiement unique, sans abonnement. Les amis sont toujours gratuits.",
    )
    val restore get() = t("Restore", "Restaurar", "Restaurar", "Wiederherstellen", "Restaurer")
    val storeUnavailable get() = t(
        "The store is not available right now.",
        "La tienda no está disponible ahora.",
        "A loja não está disponível agora.",
        "Der Store ist gerade nicht verfügbar.",
        "La boutique n'est pas disponible pour le moment.",
    )
    val buyFailed get() = t(
        "The purchase could not be completed.",
        "No se ha podido completar la compra.",
        "Não foi possível concluir a compra.",
        "Der Kauf konnte nicht abgeschlossen werden.",
        "L'achat n'a pas pu être finalisé.",
    )

    // 9. Sharing and the poster

    val share get() = t("Share", "Compartir", "Compartilhar", "Teilen", "Partager")
    val shareColorOnly get() = t("Color only", "Solo el color", "Só a cor", "Nur die Farbe", "La couleur seule")
    val shareWithPhoto get() = t("With the photo", "Con la foto", "Com a foto", "Mit dem Foto", "Avec la photo")
    val saveToPhotos get() = t("Save to Photos", "Guardar en fotos", "Salvar nas fotos", "In Fotos speichern", "Enregistrer la photo")
    val saved get() = t("Saved to your photos.", "Guardada en tus fotos.", "Salva nas suas fotos.", "In deinen Fotos gespeichert.", "Enregistrée dans tes photos.")
    val saveFailed get() = t("Couldn't save.", "No se ha podido guardar.", "Não foi possível salvar.", "Konnte nicht gespeichert werden.", "Impossible d'enregistrer.")
    val posterGrid get() = t("Grid", "Rejilla", "Grade", "Raster", "Grille")
    val posterStrip get() = t("Strip", "Tira", "Faixa", "Streifen", "Bande")
    val posterWallpaper get() = t("Wallpaper", "Fondo de pantalla", "Papel de parede", "Hintergrund", "Fond d'écran")
    val cardTagline get() = t("a color a day", "un color al día", "uma cor por dia", "eine Farbe am Tag", "une couleur par jour")

    // 10. Notification

    val reminderTitle get() = t(
        "What color is today?",
        "¿De qué color es hoy?",
        "De que cor é hoje?",
        "Welche Farbe hat heute?",
        "De quelle couleur est aujourd'hui ?",
    )
    val reminderText get() = t(
        "Look around for a moment.",
        "Mira a tu alrededor un momento.",
        "Olhe ao redor por um momento.",
        "Schau dich einen Moment um.",
        "Regarde autour de toi un instant.",
    )
    val reminderChannel get() = t("Daily reminder", "Recordatorio diario", "Lembrete diário", "Tägliche Erinnerung", "Rappel quotidien")

    // 11. Widgets

    val widgetEmpty get() = t("No color yet", "Aún sin color", "Ainda sem cor", "Noch keine Farbe", "Pas encore de couleur")
    val widgetUnlock get() = t("Tap to turn it on", "Toca para activarlo", "Toque para ativar", "Tippen zum Aktivieren", "Touche pour l'activer")
    val widgetTodayName get() = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    val widgetTodayDescription get() = t("Today's color", "El color de hoy", "A cor de hoje", "Die Farbe von heute", "La couleur du jour")
    val widgetYearName get() = t("The year", "El año", "O ano", "Das Jahr", "L'année")
    val widgetYearDescription get() = t("Your year in colors", "Tu año en colores", "Seu ano em cores", "Dein Jahr in Farben", "Ton année en couleurs")

    // 13. Friends (v1.1)

    val friendsIntro1 get() = t(
        "See the color of your friends' day.",
        "Mira el color del día de tus amigos.",
        "Veja a cor do dia dos seus amigos.",
        "Sieh die Farbe vom Tag deiner Freunde.",
        "Vois la couleur du jour de tes amis.",
    )
    val friendsIntro2 get() = t(
        "Only people you invite, and who say yes.",
        "Solo gente a la que invitas y que dice que sí.",
        "Só pessoas que você convida e que aceitam.",
        "Nur Leute, die du einlädst und die zusagen.",
        "Seulement les personnes que tu invites et qui acceptent.",
    )
    val friendsIntro3 get() = t(
        "No likes, no counts. What you don't share stays on your phone.",
        "Sin likes ni contadores. Lo que no compartes no sale de tu móvil.",
        "Sem curtidas nem contadores. O que você não compartilha não sai do seu celular.",
        "Keine Likes, keine Zahlen. Was du nicht teilst, bleibt auf deinem Handy.",
        "Pas de likes, pas de compteurs. Ce que tu ne partages pas reste sur ton téléphone.",
    )
    val signInApple get() = t("Continue with Apple", "Continuar con Apple", "Continuar com a Apple", "Weiter mit Apple", "Continuer avec Apple")
    val signInGoogle get() = t("Continue with Google", "Continuar con Google", "Continuar com o Google", "Weiter mit Google", "Continuer avec Google")
    val signInFailed get() = t(
        "Couldn't sign in. Try again.",
        "No se ha podido iniciar sesión. Inténtalo de nuevo.",
        "Não foi possível entrar. Tente de novo.",
        "Anmeldung fehlgeschlagen. Versuch es noch einmal.",
        "Connexion impossible. Réessaie.",
    )
    val nameTitle get() = t("Your name for friends", "Tu nombre para tus amigos", "Seu nome para os amigos", "Dein Name für Freunde", "Ton nom pour tes amis")
    val nameHint get() = t(
        "The one your friends know you by.",
        "El que tus amigos reconocen.",
        "O nome pelo qual seus amigos te conhecem.",
        "Der, unter dem deine Freunde dich kennen.",
        "Celui sous lequel tes amis te connaissent.",
    )
    val age16 get() = t("I'm 16 or older", "Tengo 16 años o más", "Tenho 16 anos ou mais", "Ich bin 16 oder älter", "J'ai 16 ans ou plus")
    val continueAction get() = t("Continue", "Continuar", "Continuar", "Weiter", "Continuer")
    val friendsOffline get() = t(
        "Friends can't be reached right now.",
        "Ahora mismo no se llega a Amigos.",
        "Não foi possível acessar Amigos agora.",
        "Freunde sind gerade nicht erreichbar.",
        "Impossible de joindre Amis pour le moment.",
    )
    val retry get() = t("Retry", "Reintentar", "Tentar de novo", "Erneut versuchen", "Réessayer")
    val friendsEmpty get() = t(
        "No friends here yet. Invite someone who knows you.",
        "Aún no hay amigos aquí. Invita a alguien que te conozca.",
        "Ainda não há amigos aqui. Convide alguém que te conheça.",
        "Noch keine Freunde hier. Lade jemanden ein, der dich kennt.",
        "Pas encore d'amis ici. Invite quelqu'un qui te connaît.",
    )
    val sharePrivate get() = t("Private", "Privado", "Privado", "Privat", "Privé")
    val shareLabel get() = t("Your friends see", "Tus amigos ven", "Seus amigos veem", "Deine Freunde sehen", "Tes amis voient")
    val defaultShareRow get() = t("New days", "Días nuevos", "Dias novos", "Neue Tage", "Nouveaux jours")
    val askDefaultShareTitle get() = t(
        "What do your friends see?",
        "¿Qué ven tus amigos?",
        "O que seus amigos veem?",
        "Was sehen deine Freunde?",
        "Que voient tes amis ?",
    )
    val askDefaultShareText get() = t(
        "Choose what new days share. You can change it for each day, and in Settings.",
        "Elige qué comparten los días nuevos. Puedes cambiarlo en cada día, y en Ajustes.",
        "Escolha o que os dias novos compartilham. Dá para mudar em cada dia, e em Ajustes.",
        "Wähle, was neue Tage teilen. Du kannst es für jeden Tag ändern, und in den Einstellungen.",
        "Choisis ce que partagent les nouveaux jours. Tu peux le changer pour chaque jour, et dans Réglages.",
    )
    val sectionFriends get() = t("Friends", "Amigos", "Amigos", "Freunde", "Amis")
    val nameRow get() = t("Your name", "Tu nombre", "Seu nome", "Dein Name", "Ton nom")
    val signOut get() = t("Sign out", "Cerrar sesión", "Sair", "Abmelden", "Se déconnecter")
    val signOutText get() = t(
        "Your friends stay on the account. Signing in again brings them back.",
        "Tus amigos se quedan en la cuenta. Al volver a entrar, vuelven.",
        "Seus amigos ficam na conta. Ao entrar de novo, eles voltam.",
        "Deine Freunde bleiben im Konto. Meldest du dich wieder an, sind sie wieder da.",
        "Tes amis restent sur le compte. En te reconnectant, ils reviennent.",
    )
    val inviteFriend get() = t("Invite a friend", "Invitar a un amigo", "Convidar um amigo", "Freund einladen", "Inviter un ami")
    val inviteRow get() = t("Your invite link", "Tu enlace de invitación", "Seu link de convite", "Dein Einladungslink", "Ton lien d'invitation")
    val inviteText get() = t(
        "Whoever opens it sends you a request. Nothing changes until you accept it.",
        "Quien lo abra te envía una solicitud. Nada cambia hasta que la aceptas.",
        "Quem abrir te envia um pedido. Nada muda até você aceitar.",
        "Wer ihn öffnet, schickt dir eine Anfrage. Nichts ändert sich, bis du sie annimmst.",
        "Qui l'ouvre t'envoie une demande. Rien ne change tant que tu ne l'acceptes pas.",
    )
    val shareLink get() = t("Share link", "Compartir enlace", "Compartilhar link", "Link teilen", "Partager le lien")
    val regenerateLink get() = t("New link", "Enlace nuevo", "Novo link", "Neuer Link", "Nouveau lien")
    val regenerateText get() = t(
        "The current link and its QR stop working. The friends you have stay.",
        "El enlace actual y su QR dejan de valer. Los amigos que ya tienes se quedan.",
        "O link atual e o QR param de funcionar. Os amigos que você já tem continuam.",
        "Der aktuelle Link und sein QR-Code gelten dann nicht mehr. Deine Freunde bleiben.",
        "Le lien actuel et son QR ne marcheront plus. Tes amis restent.",
    )
    val inviteSent get() = t(
        "Request sent. Once it is accepted, you will see each other's colors.",
        "Solicitud enviada. Cuando la acepte, veréis vuestros colores.",
        "Pedido enviado. Quando for aceito, vocês vão ver as cores um do outro.",
        "Anfrage gesendet. Sobald sie angenommen ist, seht ihr eure Farben.",
        "Demande envoyée. Une fois acceptée, vous verrez vos couleurs.",
    )
    val inviteAccepted get() = t("You are friends now.", "Ya sois amigos.", "Agora vocês são amigos.", "Ihr seid jetzt Freunde.", "Vous êtes amis maintenant.")
    val inviteAlready get() = t("You were already friends.", "Ya erais amigos.", "Vocês já eram amigos.", "Ihr seid schon Freunde.", "Vous étiez déjà amis.")
    val inviteSelf get() = t("This is your own link.", "Este es tu propio enlace.", "Este é o seu próprio link.", "Das ist dein eigener Link.", "C'est ton propre lien.")
    val inviteInvalid get() = t(
        "This link no longer works. Ask for a new one.",
        "Este enlace ya no vale. Pide uno nuevo.",
        "Este link não funciona mais. Peça um novo.",
        "Dieser Link gilt nicht mehr. Frag nach einem neuen.",
        "Ce lien ne marche plus. Demande-en un nouveau.",
    )
    val friendLimit get() = t(
        "Chroma keeps circles small: 50 friends at most, and one of you is there.",
        "Chroma mantiene los círculos pequeños: 50 amigos como mucho, y uno de los dos ya ha llegado.",
        "O Chroma mantém os círculos pequenos: no máximo 50 amigos, e um de vocês já chegou lá.",
        "Chroma hält Kreise klein: höchstens 50 Freunde, und einer von euch hat sie erreicht.",
        "Chroma garde les cercles petits : 50 amis au plus, et l'un de vous y est déjà.",
    )
    val feedToday get() = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    val feedYesterday get() = t("Yesterday", "Ayer", "Ontem", "Gestern", "Hier")
    val caughtUp get() = t("You're all caught up.", "Ya estás al día.", "Você está em dia.", "Du bist auf dem Laufenden.", "Tu es à jour.")
    val friendsRow get() = t("Your friends", "Tus amigos", "Seus amigos", "Deine Freunde", "Tes amis")
    val friendYearEmpty get() = t(
        "Nothing shared this year.",
        "Nada compartido este año.",
        "Nada compartilhado este ano.",
        "Dieses Jahr nichts geteilt.",
        "Rien de partagé cette année.",
    )
    val report get() = t("Report", "Reportar", "Denunciar", "Melden", "Signaler")
    val block get() = t("Block", "Bloquear", "Bloquear", "Blockieren", "Bloquer")
    val removeFriend get() = t("Remove friend", "Quitar de amigos", "Remover amigo", "Freund entfernen", "Retirer des amis")
    val reportText get() = t(
        "The card is hidden for you now, and the report reaches Chroma, which acts within 24 hours. Nobody is told who reported it.",
        "La tarjeta se te oculta ya, y el reporte llega a Chroma, que actúa en menos de 24 horas. Nadie sabrá quién lo hizo.",
        "O cartão some para você agora, e a denúncia chega ao Chroma, que age em menos de 24 horas. Ninguém saberá quem denunciou.",
        "Die Karte wird dir sofort ausgeblendet, und die Meldung geht an Chroma, das innerhalb von 24 Stunden handelt. Niemand erfährt, wer gemeldet hat.",
        "La carte est masquée pour toi dès maintenant, et le signalement arrive à Chroma, qui agit en moins de 24 heures. Personne ne saura qui l'a signalée.",
    )
    val termsAgree get() = t(
        "By continuing you accept the terms of use: zero tolerance for objectionable content and abusive users.",
        "Al continuar aceptas los términos de uso: tolerancia cero con el contenido inaceptable y con quien abuse.",
        "Ao continuar, você aceita os termos de uso: tolerância zero com conteúdo inaceitável e com abusos.",
        "Wenn du fortfährst, akzeptierst du die Nutzungsbedingungen: null Toleranz für anstößige Inhalte und Missbrauch.",
        "En continuant, tu acceptes les conditions d'utilisation : tolérance zéro pour les contenus inacceptables et les abus.",
    )
    val termsRow get() = t("Terms of use", "Términos de uso", "Termos de uso", "Nutzungsbedingungen", "Conditions d'utilisation")
    val requestsTitle get() = t("Requests", "Solicitudes", "Pedidos", "Anfragen", "Demandes")
    val accept get() = t("Accept", "Aceptar", "Aceitar", "Annehmen", "Accepter")
    val ignore get() = t("Ignore", "Ignorar", "Ignorar", "Ignorieren", "Ignorer")

    // 12. Accessibility

    val a11yBack get() = t("Back", "Volver", "Voltar", "Zurück", "Retour")
    val a11yPreviousYear get() = t("Previous year", "Año anterior", "Ano anterior", "Vorheriges Jahr", "Année précédente")
    val a11yNextYear get() = t("Next year", "Año siguiente", "Próximo ano", "Nächstes Jahr", "Année suivante")
    val a11yShare get() = t("Share", "Compartir", "Compartilhar", "Teilen", "Partager")
    val a11ySettings get() = t("Settings", "Ajustes", "Ajustes", "Einstellungen", "Réglages")
    val a11yClose get() = t("Close", "Cerrar", "Fechar", "Schließen", "Fermer")
    val a11yMore get() = t("More options", "Más opciones", "Mais opções", "Weitere Optionen", "Plus d'options")
    val a11yPhoto get() = t("Open the photo", "Abrir la foto", "Abrir a foto", "Foto öffnen", "Ouvrir la photo")
    val a11ySelected get() = t("selected", "elegido", "escolhida", "ausgewählt", "choisie")

    // --- With parameters ----------------------------------------------------------------------
    // Dates are written by hand, never with a platform formatter, so a day reads the same on both
    // systems. Plurals: the singular only for 1.

    fun shortDate(d: LocalDate): String {
        val m = monthNames()[d.month.ordinal]
        return t("$m ${d.day}", "${d.day} de $m", "${d.day} de $m", "${d.day}. $m", "${d.day} $m")
    }

    fun longDate(d: LocalDate): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal].replaceFirstChar { it.uppercase() }
        return w + t(", ", ", ", ", ", ", ", " ") + shortDate(d)
    }

    fun dayMonthYear(d: LocalDate) = shortDate(d) + yearSuffix(d.year)

    fun longDateWithYear(d: LocalDate) = longDate(d) + yearSuffix(d.year)

    fun abbrDateWithYear(d: LocalDate): String {
        val m = monthShort()[d.month.ordinal]
        val y = d.year
        return t("$m ${d.day}, $y", "${d.day} $m $y", "${d.day} $m $y", "${d.day}. $m $y", "${d.day} $m $y")
    }

    private fun yearSuffix(y: Int) = t(", $y", " de $y", " de $y", " $y", " $y")

    /** Always 24 hours, in the five languages. */
    fun clock(hour: Int, minute: Int) =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    fun counter(n: Int, max: Int) = "$n/$max"

    fun reminderAt(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t("At $time", "A las $time", "Às $time", "Um $time", "À $time")
    }

    fun offerReminder(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t(
            "Remind you every day at $time?",
            "¿Te lo recuerdo cada día a las $time?",
            "Quer que eu lembre você todo dia às $time?",
            "Soll ich dich jeden Tag um $time erinnern?",
            "Je te le rappelle tous les jours à $time ?",
        )
    }

    fun lastBackup(d: LocalDate): String {
        val date = abbrDateWithYear(d)
        return t("Last backup: $date", "Última copia: $date", "Última cópia: $date", "Letzte Sicherung: $date", "Dernière copie : $date")
    }

    fun removeFriendText(name: String) = t(
        "$name will not be told. You both stop seeing each other's days.",
        "$name no recibirá ningún aviso. Dejaréis de ver vuestros días.",
        "$name não será avisado. Vocês deixam de ver os dias um do outro.",
        "$name erfährt nichts davon. Ihr seht eure Tage nicht mehr.",
        "$name n'en saura rien. Vous ne verrez plus vos jours.",
    )

    fun blockText(name: String) = t(
        "$name stops seeing you and cannot invite you again. Nobody is told.",
        "$name deja de verte y no podrá volver a invitarte. Nadie recibe ningún aviso.",
        "$name deixa de te ver e não poderá te convidar de novo. Ninguém é avisado.",
        "$name sieht dich nicht mehr und kann dich nicht wieder einladen. Niemand wird benachrichtigt.",
        "$name ne te verra plus et ne pourra plus t'inviter. Personne n'est prévenu.",
    )

    fun inviteMessage(link: String) = t(
        "Add me on Chroma: $link",
        "Agrégame en Chroma: $link",
        "Me adicione no Chroma: $link",
        "Füg mich in Chroma hinzu: $link",
        "Ajoute-moi sur Chroma : $link",
    )

    fun version(v: String) = t("Version $v", "Versión $v", "Versão $v", "Version $v", "Version $v")

    /** The phone wins every day both have, so only the new ones are worth counting. */
    fun importSummary(added: Int, kept: Int): String {
        val new = if (added == 1) {
            t("1 new day", "1 día nuevo", "1 dia novo", "1 neuen Tag", "1 nouveau jour")
        } else {
            t("$added new days", "$added días nuevos", "$added dias novos", "$added neue Tage", "$added nouveaux jours")
        }
        val head = t("The backup brings $new.", "La copia trae $new.", "A cópia traz $new.", "Die Sicherung bringt $new.", "La copie apporte $new.")
        val tail = if (kept == 0) {
            t(" Nothing gets deleted.", " No se borra nada.", " Nada é apagado.", " Es wird nichts gelöscht.", " Rien n'est supprimé.")
        } else {
            t(
                " Days already on this phone stay as they are.",
                " Los días que ya están en este teléfono se quedan como están.",
                " Os dias que já estão neste telefone ficam como estão.",
                " Tage, die schon auf diesem Handy sind, bleiben, wie sie sind.",
                " Les jours déjà sur ce téléphone restent tels quels.",
            )
        }
        return head + tail
    }

    fun importDone(n: Int) = when (n) {
        0 -> t(
            "Up to date: there was nothing new.",
            "Al día: no había nada nuevo.",
            "Em dia: não havia nada novo.",
            "Aktuell: es gab nichts Neues.",
            "À jour : il n'y avait rien de nouveau.",
        )
        1 -> t("1 day added.", "1 día añadido.", "1 dia adicionado.", "1 Tag hinzugefügt.", "1 jour ajouté.")
        else -> t("$n days added.", "$n días añadidos.", "$n dias adicionados.", "$n Tage hinzugefügt.", "$n jours ajoutés.")
    }

    fun buy(price: String) =
        t("Buy for $price", "Comprar por $price", "Comprar por $price", "Für $price kaufen", "Acheter pour $price")

    fun a11ySwatch(key: String, selected: Boolean) = colorName(key) + if (selected) ", $a11ySelected" else ""

    fun a11yDay(d: LocalDate, key: String?): String {
        val state = key?.let(::colorName)
            ?: t("no color", "sin color", "sem cor", "keine Farbe", "pas de couleur")
        return "${shortDate(d)}, $state"
    }
}
