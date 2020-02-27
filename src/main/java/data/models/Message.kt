package data.models

data class Message(
    val id: Long,
    val userId: Long,
    val date: Long,
    val text: String,
    // true if token user id and userId here are the same
    var sentByCurrentUser: Boolean,
    var isRead: Boolean
)