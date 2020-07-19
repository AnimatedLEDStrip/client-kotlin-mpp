package animatedledstrip.test

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.pmw.tinylog.Configuration
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Level
import org.pmw.tinylog.LogEntry
import org.pmw.tinylog.writers.LogEntryValue
import org.pmw.tinylog.writers.Writer
import kotlin.test.assertTrue

/* Log Testing */

object TestLogWriter : Writer {
    private val logs = mutableSetOf<LogEntry>()
    private val logMutex = Mutex()

    fun clearLogs() = logs.clear()

    override fun getRequiredLogEntryValues(): MutableSet<LogEntryValue> =
        mutableSetOf(LogEntryValue.LEVEL, LogEntryValue.MESSAGE)

    override fun write(log: LogEntry) {
        if (logMutex.isLocked) return
        logs.add(log)
    }

    override fun init(p0: Configuration?) {}

    override fun flush() {}

    override fun close() {}

    fun assertLogs(expectedLogs: Set<Pair<Level, String>>) {
        runBlocking {
            logMutex.lock()
            val actualLogs = logs.map { Pair(it.level, it.message) }.toSet()
            logMutex.unlock()

            assertTrue(
                "logs do not match:\nexpected: $expectedLogs\nactual: $actualLogs\n" +
                        "extra values in expected: ${expectedLogs.minus(actualLogs)}"
            ) {
                actualLogs.containsAll(expectedLogs)
            }
            assertTrue(
                "logs do not match:\nexpected: $expectedLogs\nactual: $actualLogs\n" +
                        "extra values in actual: ${actualLogs.minus(expectedLogs)}"
            ) {
                expectedLogs.containsAll(actualLogs)
            }
        }
    }


    fun assertLogsInclude(expectedLogs: Set<Pair<Level, String>>) {
        runBlocking {
            logMutex.lock()
            val actualLogs = logs.map { Pair(it.level, it.message) }.toSet()
            logMutex.unlock()

            assertTrue(
                "logs do not match:\nexpected: $expectedLogs\nactual: $actualLogs\n" +
                        "extra values in expected: ${expectedLogs.minus(actualLogs)}"
            ) {
                actualLogs.containsAll(expectedLogs)
            }
        }
    }
}

fun startLogCapture() {
    Configurator.currentConfig().addWriter(TestLogWriter, Level.DEBUG).activate()
    clearLogs()
}

fun stopLogCapture() {
    Configurator.currentConfig().removeWriter(TestLogWriter).activate()
}

fun assertLogs(expectedLogs: Set<Pair<Level, String>>) = TestLogWriter.assertLogs(expectedLogs)

fun assertLogsInclude(expectedLogs: Set<Pair<Level, String>>) = TestLogWriter.assertLogsInclude(expectedLogs)

fun clearLogs() = TestLogWriter.clearLogs()
