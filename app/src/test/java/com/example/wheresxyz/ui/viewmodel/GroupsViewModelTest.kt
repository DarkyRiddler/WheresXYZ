package com.example.wheresxyz.ui.viewmodel

import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.GroupMember
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.repository.GroupsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val groupsRepository = mockk<GroupsRepository>()
    private lateinit var viewModel: GroupsViewModel

    private val currentUser = User(
        id = "uid_1",
        userCode = 1234,
        name = "Jan",
        lastname = "Kowalski",
        email = "jan@example.com"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GroupsViewModel(groupsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadGroups_success_updatesGroupsAndClearsError() = runTest {
        val groups = listOf(
            GroupItem(id = "g1", name = "Znajomi", code = "1234")
        )
        coEvery { groupsRepository.getGroupsForUser("jan@example.com") } returns Result.success(groups)

        viewModel.loadGroups("jan@example.com")
        advanceUntilIdle()

        assertEquals(groups, viewModel.groups.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loadGroups_failure_setsErrorMessage() = runTest {
        coEvery { groupsRepository.getGroupsForUser(any()) } returns Result.failure(
            Exception("Firestore timeout")
        )

        viewModel.loadGroups("jan@example.com")
        advanceUntilIdle()

        assertEquals("Firestore timeout", viewModel.error.value)
        assertTrue(viewModel.groups.value.isEmpty())
    }

    @Test
    fun joinGroup_success_appendsGroupToState() = runTest {
        val joinedGroup = GroupItem(id = "g2", name = "Nowa grupa", code = "5678")
        coEvery { groupsRepository.joinGroup("5678", currentUser) } returns Result.success(joinedGroup)

        viewModel.joinGroup("5678", currentUser)
        advanceUntilIdle()

        assertEquals(listOf(joinedGroup), viewModel.groups.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun joinGroup_failure_setsPolishErrorMessage() = runTest {
        coEvery { groupsRepository.joinGroup("9999", currentUser) } returns Result.failure(
            Exception("Nie znaleziono grupy o podanym kodzie.")
        )

        viewModel.joinGroup("9999", currentUser)
        advanceUntilIdle()

        assertTrue(viewModel.groups.value.isEmpty())
        assertEquals("Nie znaleziono grupy o podanym kodzie.", viewModel.error.value)
    }

    @Test
    fun createGroup_success_appendsGroupToState() = runTest {
        val newGroup = GroupItem(id = "g3", name = "Wycieczka", code = "4321", isAdmin = true)
        coEvery { groupsRepository.createGroup("Wycieczka", currentUser) } returns Result.success(newGroup)

        viewModel.createGroup("Wycieczka", currentUser)
        advanceUntilIdle()

        assertEquals(listOf(newGroup), viewModel.groups.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun createGroup_failure_setsErrorMessage() = runTest {
        coEvery { groupsRepository.createGroup(any(), any()) } returns Result.failure(
            Exception("Brak połączenia z bazą")
        )

        viewModel.createGroup("Test", currentUser)
        advanceUntilIdle()

        assertTrue(viewModel.groups.value.isEmpty())
        assertEquals("Brak połączenia z bazą", viewModel.error.value)
    }

    @Test
    fun updateGroupName_success_updatesGroupInState() = runTest {
        val group = GroupItem(id = "g1", name = "Stara nazwa", code = "1234")
        coEvery { groupsRepository.getGroupsForUser("jan@example.com") } returns Result.success(listOf(group))
        viewModel.loadGroups("jan@example.com")
        advanceUntilIdle()

        coEvery { groupsRepository.updateGroupName("g1", "Nowa nazwa") } returns Result.success(Unit)
        viewModel.updateGroupName("g1", "Nowa nazwa")
        advanceUntilIdle()

        assertEquals("Nowa nazwa", viewModel.groups.value.first().name)
    }

    @Test
    fun updateMemberPermissions_success_updatesMemberFlags() = runTest {
        val group = GroupItem(
            id = "g1",
            name = "Znajomi",
            code = "1234",
            isAdmin = true,
            members = listOf(
                GroupMember(
                    name = "Anna",
                    lastname = "Nowak",
                    email = "anna@example.com",
                    canDelete = false,
                    canModify = false,
                    canCreateEvents = false
                )
            )
        )
        coEvery { groupsRepository.getGroupsForUser("jan@example.com") } returns Result.success(listOf(group))
        viewModel.loadGroups("jan@example.com")
        advanceUntilIdle()

        coEvery {
            groupsRepository.updateMemberPermissions("g1", "anna@example.com", true, true, false)
        } returns Result.success(Unit)

        viewModel.updateMemberPermissions("g1", "anna@example.com", true, true, false)
        advanceUntilIdle()

        val member = viewModel.groups.value.first().members.first()
        assertTrue(member.canDelete)
        assertTrue(member.canModify)
        assertFalse(member.canCreateEvents)
    }

    @Test
    fun removeMember_whenOtherMemberRemoved_keepsGroupWithUpdatedMembers() = runTest {
        val group = GroupItem(
            id = "g1",
            name = "Znajomi",
            code = "1234",
            isAdmin = true,
            members = listOf(
                GroupMember(name = "Jan", lastname = "Kowalski", email = "jan@example.com", isMe = true),
                GroupMember(name = "Anna", lastname = "Nowak", email = "anna@example.com")
            )
        )
        coEvery { groupsRepository.getGroupsForUser("jan@example.com") } returns Result.success(listOf(group))
        viewModel.loadGroups("jan@example.com")
        advanceUntilIdle()

        coEvery { groupsRepository.removeMember("g1", "anna@example.com") } returns Result.success(Unit)
        viewModel.removeMember("g1", "anna@example.com")
        advanceUntilIdle()

        assertEquals(1, viewModel.groups.value.size)
        assertEquals(1, viewModel.groups.value.first().members.size)
        assertEquals("jan@example.com", viewModel.groups.value.first().members.first().email)
    }

    @Test
    fun clearError_resetsErrorState() = runTest {
        coEvery { groupsRepository.getGroupsForUser(any()) } returns Result.failure(Exception("Błąd"))
        viewModel.loadGroups("jan@example.com")
        advanceUntilIdle()
        assertEquals("Błąd", viewModel.error.value)

        viewModel.clearError()
        assertNull(viewModel.error.value)
    }

    @Test
    fun removeMember_whenCurrentUserLeaves_removesGroupFromState() = runTest {
        val group = GroupItem(
            id = "g1",
            name = "Znajomi",
            code = "1234",
            members = listOf(
                GroupMember(
                    name = "Jan",
                    lastname = "Kowalski",
                    email = "jan@example.com",
                    isMe = true
                )
            )
        )
        viewModel = GroupsViewModel(groupsRepository)
        coEvery { groupsRepository.getGroupsForUser("jan@example.com") } returns Result.success(listOf(group))
        viewModel.loadGroups("jan@example.com")
        advanceUntilIdle()

        coEvery { groupsRepository.removeMember("g1", "jan@example.com") } returns Result.success(Unit)
        viewModel.removeMember("g1", "jan@example.com")
        advanceUntilIdle()

        assertTrue(viewModel.groups.value.isEmpty())
    }
}
