package com.kevinnzou.sample.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

/**
 * Created By Kevin Zou On 2023/12/8
 */
object PersonalTab : Tab {
    @Composable
    override fun Content() {
        var count by rememberSaveable { mutableStateOf(0) }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                Text(text = "Count $count", fontSize = 30.sp)
                Button(onClick = { count++ }) {
                    Text(text = "Count Up")
                }
            }
        }
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "个人"
            val icon = rememberVectorPainter(Icons.Default.Person)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon,
                )
            }
        }
}
