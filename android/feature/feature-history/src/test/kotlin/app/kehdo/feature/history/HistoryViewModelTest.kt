package app.kehdo.feature.history

import app.cash.turbine.test
import app.kehdo.core.common.KehdoError
import app.kehdo.core.common.Outcome
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.ConversationStatus
import app.kehdo.domain.conversation.DeleteConversationUseCase
import app.kehdo.domain.conversation.GetHistoryPageUseCase
import app.kehdo.domain.conversation.HistoryPage
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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
class HistoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val getHistoryPage: GetHistoryPageUseCase = mockk()
    private val deleteConversation: DeleteConversationUseCase = mockk()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = HistoryViewModel(
        getHistoryPage = getHistoryPage,
        deleteConversation = deleteConversation
    )

    @Test
    fun `init loads first page and stores returned cursor`() = runTest(dispatcher) {
        coEvery { getHistoryPage(20, null) } returns Outcome.success(
            HistoryPage(items = listOf(conv("a"), conv("b")), nextCursor = "cursor-1")
        )

        val vm = viewModel()
        advanceUntilIdle()

        assertThat(vm.state.value.items.map { it.id }).containsExactly("a", "b").inOrder()
        assertThat(vm.state.value.nextCursor).isEqualTo("cursor-1")
        assertThat(vm.state.value.canLoadMore).isTrue()
        assertThat(vm.state.value.isRefreshing).isFalse()
    }

    @Test
    fun `init failure surfaces error code without crashing`() = runTest(dispatcher) {
        coEvery { getHistoryPage(20, null) } returns Outcome.failure(KehdoError.Network())

        val vm = viewModel()
        advanceUntilIdle()

        assertThat(vm.state.value.error).isEqualTo("NETWORK_ERROR")
        assertThat(vm.state.value.items).isEmpty()
    }

    @Test
    fun `LoadMoreClicked appends next page and updates cursor`() = runTest(dispatcher) {
        coEvery { getHistoryPage(20, null) } returns Outcome.success(
            HistoryPage(items = listOf(conv("a")), nextCursor = "cursor-1")
        )
        coEvery { getHistoryPage(20, "cursor-1") } returns Outcome.success(
            HistoryPage(items = listOf(conv("b"), conv("c")), nextCursor = null)
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.onEvent(HistoryEvent.LoadMoreClicked)
        advanceUntilIdle()

        assertThat(vm.state.value.items.map { it.id })
            .containsExactly("a", "b", "c").inOrder()
        // No more pages.
        assertThat(vm.state.value.nextCursor).isNull()
        assertThat(vm.state.value.canLoadMore).isFalse()
    }

    @Test
    fun `LoadMoreClicked is a no-op when cursor is null`() = runTest(dispatcher) {
        coEvery { getHistoryPage(20, null) } returns Outcome.success(
            HistoryPage(items = listOf(conv("a")), nextCursor = null)
        )

        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(HistoryEvent.LoadMoreClicked)
        advanceUntilIdle()

        // The use case must NOT have been called a second time.
        coVerify(exactly = 1) { getHistoryPage(any(), any()) }
    }

    @Test
    fun `DeleteClicked optimistically removes the row and emits Deleted effect`() =
        runTest(dispatcher) {
            coEvery { getHistoryPage(20, null) } returns Outcome.success(
                HistoryPage(items = listOf(conv("a"), conv("b")), nextCursor = null)
            )
            coEvery { deleteConversation("a") } returns Outcome.success(Unit)

            val vm = viewModel()
            advanceUntilIdle()

            vm.effects.test {
                vm.onEvent(HistoryEvent.DeleteClicked("a"))
                // Optimistic update fires before the suspended call resolves.
                assertThat(vm.state.value.items.map { it.id }).containsExactly("b")
                advanceUntilIdle()
                assertThat(awaitItem()).isEqualTo(HistoryEffect.Deleted("a"))
            }
        }

    @Test
    fun `delete failure rolls the row back into state and surfaces error`() =
        runTest(dispatcher) {
            coEvery { getHistoryPage(20, null) } returns Outcome.success(
                HistoryPage(items = listOf(conv("a"), conv("b")), nextCursor = null)
            )
            coEvery { deleteConversation("a") } returns Outcome.failure(KehdoError.Network())

            val vm = viewModel()
            advanceUntilIdle()

            vm.onEvent(HistoryEvent.DeleteClicked("a"))
            advanceUntilIdle()

            // Optimistic remove should have rolled back.
            assertThat(vm.state.value.items.map { it.id }).containsExactly("a", "b")
            assertThat(vm.state.value.error).isEqualTo("NETWORK_ERROR")
        }

    @Test
    fun `RefreshClicked retries the first page`() = runTest(dispatcher) {
        coEvery { getHistoryPage(20, null) } returnsMany listOf(
            Outcome.failure(KehdoError.Network()),
            Outcome.success(HistoryPage(items = listOf(conv("a")), nextCursor = null))
        )

        val vm = viewModel()
        advanceUntilIdle()
        assertThat(vm.state.value.error).isEqualTo("NETWORK_ERROR")

        vm.onEvent(HistoryEvent.RefreshClicked)
        advanceUntilIdle()

        assertThat(vm.state.value.error).isNull()
        assertThat(vm.state.value.items).hasSize(1)
    }

    private fun conv(id: String): Conversation = Conversation(
        id = id,
        status = ConversationStatus.READY,
        failureReason = null,
        toneCode = "WARM",
        replies = emptyList(),
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_700_000_000_000L
    )
}
