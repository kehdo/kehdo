package app.kehdo.core.network.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequestDto(
    val email: String,
    val password: String,
    val displayName: String? = null
)

@Serializable
data class SignInRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val plan: String,
    val createdAt: String
)
