package app.kehdo.domain.user

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Fetch the daily quota snapshot for the authenticated user.
 * UI calls this on UploadScreen mount to render the "X of Y left" footer.
 */
class GetUsageUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Outcome<UsageSnapshot> = repository.getUsage()
}
