package com.warbage.recycling.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warbage.recycling.auth.AuthManager
import com.warbage.recycling.data.WarbageDatabase
import com.warbage.recycling.data.entity.UserEntity
import com.warbage.recycling.ui.glass.GlassButton
import com.warbage.recycling.ui.glass.GlassCard
import com.warbage.recycling.ui.glass.GlassIconButton
import com.warbage.recycling.ui.glass.GlassTextField
import com.warbage.recycling.ui.glass.LiquidSegmentedTabs
import com.warbage.recycling.ui.glass.LiquidTopAppBar
import com.warbage.recycling.ui.glass.liquidGlass
import com.warbage.recycling.ui.theme.CyanBright
import com.warbage.recycling.ui.theme.EmeraldBright
import com.warbage.recycling.ui.theme.EmeraldNeon
import com.warbage.recycling.ui.theme.GoldBright
import com.warbage.recycling.ui.theme.VioletBright
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.warbage.recycling.data.entity.UpgradeRequestEntity

@Composable
fun ProfileScreen(
    database: WarbageDatabase,
    authManager: AuthManager,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onRoleChanged: (String) -> Unit // Keeping it to not break calls, though it's unused
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val user by database.userDao().getLastActiveUser().collectAsState(initial = null)
    val lots by database.lotDao().getLotsByUser(user?.uid ?: "").collectAsState(initial = emptyList())
    val requests by database.requestDao().getRequestsByUser(user?.uid ?: "").collectAsState(initial = emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var upgradeRecyclerName by remember { mutableStateOf("") }
    var upgradeLocation by remember { mutableStateOf("") }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val wasDeleted = authManager.checkAndDeleteExpiredAccount(uid)
            if (wasDeleted) {
                Toast.makeText(context, "Account has been permanently deleted after 7 days.", Toast.LENGTH_LONG).show()
                onLogout()
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("Eco Passport & Profile", fontWeight = FontWeight.Bold) },
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
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
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
            // Hero Profile Card
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
                    // Avatar Orb with Glowing Perimeter Ring
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .liquidGlass(
                                shape = CircleShape,
                                blurRadius = 20.dp,
                                lensHeight = 16.dp,
                                lensAmount = 18.dp,
                                tint = EmeraldBright.copy(alpha = 0.22f),
                                borderAlpha = 0.65f,
                                elevation = 6.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = user?.displayName?.firstOrNull()?.uppercase() ?: "U"
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldBright
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user?.displayName ?: "Eco Recycler",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Member",
                            tint = EmeraldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.email ?: "member@warbage.earth",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tags row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 10.dp,
                                    tint = EmeraldBright.copy(alpha = 0.16f),
                                    borderAlpha = 0.45f,
                                    elevation = 1.dp
                                )
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "ROLE: ${(user?.role ?: "collector").uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldBright,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 10.dp,
                                    tint = CyanBright.copy(alpha = 0.16f),
                                    borderAlpha = 0.45f,
                                    elevation = 1.dp
                                )
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "AUTH: ${(user?.authProvider ?: "email").uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            if (user?.role?.lowercase() == "collector") {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Recycler Program",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Apply to become a licensed recycler to receive e-waste directly from users.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GlassButton(
                            onClick = { showUpgradeDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            tint = EmeraldBright.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                Icons.Default.Factory,
                                contentDescription = null,
                                tint = EmeraldBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upgrade as Recycler",
                                color = EmeraldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Activity Impact Overview
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    if (user?.role?.lowercase() == "collector") {
                        Text(
                            text = "Sustainability & Trading Score",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ImpactStatBox(
                                icon = Icons.Default.Inventory2,
                                title = "Lots Created",
                                value = "${lots.size}",
                                accent = EmeraldBright,
                                modifier = Modifier.weight(1f)
                            )
                            ImpactStatBox(
                                icon = Icons.AutoMirrored.Filled.Send,
                                title = "Requests",
                                value = "${requests.size}",
                                accent = CyanBright,
                                modifier = Modifier.weight(1f)
                            )
                            ImpactStatBox(
                                icon = Icons.Default.Verified,
                                title = "Completed",
                                value = "${requests.count { it.status == "accepted" || it.status == "completed" }}",
                                accent = GoldBright,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = "Facility Processing Impact",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ImpactStatBox(
                                icon = Icons.AutoMirrored.Filled.Send,
                                title = "Pending Intake",
                                value = "${requests.count { it.status == "pending" }}",
                                accent = GoldBright,
                                modifier = Modifier.weight(1f)
                            )
                            ImpactStatBox(
                                icon = Icons.Default.CheckCircle,
                                title = "Accepted",
                                value = "${requests.count { it.status == "accepted" }}",
                                accent = CyanBright,
                                modifier = Modifier.weight(1f)
                            )
                            ImpactStatBox(
                                icon = Icons.Default.Verified,
                                title = "Processed",
                                value = "${requests.count { it.status == "completed" }}",
                                accent = EmeraldBright,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Contact & Account Info Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Account Security & Credentials",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ProfileItemRow(
                        icon = Icons.Default.Phone,
                        label = "Phone Contact",
                        value = if (user?.phoneNumber.isNullOrBlank()) "Add phone number" else user!!.phoneNumber
                    )

                    ProfileItemRow(
                        icon = Icons.Default.Badge,
                        label = "Account Identifier",
                        value = user?.uid?.take(14)?.let { "$it..." } ?: "Verified Local"
                    )

                    ProfileItemRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Verified Since",
                        value = user?.createdAt?.let { dateFormat.format(Date(it)) } ?: "Active Member"
                    )
                }
            }

            // Pending Deletion Banner if user requested deletion
            if (user?.isPendingDeletion == true) {
                val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
                val elapsed = System.currentTimeMillis() - (user?.deletionRequestedAt ?: 0L)
                val remainingMs = maxOf(0L, sevenDaysMs - elapsed)
                val remainingDays = remainingMs / (1000 * 60 * 60 * 24)
                val remainingHours = (remainingMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Account Scheduled for Deletion",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your account will be permanently wiped from the database in $remainingDays day(s) and $remainingHours hour(s). You can cancel this request anytime before the countdown ends.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GlassButton(
                            onClick = {
                                user?.let { u ->
                                    scope.launch {
                                        authManager.cancelAccountDeletion(u.uid)
                                        Toast.makeText(context, "Account deletion cancelled successfully.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            tint = EmeraldBright.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                Icons.Default.Restore,
                                contentDescription = null,
                                tint = EmeraldBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancel Account Deletion",
                                color = EmeraldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Logout Button
            GlassButton(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out from Warbage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Account Deletion Request Button
            if (user?.isPendingDeletion != true) {
                GlassButton(
                    onClick = { showDeleteAccountConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Request Account Deletion (7-Day Period)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Edit Profile Glass Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Edit Profile Details", fontWeight = FontWeight.Bold)
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
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                Text("Sign Out", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to sign out from your WARBAGE account?")
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
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = {
                Text("Schedule Account Deletion", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            },
            text = {
                Text(
                    "Your account will be scheduled for complete deletion from the database in 7 days.\n\n" +
                    "During this 7-day period, you can log in or open your profile anytime to cancel the deletion request. " +
                    "After 7 days, your account and all associated data will be permanently wiped.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            showDeleteAccountConfirm = false
                            user?.let { u ->
                                scope.launch {
                                    authManager.permanentlyDeleteAccount(u.uid)
                                    Toast.makeText(context, "Account permanently deleted.", Toast.LENGTH_LONG).show()
                                    onLogout()
                                }
                            }
                        }
                    ) {
                        Text("Delete Now", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(
                        onClick = {
                            showDeleteAccountConfirm = false
                            user?.let { u ->
                                scope.launch {
                                    authManager.requestAccountDeletion(u.uid)
                                    Toast.makeText(
                                        context,
                                        "Account deletion scheduled in 7 days.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Text("Schedule (7 Days)")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text("Keep Account")
                }
            }
        )
    }

    // Upgrade Recycler Dialog
    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = {
                Text("Request Recycler License", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Submit a request to the app developer to upgrade your account to a Licensed Recycler.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    GlassTextField(
                        value = upgradeRecyclerName,
                        onValueChange = { upgradeRecyclerName = it },
                        label = "Recycler Facility Name"
                    )
                    GlassTextField(
                        value = upgradeLocation,
                        onValueChange = { upgradeLocation = it },
                        label = "Facility Location"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        user?.let { u ->
                            scope.launch {
                                database.upgradeRequestDao().insertRequest(
                                    UpgradeRequestEntity(
                                        userId = u.uid,
                                        recyclerName = upgradeRecyclerName,
                                        location = upgradeLocation,
                                        dateRequested = System.currentTimeMillis()
                                    )
                                )
                                showUpgradeDialog = false
                                Toast.makeText(
                                    context,
                                    "Request submitted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    enabled = upgradeRecyclerName.isNotBlank() && upgradeLocation.isNotBlank()
                ) {
                    Text("Submit Request", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
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
            Spacer(modifier = Modifier.width(10.dp))
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                blurRadius = 10.dp,
                tint = accent.copy(alpha = 0.14f),
                borderAlpha = 0.35f,
                elevation = 2.dp
            )
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
