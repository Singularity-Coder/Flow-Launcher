![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/logo192.png)
# Flow-Launcher
Stay in the flow and achieve more!

## Concept
Core idea is iOS style work, home, etc profiles with only related apps visible in that profile. Apps, Clock, Profile name will be in the center screen. On right swipe we get weather, news headlines, etc scraped from top sources. On left swipe you will have 4 or 5 pre launched apps in foreground ready to use - like My Mind for notes, Remind Me for reminders, etc. Just like widgets but in reality just apps that never get killed until device gets killed. Not sure how to avoid an app from getting killed from task switcher though.

## Screenshots
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss1.5.png)
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss2.5.png)
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss3.5.png)
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss4.5.png)
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss5.png)
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss6.5.png)
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss7.5.png)
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/ss8.png)

## Tech stack & Open-source libraries
- Minimum SDK level 21
-  [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
  - DataBinding: Binds UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
  - Room: Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
  - [Hilt](https://dagger.dev/hilt/): for dependency injection.
  - WorkManager: WorkManager allows you to schedule work to run one-time or repeatedly using flexible scheduling windows.
- Architecture
  - MVVM Architecture (View - DataBinding - ViewModel - Model)
  - Repository Pattern
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit): Construct the REST APIs and paging network data.
- [gson](https://github.com/google/gson): A Java serialization/deserialization library to convert Java Objects into JSON and back.
- [Material-Components](https://github.com/material-components/material-components-android): Material design components for building ripple animation, and CardView.
- [Coil](https://github.com/coil-kt/coil): Image loading for Android and Compose Multiplatform.
- [Lottie](https://github.com/airbnb/lottie-android): Render After Effects animations natively on Android and iOS, Web, and React Native.
- [jsoup](https://mvnrepository.com/artifact/org.jsoup/jsoup): jsoup is a Java library that simplifies working with real-world HTML and XML.
- [zxing](https://github.com/zxing/zxing): ZXing ("Zebra Crossing") barcode scanning library for Java, Android.
- [zxing-android-embedded](https://github.com/journeyapps/zxing-android-embedded): Barcode scanner library for Android, based on the ZXing decoder.
- [Browser](https://developer.android.com/jetpack/androidx/releases/browser): Custom Chrome Tab.
- [Youtube](https://developers.google.com/youtube/v3): With the YouTube Data API, you can add a variety of YouTube features to your application.

## Architecture
![alt text](https://github.com/Singularity-Coder/Flow-Launcher/blob/main/assets/arch.png)

This App is based on the MVVM architecture and the Repository pattern, which follows the [Google's official architecture guidance](https://developer.android.com/topic/architecture).

The overall architecture of this App is composed of two layers; the UI layer and the data layer. Each layer has dedicated components and they have each different responsibilities.