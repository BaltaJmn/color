package com.baltajmn.color.model

/** The optional word under the color: a word or two, not a diary. That is what Purl is for. */
const val WORD_MAX = 24

/** Characters as a person counts them: a surrogate pair (most emoji) is one. */
fun String.codePointCount(): Int {
    var n = 0
    var i = 0
    while (i < length) {
        i += widthAt(i)
        n++
    }
    return n
}

/**
 * The first [n] code points, never splitting a surrogate pair, and never stopping right before a
 * code point that belongs to the previous one: a heart without its VS16 or a thumb without its
 * skin tone is a different character.
 *
 * ponytail: a flag (two regional indicators) cut at the exact boundary still loses its second
 * half. Needs a grapheme breaker if it ever shows up.
 */
fun String.clampCodePoints(n: Int): String {
    var i = 0
    var taken = 0
    while (i < length && taken < n) {
        i += widthAt(i)
        taken++
    }
    while (i in 1 until length && (codePointAt(i).continuesCluster() || this[i - 1].code == ZWJ)) i = startBefore(i)
    return substring(0, i)
}

/** What the field accepts, and where its cursor goes. */
data class Edit(val text: String, val cursor: Int)

/**
 * The edit the field accepts. [old] is the text before the keystroke, [new] what the keyboard or
 * the paste proposes and [cursor] where the keyboard left the cursor in [new]. Only the inserted
 * segment is trimmed, so pasting into a full word never cuts its end, and a text already over the
 * limit (imported or dictated) can shrink but not grow.
 */
fun limitEdit(old: String, new: String, cursor: Int, limit: Int = WORD_MAX): Edit {
    val allowed = maxOf(limit, old.codePointCount())
    if (new.codePointCount() <= allowed) return Edit(new, cursor)
    // The insertion ends at the cursor, so what follows it was already there. Guessing it from the
    // common prefix instead eats the next letter when the paste starts with that same letter.
    val tail = new.length - cursor
    var q: Int
    var p: Int
    if (cursor in 0..new.length && tail <= old.length && old.endsWith(new.substring(cursor))) {
        q = tail
        p = minOf(new.commonPrefixWith(old).length, cursor, old.length - q)
    } else {
        // A keyboard that reports a cursor not matching the text: fall back to the longest prefix.
        p = new.commonPrefixWith(old).length
        q = minOf(new.commonSuffixWith(old).length, new.length - p, old.length - p)
    }
    if (p > 0 && new[p - 1].isHighSurrogate()) p--
    if (q > 0 && new[new.length - q].isLowSurrogate()) q--
    val head = new.substring(0, p)
    val end = new.substring(new.length - q)
    val room = (allowed - (head + end).codePointCount()).coerceAtLeast(0)
    val kept = new.substring(p, new.length - q).clampCodePoints(room)
    return Edit(head + kept + end, p + kept.length)
}

private const val ZWJ = 0x200D

private fun String.widthAt(i: Int) = if (this[i].isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) 2 else 1

private fun String.codePointAt(i: Int): Int =
    if (widthAt(i) == 2) 0x10000 + ((this[i].code - 0xD800) shl 10) + (this[i + 1].code - 0xDC00) else this[i].code

private fun String.startBefore(i: Int) = if (i >= 2 && this[i - 1].isLowSurrogate() && this[i - 2].isHighSurrogate()) i - 2 else i - 1

/** Combining marks, VS16, the zero width joiner and the skin tones only ever extend what precedes them. */
private fun Int.continuesCluster() = this in 0x300..0x36F || this == 0xFE0F || this == ZWJ || this in 0x1F3FB..0x1F3FF
