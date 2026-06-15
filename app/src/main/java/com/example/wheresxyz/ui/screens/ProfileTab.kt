package com.example.wheresxyz.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.*

// An avatar is either null/empty (→ show initials) or a single emoji string
private fun isEmoji(value: String?) = !value.isNullOrEmpty() && !value.startsWith("http")

@Composable
fun AvatarCircle(
    name: String,
    lastname: String,
    emoji: String?,
    sizeDp: Int = 100,
    fontSizeSp: Int = 36
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .background(
                Brush.radialGradient(colors = listOf(BrandViolet, BrandIndigo)),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isEmoji(emoji)) {
            Text(
                text = emoji!!,
                fontSize = (fontSizeSp * 0.9).sp,
                textAlign = TextAlign.Center
            )
        } else {
            // Show initials
            val initials = "${name.take(1)}${lastname.take(1)}".uppercase()
            Text(
                text = initials.ifEmpty { "?" },
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
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
    // null means "use initials", an emoji string means use that emoji
    var selectedEmoji by remember(user.userPhoto) {
        mutableStateOf(if (isEmoji(user.userPhoto)) user.userPhoto else null)
    }

    val isNameValid = editedName.isNotBlank()
    val isLastnameValid = editedLastname.isNotBlank()
    var isSubmitted by remember { mutableStateOf(false) }
    val showNameError = isSubmitted && !isNameValid
    val showLastnameError = isSubmitted && !isLastnameValid

    var showCustomEmojiDialog by remember { mutableStateOf(false) }

    val defaultEmojis = listOf(
        "😀", "😎", "🤩", "🧑‍💻", "🧑‍🚀", "🧑‍🎨",
        "🦊", "🐼", "🐸", "🦁", "🐯", "🐺",
        "🍕", "🎮", "🎸", "⚽", "🏀", "🎯"
    )

    // Helper to extract the first grapheme cluster (proper emoji handling for multi-char emojis)
    val getFirstGrapheme = { text: String ->
        if (text.isEmpty()) ""
        else {
            val boundary = java.text.BreakIterator.getCharacterInstance()
            boundary.setText(text)
            val start = boundary.first()
            val end = boundary.next()
            if (end != java.text.BreakIterator.DONE) {
                text.substring(start, end)
            } else {
                text
            }
        }
    }

    // Build the grid options dynamically
    val gridItems = remember(selectedEmoji) {
        val list = mutableListOf<String?>()
        list.add(null) // Initials
        list.addAll(defaultEmojis)
        if (selectedEmoji != null && selectedEmoji !in defaultEmojis) {
            list.add(selectedEmoji)
        }
        list.add("+") // Custom emoji trigger
        list
    }

    if (showCustomEmojiDialog) {
        var tempInput by remember { mutableStateOf("") }
        val extractedEmoji = getFirstGrapheme(tempInput)

        AlertDialog(
            onDismissRequest = { showCustomEmojiDialog = false },
            title = { Text("Wpisz własne emoji", color = Color.White) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Użyj klawiatury systemowej, aby wpisać lub wkleić dowolne emoji.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AvatarCircle(
                        name = editedName,
                        lastname = editedLastname,
                        emoji = if (extractedEmoji.isNotEmpty()) extractedEmoji else null,
                        sizeDp = 80,
                        fontSizeSp = 30
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = {
                            tempInput = it
                        },
                        placeholder = { Text("Wpisz emoji...", color = TextSecondaryDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (extractedEmoji.isNotEmpty()) {
                            selectedEmoji = extractedEmoji
                        }
                        showCustomEmojiDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                ) {
                    Text("Zapisz", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomEmojiDialog = false }
                ) {
                    Text("Anuluj", color = Color.White)
                }
            },
            containerColor = DarkSurface,
            textContentColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar display
        AvatarCircle(
            name = editedName,
            lastname = editedLastname,
            emoji = selectedEmoji,
            sizeDp = 100,
            fontSizeSp = 36
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            // Emoji picker grid
            Text(
                text = "Wybierz awatar:",
                fontSize = 12.sp,
                color = TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Split into rows of 6
            gridItems.chunked(6).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { item ->
                        if (item == "+") {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    .clickable { showCustomEmojiDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "➕", fontSize = 18.sp)
                            }
                        } else {
                            val isSelected = selectedEmoji == item
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isSelected) BrandIndigo.copy(alpha = 0.25f) else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) BrandIndigo else Color.White.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                                    .clickable { selectedEmoji = item },
                                contentAlignment = Alignment.Center
                            ) {
                                if (item == null) {
                                    // Initials preview
                                    val initials = "${editedName.take(1)}${editedLastname.take(1)}".uppercase()
                                    Text(
                                        text = initials.ifEmpty { "?" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                } else {
                                    Text(text = item, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name field
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Imię", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandIndigo,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    errorBorderColor = ErrorRed,
                    errorLabelColor = ErrorRed,
                    errorSupportingTextColor = ErrorRed
                ),
                isError = showNameError,
                supportingText = {
                    if (showNameError) Text("Imię nie może być puste", color = ErrorRed)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lastname field
            OutlinedTextField(
                value = editedLastname,
                onValueChange = { editedLastname = it },
                label = { Text("Nazwisko", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandIndigo,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    errorBorderColor = ErrorRed,
                    errorLabelColor = ErrorRed,
                    errorSupportingTextColor = ErrorRed
                ),
                isError = showLastnameError,
                supportingText = {
                    if (showLastnameError) Text("Nazwisko nie może być puste", color = ErrorRed)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save / Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isEditing = false
                        isSubmitted = false
                        editedName = user.name
                        editedLastname = user.lastname
                        selectedEmoji = if (isEmoji(user.userPhoto)) user.userPhoto else null
                    },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anuluj", color = Color.White)
                }

                Button(
                    onClick = {
                        isSubmitted = true
                        if (editedName.isBlank()) {
                            Toast.makeText(context, "Imię nie może być puste", Toast.LENGTH_SHORT).show()
                        } else if (editedLastname.isBlank()) {
                            Toast.makeText(context, "Nazwisko nie może być puste", Toast.LENGTH_SHORT).show()
                        } else {
                            isEditing = false
                            isSubmitted = false
                            onSaveProfileClick(editedName, editedLastname, selectedEmoji)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Zapisz", color = Color.White)
                }
            }
        } else {
            // View mode
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
            Button(
                onClick = { isEditing = true; isSubmitted = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text("Edytuj profil", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Member code card
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
                    Text("Mój Kod Członka", fontSize = 12.sp, color = TextSecondaryDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%04d", user.userCode),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "Mój kod członka w Where's XYZ: ${String.format("%04d", user.userCode)}")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Udostępnij kod członka"))
                        },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Udostępnij kod", tint = BrandCyan)
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Kod Członka", String.format("%04d", user.userCode))
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Kod skopiowany do schowka!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopiuj kod", tint = BrandIndigo)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout
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
                Icon(Icons.Default.ExitToApp, contentDescription = "Wyloguj się", tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wyloguj się", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
