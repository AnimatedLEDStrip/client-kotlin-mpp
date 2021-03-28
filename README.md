[![Build Status](https://github.com/AnimatedLEDStrip/client-kotlin-mpp/actions/workflows/test.yml/badge.svg)](https://github.com/AnimatedLEDStrip/client-kotlin-mpp/actions/workflows/test.yml)
[![codecov](https://codecov.io/gh/AnimatedLEDStrip/client-kotlin-mpp/branch/master/graph/badge.svg)](https://codecov.io/gh/AnimatedLEDStrip/client-kotlin-mpp)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.animatedledstrip/animatedledstrip-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.animatedledstrip/animatedledstrip-client)

# AnimatedLEDStrip Client Library for Kotlin/MPP

This library allows a Kotlin/JVM or Kotlin/JS client to communicate with an AnimatedLEDStrip server.

## Creating an `ALSHttpClient`
To create a HTTP client, run `ALSHttpClient(clientEngine, ip)`.

> `clientEngine` is a [Ktor `HttpClientEngineFactory`](https://ktor.io/docs/http-client-engines.html)

```kotlin
val client = ALSHttpClient(CIO, ip = "10.0.0.254")
```

An optional configuration for the Ktor engine can be passed as a third argument.
Any configuration should include a JSON feature that uses the AnimatedLEDStrip serializer:

```kotlin
import animatedledstrip.communication.serializer as alsSerializer

install(JsonFeature) {
    serializer = KotlinxSerializer(alsSerializer)
}
```

## Communicating with the Server

This library follows the conventions laid out for [AnimatedLEDStrip client libraries](https://animatedledstrip.github.io/client-libraries).
