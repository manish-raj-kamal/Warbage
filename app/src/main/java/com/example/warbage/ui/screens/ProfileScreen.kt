package com.example.warbage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.auth.AuthManager
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.UserEntity
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.GlassTextField
import com.example.warbage.ui.glass.LiquidSegmentedTabs
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    database: WarbageDatabase,
    authManager: AuthManager,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onRoleChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val user by database.userDao().getLastActiveUser().collectAsState(initial = null)
    val lots by database.lotDao().getAllLots().collectAsState(initial = emptyList())
    val requests by database.requestDao().getAllRequests().collectAsState(initial = emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val roles = listOf("Collector", "Recycler")
    val currentRoleIndex = if (user?.role?.lowercase() == "recycler") 1 else 0

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("User Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    GlassIconButton(
                        onClick = {
                            editName = user?.displayName ?: ""
                            editPhone = user?.phoneNumber ?: ""
                            showEditDialog = true
                        },
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Profile Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Orb
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .liquidGlass(
                                shape = CircleShape,
                                blurRadius = 20.dp,
                                lensHeight = 16.dp,
                                lensAmount = 18.dp,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                borderAlpha = 0.6f,
                                elevation = 4.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = user?.displayName?.firstOrNull()?.uppercase() ?: "U"
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user?.displayName ?: "User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.email ?: "No email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Provider Tag
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 10.dp,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    borderAlpha = 0.4f,
                                    elevation = 1.dp
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Auth: ${(user?.authProvider ?: "email").uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 10.dp,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                    borderAlpha = 0.4f,
                                    elevation = 1.dp
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Role: ${(user?.role ?: "collector").replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Role Switcher Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Active Workspace Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Switch between collecting e-waste lots and managing recycler facility requests.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LiquidSegmentedTabs(
                        tabs = roles,
                        selectedTabIndex = currentRoleIndex,
                        onTabSelected = { newIndex ->
                            val newRole = if (newIndex == 0) "collector" else "recycler"
                            user?.let { u ->
                                scope.launch {
                                    authManager.updateProfile(
                                        uid = u.uid,
                                        displayName = u.displayName,
                                        phoneNumber = u.phoneNumber,
                                        role = newRole
                                    )
                                    onRoleChanged(newRole)
                                    Toast.makeText(context, "Switched to ${roles[newIndex]} mode", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Contact & Account Info Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ProfileItemRow(
                        icon = Icons.Default.Phone,
                        label = "Phone Number",
                        value = if (user?.phoneNumber.isNullOrBlank()) "Not provided" else user!!.phoneNumber
                    )

                    ProfileItemRow(
                        icon = Icons.Default.Badge,
                        label = "User UID",
                        value = user?.uid?.take(16)?.let { "$it..." } ?: "Local Session"
                    )

                    ProfileItemRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Member Since",
                        value = user?.createdAt?.let { dateFormat.format(Date(it)) } ?: "Recent"
                    )
                }
            }

            // Activity Impact Overview
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Recycling Impact",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ImpactStatBox(
                            title = "Lots Created",
                            value = "${lots.size}",
                            modifier = Modifier.weight(1f)
                        )
                        ImpactStatBox(
                            title = "Requests",
                            value = "${requests.size}",
                            modifier = Modifier.weight(1f)
                        )
                        ImpactStatBox(
                            title = "Accepted",
                            value = "${requests.count { it.status == "accepted" || it.status == "completed" }}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logout Button
            GlassButton(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Edit Profile Glass Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Edit Profile", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = "Display Name"
                    )
                    GlassTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = "Phone Number"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        user?.let { u ->
                            scope.launch {
                                authManager.updateProfile(
                                    uid = u.uid,
                                    displayName = editName.trim().ifEmpty { u.displayName },
                                    phoneNumber = editPhone.trim(),
                                    role = u.role
                                )
                                showEditDialog = false
                                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text("Log Out", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to log out from WARbage?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        scope.launch {
                            authManager.logout()
                            onLogout()
                        }
                    }
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ImpactStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .liquidGlass(
                shape = RoundedCornerShape(14.dp),
                blurRadius = 10.dp,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                borderAlpha = 0.35f,
                elevation = 1.dp
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

