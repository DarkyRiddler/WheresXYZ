package com.example.wheresxyz.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupItemTest {

    @Test
    fun groupItem_defaultsToEmptyMembersAndNotAdmin() {
        val group = GroupItem(id = "g1", name = "Test", code = "1234")

        assertTrue(group.members.isEmpty())
        assertFalse(group.isAdmin)
    }

    @Test
    fun groupMember_isMeFlagIdentifiesCurrentUser() {
        val members = listOf(
            GroupMember(name = "Jan", lastname = "Kowalski", email = "jan@example.com", isMe = true),
            GroupMember(name = "Anna", lastname = "Nowak", email = "anna@example.com", isMe = false)
        )

        assertTrue(members.first { it.isMe }.email == "jan@example.com")
        assertFalse(members.none { it.email == "anna@example.com" && it.isMe })
    }

    @Test
    fun groupItem_preservesMemberCount() {
        val group = GroupItem(
            id = "g1",
            name = "Znajomi",
            code = "5678",
            isAdmin = true,
            members = listOf(
                GroupMember(name = "Jan", lastname = "Kowalski", email = "jan@example.com", isMe = true),
                GroupMember(name = "Anna", lastname = "Nowak", email = "anna@example.com")
            )
        )

        assertEquals(2, group.members.size)
        assertEquals("5678", group.code)
        assertTrue(group.isAdmin)
    }
}
