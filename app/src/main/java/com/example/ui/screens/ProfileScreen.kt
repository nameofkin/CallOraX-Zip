package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CalloraViolet
import com.example.ui.theme.GrayTick
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.VerifiedGreen
import com.example.viewmodel.CallOraViewModel

@Composable
fun ProfileScreen(
    viewModel: CallOraViewModel,
    onNavigateToRecordings: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val simCards by viewModel.simCards.collectAsState()
    val defaultSimSlot by viewModel.defaultSimSlot.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfile(
                fullName = currentUser?.fullName ?: (currentUser?.email?.substringBefore("@") ?: "CallOra User"),
                phone = currentUser?.phoneNumber,
                avatarUrl = uri.toString()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Identity Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture with Edit Icon
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(CalloraViolet.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.fullName?.takeIf { it.isNotBlank() } ?: currentUser?.email ?: "U").take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalloraViolet
                        )
                    }

                    // Change Photo Button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(CalloraViolet)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Picture",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full Name with Verification Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: (currentUser?.email?.substringBefore("@") ?: "CallOra User"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Pro Green Badge
                    if (currentUser?.isPro == true) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Pro",
                            tint = VerifiedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Premium Gray Tick
                    if (currentUser?.isVerified == true) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Gray Tick",
                            tint = GrayTick,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "@${currentUser?.fullName?.lowercase()?.replace(" ", "")?.filter { it.isLetterOrDigit() || it == '_' }?.takeIf { it.isNotBlank() } ?: currentUser?.email?.substringBefore("@") ?: "user"}",
                    fontSize = 12.sp,
                    color = CalloraViolet,
                    fontWeight = FontWeight.SemiBold
                )

                if (!currentUser?.email.isNullOrBlank()) {
                    Text(
                        text = currentUser?.email ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (!currentUser?.phoneNumber.isNullOrBlank()) {
                    Text(
                        text = currentUser?.phoneNumber ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Role & Status Badges
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ROLE: ${currentUser?.role?.uppercase() ?: "SUPERADMIN"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )

                    Text(
                        text = if (currentUser?.isPro == true) "PRO SUBSCRIBER" else "FREE TIER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentUser?.isPro == true) VerifiedGreen else Color(0xFF94A3B8),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (currentUser?.isPro == true) VerifiedGreen.copy(alpha = 0.15f)
                                else Color(0xFF94A3B8).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showEditProfileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Edit Profile Details", fontSize = 12.sp)
                }
            }
        }

        // 10. Themes (11 Supported Themes)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Themes",
                        tint = CalloraViolet
                    )
                    Text(
                        text = "Theme & Visual Styling (11 Options)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Select your preferred color scheme. Persistent across sessions.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppTheme.values()) { theme ->
                        val isSelected = currentTheme == theme
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTheme(theme) },
                            label = {
                                Text(
                                    text = if (theme.isPremiumOnly) "${theme.displayName} ★" else theme.displayName,
                                    fontSize = 11.sp
                                )
                            },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CalloraViolet,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // 4. Dual SIM Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SimCard,
                        contentDescription = "Dual SIM",
                        tint = CalloraViolet
                    )
                    Text(
                        text = "Dual SIM Management",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Configure preferred calling SIM or prompt for every call.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Option: Ask every time
                SimChoiceRow(
                    title = "Ask Every Time",
                    subtitle = "Show SIM selector before placing calls",
                    isSelected = defaultSimSlot == -1,
                    onSelect = { viewModel.setDefaultSim(-1) }
                )

                // SIM 1
                simCards.getOrNull(0)?.let { sim1 ->
                    SimChoiceRow(
                        title = "SIM 1: ${sim1.carrierName}",
                        subtitle = "Always call using slot 1",
                        isSelected = defaultSimSlot == 0,
                        onSelect = { viewModel.setDefaultSim(0) }
                    )
                }

                // SIM 2
                simCards.getOrNull(1)?.let { sim2 ->
                    SimChoiceRow(
                        title = "SIM 2: ${sim2.carrierName}",
                        subtitle = "Always call using slot 2",
                        isSelected = defaultSimSlot == 1,
                        onSelect = { viewModel.setDefaultSim(1) }
                    )
                }
            }
        }

        // Navigation Shortcuts: Notes, Recordings, Premium, Admin
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ProfileShortcutRow(
                    icon = Icons.Default.WorkspacePremium,
                    title = "Pro & Premium Subscriptions",
                    subtitle = "Manage tiers & manual payment receipts",
                    onClick = onNavigateToPremium
                )
                ProfileShortcutRow(
                    icon = Icons.Default.Mic,
                    title = "Voice Call Recordings",
                    subtitle = "Compliant two-party audio recordings",
                    onClick = onNavigateToRecordings
                )
                ProfileShortcutRow(
                    icon = Icons.Default.Note,
                    title = "Smart Notepad",
                    subtitle = "Quick notes linked to contacts & calls",
                    onClick = onNavigateToNotes
                )
                if (currentUser?.isAdmin == true) {
                    ProfileShortcutRow(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Dashboard",
                        subtitle = "Review payments, manage roles & spam",
                        onClick = onNavigateToAdmin
                    )
                }
            }
        }

        // Account Controls: Logout & Delete Account
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.signOut() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Out", fontSize = 12.sp)
            }

            Button(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Account", fontSize = 12.sp)
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            var newName by remember { mutableStateOf(currentUser?.fullName ?: (currentUser?.email?.substringBefore("@") ?: "")) }
            var newPhone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("Edit Profile") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateProfile(newName.trim(), newPhone.trim(), currentUser?.avatarUrl)
                            showEditProfileDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Delete Account Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
                text = {
                    Text("Are you sure you want to delete your CallOra account and data? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAccount()
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Permanently Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun SimChoiceRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) CalloraViolet else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ProfileShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CalloraViolet.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = CalloraViolet, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
