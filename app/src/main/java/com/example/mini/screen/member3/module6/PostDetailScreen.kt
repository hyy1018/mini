// #member3
// One post with likes, comments and replies. The author can delete it; anyone else can report it.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.Comment
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState

@Composable
fun PostDetailScreen(postId: Int, navController: NavHostController) {
    val post = CommunityData.getPost(postId)
    val userId = UserSession.currentUserId ?: return
    val isAuthor = post?.userId == userId

    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf(CommunityData.reportReasons.first()) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            BackTopBar("Post", navController) {
                if (post != null && post.postStatus == "Active") {
                    if (isAuthor) {
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete post") }
                    } else {
                        IconButton(onClick = { showReportDialog = true }) { Icon(Icons.Filled.Flag, contentDescription = "Report post") }
                    }
                }
            }
        },
        bottomBar = {
            if (post != null && post.postStatus == "Active") {
                // comment box sits above the keyboard
                Surface(tonalElevation = 3.dp, modifier = Modifier.imePadding()) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        replyingTo?.let { comment ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Replying to ${ResidentData.getUser(comment.userId)?.name ?: "comment"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { replyingTo = null }) { Icon(Icons.Filled.Close, contentDescription = "Cancel reply") }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it.take(500) },
                                placeholder = { Text(if (replyingTo != null) "Write a reply..." else "Write a comment...") },
                                maxLines = 3,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    // a reply to a reply attaches to the top comment, keeping threads one level deep
                                    val parent = replyingTo?.let { it.parentCommentId ?: it.commentId }
                                    CommunityData.addComment(postId, userId, commentText.trim(), parent)
                                    commentText = ""
                                    replyingTo = null
                                },
                                enabled = commentText.isNotBlank()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (post == null || (post.postStatus != "Active" && !UserSession.isStaff)) {
            EmptyState("This post is no longer available", Modifier.padding(innerPadding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { PostCard(post, userId, onClick = null) }
            message?.let { text ->
                item { Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            }
            item {
                Text(
                    "Comments (${CommunityData.commentCount(postId)})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(CommunityData.topComments(postId), key = { it.commentId }) { comment ->
                Column {
                    CommentItem(comment, onReply = { replyingTo = comment })
                    CommunityData.replies(comment.commentId).forEach { reply ->
                        // replies are indented under their comment
                        Row {
                            Spacer(Modifier.width(32.dp))
                            CommentItem(reply, onReply = { replyingTo = reply })
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete post?") },
            text = { Text("The post and all its comments will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    CommunityData.deletePost(postId)
                    navController.safePopBackStack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report post") },
            text = {
                Column {
                    Text("Why are you reporting this post?")
                    CommunityData.reportReasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = reason == reportReason, onClick = { reportReason = reason }, role = Role.RadioButton),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = reason == reportReason, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(reason)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    message = CommunityData.reportPost(postId, userId, reportReason) ?: "Thanks -- management will review this post"
                }) { Text("Report") }
            },
            dismissButton = { TextButton(onClick = { showReportDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CommentItem(comment: Comment, onReply: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        PostAuthorRow(comment.userId, comment.createdAt)
        Text(comment.content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 46.dp))
        TextButton(onClick = onReply, modifier = Modifier.padding(start = 38.dp)) {
            Text("Reply", style = MaterialTheme.typography.labelMedium)
        }
    }
}
