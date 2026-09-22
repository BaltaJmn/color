package com.baltajmn.color.data

/**
 * The journal file, its previous version and the photos, in the app's private storage and never in
 * the App Group: the widgets only ever see widget.json.
 *
 * There is no cloud copy, so these files are the user's years of color. A write can never leave the main
 * file truncated, and a file that could not be read is moved aside, never overwritten.
 */
expect object Storage {
    /** entries.json, or null when it does not exist. */
    fun read(): String?

    /** entries.bak.json: the version before the last write. */
    fun readPrevious(): String?

    /** Atomic: the current file becomes the .bak and the new text replaces it in one rename. */
    fun write(text: String)

    /** Rewrites entries.json from a good .bak without rotating it, so the good copy survives. */
    fun restoreMain(text: String)

    /** Moves both files to corrupt/entries-<yyyyMMdd-HHmmss>.json and .bak.json. */
    fun quarantine()

    fun writePhoto(name: String, bytes: ByteArray)
    fun readPhoto(name: String): ByteArray?
    fun deletePhoto(name: String)
    fun listPhotos(): List<String>

    /** Path of import/, emptied and created. */
    fun importDir(): String

    /** A photo of a backup being read, parked in import/ until the user confirms. */
    fun writeImport(name: String, bytes: ByteArray)

    /** Moves an accepted photo from import/ into photos/, under the name the entry will carry. */
    fun adoptImport(name: String, asName: String)

    /** Friends' photos, in the system cache: it may be emptied at any time and they come back. */
    fun readCached(name: String): ByteArray?
    fun writeCached(name: String, bytes: ByteArray)

    /** Deletes every cached photo not named here: what the feed no longer shows goes. */
    fun keepCached(names: Set<String>)
}
