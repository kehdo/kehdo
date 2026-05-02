package app.kehdo.feature.upload

import app.cash.turbine.test
import app.kehdo.core.common.KehdoError
import app.kehdo.core.common.Outcome
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.ConversationStatus
import app.kehdo.domain.conversation.CreateConversationUseCase
import app.kehdo.domain.conversation.GenerateRepliesUseCase
import app.kehdo.domain.conversation.GetTonesUseCase
import app.kehdo.domain.conversation.Mode
import app.kehdo.domain.conversation.Tone
import app.kehdo.domain.user.GetUsageUseCase
import app.kehdo.domain.user.UsageSnapshot
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UploadViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val getTones: GetTonesUseCase = mockk()
    private val createConversation: CreateConversationUseCase = mockk()
    private val generateReplies: GenerateRepliesUseCase = mockk()
    private val getUsage: GetUsageUseCase = mockk()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(): UploadViewModel = UploadViewModel(
        getTones = getTones,
        createConversation = createConversation,
        generateReplies = generateReplies,
        getUsage = getUsage
    )

    @Test
    fun `init loads tones and usage in parallel`() = runTest(dispatcher) {
        coEvery { getTones() } returns Outcome.success(listOf(WARM_TONE))
        coEvery { getUsage() } returns Outcome.success(STARTER_USAGE)

        val vm = viewModel()
        advanceUntilIdle()

        assertThat(vm.state.value.tones).hasSize(1)
        assertThat(vm.state.value.tones.first().code).isEqualTo("WARM")
        assertThat(vm.state.value.usage).isEqualTo(STARTER_USAGE)
        assertThat(vm.state.value.isLoadingTones).isFalse()
    }

    @Test
    fun `tones load failure surfaces error code in state without crashing`() =
        runTest(dispatcher) {
            coEvery { getTones() } returns Outcome.failure(KehdoError.Network())
            coEvery { getUsage() } returns Outcome.success(STARTER_USAGE)

            val vm = viewModel()
            advanceUntilIdle()

            assertThat(vm.state.value.error).isEqualTo("NETWORK_ERROR")
            assertThat(vm.state.value.tones).isEmpty()
        }

    @Test
    fun `usage failure does not block the upload flow`() = runTest(dispatcher) {
        // Quota lookup is best-effort — a 401 there shouldn't surface to
        // the user as a primary error.
        coEvery { getTones() } returns Outcome.success(emptyList())
        coEvery { getUsage() } returns Outcome.failure(KehdoError.Unauthorized())

        val vm = viewModel()
        advanceUntilIdle()

        assertThat(vm.state.value.usage).isNull()
        assertThat(vm.state.value.error).isNull()
    }

    @Test
    fun `tonesForSelectedMode filters tones by selected mode`() = runTest(dispatcher) {
        coEvery { getTones() } returns Outcome.success(
            listOf(WARM_TONE, CASUAL_TONE, FLIRTY_TONE)
        )
        coEvery { getUsage() } returns Outcome.success(STARTER_USAGE)

        val vm = viewModel()
        advanceUntilIdle()

        vm.onEvent(UploadEvent.ModeSelected(Mode.SINCERE))
        assertThat(vm.state.value.tonesForSelectedMode.map { it.code })
            .containsExactly("WARM")

        vm.onEvent(UploadEvent.ModeSelected(Mode.CASUAL))
        assertThat(vm.state.value.tonesForSelectedMode.map { it.code })
            .containsExactly("CASUAL")
    }

    @Test
    fun `canGenerate stays false until both screenshot and tone are picked`() =
        runTest(dispatcher) {
            coEvery { getTones() } returns Outcome.success(listOf(WARM_TONE))
            coEvery { getUsage() } returns Outcome.success(STARTER_USAGE)

            val vm = viewModel()
            advanceUntilIdle()

            assertThat(vm.state.value.canGenerate).isFalse()

            vm.onEvent(UploadEvent.ScreenshotPicked("content://media/0"))
            assertThat(vm.state.value.canGenerate).isFalse() // still no tone

            vm.onEvent(UploadEvent.ToneSelected("WARM"))
            assertThat(vm.state.value.canGenerate).isTrue()
        }

    @Test
    fun `generate happy path emits NavigateToReply effect with conversation id`() =
        runTest(dispatcher) {
            coEvery { getTones() } returns Outcome.success(listOf(WARM_TONE))
            coEvery { getUsage() } returns Outcome.success(STARTER_USAGE)
            coEvery { createConversation(any()) } returns Outcome.success(blankConversation("conv-99"))
            coEvery { generateReplies("conv-99", "WARM") } returns
                Outcome.success(blankConversation("conv-99"))

            val vm = viewModel()
            advanceUntilIdle()
            vm.onEvent(UploadEvent.ScreenshotPicked("content://media/0"))
            vm.onEvent(UploadEvent.ToneSelected("WARM"))

            vm.effects.test {
                vm.onEvent(UploadEvent.GenerateClicked)
                advanceUntilIdle()
                assertThat(awaitItem()).isEqualTo(UploadEffect.NavigateToReply("conv-99"))
            }

            assertThat(vm.state.value.isGenerating).isFalse()
            assertThat(vm.state.value.error).isNull()
        }

    @Test
    fun `generate failure during create surfaces error and clears generating flag`() =
        runTest(dispatcher) {
            coEvery { getTones() } returns Outcome.success(listOf(WARM_TONE))
            coEvery { getUsage() } returns Outcome.success(STARTER_USAGE)
            coEvery { createConversation(any()) } returns
                Outcome.failure(KehdoError.Network())

            val vm = viewModel()
            advanceUntilIdle()
            vm.onEvent(UploadEvent.ScreenshotPicked("content://media/0"))
            vm.onEvent(UploadEvent.ToneSelected("WARM"))
            vm.onEvent(UploadEvent.GenerateClicked)
            advanceUntilIdle()

            assertThat(vm.state.value.isGenerating).isFalse()
            assertThat(vm.state.value.error).isEqualTo("NETWORK_ERROR")
        }

    @Test
    fun `generate failure during reply generation propagates the rate-limit code`() =
        runTest(dispatcher) {
            coEvery { getTones() } returns Outcome.success(listOf(WARM_TONE))
            coEvery { getUsage() } returns Outcome.success(STARTER_USAGE)
            coEvery { createConversation(any()) } returns
                Outcome.success(blankConversation("conv-99"))
            coEvery { generateReplies("conv-99", "WARM") } returns
                Outcome.failure(KehdoError.RateLimit(limit = 5, resetAt = 0L))

            val vm = viewModel()
            advanceUntilIdle()
            vm.onEvent(UploadEvent.ScreenshotPicked("content://media/0"))
            vm.onEvent(UploadEvent.ToneSelected("WARM"))
            vm.onEvent(UploadEvent.GenerateClicked)
            advanceUntilIdle()

            assertThat(vm.state.value.error).isEqualTo("RATE_LIMIT_EXCEEDED")
        }

    private fun blankConversation(id: String) = Conversation(
        id = id,
        status = ConversationStatus.READY,
        failureReason = null,
        toneCode = "WARM",
        replies = emptyList(),
        createdAt = 0L,
        updatedAt = 0L
    )

    private companion object {
        val WARM_TONE = Tone(
            code = "WARM", name = "Warm", emoji = "🙏", description = null,
            mode = Mode.SINCERE, isPro = false, sortOrder = 1
        )
        val CASUAL_TONE = Tone(
            code = "CASUAL", name = "Casual", emoji = "💬", description = null,
            mode = Mode.CASUAL, isPro = false, sortOrder = 2
        )
        val FLIRTY_TONE = Tone(
            code = "FLIRTY", name = "Flirty", emoji = "😉", description = null,
            mode = Mode.FLIRTY, isPro = true, sortOrder = 3
        )
        val STARTER_USAGE = UsageSnapshot(dailyUsed = 1, dailyLimit = 5, resetAtMillis = 0L)
    }
}
