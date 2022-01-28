package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import me.aartikov.replica.devtools.client.view_data.KeyedReplicaViewData
import me.aartikov.replica.devtools.client.view_data.SimpleReplicaViewData
import me.aartikov.replica.devtools.client.view_data.ViewData
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Ul

@Composable
fun Body(viewData: ViewData) {
    var isDarkTheme by remember { mutableStateOf(false) }

    Theme(isDarkTheme) {
        Container(
            attrs = {
                style {
                    position(Position.Absolute)
                    width(100.percent)
                    height(100.percent)
                    top(0.px)
                    bottom(0.px)
                    left(0.px)
                    right(0.px)
                    property("margin", auto)
                }
            }
        ) {
            Container(
                attrs = {
                    style {
                        width(100.percent)
                        height(100.percent)
                        position(Position.Absolute)
                        top(0.px)
                        left(0.px)
                    }
                }
            ) {
                Content(viewData = viewData) {
                    isDarkTheme = !isDarkTheme
                }
            }
        }
    }
}

@Composable
fun Content(viewData: ViewData, onChangeThemeClick: () -> Unit) {
    val localTheme = LocalTheme.current

    Container(
        attrs = {
            style {
                position(Position.Fixed)
                bottom(10.px)
                right(10.px)
                color(localTheme.primary)
                property("z-index", 999)
            }
        }
    ) {
        FabButton(if (localTheme.isDark) "dark_mode" else "light_mode") { onChangeThemeClick() }
    }
    Container(
        attrs = {
            style {
                width(100.percent)
                height(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Column, FlexWrap.Nowrap)
                overflowY("scroll")
            }
        }
    ) {
        Ul(
            attrs = {
                style {
                    width(100.percent)
                    margin(0.px)
                }
            }
        ) {
            viewData.items.forEach {
                when (it) {
                    is SimpleReplicaViewData -> ReplicaItem(item = it)
                    is KeyedReplicaViewData -> KeyedReplicaItem(item = it)
                }
            }
        }
    }
}