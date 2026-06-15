package com.example.wheresxyz.ui.viewmodel

import com.example.wheresxyz.data.model.SharedLocation
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationSyncViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startSharing_emitsRemoteParticipantsWithoutCurrentUser() = runTest {
        val repository = FakeLocationRepository(sessionResult = Result.success(Unit))
        val viewModel = LocationSyncViewModel(repository)
        val user = User(
            id = "uid_1",
            userCode = 1234,
            name = "Jan",
            lastname = "Kowalski",
            email = "jan@example.com"
        )

        viewModel.startSharing("event-1", user)
        advanceUntilIdle()

        repository.emit(
            listOf(
                SharedLocation(
                    userKey = "jan@example.com",
                    displayName = "Jan Kowalski",
                    latitude = 52.0,
                    longitude = 21.0,
                    updatedAt = System.currentTimeMillis()
                ),
                SharedLocation(
                    userKey = "anna@example.com",
                    displayName = "Anna Nowak",
                    latitude = 52.001,
                    longitude = 21.001,
                    updatedAt = System.currentTimeMillis()
                )
            )
        )
        advanceUntilIdle()

        val participants = viewModel.remoteParticipants.value
        assertEquals(1, participants.size)
        assertEquals("Anna Nowak", participants.first().displayName)
        assertEquals(LocationSyncState.Active, viewModel.syncState.value)
    }

    @Test
    fun startSharing_fallsBackWhenSessionFails() = runTest {
        val repository = FakeLocationRepository(
            sessionResult = Result.failure(IllegalStateException("Firebase unavailable"))
        )
        val viewModel = LocationSyncViewModel(repository)
        val user = User("uid_1", 1234, "Jan", "Kowalski", "jan@example.com")

        viewModel.startSharing("event-1", user)
        advanceUntilIdle()

        assertEquals(LocationSyncState.Fallback, viewModel.syncState.value)
        assertTrue(viewModel.remoteParticipants.value.isEmpty())
    }

    private class FakeLocationRepository(
        private val sessionResult: Result<Unit>
    ) : LocationRepository {
        private val updates = MutableSharedFlow<List<SharedLocation>>(replay = 1)
        val published = mutableListOf<SharedLocation>()

        suspend fun emit(locations: List<SharedLocation>) {
            updates.emit(locations)
        }

        override suspend fun ensureSession(): Result<Unit> = sessionResult

        override suspend fun publishLocation(eventId: String, location: SharedLocation): Result<Unit> {
            published.add(location)
            return Result.success(Unit)
        }

        override fun observeLocations(eventId: String): Flow<List<SharedLocation>> = updates.asSharedFlow()

        override suspend fun stopSharing(eventId: String, userKey: String): Result<Unit> {
            return Result.success(Unit)
        }
    }
}
