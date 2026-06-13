package com.example.wheresxyz.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                            selectedIconColor = BrandIndigo,
                            selectedTextColor = BrandIndigo,
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
                1 -> GroupsTab()
                2 -> EventsTab()
            }
        }
    }
}

@Composable
fun ProfileTab(
    user: User, 
    onLogoutClick: () -> Unit,
    onSaveProfileClick: (String, String, String?) -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    
    var editedName by remember(user.name) { mutableStateOf(user.name) }
    var editedLastname by remember(user.lastname) { mutableStateOf(user.lastname) }
    var selectedAvatar by remember(user.userPhoto) { mutableStateOf(user.userPhoto ?: "👤") }

    val presetAvatars = listOf("👤", "🧑‍💻", "🧑‍🚀", "🧑‍🎨", "🙋‍♂️", "🙋‍♀️")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAvatar = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Profile Pic or Emoji Avatar
        val isUri = selectedAvatar.startsWith("content://") || selectedAvatar.startsWith("file://")
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(BrandViolet, BrandIndigo)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isUri) {
                val bitmap = rememberUriImage(selectedAvatar, context)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Awatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = editedName.take(1) + editedLastname.take(1),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = selectedAvatar,
                    fontSize = if (selectedAvatar.length <= 2) 48.sp else 32.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            // Avatar Selector Grid
            Text(
                text = "Wybierz awatar:",
                fontSize = 12.sp,
                color = TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetAvatars.forEach { avatar ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (selectedAvatar == avatar) BrandIndigo.copy(alpha = 0.2f) else Color.Transparent,
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (selectedAvatar == avatar) BrandIndigo else Color.White.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .clickable { selectedAvatar = avatar },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatar, fontSize = 20.sp)
                    }
                }

                // Gallery Option indicator
                val isCustomSelected = selectedAvatar.startsWith("content://") || selectedAvatar.startsWith("file://")
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isCustomSelected) BrandIndigo.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isCustomSelected) BrandIndigo else Color.White.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .clickable {
                            galleryLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCustomSelected) {
                        val bitmap = rememberUriImage(selectedAvatar, context)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Wybrany awatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(text = "🖼️", fontSize = 20.sp)
                        }
                    } else {
                        Text(text = "🖼️", fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { galleryLauncher.launch("image/*") },
                colors = ButtonDefaults.textButtonColors(contentColor = BrandCyan),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Wybierz z galerii", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // First Name Field
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Imię", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandIndigo,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Last Name Field
            OutlinedTextField(
                value = editedLastname,
                onValueChange = { editedLastname = it },
                label = { Text("Nazwisko", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandIndigo,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Save/Cancel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isEditing = false
                        // Reset back to original
                        editedName = user.name
                        editedLastname = user.lastname
                        selectedAvatar = user.userPhoto ?: "👤"
                    },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anuluj", color = Color.White)
                }

                Button(
                    onClick = {
                        isEditing = false
                        onSaveProfileClick(editedName, editedLastname, selectedAvatar)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(12.dp),
                    enabled = editedName.isNotBlank() && editedLastname.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Zapisz", color = Color.White)
                }
            }
        } else {
            // View Mode
            Text(
                text = "${user.name} ${user.lastname}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = user.email,
                fontSize = 14.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Button
            Button(
                onClick = { isEditing = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text("Edytuj profil", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // User 4-Digit Code Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mój Kod Członka",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%04d", user.userCode),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share Button
                    IconButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "Mój kod członka w Where's XYZ: ${String.format("%04d", user.userCode)}")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Udostępnij kod członka"))
                        },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Udostępnij kod",
                            tint = BrandCyan
                        )
                    }

                    // Copy Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Kod Członka", String.format("%04d", user.userCode))
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Kod skopiowany do schowka!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopiuj kod",
                            tint = BrandIndigo
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout Button
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ErrorRed, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Wyloguj się",
                    tint = ErrorRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wyloguj się",
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

data class GroupMember(
    val name: String,
    val lastname: String,
    val avatar: String, // Emoji or URI
    val canDelete: Boolean = false,
    val canModify: Boolean = false,
    val canCreateEvents: Boolean = false,
    val isMe: Boolean = false
)

data class GroupItem(
    val id: String,
    val name: String,
    val code: String,
    val members: List<GroupMember>,
    val isAdmin: Boolean = false
)

@Composable
fun MemberAvatarsPreview(members: List<GroupMember>, maxVisible: Int = 4) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-10).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val visibleMembers = members.take(maxVisible)
        visibleMembers.forEach { member ->
            val isUri = member.avatar.startsWith("content://") || member.avatar.startsWith("file://")
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(1.5.dp, DarkSurface, CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(BrandViolet, BrandIndigo)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUri) {
                    val context = LocalContext.current
                    val bitmap = rememberUriImage(member.avatar, context)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = member.name.take(1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = member.avatar,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (members.size > maxVisible) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(1.5.dp, DarkSurface, CircleShape)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${members.size - maxVisible}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PermissionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) BrandIndigo.copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (selected) BrandIndigo else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) BrandCyan else TextSecondaryDark,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun GroupsTab() {
    val context = LocalContext.current
    val mockGroupsList = remember {
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

    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var selectedGroupForDetails by remember { mutableStateOf<GroupItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Moje Grupy",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Nowa Grupa", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(mockGroupsList) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .clickable { selectedGroupForDetails = group },
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = group.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (group.isAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(BrandCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Admin",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandCyan
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${group.members.size} aktywnych członków",
                                fontSize = 14.sp,
                                color = TextSecondaryDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            MemberAvatarsPreview(members = group.members)
                        }

                        Box(
                            modifier = Modifier
                                .background(BrandViolet.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, BrandViolet.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "#${group.code}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandViolet
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Join Group Button
        Button(
            onClick = { showJoinDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(BrandCyan, BrandIndigo)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .clip(RoundedCornerShape(14.dp))
        ) {
            Text(
                text = "Dołącz do grupy przez kod",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    // Join Dialog
    if (showJoinDialog) {
        var codeInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Dołącz do grupy", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Wpisz 4-cyfrowy kod grupy, aby do niej dołączyć:", color = TextSecondaryDark, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { if (it.length <= 4) codeInput = it },
                        label = { Text("Kod grupy", color = TextSecondaryDark) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (codeInput.length == 4) {
                            mockGroupsList.add(
                                GroupItem(
                                    id = (100..999).random().toString(),
                                    name = "Grupa dołączona",
                                    code = codeInput,
                                    isAdmin = false,
                                    members = listOf(
                                        GroupMember("Tomek", "Kowalski", "🧑‍🚀", canDelete = true, canModify = true, canCreateEvents = true),
                                        GroupMember("Dawid", "Kowalski", "👤", isMe = true),
                                        GroupMember("Kasia", "Zielińska", "🙋‍♀️")
                                    )
                                )
                            )
                            showJoinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    enabled = codeInput.length == 4
                ) {
                    Text("Dołącz", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Anuluj", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Create Dialog
    if (showCreateDialog) {
        var newGroupNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Utwórz nową grupę", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Wpisz nazwę nowej grupy. Zostaniesz jej administratorem:", color = TextSecondaryDark, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newGroupNameInput,
                        onValueChange = { newGroupNameInput = it },
                        label = { Text("Nazwa grupy", color = TextSecondaryDark) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupNameInput.isNotBlank()) {
                            val randomCode = (1000..9999).random().toString()
                            mockGroupsList.add(
                                GroupItem(
                                    id = (100..999).random().toString(),
                                    name = newGroupNameInput,
                                    code = randomCode,
                                    isAdmin = true,
                                    members = listOf(
                                        GroupMember("Dawid", "Kowalski", "👤", isMe = true),
                                        GroupMember("Marta", "Zielińska", "🙋‍♀️"),
                                        GroupMember("Łukasz", "Wiśniewski", "🧑‍💻")
                                    )
                                )
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    enabled = newGroupNameInput.isNotBlank()
                ) {
                    Text("Utwórz", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Anuluj", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Details Overlay Dialog
    if (selectedGroupForDetails != null) {
        val group = selectedGroupForDetails!!
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedGroupForDetails = null }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 550.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Name & Close/Edit
                    val isUserAllowedToEditName = group.isAdmin || (group.members.find { it.isMe }?.canModify == true)
                    if (isUserAllowedToEditName) {
                        var isEditingName by remember { mutableStateOf(false) }
                        var tempName by remember(group.name) { mutableStateOf(group.name) }
                        if (isEditingName) {
                            OutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandIndigo,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (tempName.isNotBlank()) {
                                            val updatedGroup = group.copy(name = tempName)
                                            val grpIndex = mockGroupsList.indexOfFirst { it.id == group.id }
                                            if (grpIndex != -1) {
                                                mockGroupsList[grpIndex] = updatedGroup
                                                selectedGroupForDetails = updatedGroup
                                            }
                                            isEditingName = false
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Zapisz", tint = BrandCyan)
                                    }
                                }
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { isEditingName = true }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edytuj nazwę", tint = BrandIndigo)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = group.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Large Group Code Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "KOD GRUPY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondaryDark,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "#${group.code}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCyan
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Kod grupy", group.code)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Kod grupy skopiowany!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Kopiuj kod",
                                    tint = BrandIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Członkowie (${group.members.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(group.members) { index, member ->
                            val isMemberAdmin = index == 0
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    Brush.radialGradient(colors = listOf(BrandViolet, BrandIndigo)),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (member.avatar.startsWith("content://") || member.avatar.startsWith("file://")) {
                                                val bitmap = rememberUriImage(member.avatar, context)
                                                if (bitmap != null) {
                                                    Image(
                                                        bitmap = bitmap,
                                                        contentDescription = member.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                    )
                                                } else {
                                                    Text(member.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Text(member.avatar, fontSize = 16.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${member.name} ${member.lastname}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                            Text(
                                                text = when {
                                                    index == 0 -> "Właściciel (Admin)"
                                                    member.isMe -> "Ty"
                                                    else -> "Członek"
                                                },
                                                fontSize = 11.sp,
                                                color = if (index == 0) BrandCyan else TextSecondaryDark
                                            )
                                        }
                                    }

                                    val canWeDelete = group.isAdmin || (group.members.find { it.isMe }?.canDelete == true)
                                    if (canWeDelete && !member.isMe && !isMemberAdmin) {
                                        TextButton(
                                            onClick = {
                                                val updatedMembers = group.members.filter { it != member }
                                                val updatedGroup = group.copy(members = updatedMembers)
                                                val grpIndex = mockGroupsList.indexOfFirst { it.id == group.id }
                                                if (grpIndex != -1) {
                                                    mockGroupsList[grpIndex] = updatedGroup
                                                    selectedGroupForDetails = updatedGroup
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                                        ) {
                                            Text("Usuń", fontSize = 13.sp)
                                        }
                                    }
                                }

                                if (!isMemberAdmin) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.padding(start = 46.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PermissionChip(
                                            label = "Usuwanie",
                                            selected = member.canDelete,
                                            enabled = group.isAdmin,
                                            onClick = {
                                                val updatedMembers = group.members.map {
                                                    if (it == member) it.copy(canDelete = !it.canDelete) else it
                                                }
                                                val updatedGroup = group.copy(members = updatedMembers)
                                                val grpIndex = mockGroupsList.indexOfFirst { it.id == group.id }
                                                if (grpIndex != -1) {
                                                    mockGroupsList[grpIndex] = updatedGroup
                                                    selectedGroupForDetails = updatedGroup
                                                }
                                            }
                                        )
                                        PermissionChip(
                                            label = "Edycja",
                                            selected = member.canModify,
                                            enabled = group.isAdmin,
                                            onClick = {
                                                val updatedMembers = group.members.map {
                                                    if (it == member) it.copy(canModify = !it.canModify) else it
                                                }
                                                val updatedGroup = group.copy(members = updatedMembers)
                                                val grpIndex = mockGroupsList.indexOfFirst { it.id == group.id }
                                                if (grpIndex != -1) {
                                                    mockGroupsList[grpIndex] = updatedGroup
                                                    selectedGroupForDetails = updatedGroup
                                                }
                                            }
                                        )
                                        PermissionChip(
                                            label = "Eventy",
                                            selected = member.canCreateEvents,
                                            enabled = group.isAdmin,
                                            onClick = {
                                                val updatedMembers = group.members.map {
                                                    if (it == member) it.copy(canCreateEvents = !it.canCreateEvents) else it
                                                }
                                                val updatedGroup = group.copy(members = updatedMembers)
                                                val grpIndex = mockGroupsList.indexOfFirst { it.id == group.id }
                                                if (grpIndex != -1) {
                                                    mockGroupsList[grpIndex] = updatedGroup
                                                    selectedGroupForDetails = updatedGroup
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedGroupForDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zamknij", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class EventItem(val title: String, val group: String, val date: String, val isActive: Boolean)

@Composable
fun EventsTab() {
    val mockEvents = remember {
        listOf(
            EventItem("Festiwal Letnich Rytmów", "Ekipa Festiwalowa", "Dziś o 20:00", true),
            EventItem("Weekendowy Szlak Górski", "Grupa Górska", "Niedziela o 08:00", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wydarzenia",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Dodaj Wydarzenie", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(mockEvents) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Z grupą: ${event.group}",
                                    fontSize = 14.sp,
                                    color = TextSecondaryDark
                                )
                            }

                            if (event.isActive) {
                                Box(
                                    modifier = Modifier
                                        .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .border(1.dp, SuccessGreen, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "NA ŻYWO",
                                        color = SuccessGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.date,
                                fontSize = 12.sp,
                                color = BrandCyan,
                                fontWeight = FontWeight.Medium
                            )

                            if (event.isActive) {
                                // Ping Trigger Mock
                                Box(
                                    modifier = Modifier
                                        .background(BrandRose.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .border(1.dp, BrandRose.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Ping",
                                            tint = BrandRose,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Wyślij Ping",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandRose
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
