/*
 *  Copyright (c) 2018-2020 AnimatedLEDStrip
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package animatedledstrip.test

import animatedledstrip.animationutils.AnimationData
import animatedledstrip.animationutils.EndAnimation
import animatedledstrip.animationutils.animation
import animatedledstrip.animationutils.predefined.color
import animatedledstrip.client.AnimationSender
import animatedledstrip.client.send
import animatedledstrip.leds.StripInfo
import animatedledstrip.leds.emulated.EmulatedAnimatedLEDStrip
import animatedledstrip.utils.delayBlocking
import kotlinx.coroutines.*
import org.junit.Test
import org.pmw.tinylog.Level
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationSenderTest {

    @Test
    fun testDefaultSender() {
        val port = 1100

        val testSender = AnimationSender("0.0.0.0", port).setAsDefaultSender()
        assertTrue { AnimationSender.defaultSender === testSender }
    }

    @Test
    fun testStart() {
        val port = 1101

        val job = GlobalScope.launch(Dispatchers.IO) {
            ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
        }

        delayBlocking(2000)

        startLogCapture()

        AnimationSender("0.0.0.0", port).start()

        runBlocking { job.join() }

        delayBlocking(500)

        assertLogsInclude(setOf(Pair(Level.INFO, "Connected to server at 0.0.0.0:$port")))

        stopLogCapture()
    }

    @Test
    fun testConnectCallback() {
        var testBoolean = false
        val port = 1102

        GlobalScope.launch(Dispatchers.IO) {
            ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setOnConnectCallback { _, _ ->
                testBoolean = true
            }
            .start()

        delayBlocking(2000)

        assertTrue { testBoolean }
    }

    @Test
    fun testDisconnectCallback() {
        var testBoolean = false
        val port = 1103

        val job = GlobalScope.launch(Dispatchers.IO) {
            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
            delay(3000)
            socket.close()
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setAsDefaultSender()
            .setOnDisconnectCallback { _, _ ->
                testBoolean = true
                AnimationSender.defaultSender.end()
            }
            .start()

        runBlocking { job.join() }
        delayBlocking(1000)
        assertTrue { testBoolean }
    }

    @Test
    fun testUnableToConnectCallback() {
        var testBoolean1 = false
        var testBoolean2 = false
        val port = 1104

        AnimationSender("0.0.0.0", port)
            .setAsDefaultSender()
            .setOnUnableToConnectCallback { _, _ ->
                testBoolean1 = true
            }
            .setOnConnectCallback { _, _ ->
                testBoolean2 = true
            }
            .start()

        delayBlocking(2000)
        assertTrue { testBoolean1 }
        assertFalse { testBoolean2 }
    }

    @Test
    fun testReceiveCallback() {
        var testBoolean = false
        val port = 1105

        GlobalScope.launch(Dispatchers.IO) {
            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
            val out = socket.getOutputStream()
            out.write(AnimationData().animation("Color").json())
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setOnReceiveCallback {
                testBoolean = true
            }
            .start()

        delayBlocking(2000)

        assertTrue { testBoolean }
    }

    @Test
    fun testNewAnimationDataCallback() {
        var testBoolean1 = false
        val port = 1106

        GlobalScope.launch(Dispatchers.IO) {
            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
            val out = socket.getOutputStream()
            out.write(AnimationData().animation("Color").json())
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setOnNewAnimationDataCallback {
                testBoolean1 = true
            }
            .start()

        delayBlocking(2000)

        assertTrue { testBoolean1 }
    }

    @Test
    fun testNewAnimationInfoCallback() {
        var testBoolean1 = false
        val port = 1107

        GlobalScope.launch(Dispatchers.IO) {
            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
            val out = socket.getOutputStream()
            out.write(color.info.json())
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setOnNewAnimationInfoCallback {
                testBoolean1 = true
                assertTrue { it == color.info }
            }
            .start()

        delayBlocking(2000)

        assertTrue { testBoolean1 }
    }

    @Test
    fun testEndAnimationCallback() {
        var testBoolean1 = false
        val port = 1108

        GlobalScope.launch(Dispatchers.IO) {
            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
            val out = socket.getOutputStream()
            out.write(EndAnimation("TEST").json())
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setOnNewEndAnimationCallback {
                testBoolean1 = true
            }
            .start()

        delayBlocking(2000)

        assertTrue { testBoolean1 }
    }

    @Test
    fun testNewSectionCallback() {
        var testBoolean1 = false
        val port = 1109

        GlobalScope.launch(Dispatchers.IO) {
            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
            val out = socket.getOutputStream()
            out.write(EmulatedAnimatedLEDStrip(10).wholeStrip.json())
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setOnNewSectionCallback {
                testBoolean1 = true
            }
            .start()

        delayBlocking(4000)

        assertTrue { testBoolean1 }
    }

    @Test
    fun testNewStripInfoCallback() {
        var testBoolean1 = false
        val port = 1110

        GlobalScope.launch(Dispatchers.IO) {
            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
            val out = socket.getOutputStream()
            out.write(StripInfo().json())
        }

        delayBlocking(2000)

        AnimationSender("0.0.0.0", port)
            .setOnNewStripInfoCallback {
                testBoolean1 = true
            }
            .start()

        delayBlocking(2000)

        assertTrue { testBoolean1 }
    }

    @Test
    fun testMultipleStarts() {
        val port = 1111

        startLogCapture()

        val testSender = AnimationSender("0.0.0.0", port).start()
        testSender.start()

        assertLogsInclude(setOf(Pair(Level.WARNING, "Sender started already")))

        stopLogCapture()
    }

    @Test
    fun testSendToNullOutput() {
        val testAnimation = AnimationData()
        val sender = AnimationSender("0.0.0.0", 0)

        startLogCapture()

        testAnimation.send(sender)
        delayBlocking(2000)

        assertLogsInclude(setOf(Pair(Level.WARNING, "Output stream null")))

        stopLogCapture()
    }

    @Test
    fun testSetIPAddress() {
        val port = 1112

        GlobalScope.launch(Dispatchers.IO) {
            val s = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"))
            s.accept()
            s.accept()
        }

        val sender = AnimationSender("0.0.0.0", port)

        assertTrue { sender.ipAddress == "0.0.0.0" }

        sender.setIPAddress("1.1.1.1", start = null)
        delayBlocking(100)
        assertTrue { sender.ipAddress == "1.1.1.1" }

        sender.setIPAddress("0.0.0.0", start = true)
        delayBlocking(1000)
        assertTrue { sender.ipAddress == "0.0.0.0" }
        assertTrue { sender.started }

        sender.setIPAddress("0.0.0.0", start = null)
        delayBlocking(3000)
        assertTrue { sender.ipAddress == "0.0.0.0" }
        assertTrue { sender.started }

        sender.setIPAddress("1.1.1.1", start = false)
        delayBlocking(100)
        assertTrue { sender.ipAddress == "1.1.1.1" }
        assertFalse { sender.started }
    }

    @Test
    fun testSetPort() {
        val port1 = 1113
        val port2 = 1114
        val port3 = 1115
        val port4 = 1116
        val port5 = 1117

        GlobalScope.launch(Dispatchers.IO) {
            ServerSocket(port3, 0, InetAddress.getByName("0.0.0.0"))
            ServerSocket(port4, 0, InetAddress.getByName("0.0.0.0"))
        }

        val sender = AnimationSender("0.0.0.0", port1)

        assertTrue { sender.port == port1 }

        sender.setPort(port2, start = null)
        delayBlocking(100)
        assertTrue { sender.port == port2 }

        sender.setPort(port3, start = true)
        delayBlocking(1000)
        assertTrue { sender.port == port3 }
        assertTrue { sender.started }

        sender.setPort(port4, start = null)
        delayBlocking(3000)
        assertTrue { sender.port == port4 }
        assertTrue { sender.started }

        sender.setPort(port5, start = false)
        delayBlocking(100)
        assertTrue { sender.port == port5 }
        assertFalse { sender.started }
    }
}
