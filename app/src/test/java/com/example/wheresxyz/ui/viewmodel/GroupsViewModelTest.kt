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
