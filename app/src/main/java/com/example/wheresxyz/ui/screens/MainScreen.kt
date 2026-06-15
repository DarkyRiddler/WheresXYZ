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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                showNotificationPermissionDialog = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        showNotificationPermissionDialog = false
        if (!isGranted) {
            Toast.makeText(
                context,
                "Bez powiadomień nie będziesz otrzymywać pingów i alertów z wydarzeń.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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
                    onAddMemberByUserCode = { groupId, code ->
                        groupsViewModel.addMemberByUserCode(groupId, code, user.email)
                    },
                    onClearError = { groupsViewModel.clearError() }
                )
                2 -> EventsTab(groupsList = groupsList, currentUser = user, eventsViewModel = eventsViewModel)
            }
        }
    }

    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationPermissionDialog = false },
            title = {
                Text(
                    text = "Włącz powiadomienia",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Aby nie przegapić pingów od znajomych oraz alertów o wejściu/wyjściu ze stref wydarzeń, zezwól aplikacji na wysyłanie powiadomień.",
                    color = TextSecondaryDark,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            showNotificationPermissionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                ) {
                    Text("Zezwól", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationPermissionDialog = false }) {
                    Text("Później", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
    }
}
