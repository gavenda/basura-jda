package basura.concurrent

import java.util.concurrent.ThreadFactory

class NamedThreadFactory(
    private val name: String
) : ThreadFactory {
    private var count = 1
    override fun newThread(r: Runnable): Thread {
        return Thread(r, "$name-${count++}")
    }
}
