package app.kehdo.data.auth.mapper

import app.kehdo.core.network.api.dto.AuthResponseDto
import app.kehdo.core.network.api.dto.UserDto
import app.kehdo.domain.auth.User

internal fun AuthResponseDto.toUser(): User = user.toDomain()

internal fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    plan = plan.toPlan(),
    quotaRemaining = 0,
    quotaResetAt = 0L
)

private fun String.toPlan(): User.Plan = when (uppercase()) {
    "STARTER", "FREE" -> User.Plan.FREE
    "PRO" -> User.Plan.PRO
    "UNLIMITED" -> User.Plan.UNLIMITED
    else -> User.Plan.FREE
}
