package com.baltajmn.color

import com.baltajmn.color.data.widgetState
import com.baltajmn.color.data.widgetView
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

// docs/tecnico.md 4.2: what the widgets may paint without the app having run.
class WidgetTest {

    private val j = mapOf(
        "2027-12-30" to ChromaEntry(color = "#112233", name = "x"),
        "2027-12-31" to ChromaEntry(color = "#3A6EA5", name = "storm_blue"),
    )

    @Test
    fun theYearGridIsProOnly() {
        val today = LocalDate.parse("2027-12-31")
        assertTrue(widgetState(j, Settings(), today, nameOf = { it }).days.isEmpty())
        assertEquals(2, widgetState(j, Settings(pro = true), today, nameOf = { it }).days.size)
    }

    @Test
    fun aStaleStateLosesTodayAndANewYearLosesTheGrid() {
        val st = widgetState(j, Settings(pro = true), LocalDate.parse("2027-12-31"), nameOf = { "Storm blue" })
        assertEquals("Storm blue", widgetView(st, LocalDate.parse("2027-12-31")).name)

        val newYear = widgetView(st, LocalDate.parse("2028-01-01"))
        assertNull(newYear.color)
        assertNull(newYear.name)
        assertEquals(2028, newYear.year)
        assertTrue(newYear.days.isEmpty())
    }

    @Test
    fun friendsColorsLastOnlyTheirDay() {
        // #42: free, with or without Pro, and gone once the day is over.
        val st = widgetState(j, Settings(), LocalDate.parse("2027-12-31"), nameOf = { it }, friends = listOf("#E07A5F", "#81B29A"))
        assertEquals(listOf("#E07A5F", "#81B29A"), widgetView(st, LocalDate.parse("2027-12-31")).friends)
        assertTrue(widgetView(st, LocalDate.parse("2028-01-01")).friends.isEmpty())
    }
}
