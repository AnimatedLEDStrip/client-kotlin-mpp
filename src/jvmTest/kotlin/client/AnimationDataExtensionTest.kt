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

//import animatedledstrip.client.ALSClient
//import animatedledstrip.client.send
//import animatedledstrip.communication.Message
//import animatedledstrip.communication.decodeJson
//import animatedledstrip.communication.toUTF8String
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import org.junit.Test
//import java.net.InetAddress
//import java.net.ServerSocket
//import kotlin.test.assertTrue
//
//class AnimationDataExtensionTest {
//
//    @Test
//    fun testSend() {
//        var testBoolean = false
//        val port = 1200
//
//        val testMessage = Message("A test")
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
//            val inStr = socket!!.getInputStream()
//            val input = ByteArray(1000)
//            val count = inStr.read(input)
//            if (input.toUTF8String(count).decodeJson() as Message == testMessage) testBoolean = true
//        }
//
//        Thread.sleep(500)
//
//        val sender =
//            ALSClient("0.0.0.0", port)
//                .start()
//
//        Thread.sleep(2000)
//
//        testMessage.send(sender)
//
//        Thread.sleep(2000)
//
//        assertTrue { testBoolean }
//    }
//
////    @Test
////    fun testEndAnimation() {
////        var testBoolean = false
////        val port = 1201
////
////        val testAnimation =
////            AnimationData()
////                .animation("Stack")
////                .color(ColorContainer(0xFF, 0xFFFF).prepare(5), index = 0)
////                .color(0xFF, index = 1)
////                .color(0xFF, index = 2)
////                .color(0xFF, index = 3)
////                .color(0xFF, index = 4)
////                .continuous(true)
////                .delay(50)
////                .direction(Direction.FORWARD)
////                .id("TEST")
////                .spacing(5)
////
////        GlobalScope.launch(Dispatchers.IO) {
////            val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
////            val inStr = socket!!.getInputStream()
////            val input = ByteArray(1000)
////            val count = inStr.read(input)
////            if (input.toUTF8(count).startsWith("END :")) testBoolean = true
////        }
////
////        delayBlocking(500)
////
////        val sender = AnimationSender("0.0.0.0", port)
////            .start()
////
////        delayBlocking(2000)
////
////        testAnimation.endAnimation(sender)
////
////        delayBlocking(2000)
////
////        assertTrue { testBoolean }
////    }
//
//}
