package michaelbukachi.battlestop

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

data class CountDownUi(
    val text: String = "0:00",
    val progress: Float = 0.6f,
    val paused: Boolean = false,
)

class MainViewModel(val dispatcher: CoroutineDispatcher = Dispatchers.Default) : ViewModel() {

    private var timeInSeconds = 0L
    private var current = AtomicLong(0L)
    private var paused = AtomicBoolean(false)
    private var running = AtomicBoolean(false)
    private var job: Job? = null

    val uiState = MutableStateFlow(CountDownUi("0:00", 1f))

    fun setAndStartTimer(text: String) {
        setTime(text)
        startTimer()
    }
    private fun setTime(text: String) {
        val chunks = text.padStart(6, '0').chunked(2)
        timeInSeconds = ((chunks[0].toInt() * 60 * 60) + (chunks[1].toInt() * 60) + chunks[2].toInt()).toLong()
    }

    private fun startTimer() {
        job?.cancel()
        current.set(timeInSeconds)
        job = viewModelScope.launch {
            countdown()
        }
    }

    private suspend fun countdown() = withContext(dispatcher) {
        updatePausedState(false)
        running.set(true)
        while (true) {
            if (!paused.get()) {
                if (current.decrementAndGet() < 0L) {
                    break
                }
                uiState.update {
                    it.copy(
                        text = DateUtils.formatElapsedTime(current.get()),
                        progress = 1f - (current.get() / timeInSeconds.toFloat()),
                    )
                }
            }
            delay(1_000)
        }

        running.set(false)
        updatePausedState(true)
    }

    fun pauseTimer() {
        if (!running.get()) {
            startTimer()
        } else {
            updatePausedState(!paused.get())
        }
    }

    private fun updatePausedState(value: Boolean) {
        paused.set(value)
        uiState.update {
            it.copy(
                paused = paused.get(),
            )
        }
    }

    fun reset() {
        current.set(timeInSeconds)
        uiState.update {
            it.copy(
                text = DateUtils.formatElapsedTime(current.get()),
                progress = 1f - (current.get() / timeInSeconds.toFloat()),
            )
        }
    }
}
