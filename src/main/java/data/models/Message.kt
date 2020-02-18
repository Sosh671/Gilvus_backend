package data.models

data class Message(
    val id: Long,
    val userId: Long,
    val date: Long,
    val text: String
)