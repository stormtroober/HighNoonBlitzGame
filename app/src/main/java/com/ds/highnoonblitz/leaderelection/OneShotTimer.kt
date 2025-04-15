import java.util.Timer
import java.util.TimerTask

class OneShotTimer(private val delay: Long, private val task: () -> Unit) {
    private var timer: Timer? = null

    fun start() {
        if (timer != null) return

        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    task()
                    timer = null
                }
            }, delay)
        }
    }

    fun isRunning(): Boolean = timer != null

    fun stop() {
        timer?.cancel()
        timer = null
    }
}