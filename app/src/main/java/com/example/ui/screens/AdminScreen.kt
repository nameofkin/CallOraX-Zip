package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.remote.PaymentSubmission
import com.example.data.remote.UserProfile
import com.example.ui.theme.CalloraCyan
import com.example.ui.theme.CalloraViolet
import com.example.ui.theme.SpamRed
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarningOrange
import com.example.viewmodel.CallOraViewModel

@Composable
fun AdminScreen(
    viewModel: CallOraViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val pendingPayments by viewModel.pendingPayments.collectAsState()
    val allUsers by viewModel.allUsersList.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Approvals (${pendingPayments.size})", "Users (${allUsers.size})", "Security & Spam")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin",
                    tint = CalloraCyan,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "CalloraX Administration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Superadmin: ${currentUser?.fullName ?: "Abdul Basit Kamboh"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Exit", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dashboard Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricCard(
                title = "Pending",
                value = "${pendingPayments.size}",
                color = WarningOrange,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Total Users",
                value = "${allUsers.size}",
                color = CalloraViolet,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Spam Blocked",
                value = "1.2k",
                color = VerifiedGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = CalloraViolet
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Payment Approvals Queue
                if (pendingPayments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No pending payment submissions", color = Color(0xFF94A3B8))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pendingPayments, key = { it.id }) { submission ->
                            PaymentApprovalCard(
                                submission = submission,
                                onApprove = {
                                    viewModel.reviewPaymentSubmission(submission.id, "approved", "Payment verified")
                                },
                                onReject = {
                                    viewModel.reviewPaymentSubmission(submission.id, "rejected", "Invalid transaction ID or slip")
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Users Management
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allUsers, key = { it.id }) { user ->
                        AdminUserCard(
                            user = user,
                            onRoleChange = { newRole ->
                                viewModel.updateUserRole(user.id, newRole)
                            },
                            onToggleVerified = {
                                viewModel.toggleUserBadges(user.id, !user.isVerified, user.isPro)
                            },
                            onTogglePro = {
                                viewModel.toggleUserBadges(user.id, user.isVerified, !user.isPro)
                            }
                        )
                    }
                }
            }

            2 -> {
                // Security & Spam Telemetry
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Centralized Caller ID & Spam Radar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Telemarketing algorithms & State Bank fraud numbers automatically blocked by CallOra RLS database rules.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SpamRed.copy(alpha = 0.15f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("+92 312 9012345", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("High Risk • 342 user reports", fontSize = 11.sp, color = SpamRed)
                            }
                            Text("BLOCKED", fontSize = 11.sp, fontWeight = FontWeight.Black, color = SpamRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun PaymentApprovalCard(
    submission: PaymentSubmission,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = submission.userName ?: "Customer (${submission.userEmail ?: "No email"})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Plan: ${submission.planTier.replace("_", " ").uppercase()} • Rs. ${submission.amountPkr.toInt()}",
                        fontSize = 12.sp,
                        color = CalloraViolet,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = submission.gateway.replace("_", " ").uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3B82F6))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction ID and Sender Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TRX ID:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(submission.trxId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                submission.senderAccountTitle?.let { title ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Sender Title:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(title, fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Approve & Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Approve & Activate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = SpamRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    onRoleChange: (String) -> Unit,
    onToggleVerified: () -> Unit,
    onTogglePro: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = user.fullName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (user.isVerified) {
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = VerifiedGreen, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = user.email ?: user.phoneNumber ?: "No contact",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Role Dropdown Button
                Box {
                    Text(
                        text = user.role.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CalloraViolet)
                            .clickable { showRoleMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        listOf("user", "moderator", "admin", "superadmin").forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.uppercase()) },
                                onClick = {
                                    showRoleMenu = false
                                    onRoleChange(role)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = user.isVerified,
                    onClick = onToggleVerified,
                    label = { Text("Verified Badge", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = user.isPro,
                    onClick = onTogglePro,
                    label = { Text("Pro Status", fontSize = 10.sp) }
                )
            }
        }
    }
}
