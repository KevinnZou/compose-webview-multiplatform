# Desktop Setup

Starting from version 1.3.0, we switched from JavaFX to the [Java CEF Browser](https://github.com/chromiumembedded/java-cef) for better performance and user experience.
After importing the library, some configurations need to be done before running the desktop app.

## Initialization

Take a look at the KCEF Compose documentation here: [DatL4g/KCEF](https://github.com/DatL4g/KCEF/blob/master/COMPOSE.md)

Please use the following example as a reference.

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir = File("jcef-bundle")
                    progress {
                        onDownloading {
                            downloading = max(it, 0F)
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                    settings {
                        cachePath = File("cache").absolutePath
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
                KCEF.dispose()
            }
        }
    }
}
```

## Flags

Make sure to include platform-required Flags to your compose configuration: [DatL4g/KCEF](https://github.com/DatL4g/KCEF/blob/master/COMPOSE.md#flags)