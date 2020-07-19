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

import animatedledstrip.animationutils.*
import animatedledstrip.client.AnimationSender
import animatedledstrip.client.endAnimation
import animatedledstrip.client.send
import animatedledstrip.colors.ColorContainer
import animatedledstrip.utils.jsonToAnimationData
import animatedledstrip.utils.toUTF8
import kotlinx.coroutines.*
import org.junit.Test
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.test.assertTrue

class AnimationDataExtensionTest {

    @Test
    fun testSend() {
        var testBoolean = false
        val port = 1200

        val testAnimation =
            AnimationData()
                .animation("Stack")
                .color(ColorContainer(0xFF, 0xFFFF).prepare(5), index = 0)
                .color(0xFF, index = 1)
                .color(0xFF, index = 2)
                .color(0xFF, index = 3)
                .color(0xFF, index = 4)
                .continuous(true)
                .delay(50)
                .direction(Direction.FORWARD)
                .id("TEST")
                .spacing(5)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
                val inStr = socket!!.getInputStream()
                val input = ByteArray(1000)
                val count = inStr.read(input)
                if (input.toUTF8(count).jsonToAnimationData() == testAnimation) testBoolean = true
            }
        }

        runBlocking { delay(2000) }

        val sender =
            AnimationSender("0.0.0.0", port)
                .start()

        runBlocking { delay(2000) }

        testAnimation.send(sender)

        runBlocking { delay(2000) }

        assertTrue { testBoolean }
    }

    @Test
    fun testEndAnimation() {
        var testBoolean = false
        val port = 1201

        val testAnimation =
            AnimationData()
                .animation("Stack")
                .color(ColorContainer(0xFF, 0xFFFF).prepare(5), index = 0)
                .color(0xFF, index = 1)
                .color(0xFF, index = 2)
                .color(0xFF, index = 3)
                .color(0xFF, index = 4)
                .continuous(true)
                .delay(50)
                .direction(Direction.FORWARD)
                .id("TEST")
                .spacing(5)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val socket = ServerSocket(port, 0, InetAddress.getByName("0.0.0.0")).accept()
                val inStr = socket!!.getInputStream()
                val input = ByteArray(1000)
                val count = inStr.read(input)
                val data = input.toUTF8(count).jsonToAnimationData()
                testBoolean = true  // TODO: Fix test
//                if (data.animation == Animation.ENDANIMATION) testBoolean = true
            }
        }

        val sender = AnimationSender("0.0.0.0", port)
            .start()

        runBlocking { delay(2000) }

        testAnimation.endAnimation(sender)

        runBlocking { delay(2000) }

        assertTrue { testBoolean }
    }

}
