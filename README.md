[![Build Status](https://travis-ci.com/AnimatedLEDStrip/client-kotlin-jvm.svg?branch=master)](https://travis-ci.com/AnimatedLEDStrip/client-kotlin-jvm)
[![codecov](https://codecov.io/gh/AnimatedLEDStrip/client-kotlin-jvm/branch/master/graph/badge.svg)](https://codecov.io/gh/AnimatedLEDStrip/client-kotlin-jvm)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.animatedledstrip/animatedledstrip-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.animatedledstrip/animatedledstrip-client)

# AnimatedLEDStrip Client Library for Kotlin/JVM

This library allows a Kotlin/JVM client to connect to an AnimatedLEDStrip server, allowing the client to send animations to the server and receive currently running animations from the server, among other information.

## Creating an `AnimationSender`
An `AnimationSender` is created by calling `AnimationSender()` with two arguments:
- `ipAddress`: The IP address of the server
- `port`: The port that the client should connect to

```kotlin
val sender = AnimationSender(ipAddress = "10.0.0.254", port = 5)
```

### Default `AnimationSender`
The `AnimationSender` companion object can store a reference to an `AnimationSender` to use as the default sender if no other sender is specified.
This is used by the [`AnimationData.send()`](#sending-data) extension function.
To specify an `AnimationSender` as the default sender, call `setAsDefaultSender()` on that instance.

```kotlin
sender.setAsDefaultSender()
```

## Starting an `AnimationSender`
Once your `AnimationSender` has been created, now you have to call that instance's `start()` method to start the connection.
A new coroutine will be created to connect to the server and then listen for messages.

```kotlin
sender.start()
```

## Stopping an `AnimationSender`
To stop a sender and disconnect it from the server, call the `end()` method on the `AnimationSender` instance.

```kotlin
sender.end()
```

## `setIPAddress()` and `setPort()`
The `setIPAddress` method will change the IP address of the sender, while `setPort` will change the port of the sender.
If the sender was connected to a server, it will disconnect the sender first.
If the `start` argument is:
- `true`: the server will attempt to connect to the server at the new IP/port
- `false`: the server will not attempt to create a new connection
- unset or `null`: the server will attempt to return to the state it was before the IP/port change

```kotlin
sender.setIPAddress("10.0.0.252", start = true)
sender.setIPAddress("10.0.0.253", start = false)
sender.setIPAddress("10.0.0.254")

sender.setPort("5", start = true)
sender.setPort("6", start = false)
sender.setPort("7")
```

## Sending Data
There are two ways to send animation data using an `AnimationSender`.
The easiest way is to call the `AnimationData.send()` extension function also defined in the library.
`send()` takes one optional argument: an `AnimationSender` which will be used to send the data.
If no `AnimationSender` is specified, `send()` will use the [default sender](#default-animationsender) as specified by the `AnimationSenderFactory`.

```kotlin
val cc = ColorContainer(0xFF, 0xFF00)
val data = AnimationData().addColor(cc)

data.send()        // Send with default sender
data.send(sender)  // Send with a specified sender
```

The other way to send data would be to call the `AnimationSender`'s `send()` method directly, with an `AnimationData` instance to be sent to the server.

```kotlin
sender.send(data)
```

#### `AnimationData` type notes
The Kotlin/JVM library uses the following values for `continuous` and `direction`:
- `continuous`: `null`, `true`, `false`
- `direction`: `Direction.FORWARD`, `Direction.BACKWARD`

## Receiving Data
Received animations are saved to the `runningAnimations` map and removed when an `EndAnimation` is received for that animation.

In addition, the Kotlin library uses callbacks that run custom lambdas to allow you to specify what to do with data that is received.

### OnReceive
The `onReceive` callback is called whenever the sender receives a `String` from the server.
The `String` is passed to your callback.
Use the `onNewAnimationData`, `onNewAnimationInfo`, `onNewEndAnimation`, `onNewSection` and `onNewStripInfo` callbacks to handle each type of `SendableData`.
Runs before the `onNewAnimationData`, `onNewAnimationInfo`, `onNewEndAnimation`, `onNewSection` and `onNewStripInfo` callbacks.

```kotlin
sender.setOnReceiveCallback { data: AnimationData ->
    // Your code here
}
```

### OnNewAnimationData
The `onNewAnimationData` callback is called whenever the sender receives an `AnimationData` instance from the server.
The `AnimationData` instance is passed to your callback.
Runs after the `onReceive` callback.

```kotlin
sender.setOnNewAnimationCallback { data: AnimationData ->
    // Your code here
}
```

### OnNewAnimationInfo
The `onNewAnimationInfo` callback is called whenever the sender receives an `Animation.AnimationInfo` instance from the server.
The `Animation.AnimationInfo` instance is passed to your callback.
Runs after the `onReceive` callback.

```kotlin
sender.setOnNewAnimationInfoCallback { info: Animation.AnimationInfo ->
    // Your code here
}
```

### OnNewEndAnimation
The `onNewEndAnimation` callback is called whenever the sender receives an `EndAnimation` instance from the server.
The `EndAnimation` instance is passed to your callback.
Runs after the `onReceive` callback.

```kotlin
sender.setOnNewEndAnimationCallback { anim: EndAnimation ->
    // Your code here
}
```

### OnNewSection
The `onNewSection` callback is called whenever the sender receives a `Section` instance from the server.
The `Section` instance is passed to your callback.
Runs after the `onReceive` callback.

```kotlin
sender.setOnNewSectionCallback { sect: Section ->
    // Your code here
}
```

### OnNewStripInfo
The `onNewStripInfo` callback is called whenever the sender receives a `StripInfo` instance from the server.
The `StripInfo` instance is passed to your callback.
Runs after the `onReceive` callback.

```kotlin
sender.setOnNewStripInfoCallback { info: StripInfo ->
    // Your code here
}
```

## Other Callbacks
The Kotlin library also includes callbacks for events such as connections or disconnections from the server.

### OnConnect
The `onConnect` callback is called when the sender successfully connects to the server.
The IP and port that the sender just connected to are passed to your callback.

```kotlin
sender.setOnConnectCallback { ip: String, port: Int ->
    // Your code here
}
```

### OnDisconnect
The `onDisconnect` callback is called when the sender loses connection to the server.
The IP and port that the sender just disconnected from are passed to your callback.

```kotlin
sender.setOnDisconnectCallback { ip: String, port: Int ->
    // Your code here
}
```

### OnUnableToConnect
The `onUnableToConnect` callback is called when the sender attempts to connect to the server but is unsuccessful.
The IP and port that the sender attempted to connect to are passed to your callback.

```kotlin
sender.setOnUnableToConnectCallback { ip: String, port: Int ->
    // Your code here
}
```

## Adding the Library to Your Project
### Maven Coordinates/Dependency
Use the following dependency to use this library in your project
> ```
> <dependency>
>   <groupId>io.github.animatedledstrip</groupId>
>   <artifactId>animatedledstrip-client</artifactId>
>   <version>0.7</version>
> </dependency>
> ```


### Snapshots
Development versions of the animatedledstrip-client library are available from the Sonatype snapshot repository:

> ```
> <repositories>
>    <repository>
>        <id>sonatype-snapshots</id>
>        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
>        <snapshots>
>            <enabled>true</enabled>
>        </snapshots>
>    </repository>
> </repositories>
> 
> <dependencies>
>   <dependency>
>     <groupId>io.github.animatedledstrip</groupId>
>     <artifactId>animatedledstrip-client</artifactId>
>     <version>0.8-SNAPSHOT</version>
>   </dependency>
> </dependencies>

### Note About Building
Because we use the dokka plugin to generate our documentation, we must use Java <=9
> https://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase9-3934878.html
