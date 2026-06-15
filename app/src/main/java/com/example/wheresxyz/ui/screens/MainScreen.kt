package com.example.wheresxyz.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.GroupMember
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.*
import com.example.wheresxyz.ui.viewmodel.GroupsViewModel

@Composable
fun rememberUriImage(uriString: String?, context: Context): ImageBitmap? {
    return remember(uriString) {
        if (uriString.isNullOrEmpty() || (!uriString.startsWith("content://") && !uriString.startsWith("file://"))) {
            null
        } else {
            try {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

@Composable
fun MainScreen(
    user: User,
    onLogoutClick: () -> Unit,
    onSaveProfileClick: (String, String, String?) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Profil", "Grupy", "Wydarzenia")

    val groupsViewModel: GroupsViewModel = hiltViewModel()
    val eventsViewModel: com.example.wheresxyz.ui.viewmodel.EventsViewModel = hiltViewModel()
    
    LaunchedEffect(user.email) {
        if (user.email.isNotEmpty()) {
            groupsViewModel.loadGroups(user.email)
        }
    }

    val groupsList by groupsViewModel.groups.collectAsState()
    val isLoadingGroups by groupsViewModel.isLoading.collectAsState()
    val groupsError by groupsViewModel.error.collectAsState()

    // Automatically load events and schedule alarms on startup as soon as groups are loaded
    LaunchedEffect(groupsList) {
        if (groupsList.isNotEmpty()) {
            eventsViewModel.loadEvents(groupsList, user)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = when (title) {
                                    "Profil" -> Icons.Default.Person
                                    "Grupy" -> Icons.Default.Group
                                    else -> Icons.Default.Today
                                },
                                contentDescription = title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan,
                            selectedTextColor = BrandCyan,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark,
                            indicatorColor = BrandIndigo.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ProfileTab(user = user, onLogoutClick = onLogoutClick, onSaveProfileClick = onSaveProfileClick)
                1 -> GroupsTab(
                    currentUser = user,
                    groupsList = groupsList,
                    isLoading = isLoadingGroups,
                    error = groupsError,
                    onCreateGroup = { name -> groupsViewModel.createGroup(name, user) },
                    onJoinGroup = { code -> groupsViewModel.joinGroup(code, user) },
                    onUpdateGroupName = { id, name -> groupsViewModel.updateGroupName(id, name) },
                    onUpdateMemberPermissions = { id, email, del, mod, create ->
                        groupsViewModel.updateMemberPermissions(id, email, del, mod, create)
                    },
                    onRemoveMember = { id, email -> groupsViewModel.removeMember(id, email) },
                    onClearError = { groupsViewModel.clearError() }
                )
                2 -> EventsTab(groupsList = groupsList, currentUser = user, eventsViewModel = eventsViewModel)
            }
        }
    }
}
