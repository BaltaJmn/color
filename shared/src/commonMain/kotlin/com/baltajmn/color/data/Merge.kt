package com.baltajmn.color.data

import com.baltajmn.color.model.Journal

data class MergeResult(
    val journal: Journal,
    /** Dates only the backup had. */
    val added: Int,
    /** Dates on both sides, where this phone's day stays. */
    val kept: Int,
    /** Photo names of the backup that have to be brought over. */
    val photosFromIncoming: Set<String>,
)

/**
 * Importing joins, it never replaces: a day this phone has stays exactly as it is, so a backup
 * imported by mistake costs nothing. A day has one color, and there is no sensible way to mix two.
 *
 * Pure. The repository is the one that copies the photos and saves.
 */
fun merge(device: Journal, incoming: Journal): MergeResult {
    val added = incoming.filterKeys { it !in device }
    return MergeResult(
        journal = device + added,
        added = added.size,
        kept = incoming.size - added.size,
        photosFromIncoming = added.values.mapNotNull { it.photo }.toSet(),
    )
}

/** True for a zip entry a ChromaEntry could point at. Keeps a foreign file from writing anywhere. */
fun isPhotoName(name: String): Boolean =
    name.startsWith(PHOTOS_DIR) && !name.substringAfter(PHOTOS_DIR).contains('/')
