package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLImageElement

@Composable
fun Container(
    attrs: AttrBuilderContext<*> = {},
    color: CSSColorValue = LocalTheme.current.backgroundColor,
    content: @Composable () -> Unit
) {
    Div(attrs = {
        attrs()
        style { backgroundColor(color) }
    }) { content() }
}

@Composable
fun RText(
    value: String,
    attrs: AttrBuilderContext<*> = {},
    color: CSSColorValue = LocalTheme.current.textColor,
    backgroundColor: CSSColorValue = LocalTheme.current.backgroundColor
) {
    Container(
        attrs = {
            style {
                fontFamily("Fira Sans", "sans-serif")
                color(color)
                fontSize(0.9.em)
                property("text-overflow", "ellipsis")
                overflow("hidden")
            }
            attrs()
        },
        color = backgroundColor
    ) {
        Text(value = value)
    }
}

@Composable
fun Divider(
    attrs: AttrBuilderContext<*> = {}
) {
    val localTheme = LocalTheme.current
    Hr(
        attrs = {
            style {
                margin(0.px)
                color(localTheme.dividerColor)
            }
            attrs()
        }
    )
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun ThemedImg(
    src: String,
    attrs: AttrBuilderContext<HTMLImageElement> = {},
) {
    val localTheme = LocalTheme.current
    Img(
        src = src,
        attrs = {
            attrs()
            style {
                if (localTheme.isDark) {
                    filter {
                        invert(100.percent)
                        brightness(80.percent)
                    }
                }
            }
        }
    )
}