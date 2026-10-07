package com.baltajmn.color.i18n

import com.baltajmn.color.color.colorLabel
import kotlinx.datetime.LocalDate

/** Two-letter code of the device language. */
expect fun systemLanguage(): String

/** The languages the app ships. Anything else falls back to English. */
internal val SUPPORTED = listOf("en", "es", "pt", "de", "fr", "it", "nl", "pl", "ru", "tr", "id", "ja", "ko")

internal fun normalizeLanguage(code: String): String {
    // Android still reports Indonesian by its old ISO code.
    val two = code.take(2).lowercase().let { if (it == "in") "id" else it }
    return two.takeIf { it in SUPPORTED } ?: "en"
}

/**
 * Every user-facing string, in one table. This file is the only copy (docs/textos.md).
 *
 * Not Compose Resources on purpose: part of these strings are drawn outside a `@Composable` (a
 * BroadcastReceiver, a Glance widget, the Canvas of the share card, a notification builder).
 *
 * The language is read on every call: Android changes it (per app since 13) by recreating the
 * Activity, not the process, so a value kept from the first access would stay in the old one. The
 * tests set [lang] to go through all of them.
 */
object S {

    private var forced: String? = null

    internal var lang: String
        get() = forced ?: normalizeLanguage(systemLanguage())
        set(value) {
            forced = value
        }

    private fun t(
        en: String, es: String, pt: String, de: String, fr: String,
        it: String, nl: String, pl: String, ru: String, tr: String, id: String, ja: String, ko: String,
    ): String = when (lang) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        "it" -> it
        "nl" -> nl
        "pl" -> pl
        "ru" -> ru
        "tr" -> tr
        "id" -> id
        "ja" -> ja
        "ko" -> ko
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
        "gennaio, febbraio, marzo, aprile, maggio, giugno, luglio, agosto, settembre, ottobre, novembre, dicembre",
        "januari, februari, maart, april, mei, juni, juli, augustus, september, oktober, november, december",
        "styczeń, luty, marzec, kwiecień, maj, czerwiec, lipiec, sierpień, wrzesień, październik, listopad, grudzień",
        "январь, февраль, март, апрель, май, июнь, июль, август, сентябрь, октябрь, ноябрь, декабрь",
        "Ocak, Şubat, Mart, Nisan, Mayıs, Haziran, Temmuz, Ağustos, Eylül, Ekim, Kasım, Aralık",
        "Januari, Februari, Maret, April, Mei, Juni, Juli, Agustus, September, Oktober, November, Desember",
        "1月, 2月, 3月, 4月, 5月, 6月, 7月, 8月, 9月, 10月, 11月, 12月",
        "1월, 2월, 3월, 4월, 5월, 6월, 7월, 8월, 9월, 10월, 11월, 12월",
    ).split(", ")

    fun monthShort(): List<String> = t(
        "Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec",
        "ene, feb, mar, abr, may, jun, jul, ago, sept, oct, nov, dic",
        "jan, fev, mar, abr, mai, jun, jul, ago, set, out, nov, dez",
        "Jan., Feb., März, Apr., Mai, Juni, Juli, Aug., Sept., Okt., Nov., Dez.",
        "janv., févr., mars, avr., mai, juin, juil., août, sept., oct., nov., déc.",
        "gen, feb, mar, apr, mag, giu, lug, ago, set, ott, nov, dic",
        "jan, feb, mrt, apr, mei, jun, jul, aug, sep, okt, nov, dec",
        "sty, lut, mar, kwi, maj, cze, lip, sie, wrz, paź, lis, gru",
        "янв., февр., мар., апр., мая, июн., июл., авг., сент., окт., нояб., дек.",
        "Oca, Şub, Mar, Nis, May, Haz, Tem, Ağu, Eyl, Eki, Kas, Ara",
        "Jan, Feb, Mar, Apr, Mei, Jun, Jul, Agu, Sep, Okt, Nov, Des",
        "1月, 2月, 3月, 4月, 5月, 6月, 7月, 8月, 9月, 10月, 11月, 12月",
        "1월, 2월, 3월, 4월, 5월, 6월, 7월, 8월, 9월, 10월, 11월, 12월",
    ).split(", ")

    fun monthInitials(): List<String> = t(
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "E, F, M, A, M, J, J, A, S, O, N, D",
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "G, F, M, A, M, G, L, A, S, O, N, D",
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "S, L, M, K, M, C, L, S, W, P, L, G",
        "Я, Ф, М, А, М, И, И, А, С, О, Н, Д",
        "O, Ş, M, N, M, H, T, A, E, E, K, A",
        "J, F, M, A, M, J, J, A, S, O, N, D",
        "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12",
        "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12",
    ).split(", ")

    fun weekdayNames(): List<String> = t(
        "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday",
        "lunes, martes, miércoles, jueves, viernes, sábado, domingo",
        "segunda-feira, terça-feira, quarta-feira, quinta-feira, sexta-feira, sábado, domingo",
        "Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag, Sonntag",
        "lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche",
        "lunedì, martedì, mercoledì, giovedì, venerdì, sabato, domenica",
        "maandag, dinsdag, woensdag, donderdag, vrijdag, zaterdag, zondag",
        "poniedziałek, wtorek, środa, czwartek, piątek, sobota, niedziela",
        "понедельник, вторник, среда, четверг, пятница, суббота, воскресенье",
        "Pazartesi, Salı, Çarşamba, Perşembe, Cuma, Cumartesi, Pazar",
        "Senin, Selasa, Rabu, Kamis, Jumat, Sabtu, Minggu",
        "月曜日, 火曜日, 水曜日, 木曜日, 金曜日, 土曜日, 日曜日",
        "월요일, 화요일, 수요일, 목요일, 금요일, 토요일, 일요일",
    ).split(", ")

    fun weekdayShort(): List<String> = t(
        "Mon, Tue, Wed, Thu, Fri, Sat, Sun",
        "lun, mar, mié, jue, vie, sáb, dom",
        "seg, ter, qua, qui, sex, sáb, dom",
        "Mo, Di, Mi, Do, Fr, Sa, So",
        "lun, mar, mer, jeu, ven, sam, dim",
        "lun, mar, mer, gio, ven, sab, dom",
        "ma, di, wo, do, vr, za, zo",
        "pon, wt, śr, czw, pt, sob, niedz",
        "пн, вт, ср, чт, пт, сб, вс",
        "Pzt, Sal, Çar, Per, Cum, Cmt, Paz",
        "Sen, Sel, Rab, Kam, Jum, Sab, Min",
        "月, 火, 水, 木, 金, 土, 日",
        "월, 화, 수, 목, 금, 토, 일",
    ).split(", ")

    // 2. Navigation

    val navToday get() = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün", "Hari ini", "今日", "오늘")
    val navYear get() = t(
        "My year",
        "Mi año",
        "Meu ano",
        "Mein Jahr",
        "Mon année",
        "Il mio anno",
        "Mijn jaar",
        "Mój rok",
        "Мой год",
        "Yılım",
        "Tahunku",
        "わたしの1年",
        "나의 한 해",
    )
    val navFriends get() = t(
        "Friends",
        "Amigos",
        "Amigos",
        "Freunde",
        "Amis",
        "Amici",
        "Vrienden",
        "Znajomi",
        "Друзья",
        "Arkadaşlar",
        "Teman",
        "友だち",
        "친구",
    )

    // 3. Today

    val todayPrompt get() = t(
        "What color is today?",
        "¿De qué color es hoy?",
        "De que cor é hoje?",
        "Welche Farbe hat heute?",
        "De quelle couleur est aujourd'hui ?",
        "Di che colore è oggi?",
        "Welke kleur heeft vandaag?",
        "Jakiego koloru jest dziś?",
        "Какого цвета сегодня?",
        "Bugün ne renk?",
        "Hari ini warna apa?",
        "今日は何色？",
        "오늘은 무슨 색일까요?",
    )
    val firstHelp get() = t(
        "Take a photo of something that caught your eye. You'll pick a color from it.",
        "Haz una foto de algo que te llame la atención. De ella sacarás un color.",
        "Tire uma foto de algo que chamou sua atenção. Dela vai sair uma cor.",
        "Fotografiere etwas, das dir ins Auge fällt. Daraus wählst du eine Farbe.",
        "Prends en photo quelque chose qui t'attire l'œil. Tu en tireras une couleur.",
        "Fotografa qualcosa che ti ha colpito. Da lì sceglierai un colore.",
        "Maak een foto van iets dat je opviel. Daaruit kies je een kleur.",
        "Zrób zdjęcie czegoś, co przyciągnęło twój wzrok. Wybierzesz z niego kolor.",
        "Сфотографируй то, что бросилось в глаза. Из снимка ты выберешь цвет.",
        "Gözüne çarpan bir şeyin fotoğrafını çek. Ondan bir renk seçeceksin.",
        "Foto sesuatu yang menarik perhatianmu. Dari situ kamu akan memilih satu warna.",
        "目にとまったものを撮ってください。その写真から色をひとつ選びます。",
        "눈길을 끈 무언가를 찍어 보세요. 그 사진에서 색을 하나 고르게 돼요.",
    )
    val takePhoto get() = t(
        "Take a photo",
        "Hacer una foto",
        "Tirar uma foto",
        "Foto aufnehmen",
        "Prendre une photo",
        "Scatta una foto",
        "Maak een foto",
        "Zrób zdjęcie",
        "Сделать фото",
        "Fotoğraf çek",
        "Ambil foto",
        "写真を撮る",
        "사진 찍기",
    )
    val fromGallery get() = t(
        "Choose a photo from today",
        "Elegir una foto de hoy",
        "Escolher uma foto de hoje",
        "Ein Foto von heute wählen",
        "Choisir une photo du jour",
        "Scegli una foto di oggi",
        "Kies een foto van vandaag",
        "Wybierz zdjęcie z dzisiaj",
        "Выбрать фото, снятое сегодня",
        "Bugün çekilmiş bir fotoğraf seç",
        "Pilih foto dari hari ini",
        "今日撮った写真を選ぶ",
        "오늘 찍은 사진 고르기",
    )
    val pickColor get() = t(
        "Tap your color. Hold one to see its name.",
        "Toca tu color. Mantén uno para ver su nombre.",
        "Toque na sua cor. Segure uma para ver o nome.",
        "Tippe auf deine Farbe. Halte eine gedrückt, um ihren Namen zu sehen.",
        "Touche ta couleur. Maintiens-en une pour voir son nom.",
        "Tocca il tuo colore. Tienine premuto uno per vederne il nome.",
        "Tik op je kleur. Houd er een vast om de naam te zien.",
        "Dotknij swojego koloru. Przytrzymaj, by zobaczyć jego nazwę.",
        "Нажми на свой цвет. Удерживай, чтобы увидеть название.",
        "Rengine dokun. Adını görmek için basılı tut.",
        "Ketuk warnamu. Tahan untuk melihat namanya.",
        "色をタップしてください。長押しで名前が見られます。",
        "색을 눌러 보세요. 길게 누르면 이름이 보여요.",
    )
    val galleryNotToday get() = t(
        "That photo is from another day. Today's color comes from today.",
        "Esa foto es de otro día. El color de hoy sale de hoy.",
        "Essa foto é de outro dia. A cor de hoje vem de hoje.",
        "Dieses Foto ist von einem anderen Tag. Die Farbe von heute kommt von heute.",
        "Cette photo date d'un autre jour. La couleur du jour vient d'aujourd'hui.",
        "Questa foto è di un altro giorno. Il colore di oggi viene da oggi.",
        "Die foto is van een andere dag. De kleur van vandaag komt van vandaag.",
        "To zdjęcie jest z innego dnia. Dzisiejszy kolor pochodzi z dzisiaj.",
        "Это фото сделано в другой день. Цвет дня берётся из этого дня.",
        "Bu fotoğraf başka bir günden. Bugünün rengi bugünden gelir.",
        "Foto itu dari hari lain. Warna hari ini datang dari hari ini.",
        "その写真は別の日のものです。今日の色は今日の写真から選びます。",
        "다른 날 찍은 사진이에요. 오늘의 색은 오늘에서 나와요.",
    )
    val photoUnreadable get() = t(
        "Couldn't read that photo.",
        "No se ha podido leer esa foto.",
        "Não foi possível ler essa foto.",
        "Dieses Foto konnte nicht gelesen werden.",
        "Impossible de lire cette photo.",
        "Impossibile leggere questa foto.",
        "Die foto kon niet worden gelezen.",
        "Nie udało się odczytać tego zdjęcia.",
        "Не удалось прочитать это фото.",
        "Bu fotoğraf okunamadı.",
        "Foto itu tidak bisa dibaca.",
        "その写真を読み込めませんでした。",
        "그 사진을 읽을 수 없어요.",
    )
    val cameraDenied get() = t(
        "Chroma isn't allowed to use the camera. You can allow it in Settings, or choose from today's photos.",
        "Chroma no tiene permiso para usar la cámara. Puedes darlo en Ajustes, o elegir de las fotos de hoy.",
        "O Chroma não tem permissão para usar a câmera. Você pode permitir em Ajustes, ou escolher das fotos de hoje.",
        "Chroma darf die Kamera nicht nutzen. Du kannst es in den Einstellungen erlauben oder aus den Fotos von heute wählen.",
        "Chroma n'a pas accès à l'appareil photo. Tu peux l'autoriser dans Réglages, ou choisir parmi les photos du jour.",
        "Chroma non ha accesso alla fotocamera. Puoi consentirlo in Impostazioni, oppure scegliere tra le foto di oggi.",
        "Chroma mag de camera niet gebruiken. Je kunt het toestaan in Instellingen, of kiezen uit de foto's van vandaag.",
        "Chroma nie ma dostępu do aparatu. Możesz na to pozwolić w Ustawieniach albo wybrać z dzisiejszych zdjęć.",
        "У Chroma нет доступа к камере. Его можно дать в Настройках или выбрать из сегодняшних фото.",
        "Chroma'nın kamerayı kullanma izni yok. Ayarlar'dan izin verebilir ya da bugünün fotoğraflarından seçebilirsin.",
        "Chroma tidak diizinkan memakai kamera. Kamu bisa mengizinkannya di Pengaturan, atau memilih dari foto hari ini.",
        "Chromaはカメラを使えません。設定で許可するか、今日の写真から選んでください。",
        "Chroma에 카메라 권한이 없어요. 설정에서 허용하거나 오늘 찍은 사진에서 골라 주세요.",
    )
    val photoNotSaved get() = t(
        "The phone is out of space: the photo couldn't be saved.",
        "El teléfono no tiene espacio: no se ha podido guardar la foto.",
        "O telefone está sem espaço: não foi possível salvar a foto.",
        "Das Handy hat keinen Platz mehr: Das Foto konnte nicht gespeichert werden.",
        "Le téléphone n'a plus de place : la photo n'a pas pu être enregistrée.",
        "Il telefono non ha più spazio: impossibile salvare la foto.",
        "De telefoon heeft geen ruimte meer: de foto kon niet worden bewaard.",
        "W telefonie brakuje miejsca: nie udało się zapisać zdjęcia.",
        "В телефоне закончилось место: фото не удалось сохранить.",
        "Telefonda yer kalmadı: fotoğraf kaydedilemedi.",
        "Ruang ponsel penuh: foto tidak bisa disimpan.",
        "端末の空き容量が足りません。写真を保存できませんでした。",
        "휴대폰 저장 공간이 부족해서 사진을 저장하지 못했어요.",
    )
    val captureFailed get() = t(
        "No app on this phone can open the camera or your photos.",
        "Ninguna app de este móvil puede abrir la cámara o tus fotos.",
        "Nenhum app deste celular consegue abrir a câmera ou suas fotos.",
        "Keine App auf diesem Handy kann die Kamera oder deine Fotos öffnen.",
        "Aucune app de ce téléphone ne peut ouvrir l'appareil photo ou tes photos.",
        "Nessuna app su questo telefono può aprire la fotocamera o le tue foto.",
        "Geen app op deze telefoon kan de camera of je foto's openen.",
        "Żadna aplikacja na tym telefonie nie może otworzyć aparatu ani twoich zdjęć.",
        "На этом телефоне нет приложения, которое откроет камеру или фото.",
        "Bu telefonda kamerayı veya fotoğraflarını açabilecek bir uygulama yok.",
        "Tidak ada aplikasi di ponsel ini yang bisa membuka kamera atau fotomu.",
        "この端末には、カメラや写真を開けるアプリがありません。",
        "이 휴대폰에는 카메라나 사진을 열 수 있는 앱이 없어요.",
    )
    val copied get() = t(
        "Copied",
        "Copiado",
        "Copiado",
        "Kopiert",
        "Copié",
        "Copiato",
        "Gekopieerd",
        "Skopiowano",
        "Скопировано",
        "Kopyalandı",
        "Disalin",
        "コピーしました",
        "복사했어요",
    )
    val a11yCopyHex get() = t(
        "Copy the code",
        "Copiar el código",
        "Copiar o código",
        "Code kopieren",
        "Copier le code",
        "Copia il codice",
        "Kopieer de code",
        "Kopiuj kod",
        "Скопировать код",
        "Kodu kopyala",
        "Salin kode",
        "コードをコピー",
        "코드 복사",
    )
    val addWord get() = t(
        "Add a word",
        "Añadir una palabra",
        "Adicionar uma palavra",
        "Ein Wort hinzufügen",
        "Ajouter un mot",
        "Aggiungi una parola",
        "Voeg een woord toe",
        "Dodaj słowo",
        "Добавить слово",
        "Bir kelime ekle",
        "Tambah satu kata",
        "ひとこと添える",
        "한 단어 더하기",
    )
    val wordPlaceholder get() = t(
        "One word for today",
        "Una palabra para hoy",
        "Uma palavra para hoje",
        "Ein Wort für heute",
        "Un mot pour aujourd'hui",
        "Una parola per oggi",
        "Eén woord voor vandaag",
        "Jedno słowo na dziś",
        "Одно слово на сегодня",
        "Bugün için bir kelime",
        "Satu kata untuk hari ini",
        "今日のひとこと",
        "오늘의 한 단어",
    )
    val retakePhoto get() = t(
        "Another photo",
        "Otra foto",
        "Outra foto",
        "Anderes Foto",
        "Une autre photo",
        "Un'altra foto",
        "Andere foto",
        "Inne zdjęcie",
        "Другое фото",
        "Başka fotoğraf",
        "Foto lain",
        "別の写真",
        "다른 사진",
    )
    val deleteDay get() = t(
        "Delete this day",
        "Borrar este día",
        "Excluir este dia",
        "Diesen Tag löschen",
        "Supprimer ce jour",
        "Elimina questo giorno",
        "Verwijder deze dag",
        "Usuń ten dzień",
        "Удалить этот день",
        "Bu günü sil",
        "Hapus tanggal ini",
        "この日を削除",
        "이 날 삭제",
    )
    val noticeSaveFailed get() = t(
        "Couldn't save. I'll try again with your next change.",
        "No se ha podido guardar. Lo intento otra vez con tu próximo cambio.",
        "Não foi possível salvar. Vou tentar de novo na sua próxima alteração.",
        "Konnte nicht gespeichert werden. Ich versuche es bei deiner nächsten Änderung erneut.",
        "Impossible d'enregistrer. Je réessaierai avec ta prochaine modification.",
        "Impossibile salvare. Riprovo alla tua prossima modifica.",
        "Opslaan is mislukt. Ik probeer het opnieuw bij je volgende wijziging.",
        "Nie udało się zapisać. Spróbuję ponownie przy następnej zmianie.",
        "Не удалось сохранить. Попробую снова при следующем изменении.",
        "Kaydedilemedi. Bir sonraki değişikliğinde yeniden deneyeceğim.",
        "Gagal menyimpan. Akan dicoba lagi saat kamu mengubah sesuatu.",
        "保存できませんでした。次に変更したときにもう一度試します。",
        "저장하지 못했어요. 다음에 바꿀 때 다시 시도할게요.",
    )
    val noticeCorrupt get() = t(
        "Couldn't read your colors. The files were saved separately and nothing was deleted.",
        "No se han podido leer tus colores. Los ficheros se han guardado aparte y no se ha borrado nada.",
        "Não foi possível ler suas cores. Os arquivos foram guardados à parte e nada foi apagado.",
        "Deine Farben konnten nicht gelesen werden. Die Dateien wurden separat gesichert, gelöscht wurde nichts.",
        "Impossible de lire tes couleurs. Les fichiers ont été sauvegardés à part, rien n'a été supprimé.",
        "Impossibile leggere i tuoi colori. I file sono stati messi da parte e non è stato eliminato nulla.",
        "Je kleuren konden niet worden gelezen. De bestanden zijn apart bewaard en er is niets verwijderd.",
        "Nie udało się odczytać twoich kolorów. Pliki zapisano osobno i nic nie zostało usunięte.",
        "Не удалось прочитать твои цвета. Файлы сохранены отдельно, ничего не удалено.",
        "Renklerin okunamadı. Dosyalar ayrı bir yere kaydedildi, hiçbir şey silinmedi.",
        "Warnamu tidak bisa dibaca. Berkasnya disimpan terpisah dan tidak ada yang dihapus.",
        "色のデータを読み込めませんでした。ファイルは別に保存してあり、何も削除されていません。",
        "색 기록을 읽지 못했어요. 파일은 따로 보관했고 아무것도 지우지 않았어요.",
    )
    val noticeBackup get() = t(
        "A month of colors. Save a copy off your phone?",
        "Un mes de colores. ¿Guardas una copia fuera del teléfono?",
        "Um mês de cores. Quer guardar uma cópia fora do telefone?",
        "Ein Monat voller Farben. Sicherst du eine Kopie außerhalb des Handys?",
        "Un mois de couleurs. Tu gardes une copie hors du téléphone ?",
        "Un mese di colori. Salvi una copia fuori dal telefono?",
        "Een maand vol kleuren. Een kopie buiten je telefoon bewaren?",
        "Miesiąc kolorów. Zapisać kopię poza telefonem?",
        "Месяц цветов. Сохранить копию вне телефона?",
        "Bir aylık renk birikti. Telefonun dışında bir yedek saklamak ister misin?",
        "Sebulan penuh warna. Simpan cadangan di luar ponselmu?",
        "1か月分の色がたまりました。端末の外にコピーを保存しますか？",
        "한 달 치 색이 모였어요. 휴대폰 밖에 사본을 저장할까요?",
    )
    val makeBackup get() = t(
        "Make a backup",
        "Hacer copia",
        "Fazer cópia",
        "Kopie erstellen",
        "Faire une copie",
        "Fai una copia",
        "Maak een back-up",
        "Zrób kopię",
        "Сделать копию",
        "Yedek al",
        "Buat cadangan",
        "バックアップする",
        "백업하기",
    )

    // 4. My year

    val viewGrid get() = t("Grid", "Rejilla", "Grade", "Raster", "Grille", "Griglia", "Raster", "Siatka", "Сетка", "Izgara", "Kisi", "グリッド", "격자")
    val viewStrip get() = t("Strip", "Tira", "Faixa", "Streifen", "Bande", "Striscia", "Strook", "Pasek", "Полоса", "Şerit", "Pita", "ストリップ", "띠")
    val yearEmpty get() = t(
        "Your year will fill in, one color at a time.",
        "Tu año se irá llenando, color a color.",
        "Seu ano vai se preenchendo, cor por cor.",
        "Dein Jahr füllt sich, Farbe für Farbe.",
        "Ton année se remplira, couleur après couleur.",
        "Il tuo anno si riempirà, un colore alla volta.",
        "Je jaar vult zich, kleur voor kleur.",
        "Twój rok będzie się wypełniał, kolor po kolorze.",
        "Твой год будет заполняться, цвет за цветом.",
        "Yılın renk renk dolacak.",
        "Tahunmu akan terisi, satu warna demi satu warna.",
        "1色ずつ、あなたの1年が埋まっていきます。",
        "한 색씩, 당신의 한 해가 채워져요.",
    )
    val stats get() = t(
        "In words",
        "En palabras",
        "Em palavras",
        "In Worten",
        "En mots",
        "In parole",
        "In woorden",
        "W słowach",
        "В словах",
        "Kelimelerle",
        "Dalam kata",
        "ことばで",
        "글로 보기",
    )
    val statsEmpty get() = t(
        "Too few days yet to tell anything.",
        "Aún hay pocos días para contar nada.",
        "Ainda há poucos dias para contar algo.",
        "Noch zu wenige Tage, um etwas zu erzählen.",
        "Encore trop peu de jours pour raconter quoi que ce soit.",
        "Ancora troppo pochi giorni per raccontare qualcosa.",
        "Nog te weinig dagen om iets te vertellen.",
        "Za mało dni, żeby coś opowiedzieć.",
        "Пока слишком мало дней, чтобы что-то рассказать.",
        "Bir şey anlatmak için henüz gün çok az.",
        "Harinya masih terlalu sedikit untuk bercerita.",
        "まだ日数が少なくて、語れることがありません。",
        "아직 이야기할 만큼 날이 쌓이지 않았어요.",
    )
    val poster get() = t("Poster", "Póster", "Pôster", "Poster", "Affiche", "Poster", "Poster", "Plakat", "Постер", "Poster", "Poster", "ポスター", "포스터")

    // 5. Open day

    val deleteTitle get() = t(
        "Delete this day?",
        "¿Borrar este día?",
        "Excluir este dia?",
        "Diesen Tag löschen?",
        "Supprimer ce jour ?",
        "Eliminare questo giorno?",
        "Deze dag verwijderen?",
        "Usunąć ten dzień?",
        "Удалить этот день?",
        "Bu gün silinsin mi?",
        "Hapus tanggal ini?",
        "この日を削除しますか？",
        "이 날을 삭제할까요?",
    )
    val deleteText get() = t(
        "The color and its photo will be deleted. This cannot be undone.",
        "Se borran el color y su foto. No se puede deshacer.",
        "A cor e a foto serão excluídas. Não é possível desfazer.",
        "Die Farbe und ihr Foto werden gelöscht. Das lässt sich nicht rückgängig machen.",
        "La couleur et sa photo seront supprimées. Cette action est irréversible.",
        "Il colore e la sua foto verranno eliminati. Non si può annullare.",
        "De kleur en de foto worden verwijderd. Dit kan niet ongedaan worden gemaakt.",
        "Kolor i jego zdjęcie zostaną usunięte. Tego nie można cofnąć.",
        "Цвет и его фото будут удалены. Это нельзя отменить.",
        "Renk ve fotoğrafı silinecek. Bu geri alınamaz.",
        "Warna dan fotonya akan dihapus. Ini tidak bisa dibatalkan.",
        "色と写真が削除されます。元に戻すことはできません。",
        "색과 사진이 삭제돼요. 되돌릴 수 없어요.",
    )
    val delete get() = t(
        "Delete",
        "Borrar",
        "Excluir",
        "Löschen",
        "Supprimer",
        "Elimina",
        "Verwijderen",
        "Usuń",
        "Удалить",
        "Sil",
        "Hapus",
        "削除",
        "삭제",
    )

    // 6. Settings

    val settingsTitle get() = t(
        "Settings",
        "Ajustes",
        "Ajustes",
        "Einstellungen",
        "Réglages",
        "Impostazioni",
        "Instellingen",
        "Ustawienia",
        "Настройки",
        "Ayarlar",
        "Pengaturan",
        "設定",
        "설정",
    )
    val sectionReminder get() = t(
        "Reminder",
        "Recordatorio",
        "Lembrete",
        "Erinnerung",
        "Rappel",
        "Promemoria",
        "Herinnering",
        "Przypomnienie",
        "Напоминание",
        "Hatırlatıcı",
        "Pengingat",
        "リマインダー",
        "알림",
    )
    val sectionCard get() = t("Card", "Tarjeta", "Cartão", "Karte", "Carte", "Card", "Kaart", "Karta", "Карточка", "Kart", "Kartu", "カード", "카드")
    val sectionPrivacy get() = t(
        "Privacy",
        "Privacidad",
        "Privacidade",
        "Datenschutz",
        "Confidentialité",
        "Privacy",
        "Privacy",
        "Prywatność",
        "Конфиденциальность",
        "Gizlilik",
        "Privasi",
        "プライバシー",
        "개인정보",
    )
    val sectionBackup get() = t(
        "Backup",
        "Copia",
        "Cópia",
        "Sicherung",
        "Sauvegarde",
        "Copia",
        "Back-up",
        "Kopia zapasowa",
        "Резервная копия",
        "Yedek",
        "Cadangan",
        "バックアップ",
        "백업",
    )
    val sectionPro get() = t(
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
    )
    val sectionMoreApps get() = t(
        "More apps",
        "Más apps",
        "Mais apps",
        "Weitere Apps",
        "Plus d'apps",
        "Altre app",
        "Meer apps",
        "Więcej aplikacji",
        "Другие приложения",
        "Diğer uygulamalar",
        "Aplikasi lain",
        "ほかのアプリ",
        "다른 앱",
    )
    val sectionAbout get() = t(
        "About",
        "Acerca de",
        "Sobre",
        "Über",
        "À propos",
        "Informazioni",
        "Over",
        "O aplikacji",
        "О приложении",
        "Hakkında",
        "Tentang",
        "このアプリについて",
        "정보",
    )
    val reminderRow get() = t(
        "Daily reminder",
        "Recordatorio diario",
        "Lembrete diário",
        "Tägliche Erinnerung",
        "Rappel quotidien",
        "Promemoria giornaliero",
        "Dagelijkse herinnering",
        "Codzienne przypomnienie",
        "Ежедневное напоминание",
        "Günlük hatırlatıcı",
        "Pengingat harian",
        "毎日のリマインダー",
        "매일 알림",
    )
    val reminderOff get() = t(
        "Off",
        "Apagado",
        "Desativado",
        "Aus",
        "Désactivé",
        "Disattivato",
        "Uit",
        "Wyłączone",
        "Выключено",
        "Kapalı",
        "Mati",
        "オフ",
        "꺼짐",
    )
    val reminderBlocked get() = t(
        "Notifications are off for Chroma",
        "Las notificaciones de Chroma están desactivadas",
        "As notificações do Chroma estão desativadas",
        "Mitteilungen für Chroma sind aus",
        "Les notifications de Chroma sont désactivées",
        "Le notifiche di Chroma sono disattivate",
        "Meldingen voor Chroma staan uit",
        "Powiadomienia Chroma są wyłączone",
        "Уведомления Chroma отключены",
        "Chroma bildirimleri kapalı",
        "Notifikasi Chroma dimatikan",
        "Chromaの通知がオフになっています",
        "Chroma 알림이 꺼져 있어요",
    )
    val openSystemSettings get() = t(
        "Open settings",
        "Abrir ajustes",
        "Abrir ajustes",
        "Einstellungen öffnen",
        "Ouvrir les réglages",
        "Apri impostazioni",
        "Open instellingen",
        "Otwórz ustawienia",
        "Открыть настройки",
        "Ayarları aç",
        "Buka pengaturan",
        "設定を開く",
        "설정 열기",
    )
    val weekColorRow get() = t(
        "Color of the week",
        "Color de la semana",
        "Cor da semana",
        "Farbe der Woche",
        "Couleur de la semaine",
        "Colore della settimana",
        "Kleur van de week",
        "Kolor tygodnia",
        "Цвет недели",
        "Haftanın rengi",
        "Warna minggu ini",
        "今週の色",
        "이번 주의 색",
    )
    val watermarkRow get() = t(
        "\"Chroma\" on shared cards",
        "\"Chroma\" en las tarjetas compartidas",
        "\"Chroma\" nos cartões compartilhados",
        "\"Chroma\" auf geteilten Karten",
        "\"Chroma\" sur les cartes partagées",
        "\"Chroma\" sulle card condivise",
        "\"Chroma\" op gedeelde kaarten",
        "\"Chroma\" na udostępnianych kartach",
        "\"Chroma\" на карточках, которыми делишься",
        "Paylaşılan kartlarda \"Chroma\"",
        "\"Chroma\" di kartu yang dibagikan",
        "共有するカードに\"Chroma\"を入れる",
        "공유하는 카드에 \"Chroma\" 표시",
    )
    val exportRow get() = t(
        "Export backup",
        "Exportar copia",
        "Exportar cópia",
        "Kopie exportieren",
        "Exporter une copie",
        "Esporta copia",
        "Back-up exporteren",
        "Eksportuj kopię",
        "Экспорт копии",
        "Yedeği dışa aktar",
        "Ekspor cadangan",
        "バックアップを書き出す",
        "백업 내보내기",
    )
    val lastBackupNever get() = t(
        "No backup yet",
        "Todavía ninguna copia",
        "Ainda nenhuma cópia",
        "Noch keine Sicherung",
        "Encore aucune copie",
        "Ancora nessuna copia",
        "Nog geen back-up",
        "Brak kopii",
        "Копий пока нет",
        "Henüz yedek yok",
        "Belum ada cadangan",
        "まだバックアップはありません",
        "아직 백업이 없어요",
    )
    val exportNothing get() = t(
        "There's nothing to back up yet.",
        "Aún no hay nada que copiar.",
        "Ainda não há nada para copiar.",
        "Es gibt noch nichts zu sichern.",
        "Il n'y a encore rien à copier.",
        "Non c'è ancora niente da copiare.",
        "Er is nog niets om te back-uppen.",
        "Na razie nie ma czego kopiować.",
        "Пока нечего сохранять.",
        "Henüz yedeklenecek bir şey yok.",
        "Belum ada yang bisa dicadangkan.",
        "まだバックアップするものがありません。",
        "아직 백업할 것이 없어요.",
    )
    val importRow get() = t(
        "Import backup",
        "Importar copia",
        "Importar cópia",
        "Kopie importieren",
        "Importer une copie",
        "Importa copia",
        "Back-up importeren",
        "Importuj kopię",
        "Импорт копии",
        "Yedeği içe aktar",
        "Impor cadangan",
        "バックアップを読み込む",
        "백업 가져오기",
    )
    val importSubtitle get() = t(
        "Joins your colors, nothing gets deleted",
        "Se junta con tus colores, sin borrar nada",
        "Se junta às suas cores, sem apagar nada",
        "Wird mit deinen Farben zusammengeführt, nichts wird gelöscht",
        "Se joint à tes couleurs, rien n'est supprimé",
        "Si unisce ai tuoi colori, senza eliminare nulla",
        "Wordt samengevoegd met je kleuren, er wordt niets verwijderd",
        "Łączy się z twoimi kolorami, nic nie jest usuwane",
        "Объединяется с твоими цветами, ничего не удаляется",
        "Renklerinle birleşir, hiçbir şey silinmez",
        "Digabung dengan warnamu, tidak ada yang dihapus",
        "今の色と合わせます。何も削除されません",
        "지금의 색과 합쳐지고, 아무것도 지워지지 않아요",
    )
    val proRow get() = t(
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
    )
    val proSubtitle get() = t(
        "Year poster, year widget and your year in words. One-time payment",
        "Póster, widget del año y tu año en palabras. Pago único",
        "Pôster, widget do ano e seu ano em palavras. Pagamento único",
        "Jahresposter, Jahres-Widget und dein Jahr in Worten. Einmalzahlung",
        "Affiche, widget de l'année et ton année en mots. Paiement unique",
        "Poster, widget dell'anno e il tuo anno in parole. Pagamento unico",
        "Jaarposter, jaarwidget en je jaar in woorden. Eenmalige betaling",
        "Plakat, widżet roku i twój rok w słowach. Jednorazowa płatność",
        "Постер, виджет года и твой год в словах. Разовая оплата",
        "Yıl posteri, yıl widget'ı ve kelimelerle yılın. Tek seferlik ödeme",
        "Poster tahunan, widget tahun, dan tahunmu dalam kata. Bayar sekali",
        "1年のポスター、1年のウィジェット、ことばで振り返る1年。買い切り",
        "한 해 포스터, 한 해 위젯, 글로 보는 한 해. 한 번 결제",
    )
    val proOwned get() = t(
        "Purchased. Thank you.",
        "Comprado. Gracias.",
        "Comprado. Obrigado.",
        "Gekauft. Danke.",
        "Acheté. Merci.",
        "Acquistato. Grazie.",
        "Gekocht. Bedankt.",
        "Kupione. Dziękujemy.",
        "Куплено. Спасибо.",
        "Satın alındı. Teşekkürler.",
        "Sudah dibeli. Terima kasih.",
        "購入済みです。ありがとうございます。",
        "구매했어요. 고마워요.",
    )
    val restoreRow get() = t(
        "Restore purchase",
        "Restaurar compra",
        "Restaurar compra",
        "Kauf wiederherstellen",
        "Restaurer l'achat",
        "Ripristina acquisto",
        "Aankoop herstellen",
        "Przywróć zakup",
        "Восстановить покупку",
        "Satın alımı geri yükle",
        "Pulihkan pembelian",
        "購入を復元",
        "구매 복원",
    )
    val restoreDone get() = t(
        "Purchase restored.",
        "Compra restaurada.",
        "Compra restaurada.",
        "Kauf wiederhergestellt.",
        "Achat restauré.",
        "Acquisto ripristinato.",
        "Aankoop hersteld.",
        "Zakup przywrócony.",
        "Покупка восстановлена.",
        "Satın alım geri yüklendi.",
        "Pembelian dipulihkan.",
        "購入を復元しました。",
        "구매를 복원했어요.",
    )
    val restoreNothing get() = t(
        "There's no purchase to restore.",
        "No hay ninguna compra que restaurar.",
        "Não há nenhuma compra para restaurar.",
        "Es gibt keinen Kauf zum Wiederherstellen.",
        "Il n'y a aucun achat à restaurer.",
        "Non c'è nessun acquisto da ripristinare.",
        "Er is geen aankoop om te herstellen.",
        "Nie ma zakupu do przywrócenia.",
        "Нет покупок для восстановления.",
        "Geri yüklenecek bir satın alım yok.",
        "Tidak ada pembelian untuk dipulihkan.",
        "復元できる購入はありません。",
        "복원할 구매가 없어요.",
    )
    val siblingQuilt get() = t(
        "Your habits, a year at a glance",
        "Tus hábitos, un año a la vista",
        "Seus hábitos, um ano à vista",
        "Deine Gewohnheiten, ein Jahr im Blick",
        "Tes habitudes, une année en un coup d'œil",
        "Le tue abitudini, un anno a colpo d'occhio",
        "Je gewoontes, een jaar in één oogopslag",
        "Twoje nawyki, cały rok w jednym spojrzeniu",
        "Твои привычки: год с одного взгляда",
        "Alışkanlıkların, bir yıl tek bakışta",
        "Kebiasaanmu, setahun dalam sekali lihat",
        "習慣を1年分ひと目で",
        "습관을 한 해 한눈에",
    )
    val siblingMood get() = t(
        "How each day went, in colour",
        "Cómo te ha ido cada día, en color",
        "Como foi cada dia, em cores",
        "Wie jeder Tag war, in Farbe",
        "Comment chaque jour s'est passé, en couleur",
        "Com'è andato ogni giorno, a colori",
        "Hoe elke dag ging, in kleur",
        "Jak minął każdy dzień, w kolorze",
        "Как прошёл каждый день, в цвете",
        "Her günün nasıl geçtiği, renklerle",
        "Bagaimana tiap hari berlalu, dalam warna",
        "毎日の調子を色で",
        "하루하루가 어땠는지, 색으로",
    )
    val siblingPurl get() = t(
        "One line a day, read years later",
        "Una línea al día, releída años después",
        "Uma linha por dia, relida anos depois",
        "Eine Zeile am Tag, Jahre später gelesen",
        "Une ligne par jour, relue des années après",
        "Una riga al giorno, riletta anni dopo",
        "Eén regel per dag, jaren later herlezen",
        "Jedna linijka dziennie, czytana po latach",
        "Одна строка в день, прочитанная годы спустя",
        "Günde bir satır, yıllar sonra okunur",
        "Satu baris sehari, dibaca bertahun kemudian",
        "1日1行、何年後かに読み返す",
        "하루 한 줄, 몇 년 뒤에 다시 읽기",
    )
    val lockRow get() = t(
        "Lock Chroma",
        "Bloquear Chroma",
        "Bloquear o Chroma",
        "Chroma sperren",
        "Verrouiller Chroma",
        "Blocca Chroma",
        "Chroma vergrendelen",
        "Zablokuj Chroma",
        "Блокировать Chroma",
        "Chroma'yı kilitle",
        "Kunci Chroma",
        "Chromaをロック",
        "Chroma 잠그기",
    )
    val lockSubtitle get() = t(
        "Asks for your face, your fingerprint or your phone code",
        "Pide tu cara, tu huella o el código del teléfono",
        "Pede seu rosto, sua digital ou o código do telefone",
        "Fragt nach deinem Gesicht, deinem Fingerabdruck oder dem Code des Handys",
        "Demande ton visage, ton empreinte ou le code du téléphone",
        "Chiede il tuo volto, la tua impronta o il codice del telefono",
        "Vraagt om je gezicht, je vingerafdruk of de code van je telefoon",
        "Prosi o twarz, odcisk palca lub kod telefonu",
        "Запрашивает лицо, отпечаток пальца или код телефона",
        "Yüzünü, parmak izini ya da telefon kodunu ister",
        "Meminta wajah, sidik jari, atau kode ponselmu",
        "顔、指紋、または端末のパスコードで開きます",
        "얼굴, 지문 또는 휴대폰 암호를 요청해요",
    )
    val lockUnavailable get() = t(
        "Set a screen lock on your phone to use this.",
        "Pon un bloqueo de pantalla en el teléfono para usarlo.",
        "Configure um bloqueio de tela no telefone para usar isso.",
        "Richte eine Bildschirmsperre auf dem Handy ein, um das zu nutzen.",
        "Active un verrouillage d'écran sur ton téléphone pour l'utiliser.",
        "Imposta un blocco schermo sul telefono per usarlo.",
        "Stel een schermvergrendeling in op je telefoon om dit te gebruiken.",
        "Ustaw blokadę ekranu w telefonie, aby z tego korzystać.",
        "Чтобы пользоваться этим, установи блокировку экрана на телефоне.",
        "Bunu kullanmak için telefonunda ekran kilidi ayarla.",
        "Pasang kunci layar di ponselmu untuk memakai ini.",
        "使うには、端末で画面ロックを設定してください。",
        "사용하려면 휴대폰에 화면 잠금을 설정하세요.",
    )
    val unlock get() = t(
        "Unlock",
        "Desbloquear",
        "Desbloquear",
        "Entsperren",
        "Déverrouiller",
        "Sblocca",
        "Ontgrendelen",
        "Odblokuj",
        "Разблокировать",
        "Kilidi aç",
        "Buka kunci",
        "ロックを解除",
        "잠금 해제",
    )
    val lockPromptTitle get() = t(
        "Open Chroma",
        "Abrir Chroma",
        "Abrir o Chroma",
        "Chroma öffnen",
        "Ouvrir Chroma",
        "Apri Chroma",
        "Chroma openen",
        "Otwórz Chroma",
        "Открыть Chroma",
        "Chroma'yı aç",
        "Buka Chroma",
        "Chromaを開く",
        "Chroma 열기",
    )
    val lockPromptSubtitle get() = t(
        "Chroma is locked",
        "Chroma está bloqueado",
        "O Chroma está bloqueado",
        "Chroma ist gesperrt",
        "Chroma est verrouillé",
        "Chroma è bloccato",
        "Chroma is vergrendeld",
        "Chroma jest zablokowana",
        "Приложение Chroma заблокировано",
        "Chroma kilitli",
        "Chroma terkunci",
        "Chromaはロックされています",
        "Chroma가 잠겨 있어요",
    )
    val privacyRow get() = t(
        "Privacy policy",
        "Política de privacidad",
        "Política de privacidade",
        "Datenschutz",
        "Confidentialité",
        "Informativa sulla privacy",
        "Privacybeleid",
        "Polityka prywatności",
        "Политика конфиденциальности",
        "Gizlilik politikası",
        "Kebijakan privasi",
        "プライバシーポリシー",
        "개인정보 처리방침",
    )

    // 7. Dialogs and notices

    val ok get() = t("OK", "Vale", "OK", "OK", "OK", "OK", "OK", "OK", "ОК", "Tamam", "OK", "OK", "확인")
    val cancel get() = t(
        "Cancel",
        "Cancelar",
        "Cancelar",
        "Abbrechen",
        "Annuler",
        "Annulla",
        "Annuleren",
        "Anuluj",
        "Отмена",
        "Vazgeç",
        "Batal",
        "キャンセル",
        "취소",
    )
    val yes get() = t("Yes", "Sí", "Sim", "Ja", "Oui", "Sì", "Ja", "Tak", "Да", "Evet", "Ya", "はい", "네")
    val notNow get() = t(
        "Not now",
        "Ahora no",
        "Agora não",
        "Jetzt nicht",
        "Pas maintenant",
        "Non ora",
        "Niet nu",
        "Nie teraz",
        "Не сейчас",
        "Şimdi değil",
        "Nanti saja",
        "今はしない",
        "나중에",
    )
    val working get() = t(
        "One moment...",
        "Un momento...",
        "Um momento...",
        "Einen Moment...",
        "Un instant...",
        "Un attimo...",
        "Een moment...",
        "Chwileczkę...",
        "Минутку...",
        "Bir saniye...",
        "Sebentar...",
        "少々お待ちください...",
        "잠시만요...",
    )
    val importTitle get() = t(
        "Import backup",
        "Importar copia",
        "Importar cópia",
        "Kopie importieren",
        "Importer une copie",
        "Importa copia",
        "Back-up importeren",
        "Importuj kopię",
        "Импорт копии",
        "Yedeği içe aktar",
        "Impor cadangan",
        "バックアップを読み込む",
        "백업 가져오기",
    )
    val importAction get() = t(
        "Import",
        "Importar",
        "Importar",
        "Importieren",
        "Importer",
        "Importa",
        "Importeren",
        "Importuj",
        "Импортировать",
        "İçe aktar",
        "Impor",
        "読み込む",
        "가져오기",
    )
    val importFailedTitle get() = t(
        "Couldn't import",
        "No se ha podido importar",
        "Não foi possível importar",
        "Import fehlgeschlagen",
        "Échec de l'import",
        "Importazione non riuscita",
        "Importeren mislukt",
        "Nie udało się zaimportować",
        "Не удалось импортировать",
        "İçe aktarılamadı",
        "Gagal mengimpor",
        "読み込めませんでした",
        "가져오지 못했어요",
    )
    val importNotBackup get() = t(
        "That file is not a backup from Chroma.",
        "Ese fichero no es una copia de Chroma.",
        "Esse arquivo não é uma cópia do Chroma.",
        "Diese Datei ist keine Sicherung von Chroma.",
        "Ce fichier n'est pas une copie de Chroma.",
        "Questo file non è una copia di Chroma.",
        "Dat bestand is geen back-up van Chroma.",
        "Ten plik nie jest kopią z Chroma.",
        "Этот файл не копия Chroma.",
        "Bu dosya bir Chroma yedeği değil.",
        "Berkas itu bukan cadangan dari Chroma.",
        "このファイルはChromaのバックアップではありません。",
        "이 파일은 Chroma 백업이 아니에요.",
    )
    val importDamaged get() = t(
        "The backup is incomplete or damaged. Your colors weren't touched.",
        "La copia está incompleta o dañada. Tus colores no se han tocado.",
        "A cópia está incompleta ou danificada. Suas cores não foram alteradas.",
        "Die Sicherung ist unvollständig oder beschädigt. Deine Farben wurden nicht verändert.",
        "La copie est incomplète ou endommagée. Tes couleurs n'ont pas été touchées.",
        "La copia è incompleta o danneggiata. I tuoi colori non sono stati toccati.",
        "De back-up is onvolledig of beschadigd. Je kleuren zijn niet aangeraakt.",
        "Kopia jest niepełna lub uszkodzona. Twoje kolory pozostały nietknięte.",
        "Копия неполная или повреждена. Твои цвета не тронуты.",
        "Yedek eksik ya da bozuk. Renklerine dokunulmadı.",
        "Cadangan tidak lengkap atau rusak. Warnamu tidak diubah.",
        "バックアップが不完全か壊れています。今の色には手を加えていません。",
        "백업이 불완전하거나 손상됐어요. 지금의 색은 그대로예요.",
    )
    val importTooNew get() = t(
        "This backup is from a newer version of Chroma. Update the app and try again.",
        "Esta copia es de una versión más nueva de Chroma. Actualiza la app y vuelve a probar.",
        "Esta cópia é de uma versão mais nova do Chroma. Atualize o app e tente de novo.",
        "Diese Sicherung stammt aus einer neueren Version von Chroma. Aktualisiere die App und versuch es erneut.",
        "Cette copie vient d'une version plus récente de Chroma. Mets à jour l'app et réessaie.",
        "Questa copia viene da una versione più recente di Chroma. Aggiorna l'app e riprova.",
        "Deze back-up komt van een nieuwere versie van Chroma. Werk de app bij en probeer het opnieuw.",
        "Ta kopia pochodzi z nowszej wersji Chroma. Zaktualizuj aplikację i spróbuj ponownie.",
        "Эта копия из более новой версии Chroma. Обнови приложение и попробуй снова.",
        "Bu yedek Chroma'nın daha yeni bir sürümünden. Uygulamayı güncelleyip tekrar dene.",
        "Cadangan ini dari versi Chroma yang lebih baru. Perbarui aplikasinya lalu coba lagi.",
        "このバックアップは新しいバージョンのChromaで作られました。アプリを更新してもう一度お試しください。",
        "더 새로운 버전의 Chroma에서 만든 백업이에요. 앱을 업데이트한 뒤 다시 시도해 주세요.",
    )
    val importEmpty get() = t(
        "The backup has no colors.",
        "La copia no tiene ningún color.",
        "A cópia não tem nenhuma cor.",
        "Die Sicherung enthält keine Farbe.",
        "La copie ne contient aucune couleur.",
        "La copia non contiene colori.",
        "De back-up bevat geen kleuren.",
        "Kopia nie zawiera kolorów.",
        "В копии нет цветов.",
        "Yedekte hiç renk yok.",
        "Cadangan ini tidak berisi warna.",
        "バックアップに色がありません。",
        "백업에 색이 없어요.",
    )
    val exportFailed get() = t(
        "Couldn't save the backup.",
        "No se ha podido guardar la copia.",
        "Não foi possível salvar a cópia.",
        "Die Sicherung konnte nicht gespeichert werden.",
        "Impossible d'enregistrer la copie.",
        "Impossibile salvare la copia.",
        "De back-up kon niet worden opgeslagen.",
        "Nie udało się zapisać kopii.",
        "Не удалось сохранить копию.",
        "Yedek kaydedilemedi.",
        "Gagal menyimpan cadangan.",
        "バックアップを保存できませんでした。",
        "백업을 저장하지 못했어요.",
    )

    // 8. Chroma Pro

    val proTitle get() = t(
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
        "Chroma Pro",
    )
    val proPoster get() = t(
        "Your year as a poster, in three styles",
        "Tu año en póster, en tres estilos",
        "Seu ano em pôster, em três estilos",
        "Dein Jahr als Poster, in drei Stilen",
        "Ton année en affiche, en trois styles",
        "Il tuo anno in un poster, in tre stili",
        "Je jaar als poster, in drie stijlen",
        "Twój rok jako plakat, w trzech stylach",
        "Твой год на постере, в трёх стилях",
        "Yılın bir posterde, üç stilde",
        "Tahunmu jadi poster, dalam tiga gaya",
        "1年をポスターに。3つのスタイルで",
        "한 해를 포스터로, 세 가지 스타일로",
    )
    val proStats get() = t(
        "Your year told in short sentences",
        "Tu año contado en frases cortas",
        "Seu ano contado em frases curtas",
        "Dein Jahr in kurzen Sätzen erzählt",
        "Ton année racontée en phrases courtes",
        "Il tuo anno raccontato in frasi brevi",
        "Je jaar verteld in korte zinnen",
        "Twój rok opowiedziany krótkimi zdaniami",
        "Твой год в коротких фразах",
        "Kısa cümlelerle anlatılan yılın",
        "Tahunmu diceritakan dalam kalimat pendek",
        "1年を短いことばで",
        "짧은 문장으로 보는 한 해",
    )
    val proYearWidget get() = t(
        "The year widget",
        "El widget del año",
        "O widget do ano",
        "Das Jahres-Widget",
        "Le widget de l'année",
        "Il widget dell'anno",
        "De jaarwidget",
        "Widżet roku",
        "Виджет года",
        "Yıl widget'ı",
        "Widget tahun",
        "1年のウィジェット",
        "한 해 위젯",
    )
    val proOnce get() = t(
        "One-time payment, no subscription.",
        "Pago único, sin suscripción.",
        "Pagamento único, sem assinatura.",
        "Einmalzahlung, kein Abo.",
        "Paiement unique, sans abonnement.",
        "Pagamento unico, nessun abbonamento.",
        "Eenmalige betaling, geen abonnement.",
        "Jednorazowa płatność, bez subskrypcji.",
        "Разовая оплата, без подписки.",
        "Tek seferlik ödeme, abonelik yok.",
        "Bayar sekali, tanpa langganan.",
        "買い切りで、サブスクリプションはありません。",
        "한 번 결제, 구독 없음.",
    )
    val proFriendsFree get() = t(
        "Friends are always free.",
        "Los amigos siempre son gratis.",
        "Os amigos são sempre grátis.",
        "Freunde sind immer kostenlos.",
        "Les amis sont toujours gratuits.",
        "Gli amici sono sempre gratis.",
        "Vrienden zijn altijd gratis.",
        "Znajomi są zawsze za darmo.",
        "Друзья всегда бесплатны.",
        "Arkadaşlar her zaman ücretsiz.",
        "Teman selalu gratis.",
        "友だち機能はずっと無料です。",
        "친구 기능은 언제나 무료예요.",
    )
    val restore get() = t(
        "Restore",
        "Restaurar",
        "Restaurar",
        "Wiederherstellen",
        "Restaurer",
        "Ripristina",
        "Herstellen",
        "Przywróć",
        "Восстановить",
        "Geri yükle",
        "Pulihkan",
        "復元",
        "복원",
    )
    val storeUnavailable get() = t(
        "The store is not available right now.",
        "La tienda no está disponible ahora.",
        "A loja não está disponível agora.",
        "Der Store ist gerade nicht verfügbar.",
        "La boutique n'est pas disponible pour le moment.",
        "Lo store non è disponibile al momento.",
        "De store is nu niet beschikbaar.",
        "Sklep jest teraz niedostępny.",
        "Магазин сейчас недоступен.",
        "Mağaza şu anda kullanılamıyor.",
        "Toko sedang tidak tersedia.",
        "現在ストアを利用できません。",
        "지금은 스토어를 사용할 수 없어요.",
    )
    val buyPending get() = t(
        "The payment is pending. Pro turns on by itself once it goes through.",
        "El pago está pendiente. Pro se activa solo cuando se complete.",
        "O pagamento está pendente. O Pro é ativado sozinho quando for concluído.",
        "Die Zahlung steht noch aus. Pro wird von selbst aktiviert, sobald sie durch ist.",
        "Le paiement est en attente. Pro s'active tout seul une fois qu'il est passé.",
        "Il pagamento è in sospeso. Pro si attiva da solo quando va a buon fine.",
        "De betaling staat nog open. Pro wordt vanzelf actief zodra die rond is.",
        "Płatność oczekuje. Pro włączy się samo, gdy zostanie zrealizowana.",
        "Платёж ожидает подтверждения. Pro включится сам, когда он пройдёт.",
        "Ödeme beklemede. Tamamlanınca Pro kendiliğinden açılır.",
        "Pembayaran sedang diproses. Pro aktif sendiri setelah selesai.",
        "支払いが保留中です。完了するとProが自動でオンになります。",
        "결제가 대기 중이에요. 완료되면 Pro가 자동으로 켜져요.",
    )
    val buyOffline get() = t(
        "No connection. If you were charged, Pro turns on by itself once you're back online. You can also tap Restore.",
        "Sin conexión. Si ya se ha cobrado, Pro se activa solo al volver la conexión. También puedes tocar Restaurar.",
        "Sem conexão. Se a cobrança já foi feita, o Pro é ativado sozinho quando a conexão voltar. Você também pode tocar em Restaurar.",
        "Keine Verbindung. Falls schon abgebucht wurde, wird Pro von selbst aktiv, sobald du wieder online bist. Du kannst auch auf Wiederherstellen tippen.",
        "Pas de connexion. Si le paiement est passé, Pro s'active tout seul au retour de la connexion. Tu peux aussi toucher Restaurer.",
        "Nessuna connessione. Se l'addebito è già avvenuto, Pro si attiva da solo quando torni online. Puoi anche toccare Ripristina.",
        "Geen verbinding. Als er al is afgeschreven, wordt Pro vanzelf actief zodra je weer online bent. Je kunt ook op Herstellen tikken.",
        "Brak połączenia. Jeśli płatność została pobrana, Pro włączy się samo po powrocie do sieci. Możesz też dotknąć Przywróć.",
        "Нет соединения. Если деньги уже списаны, Pro включится сам, когда появится сеть. Можно также нажать Восстановить.",
        "Bağlantı yok. Ücret alındıysa, yeniden çevrim içi olduğunda Pro kendiliğinden açılır. Geri yükle'ye de dokunabilirsin.",
        "Tidak ada koneksi. Jika sudah ditagih, Pro aktif sendiri saat kamu kembali online. Kamu juga bisa mengetuk Pulihkan.",
        "接続がありません。支払い済みなら、オンラインに戻ったときにProが自動でオンになります。「復元」をタップすることもできます。",
        "연결이 없어요. 이미 결제됐다면 다시 연결될 때 Pro가 자동으로 켜져요. 복원을 눌러도 돼요.",
    )
    val buyFailed get() = t(
        "The purchase could not be completed.",
        "No se ha podido completar la compra.",
        "Não foi possível concluir a compra.",
        "Der Kauf konnte nicht abgeschlossen werden.",
        "L'achat n'a pas pu être finalisé.",
        "Impossibile completare l'acquisto.",
        "De aankoop kon niet worden voltooid.",
        "Nie udało się dokończyć zakupu.",
        "Не удалось завершить покупку.",
        "Satın alma tamamlanamadı.",
        "Pembelian tidak bisa diselesaikan.",
        "購入を完了できませんでした。",
        "구매를 완료하지 못했어요.",
    )

    // 9. Sharing and the poster

    val share get() = t(
        "Share",
        "Compartir",
        "Compartilhar",
        "Teilen",
        "Partager",
        "Condividi",
        "Delen",
        "Udostępnij",
        "Поделиться",
        "Paylaş",
        "Bagikan",
        "共有",
        "공유",
    )
    val shareColorOnly get() = t(
        "Color only",
        "Solo el color",
        "Só a cor",
        "Nur die Farbe",
        "La couleur seule",
        "Solo il colore",
        "Alleen de kleur",
        "Tylko kolor",
        "Только цвет",
        "Yalnızca renk",
        "Hanya warna",
        "色だけ",
        "색만",
    )
    val shareWithPhoto get() = t(
        "With the photo",
        "Con la foto",
        "Com a foto",
        "Mit dem Foto",
        "Avec la photo",
        "Con la foto",
        "Met de foto",
        "Ze zdjęciem",
        "С фото",
        "Fotoğrafla",
        "Dengan foto",
        "写真つき",
        "사진과 함께",
    )
    /** The picture about to leave the app, a different thing from what friends see. */
    val includePhoto get() = t(
        "Include the photo",
        "Incluir la foto",
        "Incluir a foto",
        "Mit Foto",
        "Inclure la photo",
        "Includi la foto",
        "Foto toevoegen",
        "Dołącz zdjęcie",
        "Добавить фото",
        "Fotoğrafı ekle",
        "Sertakan foto",
        "写真を含める",
        "사진 포함",
    )
    val proTag get() = t("Pro", "Pro", "Pro", "Pro", "Pro", "Pro", "Pro", "Pro", "Pro", "Pro", "Pro", "Pro", "Pro")
    val saveToPhotos get() = t(
        "Save to Photos",
        "Guardar en fotos",
        "Salvar nas fotos",
        "In Fotos speichern",
        "Enregistrer la photo",
        "Salva in Foto",
        "Bewaar in Foto's",
        "Zapisz w Zdjęciach",
        "Сохранить в Фото",
        "Fotoğraflar'a kaydet",
        "Simpan ke Foto",
        "写真に保存",
        "사진에 저장",
    )
    val saved get() = t(
        "Saved to your photos.",
        "Guardada en tus fotos.",
        "Salva nas suas fotos.",
        "In deinen Fotos gespeichert.",
        "Enregistrée dans tes photos.",
        "Salvata nelle tue foto.",
        "Bewaard in je foto's.",
        "Zapisano w twoich zdjęciach.",
        "Сохранено в фото.",
        "Fotoğraflarına kaydedildi.",
        "Tersimpan di fotomu.",
        "写真に保存しました。",
        "사진에 저장했어요.",
    )
    val saveFailed get() = t(
        "Couldn't save.",
        "No se ha podido guardar.",
        "Não foi possível salvar.",
        "Konnte nicht gespeichert werden.",
        "Impossible d'enregistrer.",
        "Impossibile salvare.",
        "Opslaan mislukt.",
        "Nie udało się zapisać.",
        "Не удалось сохранить.",
        "Kaydedilemedi.",
        "Gagal menyimpan.",
        "保存できませんでした。",
        "저장하지 못했어요.",
    )
    val posterGrid get() = t("Grid", "Rejilla", "Grade", "Raster", "Grille", "Griglia", "Raster", "Siatka", "Сетка", "Izgara", "Kisi", "グリッド", "격자")
    val posterStrip get() = t("Strip", "Tira", "Faixa", "Streifen", "Bande", "Striscia", "Strook", "Pasek", "Полоса", "Şerit", "Pita", "ストリップ", "띠")
    val posterWallpaper get() = t(
        "Wallpaper",
        "Fondo de pantalla",
        "Papel de parede",
        "Hintergrund",
        "Fond d'écran",
        "Sfondo",
        "Achtergrond",
        "Tapeta",
        "Обои",
        "Duvar kağıdı",
        "Wallpaper",
        "壁紙",
        "배경화면",
    )
    val cardTagline get() = t(
        "a color a day",
        "un color al día",
        "uma cor por dia",
        "eine Farbe am Tag",
        "une couleur par jour",
        "un colore al giorno",
        "een kleur per dag",
        "kolor na każdy dzień",
        "один цвет в день",
        "günde bir renk",
        "satu warna sehari",
        "1日1色",
        "하루 한 색",
    )

    // 10. Notification

    val reminderTitle get() = t(
        "What color is today?",
        "¿De qué color es hoy?",
        "De que cor é hoje?",
        "Welche Farbe hat heute?",
        "De quelle couleur est aujourd'hui ?",
        "Di che colore è oggi?",
        "Welke kleur heeft vandaag?",
        "Jakiego koloru jest dziś?",
        "Какого цвета сегодня?",
        "Bugün ne renk?",
        "Hari ini warna apa?",
        "今日は何色？",
        "오늘은 무슨 색일까요?",
    )
    val reminderText get() = t(
        "Look around for a moment.",
        "Mira a tu alrededor un momento.",
        "Olhe ao redor por um momento.",
        "Schau dich einen Moment um.",
        "Regarde autour de toi un instant.",
        "Guardati intorno per un momento.",
        "Kijk even om je heen.",
        "Rozejrzyj się przez chwilę.",
        "Оглянись на минутку.",
        "Bir an etrafına bak.",
        "Lihat sekelilingmu sejenak.",
        "少しまわりを見てみましょう。",
        "잠깐 주위를 둘러보세요.",
    )
    val reminderChannel get() = t(
        "Daily reminder",
        "Recordatorio diario",
        "Lembrete diário",
        "Tägliche Erinnerung",
        "Rappel quotidien",
        "Promemoria giornaliero",
        "Dagelijkse herinnering",
        "Codzienne przypomnienie",
        "Ежедневное напоминание",
        "Günlük hatırlatıcı",
        "Pengingat harian",
        "毎日のリマインダー",
        "매일 알림",
    )

    // 11. Widgets

    val widgetEmpty get() = t(
        "No color yet",
        "Aún sin color",
        "Ainda sem cor",
        "Noch keine Farbe",
        "Pas encore de couleur",
        "Ancora nessun colore",
        "Nog geen kleur",
        "Jeszcze bez koloru",
        "Пока без цвета",
        "Henüz renk yok",
        "Belum ada warna",
        "まだ色がありません",
        "아직 색이 없어요",
    )
    val widgetUnlock get() = t(
        "Tap to turn it on",
        "Toca para activarlo",
        "Toque para ativar",
        "Tippen zum Aktivieren",
        "Touche pour l'activer",
        "Tocca per attivarlo",
        "Tik om aan te zetten",
        "Dotknij, aby włączyć",
        "Нажми, чтобы включить",
        "Açmak için dokun",
        "Ketuk untuk mengaktifkan",
        "タップしてオンにする",
        "탭해서 켜기",
    )
    val widgetTodayName get() = t(
        "Today",
        "Hoy",
        "Hoje",
        "Heute",
        "Aujourd'hui",
        "Oggi",
        "Vandaag",
        "Dziś",
        "Сегодня",
        "Bugün",
        "Hari ini",
        "今日",
        "오늘",
    )
    val widgetTodayDescription get() = t(
        "Today's color",
        "El color de hoy",
        "A cor de hoje",
        "Die Farbe von heute",
        "La couleur du jour",
        "Il colore di oggi",
        "De kleur van vandaag",
        "Dzisiejszy kolor",
        "Цвет дня",
        "Bugünün rengi",
        "Warna hari ini",
        "今日の色",
        "오늘의 색",
    )
    val widgetYearName get() = t(
        "The year",
        "El año",
        "O ano",
        "Das Jahr",
        "L'année",
        "L'anno",
        "Het jaar",
        "Rok",
        "Год",
        "Yıl",
        "Tahun ini",
        "1年",
        "한 해",
    )
    val widgetYearDescription get() = t(
        "Your year in colors",
        "Tu año en colores",
        "Seu ano em cores",
        "Dein Jahr in Farben",
        "Ton année en couleurs",
        "Il tuo anno a colori",
        "Je jaar in kleuren",
        "Twój rok w kolorach",
        "Твой год в цветах",
        "Renklerle yılın",
        "Tahunmu dalam warna",
        "色でつづる1年",
        "색으로 보는 한 해",
    )

    // 13. Friends (v1.1)

    val friendsIntro1 get() = t(
        "See the color of your friends' day.",
        "Mira el color del día de tus amigos.",
        "Veja a cor do dia dos seus amigos.",
        "Sieh die Farbe vom Tag deiner Freunde.",
        "Vois la couleur du jour de tes amis.",
        "Guarda il colore della giornata dei tuoi amici.",
        "Zie de kleur van de dag van je vrienden.",
        "Zobacz kolor dnia swoich znajomych.",
        "Смотри, какого цвета день у твоих друзей.",
        "Arkadaşlarının gününün rengini gör.",
        "Lihat warna hari teman-temanmu.",
        "友だちの1日の色が見られます。",
        "친구의 하루 색을 볼 수 있어요.",
    )
    val friendsIntro2 get() = t(
        "Only people you invite, and who say yes.",
        "Solo gente a la que invitas y que dice que sí.",
        "Só pessoas que você convida e que aceitam.",
        "Nur Leute, die du einlädst und die zusagen.",
        "Seulement les personnes que tu invites et qui acceptent.",
        "Solo persone che inviti e che accettano.",
        "Alleen mensen die jij uitnodigt en die ja zeggen.",
        "Tylko osoby, które zaprosisz i które się zgodzą.",
        "Только те, кого ты пригласишь и кто согласится.",
        "Yalnızca davet ettiğin ve kabul eden kişiler.",
        "Hanya orang yang kamu undang dan yang bilang ya.",
        "招待して、承認してくれた人だけ。",
        "초대해서 수락한 사람만요.",
    )
    val friendsIntro3 get() = t(
        "No likes, no counts. What you don't share stays on your phone.",
        "Sin likes ni contadores. Lo que no compartes no sale de tu móvil.",
        "Sem curtidas nem contadores. O que você não compartilha não sai do seu celular.",
        "Keine Likes, keine Zahlen. Was du nicht teilst, bleibt auf deinem Handy.",
        "Pas de likes, pas de compteurs. Ce que tu ne partages pas reste sur ton téléphone.",
        "Niente like, niente contatori. Ciò che non condividi resta sul tuo telefono.",
        "Geen likes, geen tellers. Wat je niet deelt, blijft op je telefoon.",
        "Bez lajków i liczników. To, czego nie udostępniasz, zostaje w telefonie.",
        "Без лайков и счётчиков. То, чем ты не делишься, остаётся на телефоне.",
        "Beğeni yok, sayaç yok. Paylaşmadığın şey telefonunda kalır.",
        "Tanpa like, tanpa hitungan. Yang tidak kamu bagikan tetap di ponselmu.",
        "いいねも数字もありません。共有しないものは端末の外に出ません。",
        "좋아요도, 숫자도 없어요. 공유하지 않은 건 휴대폰 밖으로 나가지 않아요.",
    )
    val signInApple get() = t(
        "Continue with Apple",
        "Continuar con Apple",
        "Continuar com a Apple",
        "Weiter mit Apple",
        "Continuer avec Apple",
        "Continua con Apple",
        "Doorgaan met Apple",
        "Kontynuuj z Apple",
        "Продолжить с Apple",
        "Apple ile devam et",
        "Lanjutkan dengan Apple",
        "Appleで続ける",
        "Apple로 계속하기",
    )
    val signInGoogle get() = t(
        "Continue with Google",
        "Continuar con Google",
        "Continuar com o Google",
        "Weiter mit Google",
        "Continuer avec Google",
        "Continua con Google",
        "Doorgaan met Google",
        "Kontynuuj z Google",
        "Продолжить с Google",
        "Google ile devam et",
        "Lanjutkan dengan Google",
        "Googleで続ける",
        "Google로 계속하기",
    )
    val signInFailed get() = t(
        "Couldn't sign in. Try again.",
        "No se ha podido iniciar sesión. Inténtalo de nuevo.",
        "Não foi possível entrar. Tente de novo.",
        "Anmeldung fehlgeschlagen. Versuch es noch einmal.",
        "Connexion impossible. Réessaie.",
        "Accesso non riuscito. Riprova.",
        "Inloggen mislukt. Probeer het opnieuw.",
        "Nie udało się zalogować. Spróbuj ponownie.",
        "Не удалось войти. Попробуй ещё раз.",
        "Giriş yapılamadı. Tekrar dene.",
        "Gagal masuk. Coba lagi.",
        "サインインできませんでした。もう一度お試しください。",
        "로그인하지 못했어요. 다시 시도해 주세요.",
    )
    val nameTitle get() = t(
        "Your name for friends",
        "Tu nombre para tus amigos",
        "Seu nome para os amigos",
        "Dein Name für Freunde",
        "Ton nom pour tes amis",
        "Il tuo nome per gli amici",
        "Je naam voor vrienden",
        "Twoje imię dla znajomych",
        "Твоё имя для друзей",
        "Arkadaşların için adın",
        "Namamu untuk teman",
        "友だちに見せる名前",
        "친구에게 보일 이름",
    )
    val nameHint get() = t(
        "The one your friends know you by.",
        "El que tus amigos reconocen.",
        "O nome pelo qual seus amigos te conhecem.",
        "Der, unter dem deine Freunde dich kennen.",
        "Celui sous lequel tes amis te connaissent.",
        "Quello con cui ti conoscono i tuoi amici.",
        "De naam waaronder je vrienden je kennen.",
        "To, pod którym znają cię znajomi.",
        "То, под которым тебя знают друзья.",
        "Arkadaşlarının seni tanıdığı ad.",
        "Nama yang dikenal teman-temanmu.",
        "友だちがあなただとわかる名前にしましょう。",
        "친구들이 알아볼 수 있는 이름이요.",
    )
    val age16 get() = t(
        "I'm 16 or older",
        "Tengo 16 años o más",
        "Tenho 16 anos ou mais",
        "Ich bin 16 oder älter",
        "J'ai 16 ans ou plus",
        "Ho 16 anni o più",
        "Ik ben 16 of ouder",
        "Mam co najmniej 16 lat",
        "Мне 16 лет или больше",
        "16 yaşında veya daha büyüğüm",
        "Usiaku 16 tahun atau lebih",
        "16歳以上です",
        "만 16세 이상이에요",
    )
    val continueAction get() = t(
        "Continue",
        "Continuar",
        "Continuar",
        "Weiter",
        "Continuer",
        "Continua",
        "Doorgaan",
        "Dalej",
        "Продолжить",
        "Devam",
        "Lanjutkan",
        "続ける",
        "계속",
    )
    val friendsOffline get() = t(
        "Friends can't be reached right now.",
        "Ahora mismo no se llega a Amigos.",
        "Não foi possível acessar Amigos agora.",
        "Freunde sind gerade nicht erreichbar.",
        "Impossible de joindre Amis pour le moment.",
        "Al momento Amici non è raggiungibile.",
        "Vrienden is nu niet bereikbaar.",
        "Znajomi są teraz niedostępni.",
        "Друзья сейчас недоступны.",
        "Arkadaşlar şu anda erişilemiyor.",
        "Teman tidak bisa dijangkau saat ini.",
        "現在、友だち機能につながりません。",
        "지금은 친구 기능에 연결할 수 없어요.",
    )
    val retry get() = t(
        "Retry",
        "Reintentar",
        "Tentar de novo",
        "Erneut versuchen",
        "Réessayer",
        "Riprova",
        "Opnieuw",
        "Ponów",
        "Повторить",
        "Tekrar dene",
        "Coba lagi",
        "再試行",
        "다시 시도",
    )
    val friendsEmpty get() = t(
        "No friends here yet. Invite someone who knows you.",
        "Aún no hay amigos aquí. Invita a alguien que te conozca.",
        "Ainda não há amigos aqui. Convide alguém que te conheça.",
        "Noch keine Freunde hier. Lade jemanden ein, der dich kennt.",
        "Pas encore d'amis ici. Invite quelqu'un qui te connaît.",
        "Ancora nessun amico qui. Invita qualcuno che ti conosce.",
        "Nog geen vrienden hier. Nodig iemand uit die je kent.",
        "Nie ma tu jeszcze znajomych. Zaproś kogoś, kto cię zna.",
        "Здесь пока нет друзей. Пригласи того, кто тебя знает.",
        "Burada henüz arkadaş yok. Seni tanıyan birini davet et.",
        "Belum ada teman di sini. Undang seseorang yang mengenalmu.",
        "まだ友だちはいません。あなたを知っている人を招待しましょう。",
        "아직 친구가 없어요. 당신을 아는 사람을 초대해 보세요.",
    )
    val sharePrivate get() = t(
        "Private",
        "Privado",
        "Privado",
        "Privat",
        "Privé",
        "Privato",
        "Privé",
        "Prywatne",
        "Только для меня",
        "Gizli",
        "Pribadi",
        "非公開",
        "비공개",
    )
    val shareLabel get() = t(
        "Your friends see",
        "Tus amigos ven",
        "Seus amigos veem",
        "Deine Freunde sehen",
        "Tes amis voient",
        "I tuoi amici vedono",
        "Je vrienden zien",
        "Znajomi widzą",
        "Друзья видят",
        "Arkadaşların görür",
        "Temanmu melihat",
        "友だちに見せるもの",
        "친구에게 보이는 것",
    )
    val defaultShareRow get() = t(
        "New days",
        "Días nuevos",
        "Dias novos",
        "Neue Tage",
        "Nouveaux jours",
        "Giorni nuovi",
        "Nieuwe dagen",
        "Nowe dni",
        "Новые дни",
        "Yeni günler",
        "Hari baru",
        "新しい日",
        "새로운 날",
    )
    val askDefaultShareTitle get() = t(
        "What do your friends see?",
        "¿Qué ven tus amigos?",
        "O que seus amigos veem?",
        "Was sehen deine Freunde?",
        "Que voient tes amis ?",
        "Cosa vedono i tuoi amici?",
        "Wat zien je vrienden?",
        "Co widzą twoi znajomi?",
        "Что видят твои друзья?",
        "Arkadaşların ne görür?",
        "Apa yang dilihat temanmu?",
        "友だちに何を見せますか？",
        "친구에게 무엇을 보여 줄까요?",
    )
    val askDefaultShareText get() = t(
        "Choose what new days share. You can change it for each day, and in Settings.",
        "Elige qué comparten los días nuevos. Puedes cambiarlo en cada día, y en Ajustes.",
        "Escolha o que os dias novos compartilham. Dá para mudar em cada dia, e em Ajustes.",
        "Wähle, was neue Tage teilen. Du kannst es für jeden Tag ändern, und in den Einstellungen.",
        "Choisis ce que partagent les nouveaux jours. Tu peux le changer pour chaque jour, et dans Réglages.",
        "Scegli cosa condividono i giorni nuovi. Puoi cambiarlo per ogni giorno, e in Impostazioni.",
        "Kies wat nieuwe dagen delen. Je kunt het per dag aanpassen, en in Instellingen.",
        "Wybierz, co udostępniają nowe dni. Możesz to zmienić dla każdego dnia i w Ustawieniach.",
        "Выбери, чем делятся новые дни. Это можно изменить для каждого дня и в Настройках.",
        "Yeni günlerin ne paylaşacağını seç. Bunu her gün için ve Ayarlar'da değiştirebilirsin.",
        "Pilih apa yang dibagikan hari-hari baru. Bisa diubah untuk tiap hari, dan di Pengaturan.",
        "新しい日に共有する内容を選んでください。日ごと、または設定で変更できます。",
        "새로운 날에 무엇을 공유할지 골라 주세요. 날마다, 그리고 설정에서 바꿀 수 있어요.",
    )
    val sectionFriends get() = t(
        "Friends",
        "Amigos",
        "Amigos",
        "Freunde",
        "Amis",
        "Amici",
        "Vrienden",
        "Znajomi",
        "Друзья",
        "Arkadaşlar",
        "Teman",
        "友だち",
        "친구",
    )
    val nameRow get() = t(
        "Your name",
        "Tu nombre",
        "Seu nome",
        "Dein Name",
        "Ton nom",
        "Il tuo nome",
        "Je naam",
        "Twoje imię",
        "Твоё имя",
        "Adın",
        "Namamu",
        "名前",
        "이름",
    )
    val signOut get() = t(
        "Sign out",
        "Cerrar sesión",
        "Sair",
        "Abmelden",
        "Se déconnecter",
        "Esci",
        "Uitloggen",
        "Wyloguj się",
        "Выйти",
        "Çıkış yap",
        "Keluar",
        "サインアウト",
        "로그아웃",
    )
    val signOutText get() = t(
        "Your friends stay on the account. Signing in again brings them back.",
        "Tus amigos se quedan en la cuenta. Al volver a entrar, vuelven.",
        "Seus amigos ficam na conta. Ao entrar de novo, eles voltam.",
        "Deine Freunde bleiben im Konto. Meldest du dich wieder an, sind sie wieder da.",
        "Tes amis restent sur le compte. En te reconnectant, ils reviennent.",
        "I tuoi amici restano nell'account. Rientrando, tornano.",
        "Je vrienden blijven in het account. Log je weer in, dan zijn ze terug.",
        "Znajomi zostają na koncie. Po ponownym zalogowaniu wrócą.",
        "Друзья остаются в аккаунте. Войдёшь снова, и они вернутся.",
        "Arkadaşların hesapta kalır. Yeniden giriş yapınca geri gelirler.",
        "Temanmu tetap ada di akun. Saat masuk lagi, mereka kembali.",
        "友だちはアカウントに残ります。もう一度サインインすると戻ります。",
        "친구는 계정에 남아 있어요. 다시 로그인하면 돌아와요.",
    )
    val inviteFriend get() = t(
        "Invite a friend",
        "Invitar a un amigo",
        "Convidar um amigo",
        "Freund einladen",
        "Inviter un ami",
        "Invita un amico",
        "Nodig een vriend uit",
        "Zaproś znajomego",
        "Пригласить друга",
        "Bir arkadaşını davet et",
        "Undang teman",
        "友だちを招待",
        "친구 초대",
    )
    val inviteText get() = t(
        "Whoever opens it sends you a request. Nothing changes until you accept it.",
        "Quien lo abra te envía una solicitud. Nada cambia hasta que la aceptas.",
        "Quem abrir te envia um pedido. Nada muda até você aceitar.",
        "Wer ihn öffnet, schickt dir eine Anfrage. Nichts ändert sich, bis du sie annimmst.",
        "Qui l'ouvre t'envoie une demande. Rien ne change tant que tu ne l'acceptes pas.",
        "Chi lo apre ti invia una richiesta. Non cambia nulla finché non la accetti.",
        "Wie hem opent, stuurt je een verzoek. Er verandert niets tot je het accepteert.",
        "Kto go otworzy, wyśle ci zaproszenie. Nic się nie zmieni, dopóki go nie przyjmiesz.",
        "Кто его откроет, отправит тебе запрос. Ничего не изменится, пока ты его не примешь.",
        "Açan kişi sana bir istek gönderir. Sen kabul edene kadar hiçbir şey değişmez.",
        "Siapa pun yang membukanya akan mengirimimu permintaan. Tidak ada yang berubah sampai kamu menerimanya.",
        "開いた人からリクエストが届きます。あなたが承認するまで何も変わりません。",
        "링크를 연 사람이 요청을 보내요. 수락하기 전까지는 아무것도 바뀌지 않아요.",
    )
    val shareLink get() = t(
        "Share link",
        "Compartir enlace",
        "Compartilhar link",
        "Link teilen",
        "Partager le lien",
        "Condividi link",
        "Link delen",
        "Udostępnij link",
        "Поделиться ссылкой",
        "Bağlantıyı paylaş",
        "Bagikan tautan",
        "リンクを共有",
        "링크 공유",
    )
    val regenerateLink get() = t(
        "New link",
        "Enlace nuevo",
        "Novo link",
        "Neuer Link",
        "Nouveau lien",
        "Nuovo link",
        "Nieuwe link",
        "Nowy link",
        "Новая ссылка",
        "Yeni bağlantı",
        "Tautan baru",
        "新しいリンク",
        "새 링크",
    )
    val regenerateText get() = t(
        "The current link and its QR stop working. The friends you have stay.",
        "El enlace actual y su QR dejan de valer. Los amigos que ya tienes se quedan.",
        "O link atual e o QR param de funcionar. Os amigos que você já tem continuam.",
        "Der aktuelle Link und sein QR-Code gelten dann nicht mehr. Deine Freunde bleiben.",
        "Le lien actuel et son QR ne marcheront plus. Tes amis restent.",
        "Il link attuale e il suo QR smettono di funzionare. Gli amici che hai restano.",
        "De huidige link en de QR-code werken dan niet meer. Je vrienden blijven.",
        "Obecny link i jego kod QR przestaną działać. Twoi znajomi zostaną.",
        "Текущая ссылка и её QR-код перестанут работать. Друзья останутся.",
        "Mevcut bağlantı ve QR kodu artık çalışmaz. Arkadaşların kalır.",
        "Tautan dan QR saat ini tidak berlaku lagi. Temanmu tetap ada.",
        "今のリンクとQRコードは使えなくなります。友だちはそのままです。",
        "지금 링크와 QR 코드는 더 이상 쓸 수 없어요. 친구는 그대로예요.",
    )
    val inviteSent get() = t(
        "Request sent. Once it is accepted, you will see each other's colors.",
        "Solicitud enviada. Cuando la acepte, veréis vuestros colores.",
        "Pedido enviado. Quando for aceito, vocês vão ver as cores um do outro.",
        "Anfrage gesendet. Sobald sie angenommen ist, seht ihr eure Farben.",
        "Demande envoyée. Une fois acceptée, vous verrez vos couleurs.",
        "Richiesta inviata. Quando verrà accettata, vedrete i vostri colori.",
        "Verzoek verstuurd. Zodra het is geaccepteerd, zien jullie elkaars kleuren.",
        "Zaproszenie wysłane. Gdy zostanie przyjęte, zobaczycie swoje kolory.",
        "Запрос отправлен. Когда его примут, вы увидите цвета друг друга.",
        "İstek gönderildi. Kabul edilince birbirinizin renklerini göreceksiniz.",
        "Permintaan terkirim. Setelah diterima, kalian bisa melihat warna satu sama lain.",
        "リクエストを送りました。承認されると、お互いの色が見られます。",
        "요청을 보냈어요. 수락되면 서로의 색을 볼 수 있어요.",
    )
    val inviteAccepted get() = t(
        "You are friends now.",
        "Ya sois amigos.",
        "Agora vocês são amigos.",
        "Ihr seid jetzt Freunde.",
        "Vous êtes amis maintenant.",
        "Ora siete amici.",
        "Jullie zijn nu vrienden.",
        "Jesteście teraz znajomymi.",
        "Теперь вы друзья.",
        "Artık arkadaşsınız.",
        "Kalian sekarang berteman.",
        "友だちになりました。",
        "이제 친구예요.",
    )
    val inviteAlready get() = t(
        "You were already friends.",
        "Ya erais amigos.",
        "Vocês já eram amigos.",
        "Ihr seid schon Freunde.",
        "Vous étiez déjà amis.",
        "Eravate già amici.",
        "Jullie waren al vrienden.",
        "Już jesteście znajomymi.",
        "Вы уже друзья.",
        "Zaten arkadaştınız.",
        "Kalian sudah berteman.",
        "すでに友だちです。",
        "이미 친구예요.",
    )
    val inviteSelf get() = t(
        "This is your own link.",
        "Este es tu propio enlace.",
        "Este é o seu próprio link.",
        "Das ist dein eigener Link.",
        "C'est ton propre lien.",
        "Questo è il tuo link.",
        "Dit is je eigen link.",
        "To twój własny link.",
        "Это твоя собственная ссылка.",
        "Bu senin kendi bağlantın.",
        "Ini tautanmu sendiri.",
        "これはあなた自身のリンクです。",
        "내 링크예요.",
    )
    val inviteInvalid get() = t(
        "This link no longer works. Ask for a new one.",
        "Este enlace ya no vale. Pide uno nuevo.",
        "Este link não funciona mais. Peça um novo.",
        "Dieser Link gilt nicht mehr. Frag nach einem neuen.",
        "Ce lien ne marche plus. Demande-en un nouveau.",
        "Questo link non funziona più. Chiedine uno nuovo.",
        "Deze link werkt niet meer. Vraag om een nieuwe.",
        "Ten link już nie działa. Poproś o nowy.",
        "Эта ссылка больше не работает. Попроси новую.",
        "Bu bağlantı artık çalışmıyor. Yenisini iste.",
        "Tautan ini sudah tidak berlaku. Minta yang baru.",
        "このリンクはもう使えません。新しいリンクをもらってください。",
        "이 링크는 더 이상 쓸 수 없어요. 새 링크를 받아 주세요.",
    )
    val friendLimit get() = t(
        "Chroma keeps circles small: 50 friends at most, and one of you is there.",
        "Chroma mantiene los círculos pequeños: 50 amigos como mucho, y uno de los dos ya ha llegado.",
        "O Chroma mantém os círculos pequenos: no máximo 50 amigos, e um de vocês já chegou lá.",
        "Chroma hält Kreise klein: höchstens 50 Freunde, und einer von euch hat sie erreicht.",
        "Chroma garde les cercles petits : 50 amis au plus, et l'un de vous y est déjà.",
        "Chroma tiene piccole le cerchie: al massimo 50 amici, e uno di voi ci è già arrivato.",
        "Chroma houdt kringen klein: hoogstens 50 vrienden, en een van jullie zit daar al.",
        "Chroma utrzymuje małe kręgi: najwyżej 50 znajomych, a jedno z was już tyle ma.",
        "Chroma держит круги маленькими: не больше 50 друзей, и у одного из вас их уже столько.",
        "Chroma çevreleri küçük tutar: en fazla 50 arkadaş, ve biriniz bu sınıra ulaştı.",
        "Chroma menjaga lingkaran tetap kecil: paling banyak 50 teman, dan salah satu dari kalian sudah mencapainya.",
        "Chromaのつながりは小さく保たれます。友だちは最大50人で、どちらかがすでに上限です。",
        "Chroma는 관계를 작게 유지해요. 친구는 최대 50명이고, 둘 중 한 명이 이미 한도에 도달했어요.",
    )
    val inviteLimit get() = t(
        "Chroma keeps circles small. One of you has reached the limit of friends or waiting requests.",
        "Chroma mantiene los círculos pequeños. Uno de los dos ha llegado al límite de amigos o de solicitudes en espera.",
        "O Chroma mantém os círculos pequenos. Um de vocês chegou ao limite de amigos ou de pedidos em espera.",
        "Chroma hält Kreise klein. Einer von euch hat das Limit an Freunden oder offenen Anfragen erreicht.",
        "Chroma garde les cercles petits. L'un de vous a atteint la limite d'amis ou de demandes en attente.",
        "Chroma tiene piccole le cerchie. Uno di voi ha raggiunto il limite di amici o di richieste in attesa.",
        "Chroma houdt kringen klein. Een van jullie heeft het maximum aan vrienden of openstaande verzoeken bereikt.",
        "Chroma utrzymuje małe kręgi. Jedno z was osiągnęło limit znajomych lub oczekujących zaproszeń.",
        "Chroma держит круги маленькими. Один из вас достиг лимита друзей или ожидающих запросов.",
        "Chroma çevreleri küçük tutar. Biriniz arkadaş ya da bekleyen istek sınırına ulaştı.",
        "Chroma menjaga lingkaran tetap kecil. Salah satu dari kalian sudah mencapai batas teman atau permintaan yang menunggu.",
        "Chromaのつながりは小さく保たれます。どちらかが友だちか保留中のリクエストの上限に達しています。",
        "Chroma는 관계를 작게 유지해요. 둘 중 한 명이 친구 또는 대기 중인 요청의 한도에 도달했어요.",
    )
    val inviteTooMany get() = t(
        "Too many tries with links that don't work. Wait a while and try again.",
        "Demasiados intentos con enlaces que no valen. Espera un rato y vuelve a probar.",
        "Tentativas demais com links que não funcionam. Espere um pouco e tente de novo.",
        "Zu viele Versuche mit ungültigen Links. Warte eine Weile und versuch es noch einmal.",
        "Trop d'essais avec des liens qui ne marchent pas. Attends un peu et réessaie.",
        "Troppi tentativi con link che non funzionano. Aspetta un po' e riprova.",
        "Te veel pogingen met links die niet werken. Wacht even en probeer het opnieuw.",
        "Zbyt wiele prób z niedziałającymi linkami. Odczekaj chwilę i spróbuj ponownie.",
        "Слишком много попыток с нерабочими ссылками. Подожди немного и попробуй снова.",
        "Çalışmayan bağlantılarla çok fazla deneme yapıldı. Biraz bekleyip tekrar dene.",
        "Terlalu banyak percobaan dengan tautan yang tidak berlaku. Tunggu sebentar lalu coba lagi.",
        "使えないリンクでの試行が多すぎます。しばらく待ってからもう一度お試しください。",
        "쓸 수 없는 링크로 너무 많이 시도했어요. 잠시 후 다시 시도해 주세요.",
    )
    val feedToday get() = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün", "Hari ini", "今日", "오늘")
    val feedYesterday get() = t("Yesterday", "Ayer", "Ontem", "Gestern", "Hier", "Ieri", "Gisteren", "Wczoraj", "Вчера", "Dün", "Kemarin", "昨日", "어제")
    val caughtUp get() = t(
        "You're all caught up.",
        "Ya estás al día.",
        "Você está em dia.",
        "Du bist auf dem Laufenden.",
        "Tu es à jour.",
        "Sei in pari.",
        "Je bent helemaal bij.",
        "Jesteś na bieżąco.",
        "Больше ничего нового.",
        "Hepsini gördün.",
        "Kamu sudah melihat semuanya.",
        "すべて見ました。",
        "모두 확인했어요.",
    )
    val friendsRow get() = t(
        "Your friends",
        "Tus amigos",
        "Seus amigos",
        "Deine Freunde",
        "Tes amis",
        "I tuoi amici",
        "Je vrienden",
        "Twoi znajomi",
        "Твои друзья",
        "Arkadaşların",
        "Temanmu",
        "友だちリスト",
        "내 친구",
    )
    val friendYearEmpty get() = t(
        "Nothing shared this year.",
        "Nada compartido este año.",
        "Nada compartilhado este ano.",
        "Dieses Jahr nichts geteilt.",
        "Rien de partagé cette année.",
        "Niente di condiviso quest'anno.",
        "Dit jaar niets gedeeld.",
        "W tym roku nic nie udostępniono.",
        "В этом году ничем не поделились.",
        "Bu yıl paylaşılan bir şey yok.",
        "Tidak ada yang dibagikan tahun ini.",
        "今年共有されたものはありません。",
        "올해 공유한 것이 없어요.",
    )
    val report get() = t(
        "Report",
        "Reportar",
        "Denunciar",
        "Melden",
        "Signaler",
        "Segnala",
        "Melden",
        "Zgłoś",
        "Пожаловаться",
        "Şikayet et",
        "Laporkan",
        "報告",
        "신고",
    )
    val block get() = t(
        "Block",
        "Bloquear",
        "Bloquear",
        "Blockieren",
        "Bloquer",
        "Blocca",
        "Blokkeren",
        "Zablokuj",
        "Заблокировать",
        "Engelle",
        "Blokir",
        "ブロック",
        "차단",
    )
    val removeFriend get() = t(
        "Remove friend",
        "Quitar de amigos",
        "Remover amigo",
        "Freund entfernen",
        "Retirer des amis",
        "Rimuovi dagli amici",
        "Vriend verwijderen",
        "Usuń ze znajomych",
        "Удалить из друзей",
        "Arkadaşlıktan çıkar",
        "Hapus teman",
        "友だちから外す",
        "친구 삭제",
    )
    val blockedRow get() = t(
        "Blocked",
        "Bloqueados",
        "Bloqueados",
        "Blockiert",
        "Bloqués",
        "Bloccati",
        "Geblokkeerd",
        "Zablokowani",
        "Заблокированные",
        "Engellenenler",
        "Diblokir",
        "ブロック中",
        "차단됨",
    )
    val blockedEmpty get() = t(
        "You haven't blocked anyone.",
        "No has bloqueado a nadie.",
        "Você não bloqueou ninguém.",
        "Du hast niemanden blockiert.",
        "Tu n'as bloqué personne.",
        "Non hai bloccato nessuno.",
        "Je hebt niemand geblokkeerd.",
        "Nikogo nie zablokowano.",
        "Заблокированных нет.",
        "Kimseyi engellemedin.",
        "Kamu belum memblokir siapa pun.",
        "ブロックしている人はいません。",
        "차단한 사람이 없어요.",
    )
    val unblock get() = t(
        "Unblock",
        "Desbloquear",
        "Desbloquear",
        "Entsperren",
        "Débloquer",
        "Sblocca",
        "Deblokkeren",
        "Odblokuj",
        "Разблокировать",
        "Engeli kaldır",
        "Buka blokir",
        "ブロックを解除",
        "차단 해제",
    )
    val reportText get() = t(
        "The card is hidden for you now, and the report reaches Chroma, which acts within 24 hours. Nobody is told who reported it.",
        "La tarjeta se te oculta ya, y el reporte llega a Chroma, que actúa en menos de 24 horas. Nadie sabrá quién lo hizo.",
        "O cartão some para você agora, e a denúncia chega ao Chroma, que age em menos de 24 horas. Ninguém saberá quem denunciou.",
        "Die Karte wird dir sofort ausgeblendet, und die Meldung geht an Chroma, das innerhalb von 24 Stunden handelt. Niemand erfährt, wer gemeldet hat.",
        "La carte est masquée pour toi dès maintenant, et le signalement arrive à Chroma, qui agit en moins de 24 heures. Personne ne saura qui l'a signalée.",
        "La card viene nascosta subito per te, e la segnalazione arriva a Chroma, che interviene entro 24 ore. Nessuno saprà chi l'ha segnalata.",
        "De kaart is nu voor jou verborgen, en de melding gaat naar Chroma, dat binnen 24 uur handelt. Niemand hoort wie het heeft gemeld.",
        "Karta jest już dla ciebie ukryta, a zgłoszenie trafia do Chroma, które reaguje w ciągu 24 godzin. Nikt nie dowie się, kto zgłosił.",
        "Карточка уже скрыта для тебя, а жалоба уходит в Chroma, где её рассмотрят в течение 24 часов. Никто не узнает, кто пожаловался.",
        "Kart artık senden gizlendi ve şikayet Chroma'ya ulaştı; Chroma 24 saat içinde harekete geçer. Kimin şikayet ettiği kimseye söylenmez.",
        "Kartu ini sekarang disembunyikan darimu, dan laporannya sampai ke Chroma, yang bertindak dalam 24 jam. Tidak ada yang tahu siapa yang melapor.",
        "このカードはあなたには表示されなくなり、報告はChromaに届きます。Chromaは24時間以内に対応します。誰が報告したかは知らされません。",
        "이 카드는 이제 보이지 않고, 신고는 Chroma에 전달돼 24시간 안에 처리돼요. 누가 신고했는지는 아무도 알 수 없어요.",
    )
    val termsAgree get() = t(
        "By continuing you accept the terms of use: zero tolerance for objectionable content and abusive users.",
        "Al continuar aceptas los términos de uso: tolerancia cero con el contenido inaceptable y con quien abuse.",
        "Ao continuar, você aceita os termos de uso: tolerância zero com conteúdo inaceitável e com abusos.",
        "Wenn du fortfährst, akzeptierst du die Nutzungsbedingungen: null Toleranz für anstößige Inhalte und Missbrauch.",
        "En continuant, tu acceptes les conditions d'utilisation : tolérance zéro pour les contenus inacceptables et les abus.",
        "Continuando accetti i termini d'uso: tolleranza zero per i contenuti inaccettabili e per chi abusa.",
        "Door verder te gaan accepteer je de gebruiksvoorwaarden: nultolerantie voor aanstootgevende inhoud en misbruik.",
        "Kontynuując, akceptujesz warunki korzystania: zero tolerancji dla niedopuszczalnych treści i nadużyć.",
        "Продолжая, ты принимаешь условия использования: нулевая терпимость к недопустимому контенту и злоупотреблениям.",
        "Devam ederek kullanım koşullarını kabul edersin: uygunsuz içeriğe ve kötüye kullanıma sıfır tolerans.",
        "Dengan melanjutkan, kamu menyetujui ketentuan penggunaan: tanpa toleransi untuk konten yang tidak pantas dan pengguna yang kasar.",
        "続けると利用規約に同意したことになります。不適切なコンテンツや迷惑行為は一切認めません。",
        "계속하면 이용약관에 동의하게 돼요. 부적절한 콘텐츠와 악용은 절대 허용하지 않아요.",
    )
    val termsRow get() = t(
        "Terms of use",
        "Términos de uso",
        "Termos de uso",
        "Nutzungsbedingungen",
        "Conditions d'utilisation",
        "Termini d'uso",
        "Gebruiksvoorwaarden",
        "Warunki korzystania",
        "Условия использования",
        "Kullanım koşulları",
        "Ketentuan penggunaan",
        "利用規約",
        "이용약관",
    )
    val deleteAccount get() = t(
        "Delete account",
        "Borrar cuenta",
        "Excluir conta",
        "Konto löschen",
        "Supprimer le compte",
        "Elimina account",
        "Account verwijderen",
        "Usuń konto",
        "Удалить аккаунт",
        "Hesabı sil",
        "Hapus akun",
        "アカウントを削除",
        "계정 삭제",
    )
    val deleteAccountText get() = t(
        "Your name, your friends, and every color and photo you shared are erased from the server. Your journal on this phone stays as it is.",
        "Se borran del servidor tu nombre, tus amigos y todos los colores y fotos que compartiste. Tu diario en este teléfono se queda como está.",
        "Seu nome, seus amigos e todas as cores e fotos que você compartilhou são apagados do servidor. Seu diário neste telefone fica como está.",
        "Dein Name, deine Freunde und alle geteilten Farben und Fotos werden vom Server gelöscht. Dein Tagebuch auf diesem Telefon bleibt, wie es ist.",
        "Ton nom, tes amis et toutes les couleurs et photos partagées sont effacés du serveur. Ton journal sur ce téléphone reste tel quel.",
        "Il tuo nome, i tuoi amici e ogni colore e foto che hai condiviso vengono cancellati dal server. Il tuo diario su questo telefono resta com'è.",
        "Je naam, je vrienden en elke kleur en foto die je hebt gedeeld worden van de server gewist. Je dagboek op deze telefoon blijft zoals het is.",
        "Twoje imię, znajomi oraz wszystkie udostępnione kolory i zdjęcia zostaną usunięte z serwera. Twój dziennik na tym telefonie zostanie bez zmian.",
        "Твоё имя, друзья и все отправленные цвета и фото будут стёрты с сервера. Дневник на этом телефоне останется как есть.",
        "Adın, arkadaşların ve paylaştığın tüm renkler ve fotoğraflar sunucudan silinir. Bu telefondaki günlüğün olduğu gibi kalır.",
        "Nama, teman, serta semua warna dan foto yang kamu bagikan dihapus dari server. Jurnalmu di ponsel ini tetap seperti semula.",
        "名前、友だち、共有したすべての色と写真がサーバーから消去されます。この端末の日記はそのまま残ります。",
        "이름, 친구, 공유한 모든 색과 사진이 서버에서 지워져요. 이 휴대폰의 일기는 그대로 남아요.",
    )
    val inTune get() = t(
        "In tune",
        "En sintonía",
        "Em sintonia",
        "Im Einklang",
        "En accord",
        "In sintonia",
        "In harmonie",
        "Zgrani",
        "В унисон",
        "Uyum içinde",
        "Sehati",
        "おそろい",
        "닮은 색",
    )
    val requestsTitle get() = t(
        "Requests",
        "Solicitudes",
        "Pedidos",
        "Anfragen",
        "Demandes",
        "Richieste",
        "Verzoeken",
        "Zaproszenia",
        "Запросы",
        "İstekler",
        "Permintaan",
        "リクエスト",
        "요청",
    )
    val accept get() = t(
        "Accept",
        "Aceptar",
        "Aceitar",
        "Annehmen",
        "Accepter",
        "Accetta",
        "Accepteren",
        "Akceptuj",
        "Принять",
        "Kabul et",
        "Terima",
        "承認",
        "수락",
    )
    val ignore get() = t(
        "Ignore",
        "Ignorar",
        "Ignorar",
        "Ignorieren",
        "Ignorer",
        "Ignora",
        "Negeren",
        "Ignoruj",
        "Игнорировать",
        "Yok say",
        "Abaikan",
        "無視",
        "무시",
    )

    // 12. Accessibility

    val a11yBack get() = t("Back", "Volver", "Voltar", "Zurück", "Retour", "Indietro", "Terug", "Wstecz", "Назад", "Geri", "Kembali", "戻る", "뒤로")
    val a11yPreviousYear get() = t(
        "Previous year",
        "Año anterior",
        "Ano anterior",
        "Vorheriges Jahr",
        "Année précédente",
        "Anno precedente",
        "Vorig jaar",
        "Poprzedni rok",
        "Предыдущий год",
        "Önceki yıl",
        "Tahun sebelumnya",
        "前の年",
        "이전 해",
    )
    val a11yNextYear get() = t(
        "Next year",
        "Año siguiente",
        "Próximo ano",
        "Nächstes Jahr",
        "Année suivante",
        "Anno successivo",
        "Volgend jaar",
        "Następny rok",
        "Следующий год",
        "Sonraki yıl",
        "Tahun berikutnya",
        "次の年",
        "다음 해",
    )
    val a11yShare get() = t(
        "Share",
        "Compartir",
        "Compartilhar",
        "Teilen",
        "Partager",
        "Condividi",
        "Delen",
        "Udostępnij",
        "Поделиться",
        "Paylaş",
        "Bagikan",
        "共有",
        "공유",
    )
    val a11ySettings get() = t(
        "Settings",
        "Ajustes",
        "Ajustes",
        "Einstellungen",
        "Réglages",
        "Impostazioni",
        "Instellingen",
        "Ustawienia",
        "Настройки",
        "Ayarlar",
        "Pengaturan",
        "設定",
        "설정",
    )
    val a11yClose get() = t(
        "Close",
        "Cerrar",
        "Fechar",
        "Schließen",
        "Fermer",
        "Chiudi",
        "Sluiten",
        "Zamknij",
        "Закрыть",
        "Kapat",
        "Tutup",
        "閉じる",
        "닫기",
    )
    val a11yInviteQr get() = t(
        "QR code with your invite link",
        "Código QR con tu enlace de invitación",
        "Código QR com seu link de convite",
        "QR-Code mit deinem Einladungslink",
        "QR code avec ton lien d'invitation",
        "Codice QR con il tuo link di invito",
        "QR-code met je uitnodigingslink",
        "Kod QR z twoim linkiem z zaproszeniem",
        "QR-код с твоей ссылкой-приглашением",
        "Davet bağlantını içeren QR kodu",
        "Kode QR berisi tautan undanganmu",
        "招待リンクのQRコード",
        "초대 링크 QR 코드",
    )
    val a11yFriendsToday get() = t(
        "Colors your friends picked today",
        "Los colores que tus amigos eligieron hoy",
        "As cores que seus amigos escolheram hoje",
        "Die Farben, die deine Freunde heute gewählt haben",
        "Les couleurs choisies aujourd'hui par tes amis",
        "I colori scelti oggi dai tuoi amici",
        "De kleuren die je vrienden vandaag kozen",
        "Kolory, które twoi znajomi wybrali dziś",
        "Цвета, которые сегодня выбрали твои друзья",
        "Arkadaşlarının bugün seçtiği renkler",
        "Warna yang dipilih temanmu hari ini",
        "友だちが今日選んだ色",
        "친구들이 오늘 고른 색",
    )
    val a11yYearWidget get() = t(
        "Your year in color",
        "Tu año en color",
        "Seu ano em cores",
        "Dein Jahr in Farben",
        "Ton année en couleurs",
        "Il tuo anno a colori",
        "Je jaar in kleur",
        "Twój rok w kolorze",
        "Твой год в цвете",
        "Renklerle yılın",
        "Tahunmu dalam warna",
        "色でつづる1年",
        "색으로 보는 한 해",
    )
    val a11yMore get() = t(
        "More options",
        "Más opciones",
        "Mais opções",
        "Weitere Optionen",
        "Plus d'options",
        "Altre opzioni",
        "Meer opties",
        "Więcej opcji",
        "Другие действия",
        "Diğer seçenekler",
        "Opsi lainnya",
        "その他のオプション",
        "옵션 더보기",
    )
    val a11yPhoto get() = t(
        "Open the photo",
        "Abrir la foto",
        "Abrir a foto",
        "Foto öffnen",
        "Ouvrir la photo",
        "Apri la foto",
        "Open de foto",
        "Otwórz zdjęcie",
        "Открыть фото",
        "Fotoğrafı aç",
        "Buka foto",
        "写真を開く",
        "사진 열기",
    )
    val a11ySelected get() = t(
        "selected",
        "elegido",
        "escolhida",
        "ausgewählt",
        "choisie",
        "selezionato",
        "geselecteerd",
        "wybrany",
        "выбран",
        "seçili",
        "dipilih",
        "選択中",
        "선택됨",
    )

    // --- With parameters ----------------------------------------------------------------------
    // Dates are written by hand, never with a platform formatter, so a day reads the same on both
    // systems. Plurals: the singular only for 1, except Polish and Russian, which have three forms
    // ([slavic]). Japanese and Korean put the year first and the weekday last, Turkish the weekday
    // last; Polish and Russian put the month of a date in the genitive.

    /** Polish and Russian: "1 день", "2 дня", "5 дней". Anything else takes [one] or [many]. */
    private fun slavic(n: Int, one: String, few: String, many: String): String {
        val tens = n % 100
        val units = n % 10
        return when {
            lang == "pl" && n == 1 -> one
            lang == "ru" && units == 1 && tens != 11 -> one
            units in 2..4 && tens !in 12..14 -> few
            else -> many
        }
    }

    /** The month inside a date: "22 września", "22 сентября". The other languages use [monthNames]. */
    private fun monthInDate(month: Int): String = when (lang) {
        "pl" -> "stycznia, lutego, marca, kwietnia, maja, czerwca, lipca, sierpnia, września, października, listopada, grudnia"
        "ru" -> "января, февраля, марта, апреля, мая, июня, июля, августа, сентября, октября, ноября, декабря"
        else -> return monthNames()[month - 1]
    }.split(", ")[month - 1]

    /** Upper case as each language writes it: Turkish dots its capital i. */
    fun caps(text: String): String = if (lang == "tr") text.replace('i', 'İ').uppercase() else text.uppercase()

    fun shortDate(d: LocalDate): String {
        val m = monthInDate(d.month.ordinal + 1)
        val n = d.day
        return t(
            "$m $n", "$n de $m", "$n de $m", "$n. $m", "$n $m",
            "$n $m", "$n $m", "$n $m", "$n $m", "$n $m", "$n $m", "$m${n}日", "$m ${n}일",
        )
    }

    fun longDate(d: LocalDate) = withWeekday(d, shortDate(d))

    private fun withWeekday(d: LocalDate, date: String): String {
        val w = weekdayNames()[d.dayOfWeek.ordinal]
        val up = w.replaceFirstChar { it.uppercase() }
        return t(
            "$up, $date", "$up, $date", "$up, $date", "$up, $date", "$up $date",
            "$up $date", "$up $date", "$up, $date", "$up, $date", "$date $w", "$up, $date", "$date $w", "$date $w",
        )
    }

    fun dayMonthYear(d: LocalDate): String {
        val date = shortDate(d)
        val y = d.year
        return t(
            "$date, $y", "$date de $y", "$date de $y", "$date $y", "$date $y",
            "$date $y", "$date $y", "$date $y", "$date $y", "$date $y", "$date $y", "${y}年$date", "${y}년 $date",
        )
    }

    /** The two halves of the masthead beside the big day number. */
    fun weekday(d: LocalDate) = weekdayNames()[d.dayOfWeek.ordinal]
    fun monthOf(d: LocalDate, withYear: Boolean = false): String {
        val m = monthNames()[d.month.ordinal]
        if (!withYear) return m
        val y = d.year
        return t("$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "$m $y", "${y}年$m", "${y}년 $m")
    }

    fun longDateWithYear(d: LocalDate) = withWeekday(d, dayMonthYear(d))

    fun abbrDateWithYear(d: LocalDate): String {
        val m = monthShort()[d.month.ordinal]
        val n = d.day
        val y = d.year
        return t(
            "$m $n, $y", "$n $m $y", "$n $m $y", "$n. $m $y", "$n $m $y",
            "$n $m $y", "$n $m $y", "$n $m $y", "$n $m $y", "$n $m $y", "$n $m $y", "${y}年$m${n}日", "${y}년 $m ${n}일",
        )
    }

    /** Always 24 hours, in every language. */
    fun clock(hour: Int, minute: Int) =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    fun counter(n: Int, max: Int) = "$n/$max"

    fun reminderAt(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t(
            "At $time", "A las $time", "Às $time", "Um $time", "À $time",
            "Alle $time", "Om $time", "O $time", "В $time", "Saat $time", "Pukul $time", "$time", "$time",
        )
    }

    fun offerReminder(hour: Int, minute: Int): String {
        val time = clock(hour, minute)
        return t(
            "Remind you every day at $time?",
            "¿Te lo recuerdo cada día a las $time?",
            "Quer que eu lembre você todo dia às $time?",
            "Soll ich dich jeden Tag um $time erinnern?",
            "Je te le rappelle tous les jours à $time ?",
            "Te lo ricordo ogni giorno alle $time?",
            "Zal ik je elke dag om $time herinneren?",
            "Przypominać ci codziennie o $time?",
            "Напоминать тебе каждый день в $time?",
            "Sana her gün $time saatinde hatırlatayım mı?",
            "Ingatkan kamu setiap hari pukul $time?",
            "毎日${time}にお知らせしましょうか？",
            "매일 ${time}에 알려 드릴까요?",
        )
    }

    fun lastBackup(d: LocalDate): String {
        val date = abbrDateWithYear(d)
        return t(
            "Last backup: $date", "Última copia: $date", "Última cópia: $date", "Letzte Sicherung: $date", "Dernière copie : $date",
            "Ultima copia: $date", "Laatste back-up: $date", "Ostatnia kopia: $date", "Последняя копия: $date",
            "Son yedek: $date", "Cadangan terakhir: $date", "前回のバックアップ: $date", "마지막 백업: $date",
        )
    }

    fun removeFriendText(name: String) = t(
        "$name will not be told. You both stop seeing each other's days.",
        "$name no recibirá ningún aviso. Dejaréis de ver vuestros días.",
        "$name não será avisado. Vocês deixam de ver os dias um do outro.",
        "$name erfährt nichts davon. Ihr seht eure Tage nicht mehr.",
        "$name n'en saura rien. Vous ne verrez plus vos jours.",
        "$name non riceverà nessun avviso. Smetterete di vedere i vostri giorni.",
        "$name krijgt hier niets over te horen. Jullie zien elkaars dagen niet meer.",
        "$name nie dostanie żadnego powiadomienia. Przestaniecie widzieć swoje dni.",
        "$name ничего не узнает. Вы перестанете видеть дни друг друга.",
        "$name bundan haberdar edilmez. Artık birbirinizin günlerini görmezsiniz.",
        "$name tidak akan diberi tahu. Kalian tidak lagi melihat hari satu sama lain.",
        "${name}には通知されません。お互いの日が見えなくなります。",
        "${name}에게는 알리지 않아요. 서로의 날을 더 이상 볼 수 없어요.",
    )

    fun blockText(name: String) = t(
        "$name stops seeing you and cannot invite you again. Nobody is told.",
        "$name deja de verte y no podrá volver a invitarte. Nadie recibe ningún aviso.",
        "$name deixa de te ver e não poderá te convidar de novo. Ninguém é avisado.",
        "$name sieht dich nicht mehr und kann dich nicht wieder einladen. Niemand wird benachrichtigt.",
        "$name ne te verra plus et ne pourra plus t'inviter. Personne n'est prévenu.",
        "$name non ti vedrà più e non potrà invitarti di nuovo. Nessuno riceve avvisi.",
        "$name ziet je niet meer en kan je niet opnieuw uitnodigen. Niemand krijgt een melding.",
        "$name przestanie cię widzieć i nie wyśle ci już zaproszenia. Nikt nie dostanie powiadomienia.",
        "$name перестанет тебя видеть и не сможет снова пригласить. Никто не получит уведомления.",
        "$name artık seni göremez ve seni yeniden davet edemez. Kimseye bildirilmez.",
        "$name tidak bisa melihatmu lagi dan tidak bisa mengundangmu lagi. Tidak ada yang diberi tahu.",
        "${name}からあなたが見えなくなり、再び招待されることもありません。誰にも通知されません。",
        "더 이상 ${name}에게 내가 보이지 않고, 다시 초대받을 수도 없어요. 아무에게도 알리지 않아요.",
    )

    fun unblockText(name: String) = t(
        "$name will be able to send you a request again. Nobody is told.",
        "$name podrá volver a enviarte una solicitud. Nadie recibe ningún aviso.",
        "$name poderá te enviar um pedido de novo. Ninguém é avisado.",
        "$name kann dir wieder eine Anfrage schicken. Niemand wird benachrichtigt.",
        "$name pourra de nouveau t'envoyer une demande. Personne n'est prévenu.",
        "$name potrà di nuovo inviarti una richiesta. Nessuno riceve avvisi.",
        "$name kan je weer een verzoek sturen. Niemand krijgt een melding.",
        "$name znów będzie mieć możliwość wysłania ci zaproszenia. Nikt nie dostanie powiadomienia.",
        "$name снова сможет отправить тебе запрос. Никто не получит уведомления.",
        "$name sana yeniden istek gönderebilecek. Kimseye bildirilmez.",
        "$name bisa mengirimimu permintaan lagi. Tidak ada yang diberi tahu.",
        "${name}から再びリクエストを受け取れるようになります。誰にも通知されません。",
        "${name}에게서 다시 요청을 받을 수 있어요. 아무에게도 알리지 않아요.",
    )

    fun weekHint(name: String) = t(
        "Color of the week: $name",
        "Color de la semana: $name",
        "Cor da semana: $name",
        "Farbe der Woche: $name",
        "Couleur de la semaine : $name",
        "Colore della settimana: $name",
        "Kleur van de week: $name",
        "Kolor tygodnia: $name",
        "Цвет недели: $name",
        "Haftanın rengi: $name",
        "Warna minggu ini: $name",
        "今週の色: $name",
        "이번 주의 색: $name",
    )

    fun statsTitle(year: Int) = t(
        "$year in words", "$year en palabras", "$year em palavras", "$year in Worten", "$year en mots",
        "$year in parole", "$year in woorden", "$year w słowach", "$year в словах", "Kelimelerle $year",
        "$year dalam kata", "ことばで見る${year}年", "글로 보는 ${year}년",
    )

    /** [month] from 1 to 12. */
    fun statsWarmest(month: Int): String {
        val m = monthNames()[month - 1]
        return t(
            "The warmest month was $m.",
            "El mes más cálido fue $m.",
            "O mês mais quente foi $m.",
            "Der wärmste Monat war der $m.",
            "Le mois le plus chaud a été $m.",
            "Il mese più caldo è stato $m.",
            "De warmste maand was $m.",
            "Najcieplejszy miesiąc: $m.",
            "Самый тёплый месяц: $m.",
            "En sıcak ay $m oldu.",
            "Bulan paling hangat adalah $m.",
            "いちばん暖かい月は${m}でした。",
            "가장 따뜻한 달은 ${m}이었어요.",
        )
    }

    /** [month] from 1 to 12. */
    fun statsColdest(month: Int): String {
        val m = monthNames()[month - 1]
        return t(
            "The coldest was $m.",
            "El más frío, $m.",
            "O mais frio, $m.",
            "Der kälteste war der $m.",
            "Le plus froid, $m.",
            "Il più freddo, $m.",
            "De koudste was $m.",
            "Najzimniejszy: $m.",
            "Самый холодный: $m.",
            "En soğuk ay $m oldu.",
            "Yang paling dingin, $m.",
            "いちばん寒い月は${m}でした。",
            "가장 차가운 달은 ${m}이었어요.",
        )
    }

    fun statsRepeated(name: String) = t(
        "The color that came back most: $name.",
        "El color que más volvió: $name.",
        "A cor que mais voltou: $name.",
        "Die Farbe, die am öftesten wiederkam: $name.",
        "La couleur qui est le plus revenue : $name.",
        "Il colore che è tornato di più: $name.",
        "De kleur die het vaakst terugkwam: $name.",
        "Kolor, który wracał najczęściej: $name.",
        "Цвет, который возвращался чаще всего: $name.",
        "En sık geri gelen renk: $name.",
        "Warna yang paling sering kembali: $name.",
        "いちばんよく戻ってきた色は${name}でした。",
        "가장 자주 돌아온 색: $name.",
    )

    /** [from] and [to] from 1 to 12: Polish and Russian decline them. */
    fun statsGreyest(from: Int, to: Int): String {
        val a = monthNames()[from - 1]
        val b = monthNames()[to - 1]
        return t(
            "The greyest stretch: $a to $b.",
            "La estación más gris: de $a a $b.",
            "A estação mais cinzenta: de $a a $b.",
            "Die grauste Jahreszeit: $a bis $b.",
            "La saison la plus grise : de $a à $b.",
            "Il periodo più grigio: da $a a $b.",
            "De grijste periode: $a tot $b.",
            "Najszarszy okres: od ${monthInDate(from)} do ${monthInDate(to)}.",
            "Самый серый период: с ${monthInDate(from)} по $b.",
            "En gri dönem: $a ile $b arası.",
            "Masa paling kelabu: $a sampai $b.",
            "いちばん灰色だったのは${a}から${b}まで。",
            "가장 잿빛이었던 시기: ${a}부터 ${b}까지.",
        )
    }

    fun statsWarmer(year: Int, before: Int) = t(
        "$year was warmer than $before.",
        "$year fue más cálido que $before.",
        "$year foi mais quente que $before.",
        "$year war wärmer als $before.",
        "$year a été plus chaud que $before.",
        "Il $year è stato più caldo del $before.",
        "$year was warmer dan $before.",
        "Rok $year był cieplejszy niż $before.",
        "$year год был теплее, чем $before.",
        "$year, $before yılından daha sıcaktı.",
        "$year lebih hangat daripada $before.",
        "${year}年は${before}年より暖かい色でした。",
        "${year}년은 ${before}년보다 따뜻했어요.",
    )

    fun statsCooler(year: Int, before: Int) = t(
        "$year was cooler than $before.",
        "$year fue más frío que $before.",
        "$year foi mais frio que $before.",
        "$year war kühler als $before.",
        "$year a été plus froid que $before.",
        "Il $year è stato più freddo del $before.",
        "$year was koeler dan $before.",
        "Rok $year był chłodniejszy niż $before.",
        "$year год был прохладнее, чем $before.",
        "$year, $before yılından daha serindi.",
        "$year lebih sejuk daripada $before.",
        "${year}年は${before}年より涼しい色でした。",
        "${year}년은 ${before}년보다 차가웠어요.",
    )

    fun statsAlike(year: Int, before: Int) = t(
        "$year looked a lot like $before.",
        "$year se pareció mucho a $before.",
        "$year foi muito parecido com $before.",
        "$year ähnelte $before sehr.",
        "$year a beaucoup ressemblé à $before.",
        "Il $year somigliava molto al $before.",
        "$year leek erg op $before.",
        "Rok $year był bardzo podobny do roku $before.",
        "$year год был очень похож на $before.",
        "$year, $before yılına çok benziyordu.",
        "$year sangat mirip dengan $before.",
        "${year}年は${before}年とよく似ていました。",
        "${year}년은 ${before}년과 많이 닮았어요.",
    )

    fun inviteMessage(link: String) = t(
        "Add me on Chroma: $link",
        "Agrégame en Chroma: $link",
        "Me adicione no Chroma: $link",
        "Füg mich in Chroma hinzu: $link",
        "Ajoute-moi sur Chroma : $link",
        "Aggiungimi su Chroma: $link",
        "Voeg me toe op Chroma: $link",
        "Dodaj mnie w Chroma: $link",
        "Добавь меня в Chroma: $link",
        "Beni Chroma'da ekle: $link",
        "Tambahkan aku di Chroma: $link",
        "Chromaで友だちになろう: $link",
        "Chroma에서 친구 추가해 줘: $link",
    )

    fun version(v: String) = t(
        "Version $v", "Versión $v", "Versão $v", "Version $v", "Version $v",
        "Versione $v", "Versie $v", "Wersja $v", "Версия $v", "Sürüm $v", "Versi $v", "バージョン $v", "버전 $v",
    )

    /** The phone wins every day both have, so only the new ones are worth counting. */
    fun importSummary(added: Int, kept: Int, photos: Int = 0): String {
        val one = added == 1
        val head = t(
            if (one) "The backup brings 1 new day." else "The backup brings $added new days.",
            if (one) "La copia trae 1 día nuevo." else "La copia trae $added días nuevos.",
            if (one) "A cópia traz 1 dia novo." else "A cópia traz $added dias novos.",
            if (one) "Die Sicherung bringt 1 neuen Tag." else "Die Sicherung bringt $added neue Tage.",
            if (one) "La copie apporte 1 nouveau jour." else "La copie apporte $added nouveaux jours.",
            if (one) "La copia porta 1 giorno nuovo." else "La copia porta $added giorni nuovi.",
            if (one) "De back-up bevat 1 nieuwe dag." else "De back-up bevat $added nieuwe dagen.",
            "Kopia zawiera " + slavic(added, "1 nowy dzień", "$added nowe dni", "$added nowych dni") + ".",
            "В копии " + slavic(added, "$added новый день", "$added новых дня", "$added новых дней") + ".",
            "Yedekte $added yeni gün var.",
            "Cadangan ini membawa $added hari baru.",
            "このバックアップには新しい日が${added}日分あります。",
            "이 백업에는 새로운 날이 ${added}일 있어요.",
        )
        val tail = if (kept == 0) {
            t(
                " Nothing gets deleted.", " No se borra nada.", " Nada é apagado.", " Es wird nichts gelöscht.", " Rien n'est supprimé.",
                " Non viene eliminato nulla.", " Er wordt niets verwijderd.", " Nic nie zostanie usunięte.", " Ничего не удаляется.",
                " Hiçbir şey silinmez.", " Tidak ada yang dihapus.", "何も削除されません。", " 아무것도 지워지지 않아요.",
            )
        } else {
            t(
                " Days already on this phone stay as they are.",
                " Los días que ya están en este teléfono se quedan como están.",
                " Os dias que já estão neste telefone ficam como estão.",
                " Tage, die schon auf diesem Handy sind, bleiben, wie sie sind.",
                " Les jours déjà sur ce téléphone restent tels quels.",
                " I giorni già presenti su questo telefono restano come sono.",
                " Dagen die al op deze telefoon staan, blijven zoals ze zijn.",
                " Dni, które już są na tym telefonie, zostają bez zmian.",
                " Дни, которые уже есть на этом телефоне, остаются как есть.",
                " Bu telefonda zaten olan günler olduğu gibi kalır.",
                " Hari yang sudah ada di ponsel ini tetap seperti semula.",
                "この端末にすでにある日はそのまま残ります。",
                " 이 휴대폰에 이미 있는 날은 그대로 남아요.",
            )
        }
        val back = if (photos == 0) "" else t(
            if (photos == 1) " It also brings back 1 photo missing from this phone." else " It also brings back $photos photos missing from this phone.",
            if (photos == 1) " También devuelve 1 foto que faltaba en este teléfono." else " También devuelve $photos fotos que faltaban en este teléfono.",
            if (photos == 1) " Também devolve 1 foto que faltava neste telefone." else " Também devolve $photos fotos que faltavam neste telefone.",
            if (photos == 1) " Außerdem bringt sie 1 Foto zurück, das auf diesem Handy fehlte." else " Außerdem bringt sie $photos Fotos zurück, die auf diesem Handy fehlten.",
            if (photos == 1) " Elle rend aussi 1 photo qui manquait sur ce téléphone." else " Elle rend aussi $photos photos qui manquaient sur ce téléphone.",
            if (photos == 1) " Riporta anche 1 foto che mancava su questo telefono." else " Riporta anche $photos foto che mancavano su questo telefono.",
            if (photos == 1) " Ook komt 1 foto terug die op deze telefoon ontbrak." else " Ook komen $photos foto's terug die op deze telefoon ontbraken.",
            " Przywraca też " + slavic(
                photos,
                "1 zdjęcie, którego brakowało",
                "$photos zdjęcia, których brakowało",
                "$photos zdjęć, których brakowało",
            ) + " na tym telefonie.",
            " Также возвращает " + slavic(
                photos,
                "$photos фото, которого не было",
                "$photos фото, которых не было",
                "$photos фото, которых не было",
            ) + " на этом телефоне.",
            " Bu telefonda eksik olan $photos fotoğrafı da geri getirir.",
            " Juga mengembalikan $photos foto yang hilang dari ponsel ini.",
            "この端末になかった写真${photos}枚も戻ります。",
            " 이 휴대폰에 없던 사진 ${photos}장도 되돌려요.",
        )
        return head + tail + back
    }

    fun importDone(n: Int, photos: Int = 0): String {
        val one = photos == 1
        val recovered = if (photos == 0) null else t(
            if (one) "1 photo recovered." else "$photos photos recovered.",
            if (one) "1 foto recuperada." else "$photos fotos recuperadas.",
            if (one) "1 foto recuperada." else "$photos fotos recuperadas.",
            if (one) "1 Foto wiederhergestellt." else "$photos Fotos wiederhergestellt.",
            if (one) "1 photo récupérée." else "$photos photos récupérées.",
            if (one) "1 foto recuperata." else "$photos foto recuperate.",
            if (one) "1 foto hersteld." else "$photos foto's hersteld.",
            slavic(photos, "Odzyskano 1 zdjęcie.", "Odzyskano $photos zdjęcia.", "Odzyskano $photos zdjęć."),
            slavic(photos, "Восстановлено $photos фото.", "Восстановлено $photos фото.", "Восстановлено $photos фото."),
            "$photos fotoğraf kurtarıldı.",
            "$photos foto dipulihkan.",
            "写真を${photos}枚復元しました。",
            "사진 ${photos}장을 복구했어요.",
        )
        if (n == 0 && recovered != null) return recovered
        return listOfNotNull(importedDays(n), recovered).joinToString(" ")
    }

    private fun importedDays(n: Int): String {
        if (n == 0) {
            return t(
                "Up to date: there was nothing new.",
                "Al día: no había nada nuevo.",
                "Em dia: não havia nada novo.",
                "Aktuell: es gab nichts Neues.",
                "À jour : il n'y avait rien de nouveau.",
                "Tutto aggiornato: non c'era niente di nuovo.",
                "Bijgewerkt: er was niets nieuws.",
                "Aktualne: nie było nic nowego.",
                "Всё актуально: ничего нового не было.",
                "Güncel: yeni bir şey yoktu.",
                "Sudah terbaru: tidak ada yang baru.",
                "最新です。新しいものはありませんでした。",
                "최신 상태예요. 새로운 것이 없었어요.",
            )
        }
        val one = n == 1
        return t(
            if (one) "1 day added." else "$n days added.",
            if (one) "1 día añadido." else "$n días añadidos.",
            if (one) "1 dia adicionado." else "$n dias adicionados.",
            if (one) "1 Tag hinzugefügt." else "$n Tage hinzugefügt.",
            if (one) "1 jour ajouté." else "$n jours ajoutés.",
            if (one) "1 giorno aggiunto." else "$n giorni aggiunti.",
            if (one) "1 dag toegevoegd." else "$n dagen toegevoegd.",
            slavic(n, "Dodano 1 dzień.", "Dodano $n dni.", "Dodano $n dni."),
            slavic(n, "Добавлен $n день.", "Добавлено $n дня.", "Добавлено $n дней."),
            "$n gün eklendi.",
            "$n hari ditambahkan.",
            "${n}日分を追加しました。",
            "${n}일을 추가했어요.",
        )
    }

    fun buy(price: String) = t(
        "Buy for $price", "Comprar por $price", "Comprar por $price", "Für $price kaufen", "Acheter pour $price",
        "Acquista a $price", "Kopen voor $price", "Kup za $price", "Купить за $price", "$price ile satın al",
        "Beli seharga $price", "${price}で購入", "${price}에 구매",
    )

    /** Two candidates often share a nearest name: the place in the row tells them apart. */
    fun a11ySwatch(key: String, selected: Boolean, n: Int, of: Int) =
        colorName(key) + ", " + t(
            "$n of $of", "$n de $of", "$n de $of", "$n von $of", "$n sur $of",
            "$n di $of", "$n van $of", "$n z $of", "$n из $of", "$n/$of", "$n dari $of", "$n/$of", "${of}개 중 ${n}번째",
        ) + if (selected) ", $a11ySelected" else ""

    fun a11yYearStrip(year: Int, days: Int): String {
        val one = days == 1
        return "$year, " + t(
            if (one) "1 day with a color" else "$days days with a color",
            if (one) "1 día con color" else "$days días con color",
            if (one) "1 dia com cor" else "$days dias com cor",
            if (one) "1 Tag mit Farbe" else "$days Tage mit Farbe",
            if (one) "1 jour en couleur" else "$days jours en couleur",
            if (one) "1 giorno con un colore" else "$days giorni con un colore",
            if (one) "1 dag met een kleur" else "$days dagen met een kleur",
            slavic(days, "1 dzień z kolorem", "$days dni z kolorem", "$days dni z kolorem"),
            slavic(days, "$days день с цветом", "$days дня с цветом", "$days дней с цветом"),
            "$days renkli gün",
            "$days hari berwarna",
            "色のある日 ${days}日",
            "색이 있는 날 ${days}일",
        )
    }

    fun a11yDay(d: LocalDate, key: String?): String {
        val state = key?.let(::colorName) ?: t(
            "no color", "sin color", "sem cor", "keine Farbe", "pas de couleur",
            "nessun colore", "geen kleur", "bez koloru", "без цвета", "renk yok", "tanpa warna", "色なし", "색 없음",
        )
        return "${shortDate(d)}, $state"
    }
}
