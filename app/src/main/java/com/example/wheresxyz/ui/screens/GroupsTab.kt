package com.example.wheresxyz.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.GroupMember
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.*

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
fun GroupsTab(
    currentUser: User,
    groupsList: List<GroupItem>,
    isLoading: Boolean,
    error: String?,
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onUpdateGroupName: (String, String) -> Unit,
    onUpdateMemberPermissions: (String, String, Boolean, Boolean, Boolean) -> Unit,
    onRemoveMember: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onClearError()
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var selectedGroupIdForDetails by remember { mutableStateOf<String?>(null) }
    val selectedGroupForDetails = groupsList.find { it.id == selectedGroupIdForDetails }

    Box(modifier = Modifier.fillMaxSize()) {
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
                items(groupsList) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable { selectedGroupIdForDetails = group.id },
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

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandCyan)
            }
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
                            onJoinGroup(codeInput)
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
                            onCreateGroup(newGroupNameInput)
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
        val group = selectedGroupForDetails
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedGroupIdForDetails = null }
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
                                            onUpdateGroupName(group.id, tempName)
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
                                                onRemoveMember(group.id, member.email)
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
                                                onUpdateMemberPermissions(
                                                    group.id,
                                                    member.email,
                                                    !member.canDelete,
                                                    member.canModify,
                                                    member.canCreateEvents
                                                )
                                            }
                                        )
                                        PermissionChip(
                                            label = "Edycja",
                                            selected = member.canModify,
                                            enabled = group.isAdmin,
                                            onClick = {
                                                onUpdateMemberPermissions(
                                                    group.id,
                                                    member.email,
                                                    member.canDelete,
                                                    !member.canModify,
                                                    member.canCreateEvents
                                                )
                                            }
                                        )
                                        PermissionChip(
                                            label = "Eventy",
                                            selected = member.canCreateEvents,
                                            enabled = group.isAdmin,
                                            onClick = {
                                                onUpdateMemberPermissions(
                                                    group.id,
                                                    member.email,
                                                    member.canDelete,
                                                    member.canModify,
                                                    !member.canCreateEvents
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedGroupIdForDetails = null },
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
