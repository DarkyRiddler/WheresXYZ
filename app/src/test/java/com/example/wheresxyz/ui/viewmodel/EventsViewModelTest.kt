package com.example.wheresxyz.ui.viewmodel

import com.example.wheresxyz.data.model.Event
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.repository.EventsRepository
import com.example.wheresxyz.util.EventAlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val eventsRepository = mockk<EventsRepository>()
    private val eventAlarmScheduler = mockk<EventAlarmScheduler>(relaxed = true)
    private lateinit var viewModel: EventsViewModel

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
        viewModel = EventsViewModel(eventsRepository, eventAlarmScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadEvents_withEmptyGroups_clearsEventsWithoutCallingRepository() = runTest {
        viewModel.loadEvents(emptyList(), currentUser)
        advanceUntilIdle()

        assertTrue(viewModel.events.value.isEmpty())
        coVerify(exactly = 0) { eventsRepository.getEventsForGroups(any()) }
    }

    @Test
    fun loadEvents_success_updatesEventsList() = runTest {
        val groups = listOf(GroupItem(id = "g1", name = "Znajomi", code = "1234"))
        val events = listOf(
            Event(id = "e1", title = "Spotkanie", startDate = 2_000L, groupId = "g1"),
            Event(id = "e2", title = "Koncert", startDate = 1_000L, groupId = "g1")
        )
        coEvery { eventsRepository.getEventsForGroups(listOf("g1")) } returns Result.success(events)

        viewModel.loadEvents(groups, currentUser)
        advanceUntilIdle()

        assertEquals(events, viewModel.events.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun createEvent_success_prependsAndSortsByStartDate() = runTest {
        val existingEvent = Event(id = "e-old", title = "Stare", startDate = 500L, groupId = "g1")
        val newEvent = Event(id = "e-new", title = "Nowe", startDate = 2_000L, groupId = "g1")
        coEvery { eventsRepository.getEventsForGroups(any()) } returns Result.success(listOf(existingEvent))
        coEvery {
            eventsRepository.createEvent(
                title = "Nowe",
                description = "Opis",
                startDate = 2_000L,
                endDate = 3_000L,
                groupId = "g1",
                groupName = "Znajomi",
                createdBy = "jan@example.com",
                startLatitude = 52.0,
                startLongitude = 21.0,
                allowedDistance = 100.0
            )
        } returns Result.success(newEvent)

        viewModel.loadEvents(listOf(GroupItem(id = "g1", name = "Znajomi", code = "1234")), currentUser)
        advanceUntilIdle()

        var successCalled = false
        viewModel.createEvent(
            title = "Nowe",
            description = "Opis",
            startDate = 2_000L,
            endDate = 3_000L,
            groupId = "g1",
            groupName = "Znajomi",
            currentUser = currentUser,
            startLatitude = 52.0,
            startLongitude = 21.0,
            allowedDistance = 100.0
        ) {
            successCalled = true
        }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(listOf("e-new", "e-old"), viewModel.events.value.map { it.id })
    }

    @Test
    fun deleteEvent_success_removesEventFromState() = runTest {
        val events = listOf(
            Event(id = "e1", title = "A", groupId = "g1"),
            Event(id = "e2", title = "B", groupId = "g1")
        )
        coEvery { eventsRepository.getEventsForGroups(any()) } returns Result.success(events)
        coEvery { eventsRepository.deleteEvent("e1") } returns Result.success(Unit)

        viewModel.loadEvents(listOf(GroupItem(id = "g1", name = "Znajomi", code = "1234")), currentUser)
        advanceUntilIdle()

        var successCalled = false
        viewModel.deleteEvent(events.first()) { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(listOf("e2"), viewModel.events.value.map { it.id })
    }
}
