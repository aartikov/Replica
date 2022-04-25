# Replica
[![Maven Central](https://img.shields.io/maven-central/v/com.github.aartikov/replica-core)](https://repo1.maven.org/maven2/com/github/aartikov/replica-core/)
[![license](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

#### Developed in [MobileUp](https://mobileup.ru/) company

## What is Replica?
**Replica** is an Android library for organizing of network communication. While libraries such as *Retrofit* and *Kotlin Serialization* help to make network requests, Replica takes on another challenges:

- Optimize amount of network calls
- Display up-to-date data
- Provide delightful User Experience (UX)
- Provide amazing Developer Experience (DX)

## How Replica works?
### Data Replication
The library is based on a concept called **data replication**. Imagine that there is some chunk of data on a server. The task of Replica is to make a copy of this data on a client. Sometimes data is changing on a server but Replica tries to keep it in sync.

<img src="images/data_replication.png">

### Replication primitives
Replica (a library) provides replication primitives called **Replicas**. Replica (a replication primitive) is located on a client side and performs data replication.

<img src="images/replication_primitive.png">

### Replica Observers
**Replica observer** is an agent that connects to a Replica and watches what is happening inside. Replica can have zero, one or multiple observers. Each observer can be **active** or **inactive**. By connecting to a Replica an observer gets access to its state. Replica by itself knows how many active and inactive observers it has.

<img src="images/replica_observers.png">

And the most important concept here is **Replica observer is associated with some UI screen.**

<img src="images/ui_observer.png">
That means:
- When the screen is visible for an user, the Replica has an active observer.
- When the screen is invisible (it is in a backstack or the whole app is in background), the Replica has inactive observer.
- When an user leaves the screen by going back, the observer disconnects from the Replica.

### Automatic behaviour
To replicate data Replica performs a quite complex automatic behaviour:
- Replica loads missing data when an active observer connects.
- Replica keeps track of data staleness.
- Replica refreshes stale data when an active observer is connected.
- Replica cancels network request when a last observer is disconnected.
- Replica clear data when it has no observers for a long time.

> **Note**
> This behaviour is implemented on a public library API so developers can add their own automatic logic.

### Let's summarize
For each chunk of data required from server a client has a corresponding Replica. When a Replica has an active observer it performs automatic data replication. The observed Replica state gets to UI and displayed to an user.

<img src="images/how_replica_works_summarize.png">

## How to use Replica?

### Gradle Setup
First of all add a dependency to a Gradle script. Start with just `replica-core` and add other artifacts later when you need it.
```gradle
dependencies {
    // Basic usage
    implementation 'com.github.aartikov:replica-core:1.0.0-alpha4'

    // Automatic reaction on changes of network connection status
    implementation 'com.github.aartikov:replica-android-network:1.0.0-alpha4'

    // Transforming and combining replicas
    implementation 'com.github.aartikov:replica-algebra:1.0.0-alpha4'
    
    // Integration with Decompose library
    implementation 'com.github.aartikov:replica-decompose:1.0.0-alpha4'
    
    // Debuging tool
    debugImplementation 'com.github.aartikov:replica-devtools:1.0.0-alpha4'
    releaseImplementation 'com.github.aartikov:replica-devtools-noop:1.0.0-alpha4'
}
```

### Create a replica client
 `ReplicaClient` is required to create Replicas. Create it and make it a singleton.

```kotlin

```

## Contact the author
Artur Artikov <a href="mailto:a.artikov@gmail.com">a.artikov@gmail.com</a>

## License
```
The MIT License (MIT)

Copyright (c) 2022 Artur Artikov, Egor Belov
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```