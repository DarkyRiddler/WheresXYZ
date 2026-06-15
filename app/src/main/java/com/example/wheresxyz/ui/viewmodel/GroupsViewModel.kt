package com.example.wheresxyz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.GroupMember
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.repository.GroupsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _groups = MutableStateFlow<List<GroupItem>>(emptyList())
    val groups: StateFlow<List<GroupItem>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadGroups(userEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            groupsRepository.getGroupsForUser(userEmail)
                .onSuccess { list ->
                    _groups.value = list
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Błąd podczas ładowania grup"
                }
            _isLoading.value = false
        }
    }

    fun createGroup(groupName: String, currentUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            groupsRepository.createGroup(groupName, currentUser)
                .onSuccess { newGroup ->
                    _groups.value = _groups.value + newGroup
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Nie udało się utworzyć grupy"
                }
            _isLoading.value = false
        }
    }

    fun joinGroup(code: String, currentUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            groupsRepository.joinGroup(code, currentUser)
                .onSuccess { joinedGroup ->
                    _groups.value = _groups.value + joinedGroup
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Nie udało się dołączyć do grupy"
                }
            _isLoading.value = false
        }
    }

    fun updateGroupName(groupId: String, newName: String) {
        viewModelScope.launch {
            _error.value = null
            groupsRepository.updateGroupName(groupId, newName)
                .onSuccess {
                    _groups.value = _groups.value.map {
                        if (it.id == groupId) it.copy(name = newName) else it
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Nie udało się zmienić nazwy grupy"
                }
        }
    }

    fun updateMemberPermissions(
        groupId: String,
        targetEmail: String,
        canDelete: Boolean,
        canModify: Boolean,
        canCreateEvents: Boolean
    ) {
        viewModelScope.launch {
            _error.value = null
            groupsRepository.updateMemberPermissions(groupId, targetEmail, canDelete, canModify, canCreateEvents)
                .onSuccess {
                    _groups.value = _groups.value.map { group ->
                        if (group.id == groupId) {
                            val updatedMembers = group.members.map { member ->
                                if (member.email == targetEmail) {
                                    member.copy(canDelete = canDelete, canModify = canModify, canCreateEvents = canCreateEvents)
                                } else {
                                    member
                                }
                            }
                            group.copy(members = updatedMembers)
                        } else {
                            group
                        }
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Nie udało się zaktualizować uprawnień"
                }
        }
    }

    fun removeMember(groupId: String, targetEmail: String) {
        viewModelScope.launch {
            _error.value = null
            groupsRepository.removeMember(groupId, targetEmail)
                .onSuccess {
                    _groups.value = _groups.value.mapNotNull { group ->
                        if (group.id == groupId) {
                            val updatedMembers = group.members.filter { it.email != targetEmail }
                            // If the current user removed themselves, or they were removed, they shouldn't see the group anymore
                            val isMeStillInGroup = updatedMembers.any { it.isMe }
                            if (isMeStillInGroup) {
                                group.copy(members = updatedMembers)
                            } else {
                                null
                            }
                        } else {
                            group
                        }
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Nie udało się usunąć członka"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
