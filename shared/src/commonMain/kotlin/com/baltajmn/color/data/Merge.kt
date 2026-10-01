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
    /** Days of this phone whose photo file is missing, to the backup photo that takes its place. */
    val recovered: Map<String, String> = emptyMap(),
)

/**
 * Importing joins, it never replaces: a day this phone has stays exactly as it is, so a backup
 * imported by mistake costs nothing. A day has one color, and there is no sensible way to mix two.
 *
 * One exception, for the photo alone: a day whose photo file is gone ([missing], names this phone
 * points at and does not have) takes the backup's. That is the phone restored from Android's cloud
 * copy, which carries the days and not the photos; the zip is the way to bring them back.
 *
 * Pure. The repository is the one that copies the photos and saves.
 */
fun merge(device: Journal, incoming: Journal, missing: Set<String> = emptySet()): MergeResult {
    val added = incoming.filterKeys { it !in device }
    val recovered = device.mapNotNull { (day, entry) ->
        if (entry.photo == null || entry.photo !in missing) null else incoming[day]?.photo?.let { day to it }
    }.toMap()
    return MergeResult(
        journal = device + added,
        added = added.size,
        kept = incoming.size - added.size,
        photosFromIncoming = added.values.mapNotNull { it.photo }.toSet() + recovered.values,
        recovered = recovered,
    )
}

/** True for a zip entry a ChromaEntry could point at. Keeps a foreign file from writing anywhere. */
fun isPhotoName(name: String): Boolean =
    name.startsWith(PHOTOS_DIR) && !name.substringAfter(PHOTOS_DIR).contains('/')
