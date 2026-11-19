package com.warbage.recycling.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
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
import com.warbage.recycling.auth.AuthManager
import com.warbage.recycling.data.WarbageDatabase
import com.warbage.recycling.data.entity.RecyclerEntity
import com.warbage.recycling.data.entity.UserEntity
import com.warbage.recycling.ui.glass.GlassButton
import com.warbage.recycling.ui.glass.GlassCard
import com.warbage.recycling.ui.glass.GlassTextField
import com.warbage.recycling.ui.glass.LiquidSegmentedTabs
import com.warbage.recycling.ui.glass.liquidGlass
import com.warbage.recycling.ui.theme.CyanBright
import com.warbage.recycling.ui.theme.EmeraldBright
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    database: WarbageDatabase,
    authManager: AuthManager,
    onSignupSuccess: (UserEntity) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var selectedRoleIndex by remember { mutableIntStateOf(0) } // 0: Collector, 1: Recycler
    val roles = listOf("Collector & Seller", "Licensed Recycler")
    val roleIcons = listOf(Icons.Default.Person, Icons.Default.Factory)
    val selectedRole = if (selectedRoleIndex == 0) "collector" else "recycler"

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Recycler Specific Fields
    var companyName by remember { mutableStateOf("") }
    var facilityLocation by remember { mutableStateOf("") }
    var acceptedMaterials by remember { mutableStateOf("PCB,Batteries,Mobile,Cables") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Google Sign-In Recycler Complete Details Dialog
    var showGoogleRecyclerDialog by remember { mutableStateOf(false) }
    var pendingGoogleUser by remember { mutableStateOf<UserEntity?>(null) }

    // Email Verification Flow State
    var isEmailVerificationSent by remember { mutableStateOf(false) }
    var verificationEmailAddress by remember { mutableStateOf("") }
    var pendingRegisteredDisplayName by remember { mutableStateOf("") }
    var isCheckingVerification by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }

    if (isEmailVerificationSent) {
        // Dedicated Email Verification Required Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .liquidGlass(
                                shape = CircleShape,
                                blurRadius = 20.dp,
                                tint = CyanBright.copy(alpha = 0.25f),
                                borderAlpha = 0.6f
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = "Verify Email",
                            modifier = Modifier.size(40.dp),
                            tint = CyanBright
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Verify Your Email",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We have sent a verification link to:\n$verificationEmailAddress",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Please check your inbox (and spam folder), open the email from Warbage, and click the link to activate your account.\n\nUntil you verify, access to the app will be restricted.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (verificationError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = verificationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GlassButton(
                        onClick = {
                            isCheckingVerification = true
                            verificationError = null
                            scope.launch {
                                val result = authManager.verifyEmailAndActivate(
                                    displayName = pendingRegisteredDisplayName,
                                    role = selectedRole
                                )
                                isCheckingVerification = false
                                result.onSuccess { user ->
                                    if (selectedRole == "recycler") {
                                        database.recyclerDao().insertRecycler(
                                            RecyclerEntity(
                                                name = companyName.trim().ifEmpty { name.trim() },
                                                location = facilityLocation.trim().ifEmpty { "Local Area" },
                                                latitude = 20.5937,
                                                longitude = 78.9629,
                                                materials = acceptedMaterials.trim().ifEmpty { "PCB,Batteries,Mobile,Cables" },
                                                pricePerKg = 500,
                                                authorized = true
                                            )
                                        )
                                    }
                                    Toast.makeText(context, "Email verified successfully! Welcome to Warbage.", Toast.LENGTH_SHORT).show()
                                    onSignupSuccess(user)
                                }.onFailure { error ->
                                    verificationError = error.localizedMessage ?: "Email not verified yet. Please click the link in your email first."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isCheckingVerification,
                        isLoading = isCheckingVerification,
                        tint = EmeraldBright.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = "I've Verified My Email",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassButton(
                        onClick = {
                            scope.launch {
                                val res = authManager.resendVerificationEmail()
                                res.onSuccess {
                                    Toast.makeText(context, "Verification email resent! Check your inbox.", Toast.LENGTH_SHORT).show()
                                }.onFailure { e ->
                                    Toast.makeText(context, "Failed to resend: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Resend Verification Email",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            isEmailVerificationSent = false
                            scope.launch { authManager.logout() }
                        }
                    ) {
                        Text("Back / Change Email", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    } else {
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
            // Header Orb
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        blurRadius = 22.dp,
                        lensHeight = 16.dp,
                        lensAmount = 20.dp,
                        tint = EmeraldBright.copy(alpha = 0.22f),
                        borderAlpha = 0.65f,
                        elevation = 8.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = "Warbage Logo",
                    modifier = Modifier.size(46.dp),
                    tint = EmeraldBright
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Join WARBAGE",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Create your verified account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Signup Glass Card
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
                        text = "Create Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Error feedback banner
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

                    // Role Selector Toggle
                    Column {
                        Text(
                            text = "SELECT YOUR ROLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LiquidSegmentedTabs(
                            tabs = roles,
                            icons = roleIcons,
                            selectedTabIndex = selectedRoleIndex,
                            onTabSelected = { selectedRoleIndex = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Full Name / Contact Person
                    GlassTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        label = if (selectedRole == "recycler") "Contact Person Name" else "Full Name",
                        placeholder = "e.g. Alex Green",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Recycler specific fields
                    if (selectedRole == "recycler") {
                        GlassTextField(
                            value = companyName,
                            onValueChange = {
                                companyName = it
                                errorMessage = null
                            },
                            label = "Recycler Facility / Company Name",
                            placeholder = "e.g. Green Recycle Yard Ltd",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        GlassTextField(
                            value = facilityLocation,
                            onValueChange = {
                                facilityLocation = it
                                errorMessage = null
                            },
                            label = "Facility Location / City",
                            placeholder = "e.g. Mumbai, Maharashtra",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        GlassTextField(
                            value = acceptedMaterials,
                            onValueChange = {
                                acceptedMaterials = it
                                errorMessage = null
                            },
                            label = "Accepted E-Waste Types",
                            placeholder = "e.g. PCB,Batteries,Mobile,Cables",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
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
                        label = "Password (min 6 characters)",
                        placeholder = "Choose a secure password",
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sign Up Button
                    GlassButton(
                        onClick = {
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all required fields."
                                return@GlassButton
                            }
                            if (selectedRole == "recycler" && (companyName.isBlank() || facilityLocation.isBlank())) {
                                errorMessage = "Please enter your facility name and location."
                                return@GlassButton
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@GlassButton
                            }
                            focusManager.clearFocus()
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authManager.signupWithEmail(
                                    email = email.trim(),
                                    password = password,
                                    displayName = if (selectedRole == "recycler") companyName.trim().ifEmpty { name.trim() } else name.trim(),
                                    role = selectedRole
                                )
                                isLoading = false
                                result.onSuccess { _ ->
                                    isEmailVerificationSent = true
                                    verificationEmailAddress = email.trim()
                                    pendingRegisteredDisplayName = if (selectedRole == "recycler") companyName.trim().ifEmpty { name.trim() } else name.trim()
                                    Toast.makeText(context, "Verification email sent! Please check your inbox.", Toast.LENGTH_LONG).show()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Registration failed."
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
                            text = if (selectedRole == "recycler") "Register Recycler Yard" else "Create Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Divider
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

                    // Google Sign-Up Button
                    GlassButton(
                        onClick = {
                            focusManager.clearFocus()
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authManager.signInWithGoogle(context, role = selectedRole)
                                isLoading = false
                                result.onSuccess { user ->
                                    if (selectedRole == "recycler") {
                                        pendingGoogleUser = user
                                        showGoogleRecyclerDialog = true
                                    } else {
                                        onSignupSuccess(user)
                                    }
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Google Sign-Up failed."
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
                                text = "Sign up with Google",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
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
                            onSignupSuccess(pendingGoogleUser!!)
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
                        authManager.logout() // Cancel signup since they refused to provide info
                        showGoogleRecyclerDialog = false 
                    }
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
