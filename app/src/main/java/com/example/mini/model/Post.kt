// #member3
// Community social feed (report 3NF: Post, PostLike, Comment, PostReport).
package com.example.mini.model

data class Post(
    val postId: Int,
    val userId: Int,
    val content: String,
    val photoPath: String?,
    val postStatus: String = "Active", // Active or Removed (by moderation)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// composite key (postId, userId): a user can like a post once
data class PostLike(
    val postId: Int,
    val userId: Int,
    val likedAt: Long = System.currentTimeMillis()
)

// parentCommentId is null for a comment, or the comment being replied to
data class Comment(
    val commentId: Int,
    val postId: Int,
    val userId: Int,
    val parentCommentId: Int?,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class PostReport(
    val reportId: Int,
    val postId: Int,
    val reportedByUserId: Int,
    val reportReason: String,
    val reportStatus: String = "Pending", // Pending, Removed (post taken down) or Dismissed
    val reviewedByUserId: Int? = null,
    val reportedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)
