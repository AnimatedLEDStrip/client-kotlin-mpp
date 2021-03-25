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
import animatedledstrip.animations.groups.AnimationGroup
import animatedledstrip.leds.animationmanagement.AnimationToRunParams
import animatedledstrip.leds.animationmanagement.RunningAnimationParams
import animatedledstrip.leds.sectionmanagement.Section
import animatedledstrip.leds.stripmanagement.StripInfo
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import animatedledstrip.communication.serializer as alsSerializer

class ALSHttpClient<out C : HttpClientEngineConfig>(
    clientEngine: HttpClientEngineFactory<C>,
    var ip: String,
    clientConfig: HttpClientConfig<C>.() -> Unit = {
        install(JsonFeature) {
            serializer = KotlinxSerializer(alsSerializer)
        }
    },
) {

    private val client: HttpClient = HttpClient(clientEngine, clientConfig)

    internal fun resolvePath(path: String): String =
        "http://$ip:8080${if (path.startsWith("/")) path else "/$path"}"


    suspend fun getAnimationInfo(name: String): Animation.AnimationInfo {
        require(name.isNotBlank())
        return client.get(resolvePath( "/animation/$name"))
    }


    suspend fun getSupportedAnimations(): List<Animation.AnimationInfo> = client.get(resolvePath("/animations"))

    suspend fun getSupportedAnimationsMap(): Map<String, Animation.AnimationInfo> =
        client.get(resolvePath("/animations/map"))

    suspend fun getSupportedAnimationsNames(): List<String> = client.get(resolvePath("/animations/names"))

    suspend fun createNewGroup(groupInfo: AnimationGroup.NewAnimationGroupInfo): Animation.AnimationInfo =
        client.post(resolvePath("/animations/newGroup")) {
            body = groupInfo
            contentType(ContentType.Application.Json)
        }


    suspend fun getRunningAnimations(): Map<String, RunningAnimationParams> = client.get(resolvePath("/running"))

    suspend fun getRunningAnimationsIds(): List<String> = client.get(resolvePath("/running/ids"))

    suspend fun getRunningAnimationParams(animId: String): RunningAnimationParams {
        require(animId.isNotBlank())
        return client.get(resolvePath("/running/$animId"))
    }

    suspend fun endAnimation(animId: String): RunningAnimationParams {
        require(animId.isNotBlank())
        return client.delete(resolvePath("/running/$animId"))
    }

    suspend fun endAnimation(animParams: RunningAnimationParams): RunningAnimationParams = endAnimation(animParams.id)


    suspend fun getSections(): List<Section> = client.get(resolvePath("/sections"))

    suspend fun getSectionsMap(): Map<String, Section> = client.get(resolvePath("/sections/map"))

    suspend fun getSection(sectionId: String): Section =
        if (sectionId.isBlank()) getFullStripSection()
        else client.get(resolvePath("/section/$sectionId"))

    suspend fun getFullStripSection(): Section = client.get(resolvePath("/section/fullStrip"))

    suspend fun createNewSection(section: Section): Section =
        client.post(resolvePath("/sections")) {
            body = section
            contentType(ContentType.Application.Json)
        }


    suspend fun startAnimation(animParams: AnimationToRunParams): RunningAnimationParams =
        client.post(resolvePath("/start")) {
            body = animParams
            contentType(ContentType.Application.Json)
        }


    suspend fun getStripInfo(): StripInfo = client.get(resolvePath("/strip/info"))

    suspend fun getCurrentStripColor(): List<Int> = client.get(resolvePath("/strip/color"))

    suspend fun clearStrip() {
        client.post<Any?>(resolvePath("/strip/clear")) {}
    }
}
