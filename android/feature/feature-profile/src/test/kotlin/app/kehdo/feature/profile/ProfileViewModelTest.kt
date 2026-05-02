package app.kehdo.feature.profile

import app.cash.turbine.test
import app.kehdo.core.common.KehdoError
import app.kehdo.core.common.Outcome
import app.kehdo.domain.auth.ObserveCurrentUserUseCase
import app.kehdo.domain.auth.RefreshCurrentUserUseCase
import app.kehdo.domain.auth.SignOutUseCase
import app.kehdo.domain.auth.User
import app.kehdo.domain.user.GetUsageUseCase
import app.kehdo.domain.user.UsageSnapshot
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val observeCurrentUser: ObserveCurrentUserUseCase = mockk()
    private val refreshCurrentUser: RefreshCurrentUserUseCase = mockk(relaxed = true)
    private val getUsage: GetUsageUseCase = mockk()
    private val signOut: SignOutUseCase = mockk(relaxed = true)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ProfileViewModel(
        observeCurrentUser = observeCurrentUser,
        refreshCurrentUser = refreshCurrentUser,
        getUsage = getUsage,
        signOut = signOut
    )

    @Test
    fun `init refreshes the current user and loads usage`() = runTest(dispatcher) {
        every { observeCurrentUser() } returns flowOf(USER_PRO)
        coEvery { getUsage() } returns Outcome.success(USAGE_PRO)

        val vm = viewModel()
        vm.state.test {
            advanceUntilIdle()
            // The state stream is `WhileSubscribed(5_000)` — drain until we
            // see the populated value.
            var last = awaitItem()
            while (last.user == null || last.usage == null) {
                last = awaitItem()
            }
            assertThat(last.user).isEqualTo(USER_PRO)
            assertThat(last.usage).isEqualTo(USAGE_PRO)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { refreshCurrentUser() }
    }

    @Test
    fun `usage failure leaves state intact and does not crash`() = runTest(dispatcher) {
        // Best-effort load — a 401 shouldn't blank the screen.
        every { observeCurrentUser() } returns flowOf(USER_PRO)
        coEvery { getUsage() } returns Outcome.failure(KehdoError.Unauthorized())

        val vm = viewModel()
        vm.state.test {
            advanceUntilIdle()
            // Drain until user populates; usage stays null forever.
            var last = awaitItem()
            while (last.user == null) {
                last = awaitItem()
            }
            assertThat(last.user).isEqualTo(USER_PRO)
            assertThat(last.usage).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state combines observed user with refreshed usage on RefreshUsage event`() =
        runTest(dispatcher) {
            every { observeCurrentUser() } returns flowOf(USER_FREE)
            // First call from init returns "stale" usage; explicit refresh
            // returns updated counts.
            coEvery { getUsage() } returnsMany listOf(
                Outcome.success(UsageSnapshot(0, 5, RESET_AT)),
                Outcome.success(UsageSnapshot(3, 5, RESET_AT))
            )

            val vm = viewModel()
            vm.state.test {
                advanceUntilIdle()
                // Drain the emitted states until we see the post-init value.
                var last = awaitItem()
                while (last.user == null || last.usage == null) {
                    last = awaitItem()
                }
                assertThat(last.usage?.dailyUsed).isEqualTo(0)

                vm.onEvent(ProfileEvent.RefreshUsage)
                advanceUntilIdle()
                val refreshed = awaitItem()
                assertThat(refreshed.usage?.dailyUsed).isEqualTo(3)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `SignOutClicked event invokes signOut use case`() = runTest(dispatcher) {
        every { observeCurrentUser() } returns flowOf(USER_PRO)
        coEvery { getUsage() } returns Outcome.success(USAGE_PRO)

        val vm = viewModel()
        advanceUntilIdle()

        vm.onEvent(ProfileEvent.SignOutClicked)
        advanceUntilIdle()

        coVerify(exactly = 1) { signOut() }
    }

    private companion object {
        const val RESET_AT = 1_700_000_000_000L
        val USER_PRO = User(
            id = "u1",
            email = "alex@example.com",
            displayName = "Alex",
            plan = User.Plan.PRO,
            quotaRemaining = 0,
            quotaResetAt = 0L
        )
        val USER_FREE = USER_PRO.copy(plan = User.Plan.FREE)
        val USAGE_PRO = UsageSnapshot(dailyUsed = 7, dailyLimit = 100, resetAtMillis = RESET_AT)
    }
}
