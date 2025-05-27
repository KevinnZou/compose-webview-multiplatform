package com.kevinnzou.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.kevinnzou.sample.navigation.HomeTab
import com.kevinnzou.sample.navigation.PersonalTab

/**
 * Created By Kevin Zou On 2023/12/8
 */
@Composable
fun VoyagerNavigationSample(navHostController: NavHostController? = null) {
    TabNavigator(HomeTab) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier =
                        Modifier
                            .background(
                                color = MaterialTheme.colors.primary,
                            ).padding(
                                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                            ),
                    title = { Text(text = "SaveState Sample") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navHostController?.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
            content = {
                CurrentTab()
            },
            bottomBar = {
                BottomNavigation {
                    TabNavigationItem(HomeTab)
                    TabNavigationItem(PersonalTab)
                }
            },
        )
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    BottomNavigationItem(
        selected = tabNavigator.current.key == tab.key,
        onClick = { tabNavigator.current = tab },
        icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
    )
}
