package com.kevinnzou.sample.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter

/**
 * Created By Kevin Zou On 2023/12/12
 */

@Composable
fun CustomNavigationSample() {
    CustomNavigationScreen()
}

@Composable
fun <T : Any> Navigation(
    currentScreen: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    // create SaveableStateHolder.
    val saveableStateHolder = rememberSaveableStateHolder()
    Box(modifier) {
        // Wrap the content representing the `currentScreen` inside `SaveableStateProvider`.
        // Here you can also add a screen switch animation like Crossfade where during the
        // animation multiple screens will be displayed at the same time.
        saveableStateHolder.SaveableStateProvider(currentScreen) {
            content(currentScreen)
        }
    }
}

@Composable
fun CustomNavigationScreen() {
    var screen by rememberSaveable { mutableStateOf("screen1") }
    Scaffold(
        content = {
            Navigation(screen, Modifier.fillMaxSize()) { currentScreen ->
                if (currentScreen == "screen1") {
                    Home()
                } else {
                    Personal()
                }
            }
        },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    selected = screen == "screen1",
                    onClick = { screen = "screen1" },
                    icon = {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Home),
                            contentDescription = "Home",
                        )
                    },
                )
                BottomNavigationItem(
                    selected = screen == "screen2",
                    onClick = { screen = "screen2" },
                    icon = {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Person),
                            contentDescription = "Personal",
                        )
                    },
                )
            }
        },
    )
}
