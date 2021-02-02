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

import animatedledstrip.animations.predefined.*
import animatedledstrip.client.ALSHttpClient
import animatedledstrip.leds.animationmanagement.AnimationToRunParams
import animatedledstrip.leds.animationmanagement.RunningAnimationParams
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import animatedledstrip.communication.serializer as alsSerializer


class ALSHttpClientTest {

    private inline fun <reified T> MockRequestHandleScope.serializeAndRespond(data: T) =
        respond(alsSerializer.encodeToString(data),
                HttpStatusCode.OK,
                headersOf("Content-Type", "application/json"))

    private val testClient = ALSHttpClient(MockEngine, "testURL") {
        install(JsonFeature) {
            serializer = KotlinxSerializer(alsSerializer)
        }
        engine {
            addHandler { request ->
                when (request.url.fullPath) {
                    "/animation/alternate" -> serializeAndRespond(alternate.info)
                    "/animations/names" -> serializeAndRespond(listOf("anim1", "anim2", "anim3"))
                    "/animations" -> serializeAndRespond(listOf(alternate.info, bounce.info))
                    "/animations/map" -> serializeAndRespond(mapOf("Alternate" to alternate.info,
                                                                   "Bounce" to bounce.info))
                    "/animations/newOrderedGroup" -> serializeAndRespond(ripple.info)
                    "/animations/newRandomizedGroup" -> serializeAndRespond(pixelRun.info)
                    "/running" -> serializeAndRespond(
                        mapOf("anim1" to RunningAnimationParams(color,
                                                                "color",
                                                                listOf(),
                                                                "anim1",
                                                                "",
                                                                -1,
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                AnimationToRunParams()),
                              "anim2" to RunningAnimationParams(color,
                                                                "color",
                                                                listOf(),
                                                                "anim2",
                                                                "",
                                                                -1,
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                mapOf(),
                                                                AnimationToRunParams())))
                    "/running/ids" -> serializeAndRespond(listOf("anim4", "anim5", "anim6"))
                    "/running/anim7" -> serializeAndRespond(RunningAnimationParams(color,
                                                                                   "color",
                                                                                   listOf(),
                                                                                   "anim7",
                                                                                   "",
                                                                                   -1,
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   AnimationToRunParams()))
                    "/running/anim8" -> serializeAndRespond(RunningAnimationParams(color,
                                                                                   "color",
                                                                                   listOf(),
                                                                                   "anim8",
                                                                                   "",
                                                                                   -1,
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   AnimationToRunParams()))
                    "/running/anim9" -> serializeAndRespond(RunningAnimationParams(color,
                                                                                   "color",
                                                                                   listOf(),
                                                                                   "anim9",
                                                                                   "",
                                                                                   -1,
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   mapOf(),
                                                                                   AnimationToRunParams()))
                    "/start" -> serializeAndRespond(RunningAnimationParams(color,
                                                                           "color",
                                                                           listOf(),
                                                                           "",
                                                                           "",
                                                                           -1,
                                                                           mapOf(),
                                                                           mapOf(),
                                                                           mapOf(),
                                                                           mapOf(),
                                                                           mapOf(),
                                                                           mapOf(),
                                                                           mapOf(),
                                                                           AnimationToRunParams()))
                    else -> error("Unhandled ${request.url.fullPath}")
                }
            }
        }
    }

    @Test
    fun `resolve path`() {
        testClient.resolvePath("/testEndpoint") shouldBe "http://testURL:8080/testEndpoint"
        testClient.resolvePath("testEndpoint") shouldBe "http://testURL:8080/testEndpoint"
    }

    @Test
    fun `get animation info`() = runInCoroutine {
        shouldThrow<IllegalArgumentException> {
            testClient.getAnimationInfo("")
        }
        testClient.getAnimationInfo("alternate") shouldBe alternate.info
    }

    @Test
    fun `get supported animations names`() = runInCoroutine {
        testClient.getSupportedAnimationsNames() shouldBe listOf("anim1", "anim2", "anim3")
    }

    @Test
    fun `get supported animations`() = runInCoroutine {
        testClient.getSupportedAnimations() shouldBe listOf(alternate.info, bounce.info)
    }

    @Test
    fun `get supported animations map`() = runInCoroutine {
        testClient.getSupportedAnimationsMap() shouldBe mapOf("Alternate" to alternate.info,
                                                              "Bounce" to bounce.info)
    }

    @Test
    fun `get running animations`() = runInCoroutine {
        testClient.getRunningAnimations() shouldBe
                mapOf("anim1" to RunningAnimationParams(color,
                                                        "color",
                                                        listOf(),
                                                        "anim1",
                                                        "",
                                                        -1,
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        AnimationToRunParams()),
                      "anim2" to RunningAnimationParams(color,
                                                        "color",
                                                        listOf(),
                                                        "anim2",
                                                        "",
                                                        -1,
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        mapOf(),
                                                        AnimationToRunParams()))
    }

    @Test
    fun `get running animations ids`() = runInCoroutine {
        testClient.getRunningAnimationsIds() shouldBe listOf("anim4", "anim5", "anim6")
    }

    @Test
    fun `get running animation params`() = runInCoroutine {
        testClient.getRunningAnimationParams("anim7") shouldBe
                RunningAnimationParams(color,
                                       "color",
                                       listOf(),
                                       "anim7",
                                       "",
                                       -1,
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       AnimationToRunParams())
    }

    @Test
    fun `end animation`() = runInCoroutine {
        testClient.endAnimation("anim8") shouldBe
                RunningAnimationParams(color,
                                       "color",
                                       listOf(),
                                       "anim8",
                                       "",
                                       -1,
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       AnimationToRunParams())

        testClient.endAnimation(
            RunningAnimationParams(color,
                                   "color",
                                   listOf(),
                                   "anim9",
                                   "",
                                   -1,
                                   mapOf(),
                                   mapOf(),
                                   mapOf(),
                                   mapOf(),
                                   mapOf(),
                                   mapOf(),
                                   mapOf(),
                                   AnimationToRunParams())) shouldBe
                RunningAnimationParams(color,
                                       "color",
                                       listOf(),
                                       "anim9",
                                       "",
                                       -1,
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       mapOf(),
                                       AnimationToRunParams())
    }

    @Test
    fun `start animation`() = runInCoroutine {
        val params = AnimationToRunParams("color")
        testClient.startAnimation(params) shouldBe RunningAnimationParams(color,
                                                                          "color",
                                                                          listOf(),
                                                                          "",
                                                                          "",
                                                                          -1,
                                                                          mapOf(),
                                                                          mapOf(),
                                                                          mapOf(),
                                                                          mapOf(),
                                                                          mapOf(),
                                                                          mapOf(),
                                                                          mapOf(),
                                                                          AnimationToRunParams())
    }

}
