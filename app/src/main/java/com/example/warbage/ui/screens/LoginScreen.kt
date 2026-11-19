package com.example.warbage.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.auth.AuthManager
import com.example.warbage.data.entity.UserEntity
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassTextField
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import com.example.warbage.ui.theme.EmeraldNeon
import kotlinx.coroutines.launch

import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.RecyclerEntity

@Composable
fun LoginScreen(
    database: WarbageDatabase,
    authManager: AuthManager,
    onLoginSuccess: (UserEntity) -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var isSendingResetEmail by remember { mutableStateOf(false) }

    // Google Sign-In Recycler Complete Details Dialog
    var showGoogleRecyclerDialog by remember { mutableStateOf(false) }
    var pendingGoogleUser by remember { mutableStateOf<UserEntity?>(null) }
    var companyName by remember { mutableStateOf("") }
    var facilityLocation by remember { mutableStateOf("") }
    var acceptedMaterials by remember { mutableStateOf("PCB,Batteries,Mobile,Cables") }

    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authManager.currentDbUser.collect { user ->
            if (user != null && !hasNavigated) {
                if (user.role.lowercase() == "recycler") {
                    val recycler = database.recyclerDao().getRecyclerByNameOnce(user.displayName)
                    if (recycler == null) {
                        pendingGoogleUser = user
                        showGoogleRecyclerDialog = true
                    } else {
                        hasNavigated = true
                        onLoginSuccess(user)
                    }
                } else {
                    hasNavigated = true
                    onLoginSuccess(user)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-Tech Hero Emblem
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        blurRadius = 24.dp,
                        lensHeight = 18.dp,
                        lensAmount = 22.dp,
                        tint = EmeraldBright.copy(alpha = 0.22f),
                        borderAlpha = 0.65f,
                        elevation = 8.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = "Warbage Logo",
                    modifier = Modifier.size(52.dp),
                    tint = EmeraldBright
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "WARBAGE",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Next-Gen E-Waste Recycling & Exchange",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Glass Form Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Error Feedback Banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { error ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlass(
                                        shape = RoundedCornerShape(14.dp),
                                        blurRadius = 10.dp,
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                        borderAlpha = 0.5f
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Email Field
                    GlassTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = "Email Address",
                        placeholder = "name@domain.com",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Password Field
                    GlassTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = "Password",
                        placeholder = "Enter your password",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                forgotPasswordEmail = email
                                showForgotPasswordDialog = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Primary Sign In Button
                    GlassButton(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both email and password."
                                return@GlassButton
                            }
                            focusManager.clearFocus()
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val trimmedEmail = email.trim().lowercase()
                                if (trimmedEmail.contains("demo.collector")) {
                                    val result = authManager.signInAsDemoUser("collector")
                                    isLoading = false
                                    result.onSuccess { onLoginSuccess(it) }
                                } else if (trimmedEmail.contains("demo.recycler")) {
                                    val result = authManager.signInAsDemoUser("recycler")
                                    isLoading = false
                                    result.onSuccess { onLoginSuccess(it) }
                                } else {
                                    val result = authManager.loginWithEmail(email.trim(), password)
                                    isLoading = false
                                    result.onSuccess { user ->
                                        onLoginSuccess(user)
                                    }.onFailure { error ->
                                        errorMessage = error.localizedMessage ?: "Sign in failed. Check credentials."
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        isLoading = isLoading,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = "Sign In to Warbage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Modern Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                        Text(
                            text = "  OR CONTINUE WITH  ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }

                    // Google Sign-In Glass Button
                    GlassButton(
                        onClick = {
                            focusManager.clearFocus()
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authManager.signInWithGoogle(context)
                                isLoading = false
                                result.onSuccess { user ->
                                    if (user.role.lowercase() == "recycler") {
                                        val recycler = database.recyclerDao().getRecyclerByNameOnce(user.displayName)
                                        if (recycler == null) {
                                            pendingGoogleUser = user
                                            showGoogleRecyclerDialog = true
                                        } else {
                                            Toast.makeText(context, "Welcome back, ${user.displayName}!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess(user)
                                        }
                                    } else {
                                        Toast.makeText(context, "Welcome, ${user.displayName}!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(user)
                                    }
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Google sign in was cancelled or failed."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Demo Guest Access Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "GOOGLE TESTER QUICK DEMO ACCESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val result = authManager.signInAsDemoUser("collector")
                                        isLoading = false
                                        result.onSuccess { user ->
                                            onLoginSuccess(user)
                                        }.onFailure { error ->
                                            errorMessage = error.localizedMessage
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                enabled = !isLoading,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    text = "Demo Collector",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            GlassButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val result = authManager.signInAsDemoUser("recycler")
                                        isLoading = false
                                        result.onSuccess { user ->
                                            onLoginSuccess(user)
                                        }.onFailure { error ->
                                            errorMessage = error.localizedMessage
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                enabled = !isLoading,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    text = "Demo Recycler",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Navigation Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToSignup() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trust highlights pill row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrustBadge("🌱 Zero Landfill")
                TrustBadge("⚡ Instant Escrow")
                TrustBadge("🤖 AI Valuation")
            }
        }
    }

    // Forgot Password Reset Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSendingResetEmail) showForgotPasswordDialog = false },
            title = {
                Text("Reset Password", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Enter your registered email address below. We'll send you a password reset link so you can set a password for your account.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    GlassTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = "Email Address",
                        placeholder = "name@domain.com"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (forgotPasswordEmail.isBlank()) {
                            Toast.makeText(context, "Please enter an email address", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        isSendingResetEmail = true
                        scope.launch {
                            val res = authManager.sendPasswordResetEmail(forgotPasswordEmail)
                            isSendingResetEmail = false
                            showForgotPasswordDialog = false
                            res.onSuccess {
                                Toast.makeText(
                                    context,
                                    "Password reset link sent to $forgotPasswordEmail! Please check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure { err ->
                                Toast.makeText(
                                    context,
                                    err.localizedMessage ?: "Failed to send reset email.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = !isSendingResetEmail
                ) {
                    Text("Send Reset Link", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false },
                    enabled = !isSendingResetEmail
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Google Sign-In Recycler Facility Info Modal
    if (showGoogleRecyclerDialog && pendingGoogleUser != null) {
        AlertDialog(
            onDismissRequest = { /* Force completion */ },
            title = {
                Text("Complete Recycler Registration", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Please provide your processing facility details to complete your Recycler account.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    GlassTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = "Company / Facility Name",
                        placeholder = "e.g. Green Recycling Yard"
                    )
                    GlassTextField(
                        value = facilityLocation,
                        onValueChange = { facilityLocation = it },
                        label = "Facility Location",
                        placeholder = "e.g. Mumbai, Maharashtra"
                    )
                    GlassTextField(
                        value = acceptedMaterials,
                        onValueChange = { acceptedMaterials = it },
                        label = "Accepted E-Waste Types",
                        placeholder = "e.g. PCB,Batteries,Mobile,Cables"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            database.recyclerDao().insertRecycler(
                                RecyclerEntity(
                                    name = companyName.trim().ifEmpty { pendingGoogleUser!!.displayName },
                                    location = facilityLocation.trim().ifEmpty { "Local Area" },
                                    latitude = 20.5937,
                                    longitude = 78.9629,
                                    materials = acceptedMaterials.trim().ifEmpty { "PCB,Batteries,Mobile,Cables" },
                                    pricePerKg = 500,
                                    authorized = true
                                )
                            )
                            showGoogleRecyclerDialog = false
                            onLoginSuccess(pendingGoogleUser!!)
                        }
                    },
                    enabled = companyName.isNotBlank() && facilityLocation.isNotBlank()
                ) {
                    Text("Save & Complete", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    scope.launch {
                        authManager.logout() // Cancel login since they refused to provide info
                        showGoogleRecyclerDialog = false 
                    }
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TrustBadge(text: String) {
    Box(
        modifier = Modifier
            .liquidGlass(
                shape = CircleShape,
                blurRadius = 8.dp,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                borderAlpha = 0.25f,
                elevation = 0.dp
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Authentic multi-color Google "G" logo drawn via Canvas
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = this.size.minDimension
        val strokeWidth = size * 0.22f
        val radius = (size - strokeWidth) / 2f
        val center = Offset(size / 2f, size / 2f)

        // Blue arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Green arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Yellow arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Red arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth
        )
    }
}
