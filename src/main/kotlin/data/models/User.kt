package data.models

data class User(
    val id: Long?,
    val name: String,
    val phone: String,
    val password: String?,
    val avatarUrl: String?
)