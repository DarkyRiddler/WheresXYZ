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

data class GroupItem(val name: String, val memberCount: Int, val code: String)

@Composable
fun GroupsTab() {
    val mockGroups = remember {
        listOf(
            GroupItem("Ekipa Festiwalowa", 8, "2498"),
            GroupItem("Wyjazd Rodzinny", 4, "8051"),
            GroupItem("Grupa Górska", 12, "4490")
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
                text = "Moje Grupy",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = {},
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
            items(mockGroups) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
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
                        Column {
                            Text(
                                text = group.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${group.memberCount} aktywnych członków",
                                fontSize = 14.sp,
                                color = TextSecondaryDark
                            )
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
            onClick = {},
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
