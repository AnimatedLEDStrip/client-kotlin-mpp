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

import animatedledstrip.animations.groups.AnimationGroup
import animatedledstrip.animations.groups.GroupType
import animatedledstrip.animations.predefined.*
import animatedledstrip.animations.predefinedgroups.sortingAnimations
import animatedledstrip.client.ALSHttpClient
import animatedledstrip.leds.animationmanagement.AnimationToRunParams
import animatedledstrip.leds.animationmanagement.RunningAnimationParams
import animatedledstrip.leds.sectionmanagement.Section
import animatedledstrip.leds.stripmanagement.StripInfo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.HttpMethod
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
                    "/animations/newGroup" -> serializeAndRespond(sortingAnimations.groupInfo)
                    "/sections" -> {
                        when (request.method) {
                            HttpMethod.Get -> serializeAndRespond(listOf(Section("s1", listOf(1, 2, 3, 4), "s0"),
                                                                         Section("s2", listOf(5, 8, 1, 3))))
                            HttpMethod.Post -> serializeAndRespond(Section("s3", listOf(5, 6, 7, 8), "s2"))
                            else -> error("Unsupported method ${request.method}")
                        }

                    }
                    "/sections/map" -> serializeAndRespond(mapOf("s1" to Section("s1", listOf(1, 2, 3, 4), "s0"),
                                                                 "s2" to Section("s2", listOf(5, 8, 1, 3))))
                    "/section/s1" -> serializeAndRespond(Section("s1", listOf(1, 2, 3, 4), "s0"))
                    "/section/s2" -> serializeAndRespond(Section("s2", listOf(5, 8, 1, 3)))
                    "/section/fullStrip" -> serializeAndRespond(Section("s0", (0..9).toList()))
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
                    "/strip/info" -> serializeAndRespond(StripInfo(10, 12, is2DSupported = true))
                    "/strip/color" -> serializeAndRespond(listOf(0xFF, 0xFF, 0xFFFF, 0xFFFF, 0xFFFFFF, 0xFFFFFF))
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
    fun `get supported animations`() = runInCoroutine {
        testClient.getSupportedAnimations() shouldBe listOf(alternate.info, bounce.info)
    }

    @Test
    fun `get supported animations map`() = runInCoroutine {
        testClient.getSupportedAnimationsMap() shouldBe mapOf("Alternate" to alternate.info,
                                                              "Bounce" to bounce.info)
    }

    @Test
    fun `get supported animations names`() = runInCoroutine {
        testClient.getSupportedAnimationsNames() shouldBe listOf("anim1", "anim2", "anim3")
    }

    @Test
    fun `create new group`() = runInCoroutine {
        testClient.createNewGroup(sortingAnimations) shouldBe sortingAnimations.groupInfo
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
    fun `get sections`() = runInCoroutine {
        val sections = testClient.getSections()
        sections.shouldHaveSize(2)
        sections[0].name shouldBe "s1"
        sections[0].pixels shouldBe listOf(1, 2, 3, 4)
        sections[0].parentSectionName shouldBe "s0"
        sections[1].name shouldBe "s2"
        sections[1].pixels shouldBe listOf(5, 8, 1, 3)
        sections[1].parentSectionName shouldBe ""
    }

    @Test
    fun `get sections map`() = runInCoroutine {
        val sections = testClient.getSectionsMap()
        sections.shouldHaveSize(2)
        sections.shouldHaveKeys("s1", "s2")
        sections["s1"]!!.name shouldBe "s1"
        sections["s1"]!!.pixels shouldBe listOf(1, 2, 3, 4)
        sections["s1"]!!.parentSectionName shouldBe "s0"
        sections["s2"]!!.name shouldBe "s2"
        sections["s2"]!!.pixels shouldBe listOf(5, 8, 1, 3)
        sections["s2"]!!.parentSectionName shouldBe ""
    }

    @Test
    fun `get section`() = runInCoroutine {
        val section1 = testClient.getSection("s1")
        section1.name shouldBe "s1"
        section1.pixels shouldBe listOf(1, 2, 3, 4)
        section1.parentSectionName shouldBe "s0"

        val section2 = testClient.getSection("s2")
        section2.name shouldBe "s2"
        section2.pixels shouldBe listOf(5, 8, 1, 3)
        section2.parentSectionName shouldBe ""
    }

    @Test
    fun `get full strip section`() = runInCoroutine {
        val section1 = testClient.getFullStripSection()
        section1.name shouldBe "s0"
        section1.pixels shouldBe listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        section1.parentSectionName shouldBe ""

        val section2 = testClient.getSection("")
        section2.name shouldBe "s0"
        section2.pixels shouldBe listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        section2.parentSectionName shouldBe ""
    }

    @Test
    fun `create new section`() = runInCoroutine {
        val newSection = testClient.createNewSection(Section("s3", listOf(5, 6, 7, 8), "s2"))
        newSection.name shouldBe "s3"
        newSection.pixels shouldBe listOf(5, 6, 7, 8)
        newSection.parentSectionName shouldBe "s2"
    }


    @Test
    fun `start animation`() = runInCoroutine {
        testClient.startAnimation(AnimationToRunParams("color")) shouldBe
                RunningAnimationParams(color,
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


    @Test
    fun `get strip info`() = runInCoroutine {
        testClient.getStripInfo() shouldBe StripInfo(10, 12, is2DSupported = true)
    }

    @Test
    fun `get current strip color`() = runInCoroutine {
        testClient.getCurrentStripColor() shouldBe listOf(0xFF, 0xFF, 0xFFFF, 0xFFFF, 0xFFFFFF, 0xFFFFFF)
    }
}
