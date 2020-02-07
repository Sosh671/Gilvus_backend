package db.models

data class User(
    val id: Long?,
    val name: String,
    val phone: String,
    val avatarUrl: String?
)