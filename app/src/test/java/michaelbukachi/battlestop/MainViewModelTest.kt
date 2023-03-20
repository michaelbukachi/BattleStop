package michaelbukachi.battlestop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class MainViewModelTest {

    @Test
    fun `test timer count down works properly`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val viewModel = MainViewModel(testDispatcher)
            launch { viewModel.setAndStartTimer("10") }
            advanceTimeBy(2_000)
            assertThat(viewModel.uiState.value.text, `is`("00:08"))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `test timer pause and resume works properly`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val viewModel = MainViewModel(testDispatcher)
            launch { viewModel.setAndStartTimer("10") }
            advanceTimeBy(2_000)
            viewModel.pauseTimer()
            advanceTimeBy(2_000)
            viewModel.pauseTimer()
            advanceTimeBy(2_000)
            assertThat(viewModel.uiState.value.text, `is`("00:06"))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `test timer reset works properly`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val viewModel = MainViewModel(testDispatcher)
            launch { viewModel.setAndStartTimer("10") }
            advanceTimeBy(2_000)
            viewModel.reset()
            assertThat(viewModel.uiState.value.text, `is`("00:10"))
        } finally {
            Dispatchers.resetMain()
        }
    }
}
