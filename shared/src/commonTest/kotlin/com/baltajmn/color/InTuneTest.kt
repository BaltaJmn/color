package com.baltajmn.color

import com.baltajmn.color.color.deltaE
import com.baltajmn.color.color.labOf
import com.baltajmn.color.social.SYNC_DELTA_E
import com.baltajmn.color.social.inTune
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// 14. docs/tecnico.md 6.11.
class InTuneTest {

    @Test
    fun theThresholdIsStrictRightAtTheEdge() {
        // Walk blue away from a grey until the distance first reaches the knob: the step before is in
        // tune, that step is not.
        val base = "#808080"
        val steps = (0x80..0xFF).map { "#8080" + it.toString(16).uppercase().padStart(2, '0') }
        val edge = steps.indexOfFirst { deltaE(labOf(base), labOf(it)) >= SYNC_DELTA_E }
        assertTrue(edge > 0)
        assertTrue(inTune(base, steps[edge - 1]))
        assertFalse(inTune(base, steps[edge]))
    }

    @Test
    fun theSameColorWrittenTwoWaysIsInTune() {
        assertTrue(inTune("#3D5A80", "3d5a80"))
        assertTrue(inTune("#e07a5f", "#E07A5F"))
        assertFalse(inTune("#E07A5F", "#5B8DB8"))
    }
}
