package com.example.wheresxyz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheresxyz.data.model.Event
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.repository.EventsRepository
import com.example.wheresxyz.util.EventAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    private val eventAlarmScheduler: EventAlarmScheduler
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadEvents(groups: List<GroupItem>, currentUser: User) {
        val groupIds = groups.map { it.id }
        if (groupIds.isEmpty()) {
            _events.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            eventsRepository.getEventsForGroups(groupIds)
                .onSuccess { list ->
                    _events.value = list
                    eventAlarmScheduler.scheduleEvents(list, currentUser)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Błąd podczas ładowania wydarzeń"
                }
            _isLoading.value = false
        }
    }

    fun createEvent(
        title: String,
        description: String,
        startDate: Long,
        endDate: Long,
        groupId: String,
        groupName: String,
        currentUser: User,
        startLatitude: Double,
        startLongitude: Double,
        allowedDistance: Double,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            eventsRepository.createEvent(
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                groupId = groupId,
                groupName = groupName,
                createdBy = currentUser.email,
                startLatitude = startLatitude,
                startLongitude = startLongitude,
                allowedDistance = allowedDistance
            ).onSuccess { newEvent ->
                _events.value = (listOf(newEvent) + _events.value).sortedByDescending { it.startDate }
                eventAlarmScheduler.scheduleAlarms(newEvent, currentUser)
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Błąd podczas tworzenia wydarzenia"
            }
            _isLoading.value = false
        }
    }

    fun deleteEvent(event: Event, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            eventsRepository.deleteEvent(event.id)
                .onSuccess {
                    eventAlarmScheduler.cancelAlarms(event)
                    _events.value = _events.value.filter { it.id != event.id }
                    onSuccess()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Błąd podczas usuwania wydarzenia"
                }
            _isLoading.value = false
        }
    }
}
