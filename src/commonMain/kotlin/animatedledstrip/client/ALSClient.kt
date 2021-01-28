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

import animatedledstrip.animations.Animation
import animatedledstrip.communication.*
import animatedledstrip.leds.animationmanagement.AnimationToRunParams
import animatedledstrip.leds.animationmanagement.EndAnimation
import animatedledstrip.leds.animationmanagement.RunningAnimationParams
import animatedledstrip.leds.colormanagement.CurrentStripColor
import animatedledstrip.leds.sectionmanagement.Section
import animatedledstrip.leds.stripmanagement.StripInfo
import animatedledstrip.utils.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Deprecated("Use ALSHttpClient")
class ALSClient(var address: String, var port: Int) {

    /* Connection */

    private var connection: ALSClientConnection = ALSClientConnection(address, port)


    /* Status */

    var started: Boolean = false
        private set
    val connected: Boolean
        get() = connection.connected

    private var connectedIp = ""


    /* Information about the connected server */

    val runningAnimations = mutableMapOf<String, RunningAnimationParams>()
    val sections = mutableMapOf<String, Section>()
    val supportedAnimations = mutableMapOf<String, Animation.AnimationInfo>()
    var stripInfo: StripInfo? = null


    /* Callbacks */

    private var onConnectCallback: ((String, Int) -> Any?)? = null
    private var onDisconnectCallback: ((String, Int) -> Any?)? = null
    private var onUnableToConnectCallback: ((String, Int) -> Any?)? = null

    private var onReceiveCallback: ((String) -> Any?)? = null
    private var onNewAnimationInfoCallback: ((Animation.AnimationInfo) -> Any?)? = null
    private var onNewCurrentStripColorCallback: ((CurrentStripColor) -> Any?)? = null
    private var onNewEndAnimationCallback: ((EndAnimation) -> Any?)? = null
    private var onNewMessageCallback: ((Message) -> Any?)? = null
    private var onNewRunningAnimationParamsCallback: ((RunningAnimationParams) -> Any?)? = null
    private var onNewSectionCallback: ((Section) -> Any?)? = null
    private var onNewStripInfoCallback: ((StripInfo) -> Any?)? = null



    /**
     * Start this connection
     */
    fun start(): ALSClient {
        if (started) return this

        runningAnimations.clear()
        sections.clear()
        supportedAnimations.clear()
        stripInfo = null

        started = true

        try {
            connectedIp = connection.connect()
        } catch (e: ConnectionException) {
            onUnableToConnectCallback?.invoke(address, port)
            started = false
            return this
        }

        onConnectCallback?.invoke(connectedIp, port)

        GlobalScope.launch {
            receiveLoop()
        }
        return this
    }

    fun startHeadless() {
        GlobalScope.launch { start() }
    }

    /**
     * Stop this connection
     */
    fun end() {
        started = false
        connection.close()
        supportedAnimations.clear()
    }

    private suspend fun receiveLoop() {
        try {
            while (connected) processData(receiveData())
        } catch (e: ConnectionException) {
            started = false
            onDisconnectCallback?.invoke(connectedIp, port)
        }
    }

    private suspend fun receiveData(): String = connection.receiveData()

    private fun processData(input: String) {
        for (d in splitData(input)) {
            if (d.isEmpty()) continue

            onReceiveCallback?.invoke(d)

            when (val data = d.decodeJson()) {
                is Animation.AnimationInfo -> {
                    supportedAnimations[data.name] = data
                    onNewAnimationInfoCallback?.invoke(data)
                }
                is AnimationToRunParams -> Logger.w { "Receiving AnimationToRunParams is not supported by client" }
                is ClientParams -> Logger.w { "Receiving ClientParams is not supported by client" }
                is Command -> Logger.w { "Receiving Command is not supported by client" }
                is CurrentStripColor ->
                    onNewCurrentStripColorCallback?.invoke(data)
                is EndAnimation -> {
                    onNewEndAnimationCallback?.invoke(data)
                    runningAnimations.remove(data.id)
                }
                is Message ->
                    onNewMessageCallback?.invoke(data)
                is RunningAnimationParams -> {
                    onNewRunningAnimationParamsCallback?.invoke(data)
                    runningAnimations[data.id] = data
                }
                is Section -> {
                    sections[data.name] = data
                    onNewSectionCallback?.invoke(data)
                }
                is StripInfo -> {
                    stripInfo = data
                    onNewStripInfoCallback?.invoke(data)
                }
                else -> Logger.w { "Unrecognized data type: $data" }
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
        try {
            sendBytes(args.json())
            Logger.d { "Sent $args" }
        } catch (e: ConnectionException) {
            Logger.e { "Unable to send because of\n${e.stackTraceToString()}" }
        }
    }


    private fun sendBytes(str: ByteArray) = connection.sendBytes(str)


    /* Set methods for callbacks and IP */

    /**
     * Specify an action to perform when a connection is established
     */
    fun setOnConnectCallback(action: (String, Int) -> Any?): ALSClient {
        onConnectCallback = action
        return this
    }

    /**
     * Specify an action to perform when a connection is lost
     */
    fun setOnDisconnectCallback(action: (String, Int) -> Any?): ALSClient {
        onDisconnectCallback = action
        return this
    }

    /**
     * Specify an action to perform when a connection cannot be made
     */
    fun setOnUnableToConnectCallback(action: (String, Int) -> Any?): ALSClient {
        onUnableToConnectCallback = action
        return this
    }

    /**
     * Specify an action to perform when data is received from the server
     */
    fun <R> setOnReceiveCallback(action: (String) -> R?): ALSClient {
        onReceiveCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new AnimationInfo instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewAnimationInfoCallback(action: (Animation.AnimationInfo) -> R?): ALSClient {
        onNewAnimationInfoCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new CurrentStripColor instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewCurrentStripColorCallback(action: (CurrentStripColor) -> R?): ALSClient {
        onNewCurrentStripColorCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new EndAnimation instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewEndAnimationCallback(action: (EndAnimation) -> R?): ALSClient {
        onNewEndAnimationCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new Message instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewMessageCallback(action: (Message) -> R?): ALSClient {
        onNewMessageCallback = action
        return this
    }


    /**
     * Specify an action to perform when a new RunningAnimationParams instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewRunningAnimationParamsCallback(action: (RunningAnimationParams) -> R?): ALSClient {
        onNewRunningAnimationParamsCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new Section instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewSectionCallback(action: (Section) -> R?): ALSClient {
        onNewSectionCallback = action
        return this
    }

    /**
     * Specify an action to perform when a new StripInfo instance is received from the server.
     * Runs after onReceive callback.
     */
    fun <R> setOnNewStripInfoCallback(action: (StripInfo) -> R?): ALSClient {
        onNewStripInfoCallback = action
        return this
    }

    /**
     * Set this sender as the default sender
     */
    fun setAsDefaultClient(): ALSClient {
        defaultClient = this
        return this
    }

    private fun restartSenderWithChange(start: Boolean?, change: ALSClient.() -> Unit) {
        GlobalScope.launch {
            val wasStarted = started
            if (started) {
                end()
                if (start != false) delay(2000)
            }
            this@ALSClient.change()
            if (start ?: wasStarted) start()
        }
    }

    /**
     * Set this connection's IP address.
     * Will start/restart connection if start = true or if
     * connection is running and start = null.
     *
     * @param newAddress A string representing an IPv4 address
     */
    fun setIPAddress(newAddress: String, start: Boolean? = null): ALSClient {
        restartSenderWithChange(start) {
            address = newAddress
            connection.address = newAddress
        }
        return this
    }

    /**
     * Set this connection's port.
     * Will start/restart connection if start = true or if
     * connection is running and start = null.
     */
    fun setPort(newPort: Int, start: Boolean? = null): ALSClient {
        restartSenderWithChange(start) {
            port = newPort
            connection.port = newPort
        }
        return this
    }

    companion object {
        /**
         * The default sender if none is specified
         */
        lateinit var defaultClient: ALSClient
    }

}
