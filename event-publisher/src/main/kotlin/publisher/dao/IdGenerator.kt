package publisher.dao

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IdGenerator {
    @Volatile
    var lastMillis = 0L

    @Volatile
    var subseconds = 0

    val mutex = Mutex()

    suspend fun generate(): Long =
        mutex.withLock {
            val now = System.currentTimeMillis()

            if (now > lastMillis) {
                lastMillis = now
                subseconds = 0
            } else {
                subseconds += 1
            }

            (now shl 10) + subseconds
        }
}
