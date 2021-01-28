/*
 * Copyright (c) 2018-2021 AnimatedLEDStrip
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package animatedledstrip.test.client

import kotlinx.coroutines.runBlocking

actual fun runInCoroutine(block: suspend () -> Unit) {
    runBlocking {
        block()
    }
}

//
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.sync.Mutex
//import org.pmw.tinylog.Configuration
//import org.pmw.tinylog.Configurator
//import org.pmw.tinylog.Level
//import org.pmw.tinylog.LogEntry
//import org.pmw.tinylog.writers.LogEntryValue
//import org.pmw.tinylog.writers.Writer
//import kotlin.test.assertTrue
//
///* Log Testing */
//
//object TestLogWriter : Writer {
//    private val logs = mutableSetOf<LogEntry>()
//    private val logMutex = Mutex()
//
//    fun clearLogs() = logs.clear()
//
//    override fun getRequiredLogEntryValues(): MutableSet<LogEntryValue> =
//        mutableSetOf(LogEntryValue.LEVEL, LogEntryValue.MESSAGE)
//
//    override fun write(log: LogEntry) {
//        if (logMutex.isLocked) return
//        logs.add(log)
//    }
//
//    override fun init(p0: Configuration?) {}
//
//    override fun flush() {}
//
//    override fun close() {}
//
//    fun assertLogs(expectedLogs: Set<Pair<Level, String>>) {
//        runBlocking {
//            logMutex.lock()
//            val actualLogs = logs.map { Pair(it.level, it.message) }.toSet()
//            logMutex.unlock()
//
//            assertTrue(
//                "logs do not match:\nexpected: $expectedLogs\nactual: $actualLogs\n" +
//                "extra values in expected: ${expectedLogs.minus(actualLogs)}"
//            ) {
//                actualLogs.containsAll(expectedLogs)
//            }
//            assertTrue(
//                "logs do not match:\nexpected: $expectedLogs\nactual: $actualLogs\n" +
//                "extra values in actual: ${actualLogs.minus(expectedLogs)}"
//            ) {
//                expectedLogs.containsAll(actualLogs)
//            }
//        }
//    }
//
//
//    fun assertLogsInclude(expectedLogs: Set<Pair<Level, String>>) {
//        runBlocking {
//            logMutex.lock()
//            val actualLogs = logs.map { Pair(it.level, it.message) }.toSet()
//            logMutex.unlock()
//
//            assertTrue(
//                "logs do not match:\nexpected: $expectedLogs\nactual: $actualLogs\n" +
//                "extra values in expected: ${expectedLogs.minus(actualLogs)}"
//            ) {
//                actualLogs.containsAll(expectedLogs)
//            }
//        }
//    }
//}
//
//fun startLogCapture() {
//    Configurator.currentConfig().addWriter(TestLogWriter, Level.DEBUG).activate()
//    clearLogs()
//}
//
//fun stopLogCapture() {
//    Configurator.currentConfig().removeWriter(TestLogWriter).activate()
//}
//
//fun assertLogs(expectedLogs: Set<Pair<Level, String>>) = TestLogWriter.assertLogs(expectedLogs)
//
//fun assertLogsInclude(expectedLogs: Set<Pair<Level, String>>) = TestLogWriter.assertLogsInclude(expectedLogs)
//
//fun clearLogs() = TestLogWriter.clearLogs()
