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
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    onRegisterClick: (String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Validation
    val isNameValid = name.isNotBlank()
    val isLastnameValid = lastname.isNotBlank()
    val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.length >= 6
    val isFormValid = isNameValid && isLastnameValid && isEmailValid && isPasswordValid

    var isSubmitted by remember { mutableStateOf(false) }
    val showNameError = isSubmitted && !isNameValid
    val showLastnameError = isSubmitted && !isLastnameValid
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
                text = "Załóż konto",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dołącz do Where's XYZ, aby pozostać w kontakcie",
                fontSize = 16.sp,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Form card
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
                    // First Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Imię", color = TextSecondaryDark) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Imię",
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
                        isError = showNameError,
                        supportingText = {
                            if (showNameError) {
                                Text("Imię nie może być puste", color = ErrorRed)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Last Name
                    OutlinedTextField(
                        value = lastname,
                        onValueChange = { lastname = it },
                        label = { Text("Nazwisko", color = TextSecondaryDark) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Nazwisko",
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
                        isError = showLastnameError,
                        supportingText = {
                            if (showLastnameError) {
                                Text("Nazwisko nie może być puste", color = ErrorRed)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        label = { Text("Hasło (min. 6 znaków)", color = TextSecondaryDark) },
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

                    // Error text
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                        // Auto clear error on form change
                        LaunchedEffect(name, lastname, email, password) {
                            onClearError()
                        }
                    }

                    // Register Button
                    Button(
                        onClick = {
                            isSubmitted = true
                            if (name.isBlank()) {
                                Toast.makeText(context, "Wpisz swoje imię", Toast.LENGTH_SHORT).show()
                            } else if (lastname.isBlank()) {
                                Toast.makeText(context, "Wpisz swoje nazwisko", Toast.LENGTH_SHORT).show()
                            } else if (email.isBlank()) {
                                Toast.makeText(context, "Wpisz swój adres e-mail", Toast.LENGTH_SHORT).show()
                            } else if (!isEmailValid) {
                                Toast.makeText(context, "Niepoprawny format adresu e-mail", Toast.LENGTH_SHORT).show()
                            } else if (password.isEmpty()) {
                                Toast.makeText(context, "Wpisz hasło", Toast.LENGTH_SHORT).show()
                            } else if (!isPasswordValid) {
                                Toast.makeText(context, "Hasło jest za krótkie (min. 6 znaków)", Toast.LENGTH_SHORT).show()
                            } else {
                                onRegisterClick(name, lastname, email, password)
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
                                    text = "Zarejestruj się",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Redirect to Login
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Masz już konto?",
                    fontSize = 14.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Zaloguj się",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandCyan,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
