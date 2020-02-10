package db.models

data class User(
    val id: Long?,
    val name: String,
    val phone: Int,
    val password: String?,
    val avatarUrl: String?
)