# Desktop Setup

Starting from version 1.3.0, we switched from JavaFX to the [Java CEF Browser](https://github.com/chromiumembedded/java-cef) for better performance and user experience.

Starting from version 1.7.0, we switched from Java CEF Browser to [KCEF](https://github.com/DatL4g/KCEF/tree/master) for more features and better performance.

If you are using a version before 1.7.0, please refer to the [old documentation](https://github.com/KevinnZou/compose-webview-multiplatform/blob/1.6.0/README.desktop.md).

After importing the library, some configurations need to be done before running the desktop app.

## Initialization

> [!NOTE]  
> Starting from version 1.9.40, KCEF v2024.04.20.4 is used and it is possible to load JCEF directly from bundled binary.
>
>  So if you build your app with the JetBrains Runtime JDK, it's no longer required to download the packages.

Take a look at the KCEF Compose documentation here: [DatL4g/KCEF](https://github.com/DatL4g/KCEF/blob/master/COMPOSE.md)

Please use the following example as a reference.

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }
        val download: Download = remember { Builder().github().build() }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    
                    /*
                      Add this code when using JDK 17.
                      Builder().github {
                          release("jbr-release-17.0.10b1087.23")
                      }.buffer(download.bufferSize).build()
                     */
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
                KCEF.disposeBlocking()
            }
        }
    }
}
```

## Flags

Make sure to include platform-required Flags to your compose configuration: [DatL4g/KCEF](https://github.com/DatL4g/KCEF/blob/master/COMPOSE.md#flags)
```kotlin
compose.desktop {
  application {
    // all your other configuration, etc

    jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED") // recommended but not necessary

    if (System.getProperty("os.name").contains("Mac")) {
      jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
      jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
    }
  }
}
```
## ProGuard

Make sure to add ProGuard rules in order for KCEF to work in required build types: [DatL4g/KCEF](https://github.com/DatL4g/KCEF/blob/master/COMPOSE.md#flags)

Simply add a ProGuard configuration file to your target platform, for example `compose-desktop.pro` and add the following lines:

```
-keep class org.cef.** { *; }
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory
```

Then make sure to use this config by adding the following lines to your `build.gradle.kts`:

```kotlin
compose.desktop {
  application {
    // all your other configuration, etc

    buildTypes.release.proguard {
      configurationFiles.from("compose-desktop.pro")
    }
  }
}
```

## Dependencies
This library requires Jogamp's Maven to be added to your project's repositories.

```kotlin
repositories {
    maven("https://jogamp.org/deployment/maven")
}
```
