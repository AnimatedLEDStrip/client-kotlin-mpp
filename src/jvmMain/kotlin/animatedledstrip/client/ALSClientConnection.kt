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

package animatedledstrip.client

import animatedledstrip.communication.toUTF8String
import animatedledstrip.utils.Logger
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

@Deprecated("Use ALSHttpClient")
actual class ALSClientConnection actual constructor(actual var address: String, actual var port: Int) {
    private var socket: Socket = Socket()
    private var socIn: InputStream? = null
    private var socOut: OutputStream? = null

    actual var connected: Boolean = false
        private set

    actual fun connect(): String {
        try {
            socket = Socket()
            socket.soTimeout = SOCKET_TIMEOUT

            socket.connect(InetSocketAddress(address, port), SOCKET_TIMEOUT)

            socOut = socket.getOutputStream()
            socIn = socket.getInputStream()
        } catch (e: SocketException) {
            connected = false
            throw ConnectionException(message = e.message, cause = e)
        } catch (e: SocketTimeoutException) {
            connected = false
            throw ConnectionException(message = e.message, cause = e)
        }
        connected = true
        return address
    }

    actual suspend fun receiveData(): String {
        val input = ByteArray(INPUT_SIZE)
        var count: Int = -1

        while (true)
            try {
                withContext(Dispatchers.IO) {
                    count = socIn?.read(input) ?: throw ConnectionException("Socket null")
                }
                break
            } catch (e: SocketTimeoutException) {
                yield()
                continue
            } catch (e: SocketException) {
                throw ConnectionException("Connection closed")
            }

        if (count == -1) throw ConnectionException("Connection closed")
        return input.toUTF8String(count)
    }

    actual fun sendBytes(bytes: ByteArray) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (!connected) {
                    Logger.w { "Not connected" }
                } else socOut?.write(bytes) ?: Logger.w { "Output stream null" }
            } catch (e: SocketException) {
                Logger.e { "Socket Exception\n${e.stackTraceToString()}" }
            }
        }
    }

    actual fun close() {
        connected = false
        socket.close()
    }


}