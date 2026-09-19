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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.remote.CommunityPost
import com.example.ui.theme.CalloraCyan
import com.example.ui.theme.CalloraViolet
import com.example.ui.theme.SpamRed
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarningOrange
import com.example.viewmodel.CallOraViewModel

@Composable
fun CommunityScreen(
    viewModel: CallOraViewModel
) {
    val posts by viewModel.communityPosts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Scam Alerts", "Safety Tips", "General")
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, selectedCategory) {
        when (selectedCategory) {
            "Scam Alerts" -> posts.filter { it.category == "scam_alert" }
            "Safety Tips" -> posts.filter { it.category == "safety_tip" }
            "General" -> posts.filter { it.category == "general" }
            else -> posts
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CalloraViolet.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Posts Feed
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        canDelete = currentUser?.isAdmin == true || currentUser?.id == post.authorId,
                        onLike = { viewModel.togglePostLike(post.id) },
                        onDelete = { viewModel.deleteCommunityPost(post.id) }
                    )
                }
            }
        }

        // Create Post FAB
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_post_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Post")
        }

        if (showCreateDialog) {
            CreatePostDialog(
                onDismiss = { showCreateDialog = false },
                onSubmit = { cat, caption, flaggedNumber ->
                    viewModel.createCommunityPost(
                        category = cat,
                        caption = caption,
                        flaggedNumber = flaggedNumber,
                        imageUrl = null
                    )
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    canDelete: Boolean,
    onLike: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryBadge = when (post.category) {
        "scam_alert" -> Triple(SpamRed.copy(alpha = 0.15f), SpamRed, "SCAM ALERT")
        "safety_tip" -> Triple(CalloraCyan.copy(alpha = 0.15f), CalloraCyan, "SAFETY TIP")
        else -> Triple(CalloraViolet.copy(alpha = 0.15f), CalloraViolet, "GENERAL")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Author Avatar, Name, Category Pill, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CalloraViolet.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalloraViolet
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = post.authorName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (post.authorVerified) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = VerifiedGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (post.authorRole in listOf("admin", "superadmin")) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = post.createdAt,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = categoryBadge.third,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryBadge.second,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryBadge.first)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )

                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Caption
            Text(
                text = post.caption,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                lineHeight = 19.sp
            )

            // Flagged Number Warning Banner (if present)
            if (!post.flaggedNumber.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SpamRed.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Flagged Number",
                        tint = SpamRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Flagged High-Risk Number:",
                            fontSize = 10.sp,
                            color = SpamRed
                        )
                        Text(
                            text = post.flaggedNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactions: Likes & Comments counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable(onClick = onLike)
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.likesCount} Helpful",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.commentsCount} Comments",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (category: String, caption: String, flaggedNumber: String?) -> Unit
) {
    var category by remember { mutableStateOf("scam_alert") }
    var caption by remember { mutableStateOf("") }
    var flaggedNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share with Community", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = category == "scam_alert",
                        onClick = { category = "scam_alert" },
                        label = { Text("Scam Alert", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = category == "safety_tip",
                        onClick = { category = "safety_tip" },
                        label = { Text("Safety Tip", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = category == "general",
                        onClick = { category = "general" },
                        label = { Text("General", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Describe the situation / scam method...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                if (category == "scam_alert") {
                    OutlinedTextField(
                        value = flaggedNumber,
                        onValueChange = { flaggedNumber = it },
                        label = { Text("Flagged Phone Number (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (caption.isNotBlank()) {
                        onSubmit(category, caption.trim(), flaggedNumber.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = caption.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
