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

package animatedledstrip.client

import animatedledstrip.animationutils.Animation
import animatedledstrip.animationutils.AnimationData
import animatedledstrip.animationutils.EndAnimation
import animatedledstrip.leds.AnimatedLEDStrip
import animatedledstrip.leds.StripInfo
import animatedledstrip.utils.*
import kotlinx.coroutines.*
import org.pmw.tinylog.Logger
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

class AnimationSender(var address: String, var port: Int) {

    /* Connection */

    private var connection: Socket = Socket()
    private var socIn: InputStream? = null
    private var socOut: OutputStream? = null

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val senderCoroutineScope = newSingleThreadContext("Animation Sender port $port")


    /* Status */

    var started: Boolean = false
        private set
    var connected: Boolean = false
        private set

    private var connectedIp = ""


    /* Information about the connected server */

    val runningAnimations = mutableMapOf<String, AnimationData>()
    val sections = mutableMapOf<String, AnimatedLEDStrip.Section>()
    val supportedAnimations = mutableMapOf<String, Animation.AnimationInfo>()
    var stripInfo: StripInfo? = null


    /* Callbacks */

    private var onConnectCallback: ((String, Int) -> Any?)? = null
    private var onDisconnectCallback: ((String, Int) -> Any?)? = null
    private var onUnableToConnectCallback: ((String, Int) -> Any?)? = null

    private var onReceiveCallback: ((String) -> Any?)? = null
    private var onNewAnimationDataCallback: ((AnimationData) -> Any?)? = null
    private var onNewAnimationInfoCallback: ((Animation.AnimationInfo) -> Any?)? = null
    private var onNewEndAnimationCallback: ((EndAnimation) -> Any?)? = null
    private var onNewMessageCallback: ((Message) -> Any?)? = null
    private var onNewSectionCallback: ((AnimatedLEDStrip.Section) -> Any?)? = null
    private var onNewStripInfoCallback: ((StripInfo) -> Any?)? = null


    /**
     * Start this connection
     */
    fun start(): AnimationSender {
        if (started) return this

        runningAnimations.clear()
        sections.clear()
        supportedAnimations.clear()
        stripInfo = null

        started = true

        try {
            connection = Socket()
            connection.soTimeout = SOCKET_TIMEOUT

            connection.connect(InetSocketAddress(address, port), SOCKET_TIMEOUT)

            connectedIp = address

            socOut = connection.getOutputStream()
            socIn = connection.getInputStream()
        } catch (e: SocketException) {
            onUnableToConnectCallback?.invoke(address, port)
            started = false
            connected = false
            return this
        } catch (e: SocketTimeoutException) {
            onUnableToConnectCallback?.invoke(address, port)
            started = false
            connected = false
            return this
        }

        connected = true
        onConnectCallback?.invoke(connectedIp, port)

        GlobalScope.launch(senderCoroutineScope) {
            receiveLoop()
        }
        return this
    }

    /**
     * Stop this connection
     */
    fun end() {
        started = false
        connected = false
        connection.close()
        supportedAnimations.clear()
    }


    /* Handle receiving data from server */

    private suspend fun receiveLoop() {
        try {
            while (connected) processData(receiveData())
        } catch (e: IOException) {
            started = false
            connected = false
            onDisconnectCallback?.invoke(connectedIp, port)
        }
    }

    private suspend fun receiveData(): String {
        val input = ByteArray(INPUT_SIZE)
        var count: Int = -1

        while (true)
            try {
                withContext(Dispatchers.IO) {
                    count = socIn?.read(input) ?: throw SocketException("Socket null")
                }
                break
            } catch (e: SocketTimeoutException) {
                yield()
                continue
            }

        if (count == -1) throw SocketException("Connection closed")
        return input.toUTF8(count)
    }

    private fun processData(input: String) {
        for (d in splitData(input)) {
            if (d.isEmpty()) continue

            onReceiveCallback?.invoke(d)

            when (val dataType = d.getDataTypePrefix()) {
                AnimationData.prefix -> {
                    val data = d.jsonToAnimationData()
                    onNewAnimationDataCallback?.invoke(data)
                    runningAnimations[data.id] = data
                }
                Animation.AnimationInfo.prefix -> {
                    val info = d.jsonToAnimationInfo()
                    supportedAnimations[info.name] = info
                    onNewAnimationInfoCallback?.invoke(info)
                }
                Command.prefix -> Logger.warn("Receiving Command is not supported by client")
                EndAnimation.prefix -> {
                    val end = d.jsonToEndAnimation()
                    onNewEndAnimationCallback?.invoke(end)
                    runningAnimations.remove(end.id)
                }
                Message.prefix -> {
                    val msg = d.jsonToMessage()
                    onNewMessageCallback?.invoke(msg)
                }
                AnimatedLEDStrip.sectionPrefix -> {     // AnimatedLEDStrip.Section
                    val sect = d.jsonToSection()
                    sections[sect.name] = sect
                    onNewSectionCallback?.invoke(sect)
                }
                StripInfo.prefix -> {
                    val info = d.jsonToStripInfo()
                    stripInfo = info
                    onNewStripInfoCallback?.invoke(info)
                }
                else -> Logger.warn("Unrecognized data type: $dataType")
            }
        }
    }

    private val partialData = StringBuilder()

    private fun String.withPartialData(): String {
        val newStr = partialData.toString() + this
        partialData.clear()
        return newStr
    }

    private fun handlePartialData(inputData: List<String>): List<String> {
        partialData.append(inputData.last())
        return inputData.dropLast(1)
    }

    private fun splitData(input: String): List<String> {
        val inputData = input.withPartialData().split(DELIMITER)

        return if (!input.endsWith(DELIMITER))
            handlePartialData(inputData)
        else
            inputData
    }


    /**
     * Send data via this connection
     */
    fun send(args: SendableData) {
        sendBytes(args.json())
        Logger.debug(args)
    }


    private fun sendBytes(str: ByteArray) {
        GlobalScope.launch(Dispatchers.IO) {
            socOut?.write(str) ?: Logger.warn("Output stream null")
        }
    }


    /* Set methods for callbacks and IP */

    /**
     * Specify an action to perform when a connection is established
     */
    fun setOnConnectCallback(action: (String, Int) -> Any?): AnimationSender {
        onConnectCallback = action
        return this
    }

    /**
     * Specify an action to perform when a connection is lost
     */
    fun setOnDisconnectCallback(action: (String, Int) -> Any?): AnimationSender {
        onDisconnectCallback = action
        return this
    }

    /**
     * Specify an action to perform when a connection cannot be made
     */
    fun setOnUnableToConnectCallback(action: (String, Int) -> Any?): AnimationSender {
        onUnableToConnectCallback = action
        return this
    }

    /**
     * Specify an action to perform when data is received from the server
     */
    fun <R> setOnReceiveCallback(action: (String) -> R?): AnimationSender {
        onReceiveCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new AnimationData instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewAnimationDataCallback(action: (AnimationData) -> R?): AnimationSender {
        onNewAnimationDataCallback = action
        return this
    }

    fun <R> setOnNewAnimationInfoCallback(action: (Animation.AnimationInfo) -> R?): AnimationSender {
        onNewAnimationInfoCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new EndAnimation instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewEndAnimationCallback(action: (EndAnimation) -> R?): AnimationSender {
        onNewEndAnimationCallback = action
        return this
    }

    fun <R> setOnNewMessageCallback(action: (Message) -> R?): AnimationSender {
        onNewMessageCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new Section instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewSectionCallback(action: (AnimatedLEDStrip.Section) -> R?): AnimationSender {
        onNewSectionCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new StripInfo instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewStripInfoCallback(action: (StripInfo) -> R?): AnimationSender {
        onNewStripInfoCallback = action
        return this
    }

    /**
     * Set this sender as the default sender
     */
    fun setAsDefaultSender(): AnimationSender {
        defaultSender = this
        return this
    }

    private fun restartSenderWithChange(start: Boolean?, change: AnimationSender.() -> Unit) {
        GlobalScope.launch {
            val wasStarted = started
            if (started) {
                end()
                if (start != false) delayBlocking(2000)
            }
            this@AnimationSender.change()
            if (start ?: wasStarted) start()
        }
    }

    /**
     * Set this connection's IP address.
     * Will start/restart connection if start = true or if
     * connection is running and start = null.
     *
     * @param address A string representing an IPv4 address
     */
    fun setIPAddress(address: String, start: Boolean? = null): AnimationSender {
        restartSenderWithChange(start) {
            this.address = address
        }
        return this
    }

    /**
     * Set this connection's port.
     * Will start/restart connection if start = true or if
     * connection is running and start = null.
     */
    fun setPort(newPort: Int, start: Boolean? = null): AnimationSender {
        restartSenderWithChange(start) {
            port = newPort
        }
        return this
    }

    companion object {
        /**
         * The default sender if none is specified
         */
        lateinit var defaultSender: AnimationSender
    }

}
