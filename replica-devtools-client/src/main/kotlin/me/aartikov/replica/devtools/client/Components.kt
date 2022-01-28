package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.dom.*

@Composable
fun Container(
    attrs: AttrBuilderContext<*> = {},
    color: CSSColorValue = LocalTheme.current.background,
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
    color: CSSColorValue = LocalTheme.current.onBackground
) {
    Div(
        attrs = {
            style {
                color(color)
                property("text-overflow", "ellipsis")
                overflow("hidden")
            }
            attrs()
        }
    ) {
        Text(value = value)
    }
}

@Composable
fun MaterialIcon(
    name: String,
    attrs: AttrBuilderContext<*> = {},
    color: CSSColorValue = LocalTheme.current.onBackground
) {
    I(attrs = {
        classes("material-icons")
        attrs()
        style { color(color) }
    }
    ) { Text(value = name) }
}

@Composable
private fun NavBarIcon(icon: NavBarIcon) {
    Li {
        A(
            attrs = {
                onClick { icon.onClick() }
            }
        ) {
            MaterialIcon(name = icon.name)
        }
    }
}

class NavBarIcon(
    val name: String,
    val onClick: () -> Unit
)

@Composable
fun Divider(
    attrs: AttrBuilderContext<*> = {}
) {
    Div(
        attrs = {
            classes("divider")
            attrs()
        }
    )
}

@Composable
fun FabButton(
    name: String,
    color: CSSColorValue = LocalTheme.current.primary,
    iconColor: CSSColorValue = LocalTheme.current.onPrimary,
    onClick: (() -> Unit)? = null,
) {
    A(
        attrs = {
            classes("btn-floating", "btn-small", "waves-effect")
            style {
                backgroundColor(color)
            }
            onClick?.let { this.onClick { it() } }
        }
    ) {
        MaterialIcon(name = name, color = iconColor)
    }
}