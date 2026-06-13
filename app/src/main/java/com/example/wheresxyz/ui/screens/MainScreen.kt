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
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.*

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

    val sharedGroupsList = remember {
        mutableStateListOf(
            GroupItem(
                id = "1",
                name = "Ekipa Festiwalowa",
                code = "2498",
                isAdmin = true,
                members = listOf(
                    GroupMember("Dawid", "Kowalski", "👤", isMe = true),
                    GroupMember("Kamil", "Nowak", "🧑‍💻", canModify = true, canCreateEvents = true),
                    GroupMember("Anna", "Zielińska", "🙋‍♀️", canCreateEvents = true),
                    GroupMember("Piotr", "Wiśniewski", "🧑‍🚀"),
                    GroupMember("Maja", "Szymańska", "🧑‍🎨"),
                    GroupMember("Jan", "Kozłowski", "🙋‍♂️")
                )
            ),
            GroupItem(
                id = "2",
                name = "Wyjazd Rodzinny",
                code = "8051",
                isAdmin = false,
                members = listOf(
                    GroupMember("Marek", "Kowalski", "🙋‍♂️", canDelete = true, canModify = true, canCreateEvents = true),
                    GroupMember("Dawid", "Kowalski", "👤", isMe = true),
                    GroupMember("Zofia", "Kowalski", "🙋‍♀️"),
                    GroupMember("Helena", "Kowalski", "👵")
                )
            ),
            GroupItem(
                id = "3",
                name = "Grupa Górska",
                code = "4490",
                isAdmin = false,
                members = listOf(
                    GroupMember("Tomek", "Wójcik", "🧑‍🚀", canDelete = true, canModify = true, canCreateEvents = true),
                    GroupMember("Dawid", "Kowalski", "👤", isMe = true, canCreateEvents = true),
                    GroupMember("Ola", "Kowalczyk", "🧑‍🎨")
                )
            )
        )
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
                1 -> GroupsTab(groupsList = sharedGroupsList)
                2 -> EventsTab(groupsList = sharedGroupsList)
            }
        }
    }
}
