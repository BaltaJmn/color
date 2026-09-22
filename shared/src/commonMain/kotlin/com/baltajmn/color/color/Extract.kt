package com.baltajmn.color.color

import kotlin.random.Random

const val MAX_SWATCHES = 5
const val KMEANS_K = 8
const val KMEANS_ITERATIONS = 12
const val KMEANS_SEED = 7
const val MERGE_DELTA_E = 10.0

/** Groups under 1 % of the photo are noise: a reflection, a sensor speck. */
const val MIN_SHARE = 0.01

/** Chroma a group needs to count as "the vivid one" that always gets offered. */
const val VIVID_CHROMA = 20.0

/** One candidate: a color and how much of the photo it covers. */
data class Swatch(val color: String, val share: Double)

private class Group(var l: Double, var a: Double, var b: Double, var n: Int) {
    val lab get() = Lab(l, a, b)

    fun absorb(o: Group) {
        val total = n + o.n
        l = (l * n + o.l * o.n) / total
        a = (a * n + o.a * o.n) / total
        b = (b * n + o.b * o.n) / total
        n = total
    }
}

/**
 * The colors a photo offers, most present first (docs/tecnico.md 6.3). Deterministic: the same
 * pixels give the same swatches on both platforms, because everything past decoding runs here.
 */
fun extractSwatches(pixels: IntArray): List<Swatch> {
    val labs = pixels.filter { (it ushr 24) >= 128 }.map(::labOf)
    if (labs.isEmpty()) return emptyList()
    val n = labs.size
    val groups = kmeans(labs)

    // Two centers that look alike are one color the photo happens to have a lot of.
    while (groups.size > 1) {
        var best = Double.MAX_VALUE
        var bi = -1
        var bj = -1
        for (i in groups.indices) for (j in i + 1 until groups.size) {
            val d = deltaE(groups[i].lab, groups[j].lab)
            if (d < best) {
                best = d
                bi = i
                bj = j
            }
        }
        if (best >= MERGE_DELTA_E) break
        groups[bi].absorb(groups[bj])
        groups.removeAt(bj)
    }

    val kept = groups.filter { it.n.toDouble() / n >= MIN_SHARE }.ifEmpty { listOf(groups.maxBy { it.n }) }
    val byWeight = kept.sortedByDescending { it.n }
    val chosen = mutableListOf<Group>()
    // A beige room still has a red mug in it: the most colorful group is always on offer.
    kept.maxBy { it.lab.chroma }.takeIf { it.lab.chroma >= VIVID_CHROMA }?.let(chosen::add)
    for (g in byWeight) {
        if (chosen.size == MAX_SWATCHES) break
        if (g in chosen) continue
        if (chosen.all { deltaE(it.lab, g.lab) >= MERGE_DELTA_E }) chosen += g
    }
    return chosen.sortedByDescending { it.n }.map { Swatch(hexOf(it.lab.toRgb()), it.n.toDouble() / n) }
}

private fun kmeans(points: List<Lab>): MutableList<Group> {
    val random = Random(KMEANS_SEED)
    val centers = mutableListOf(points[random.nextInt(points.size)])
    val nearest = DoubleArray(points.size) { Double.MAX_VALUE }
    // k-means++: each new center is drawn with probability proportional to its squared distance.
    while (centers.size < KMEANS_K) {
        val last = centers.last()
        var sum = 0.0
        for (i in points.indices) {
            val d = deltaE(points[i], last)
            if (d * d < nearest[i]) nearest[i] = d * d
            sum += nearest[i]
        }
        if (sum == 0.0) break
        var r = random.nextDouble() * sum
        var pick = points.lastIndex
        for (i in points.indices) {
            r -= nearest[i]
            if (r <= 0.0) {
                pick = i
                break
            }
        }
        centers += points[pick]
    }

    val assignment = IntArray(points.size) { -1 }
    var groups = centers.map { Group(it.l, it.a, it.b, 0) }
    repeat(KMEANS_ITERATIONS) {
        var changed = false
        for (i in points.indices) {
            var best = 0
            var bestD = Double.MAX_VALUE
            for (c in groups.indices) {
                val d = deltaE(points[i], groups[c].lab)
                if (d < bestD) {
                    bestD = d
                    best = c
                }
            }
            if (assignment[i] != best) {
                assignment[i] = best
                changed = true
            }
        }
        val sums = Array(groups.size) { DoubleArray(4) }
        for (i in points.indices) {
            val s = sums[assignment[i]]
            s[0] += points[i].l
            s[1] += points[i].a
            s[2] += points[i].b
            s[3] += 1.0
        }
        groups = groups.mapIndexed { c, g ->
            val s = sums[c]
            if (s[3] == 0.0) Group(g.l, g.a, g.b, 0) else Group(s[0] / s[3], s[1] / s[3], s[2] / s[3], s[3].toInt())
        }
        if (!changed) return groups.filter { it.n > 0 }.toMutableList()
    }
    return groups.filter { it.n > 0 }.toMutableList()
}
