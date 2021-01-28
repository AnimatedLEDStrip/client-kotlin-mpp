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

//import animatedledstrip.animations.predefined.color
//import animatedledstrip.client.ALSClient
//import animatedledstrip.client.send
//import animatedledstrip.communication.Message
//import animatedledstrip.leds.animationmanagement.AnimationToRunParams
//import animatedledstrip.leds.animationmanagement.EndAnimation
//import animatedledstrip.leds.animationmanagement.RunningAnimationParams
//import animatedledstrip.leds.stripmanagement.StripInfo
//import kotlinx.coroutines.*
//import org.junit.Test
//import java.net.InetAddress
//import java.net.ServerSocket
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//class ALSClientTest {
//
//    @Test
//    fun testDefaultSender() {
//        val port = 1100
//
//        val testSender = ALSClient("0.0.0.0", port).setAsDefaultClient()
//        assertTrue { ALSClient.defaultClient === testSender }
//    }
//
//    @Test
//    fun testStart() {
//        val port = 1101
//        var testBoolean = false
//
//        val job = GlobalScope.launch(Dispatchers.IO) {
//            ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            testBoolean = true
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port).start()
//
//        runBlocking { job.join() }
//
//        assertTrue { testBoolean }
//    }
//
//    @Test
//    fun testConnectCallback() {
//        var testBoolean = false
//        val port = 1102
//
//        GlobalScope.launch(Dispatchers.IO) {
//            ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port)
//            .setOnConnectCallback { _, _ ->
//                testBoolean = true
//                Unit
//            }
//            .start()
//
//        Thread.sleep(2000)
//
//        assertTrue { testBoolean }
//    }
//
//    @Test
//    fun testDisconnectCallback() {
//        var testBoolean = false
//        val port = 1103
//
//        val job = GlobalScope.launch(Dispatchers.IO) {
//            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            delay(3000)
//            socket.close()
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port)
//            .setAsDefaultClient()
//            .setOnDisconnectCallback { _, _ ->
//                testBoolean = true
//                ALSClient.defaultClient.end()
//            }
//            .start()
//
//        runBlocking { job.join() }
//        Thread.sleep(1000)
//        assertTrue { testBoolean }
//    }
//
//    @Test
//    fun testUnableToConnectCallback() {
//        var testBoolean1 = false
//        var testBoolean2 = false
//        val port = 1104
//
//        ALSClient("0.0.0.0", port)
//            .setAsDefaultClient()
//            .setOnUnableToConnectCallback { _, _ ->
//                testBoolean1 = true
//                Unit
//            }
//            .setOnConnectCallback { _, _ ->
//                testBoolean2 = true
//                Unit
//            }
//            .start()
//
//        Thread.sleep(2000)
//        assertTrue { testBoolean1 }
//        assertFalse { testBoolean2 }
//    }
//
//    @Test
//    fun testReceiveCallback() {
//        var testBoolean = false
//        val port = 1105
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            val out = socket.getOutputStream()
//            out.write(Message("Test").json())
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port)
//            .setOnReceiveCallback {
//                testBoolean = true
//            }
//            .start()
//
//        Thread.sleep(2000)
//
//        assertTrue { testBoolean }
//    }
//
//    @Test
//    fun testNewRunningAnimationParamsCallback() {
//        var testBoolean1 = false
//        val port = 1106
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            val out = socket.getOutputStream()
//            out.write(RunningAnimationParams(color, "", listOf(), "", "", -1,
//                                             mapOf(), mapOf(), mapOf(), mapOf(), mapOf(),
//                                             mapOf(), mapOf(), AnimationToRunParams()).json())
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port)
//            .setOnNewRunningAnimationParamsCallback {
//                testBoolean1 = true
//            }
//            .start()
//
//        Thread.sleep(2000)
//
//        assertTrue { testBoolean1 }
//    }
//
//    @Test
//    fun testNewAnimationInfoCallback() {
//        var testBoolean1 = false
//        val port = 1107
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            val out = socket.getOutputStream()
//            out.write(color.info.json())
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port)
//            .setOnNewAnimationInfoCallback {
//                testBoolean1 = true
//                assertTrue { it == color.info }
//            }
//            .start()
//
//        Thread.sleep(2000)
//
//        assertTrue { testBoolean1 }
//    }
//
//    @Test
//    fun testEndAnimationCallback() {
//        var testBoolean1 = false
//        val port = 1108
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            val out = socket.getOutputStream()
//            out.write(EndAnimation("TEST").json())
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port)
//            .setOnNewEndAnimationCallback {
//                testBoolean1 = true
//            }
//            .start()
//
//        Thread.sleep(2000)
//
//        assertTrue { testBoolean1 }
//    }
//
////    @Test
////    fun testNewSectionCallback() {
////        var testBoolean1 = false
////        val port = 1109
////
////        GlobalScope.launch(Dispatchers.IO) {
////            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
////            val out = socket.getOutputStream()
////            out.write(.json())
////        }
////
////        Thread.sleep(2000)
////
////        _root_ide_package_.animatedledstrip.client.ALSClient("0.0.0.0", port)
////            .setOnNewSectionCallback {
////                testBoolean1 = true
////            }
////            .start()
////
////        Thread.sleep(4000)
////
////        assertTrue { testBoolean1 }
////    }
//
//    @Test
//    fun testNewStripInfoCallback() {
//        var testBoolean1 = false
//        val port = 1110
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            val out = socket.getOutputStream()
//            out.write(StripInfo().json())
//        }
//
//        Thread.sleep(2000)
//
//        ALSClient("0.0.0.0", port)
//            .setOnNewStripInfoCallback {
//                testBoolean1 = true
//            }
//            .start()
//
//        Thread.sleep(2000)
//
//        assertTrue { testBoolean1 }
//    }
//
//    @Test
//    fun testMultipleStarts() {
//        val port = 1111
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val s = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"))
//            s.accept()
//        }
//
//        Thread.sleep(500)
//
//        val testSender = ALSClient("0.0.0.0", port).start()
//
//        Thread.sleep(2000)
//
//        assertTrue { testSender.started }
//
//        testSender.start()
//
//        assertTrue { testSender.started }
//    }
//
//    @Test
//    fun testSendToNullOutput() {
//        val testMessage = Message()
//        val sender = ALSClient("0.0.0.0", 0)
//
////        startLogCapture()
//
//        testMessage.send(sender)
//        Thread.sleep(2000)
//
////        assertLogsInclude(setOf(Pair(Level.WARNING, "Output stream null")))
//
////        stopLogCapture()
//    }
//
//    @Test
//    fun testSetIPAddress() {
//        val port = 1112
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val s = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"))
//            s.accept()
//            s.accept()
//        }
//
//        val sender = ALSClient("0.0.0.0", port)
//
//        assertTrue { sender.address == "0.0.0.0" }
//
//        sender.setIPAddress("1.1.1.1", start = null)
//        Thread.sleep(100)
//        assertTrue { sender.address == "1.1.1.1" }
//
//        sender.setIPAddress("0.0.0.0", start = true)
//        Thread.sleep(1000)
//        assertTrue { sender.address == "0.0.0.0" }
//        assertTrue { sender.started }
//
//        sender.setIPAddress("0.0.0.0", start = null)
//        Thread.sleep(3000)
//        assertTrue { sender.address == "0.0.0.0" }
//        assertTrue { sender.started }
//
//        sender.setIPAddress("1.1.1.1", start = false)
//        Thread.sleep(100)
//        assertTrue { sender.address == "1.1.1.1" }
//        assertFalse { sender.started }
//    }
//
//    @Test
//    fun testSetPort() {
//        val port1 = 1113
//        val port2 = 1114
//        val port3 = 1115
//        val port4 = 1116
//        val port5 = 1117
//
//        GlobalScope.launch(Dispatchers.IO) {
//            ServerSocket(port3, 0, InetAddress.getByName("0.0.0.0"))
//            ServerSocket(port4, 0, InetAddress.getByName("0.0.0.0"))
//        }
//
//        val sender = ALSClient("0.0.0.0", port1)
//
//        assertTrue { sender.port == port1 }
//
//        sender.setPort(port2, start = null)
//        Thread.sleep(100)
//        assertTrue { sender.port == port2 }
//
//        sender.setPort(port3, start = true)
//        Thread.sleep(1000)
//        assertTrue { sender.port == port3 }
//        assertTrue { sender.started }
//
//        sender.setPort(port4, start = null)
//        Thread.sleep(3000)
//        assertTrue { sender.port == port4 }
//        assertTrue { sender.started }
//
//        sender.setPort(port5, start = false)
//        Thread.sleep(100)
//        assertTrue { sender.port == port5 }
//        assertFalse { sender.started }
//    }
//}
