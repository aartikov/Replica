package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
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
    Div(
        attrs = {
            style {
                left(0.px)
                right(0.px)
                height(1.px)
                backgroundColor(localTheme.dividerColor)
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
                if (localTheme.isDark) filter { invert(100.percent) }
            }
        }
    )
}