# Desktop Setup
Starting from version 1.3.0, we switched from JavaFX to the [Java CEF Browser](https://github.com/chromiumembedded/java-cef) for better performance and user experience.
After importing the library, some configurations need to be done before running the desktop app.

## Initialization
The first thing you need to do is to initialize the Java CEF Module. 
This can (should) be called in the app init process and only needs to be called once.
```kotlin
LaunchedEffect(Unit) {
    Cef.init(builder = {
            installDir = File("jcef-bundle")
    },{})
}
```

Then, run your app once and it will download the JCEF Bundle to the install directory. 
After that, just restart your app and it will work properly.

## Disposal
When the app is closing, you need to dispose of the CEF Module.
```kotlin
DisposableEffect(Unit) {
    onDispose {
        Cef.dispose()
    }
}
```

> **Note**
> Make sure to exclude the `installDir` from upstreaming by adding it to the `.gitignore`.
> The downloaded files are platform-specific and therefore differ to each user.

Please use the following example as a reference.
```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                Cef.init(builder = {
                    installDir = File("jcef-bundle")
                    settings {
                        cachePath = File("cache").absolutePath
                    }
                }, initProgress = {
                    onDownloading {
                        downloading = max(it, 0F)
                    }
                    onInitialized {
                        initialized = true
                    }
                }, onError = {
                    it.printStackTrace()
                }, onRestartRequired = {
                    restartRequired = true
                })
            }
        }

        if (restartRequired) {
            Text(text = "Restart required.")
        } else {
            if (initialized) {
                MainWebView()
            } else {
                Text(text = "Downloading $downloading%")
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                Cef.dispose()
            }
        }
    }
}
```

## Flags
Some platforms require the addition of specific flags.
To use on MacOSX, add the following JVM flags:
```shell
--add-opens java.desktop/sun.awt=ALL-UNNAMED
--add-opens java.desktop/sun.lwawt=ALL-UNNAMED
--add-opens java.desktop/sun.lwawt.macosx=ALL-UNNAMED
```
For gradle project, you can configure it in the build.gradle.kts like that:
```kotlin
afterEvaluate {
    tasks.withType<JavaExec> {
        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}
```
For packaging purposes, you also have to add this to build.gradle.kts:
```kotlin
compose.desktop {
    application {

        nativeDistributions {
            // ....
            includeAllModules = true
        }
    }
}
```
Then you are free to run your desktop applications.