package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import kotlinx.browser.window
import me.aartikov.replica.devtools.client.view_data.ConnectionStatusType
import me.aartikov.replica.devtools.client.view_data.KeyedReplicaViewData
import me.aartikov.replica.devtools.client.view_data.SimpleReplicaViewData
import me.aartikov.replica.devtools.client.view_data.ViewData
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

@Composable
fun Body(viewData: ViewData) {
    var isDarkTheme by remember {
        val localStorageTheme = window.localStorage.getItem(LocalStorageThemeKey)
        mutableStateOf(localStorageTheme == Theme.darkTheme.name)
    }

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
            Content(viewData = viewData) {
                isDarkTheme = !isDarkTheme
            }
        }
    }
}

@Composable
fun Content(viewData: ViewData, onChangeThemeClick: () -> Unit) {
    Container(
        attrs = {
            style {
                width(100.percent)
                height(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Column, FlexWrap.Nowrap)
            }
        }
    ) {
        Container(
            attrs = {
                style {
                    property("flex-grow", 1)
                    display(DisplayStyle.Flex)
                    flexFlow(FlexDirection.Column, FlexWrap.Nowrap)
                    overflowY("scroll")
                }
            }
        ) {
            if (viewData.items.isEmpty()) {
                ContentPlaceholder()
            } else {
                viewData.items.forEach {
                    key(it.id) {
                        when (it) {
                            is SimpleReplicaViewData -> ReplicaItem(item = it)
                            is KeyedReplicaViewData -> KeyedReplicaItem(item = it)
                        }
                    }
                }
            }
        }
        BottomBar(viewData.connectionStatusType, onChangeThemeClick)
    }
}

@Composable
private fun BottomBar(
    connectionStatusType: ConnectionStatusType,
    onChangeThemeClick: () -> Unit
) {
    val localTheme = LocalTheme.current

    Container(
        attrs = {
            style {
                right(0.px)
                left(0.px)
                paddingRight(8.px)
                paddingLeft(8.px)
                display(DisplayStyle.Flex)
                height(24.px)
                flexFlow(FlexDirection.Row, FlexWrap.Nowrap)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
            }
        },
        color = localTheme.bottomBarColor
    ) {
        RText(
            value = connectionStatusType.text,
            backgroundColor = localTheme.bottomBarColor
        )
        ThemedImg(
            src = if (localTheme.isDark) "sun_24_black.png" else "moon_24_black.png",
            attrs = {
                style {
                    width(20.px)
                    height(20.px)
                }
                onClick { onChangeThemeClick() }
            }
        )
    }
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun ContentPlaceholder() {
    RText(
        value = "No replicas",
        attrs = {
            style {
                position(Position.Absolute)
                top(50.percent)
                left(50.percent)
                marginRight((-50).percent)
                transform { translate((-50).percent, (-50).percent) }
                property("font-size", "x-large")
            }
        }
    )
}