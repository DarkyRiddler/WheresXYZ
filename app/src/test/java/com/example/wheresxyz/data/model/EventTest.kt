package com.example.wheresxyz.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventTest {

    @Test
    fun isActive_returnsTrueWhenNowIsInsideWindow() {
        val now = 1_000_000L
        val event = Event(
            startDate = now - 60_000L,
            endDate = now + 60_000L
        )

        assertTrue(event.isActiveAt(now))
    }

    @Test
    fun isActive_returnsTrueAtExactBoundaries() {
        val start = 1_000_000L
        val end = 2_000_000L
        val event = Event(startDate = start, endDate = end)

        assertTrue(event.isActiveAt(start))
        assertTrue(event.isActiveAt(end))
    }

    @Test
    fun isActive_returnsFalseBeforeStart() {
        val event = Event(startDate = 2_000_000L, endDate = 3_000_000L)

        assertFalse(event.isActiveAt(1_999_999L))
    }

    @Test
    fun isActive_returnsFalseAfterEnd() {
        val event = Event(startDate = 1_000_000L, endDate = 2_000_000L)

        assertFalse(event.isActiveAt(2_000_001L))
    }
}

private fun Event.isActiveAt(nowMillis: Long): Boolean {
    return nowMillis in startDate..endDate
}
