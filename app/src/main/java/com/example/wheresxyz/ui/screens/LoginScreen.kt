package com.example.wheresxyz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wheresxyz.ui.theme.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onOAuthClick: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Validation
    val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.length >= 6
    val isFormValid = isEmailValid && isPasswordValid

    var isSubmitted by remember { mutableStateOf(false) }
    val showEmailError = isSubmitted && !isEmailValid
    val showPasswordError = isSubmitted && !isPasswordValid

    // Show toast on firebase/auth errors
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0F19), // Deep Space Dark
                        Color(0xFF1E1B4B)  // Dark Indigo
                    )
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Witaj ponownie",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Zaloguj się, aby zlokalizować swoją grupę",
                fontSize = 16.sp,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Glassmorphism login card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adres e-mail", color = TextSecondaryDark) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "E-mail",
                                tint = BrandIndigo
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = BrandIndigo,
                            unfocusedLabelColor = TextSecondaryDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            errorTextColor = Color.White,
                            errorBorderColor = ErrorRed,
                            errorLabelColor = ErrorRed,
                            errorSupportingTextColor = ErrorRed
                        ),
                        isError = showEmailError,
                        supportingText = {
                            if (showEmailError) {
                                Text("Wprowadź poprawny adres e-mail", color = ErrorRed)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Hasło", color = TextSecondaryDark) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Hasło",
                                tint = BrandIndigo
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Ukryj hasło" else "Pokaż hasło",
                                    tint = TextSecondaryDark
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = BrandIndigo,
                            unfocusedLabelColor = TextSecondaryDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            errorTextColor = Color.White,
                            errorBorderColor = ErrorRed,
                            errorLabelColor = ErrorRed,
                            errorSupportingTextColor = ErrorRed
                        ),
                        isError = showPasswordError,
                        supportingText = {
                            if (showPasswordError) {
                                Text("Hasło musi mieć co najmniej 6 znaków", color = ErrorRed)
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Alert Dialog / text
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                        // Auto clear error on state change
                        LaunchedEffect(email, password) {
                            onClearError()
                        }
                    }

                    // Log In Button
                    Button(
                        onClick = {
                            isSubmitted = true
                            if (email.isBlank()) {
                                Toast.makeText(context, "Wpisz swój adres e-mail", Toast.LENGTH_SHORT).show()
                            } else if (!isEmailValid) {
                                Toast.makeText(context, "Niepoprawny format adresu e-mail", Toast.LENGTH_SHORT).show()
                            } else if (password.isEmpty()) {
                                Toast.makeText(context, "Wpisz swoje hasło", Toast.LENGTH_SHORT).show()
                            } else if (!isPasswordValid) {
                                Toast.makeText(context, "Hasło jest za krótkie (min. 6 znaków)", Toast.LENGTH_SHORT).show()
                            } else {
                                onLoginClick(email, password)
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = BrandIndigo.copy(alpha = 0.3f)
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = if (isFormValid) {
                                        listOf(BrandIndigo, BrandViolet)
                                    } else {
                                        listOf(BrandIndigo.copy(alpha = 0.5f), BrandViolet.copy(alpha = 0.5f))
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = if (isFormValid) {
                                            listOf(BrandIndigo, BrandViolet)
                                        } else {
                                            listOf(BrandIndigo.copy(alpha = 0.4f), BrandViolet.copy(alpha = 0.4f))
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Zaloguj się",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // OAuth Dividers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    color = Color.White.copy(alpha = 0.15f),
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Lub kontynuuj za pomocą",
                    fontSize = 14.sp,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(
                    color = Color.White.copy(alpha = 0.15f),
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google OAuth Button
                OAuthButton(
                    providerName = "Google",
                    backgroundColor = Color(0xFFEA4335).copy(alpha = 0.15f),
                    textColor = Color(0xFFFEEA35),
                    borderColor = Color(0xFFEA4335).copy(alpha = 0.4f),
                    onClick = { onOAuthClick("Google") },
                    modifier = Modifier.weight(1f)
                )

                // Github OAuth Button
                OAuthButton(
                    providerName = "GitHub",
                    backgroundColor = Color(0xFF24292E).copy(alpha = 0.4f),
                    textColor = Color.White,
                    borderColor = Color.White.copy(alpha = 0.2f),
                    onClick = { onOAuthClick("GitHub") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Redirect to Register
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nie masz konta?",
                    fontSize = 14.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Zarejestruj się",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandCyan,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

@Composable
fun OAuthButton(
    providerName: String,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = providerName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
