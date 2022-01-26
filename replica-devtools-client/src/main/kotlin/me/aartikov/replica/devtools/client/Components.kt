package me.aartikov.replica.devtools.client

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun Card(attrs: AttrBuilderContext<*> = {}, content: @Composable () -> Unit) {
    Div(
        attrs = {
            classes("card")
            attrs()
        }
    ) {
        content()
    }
}

@Composable
fun ImageButton(
    onClick: (() -> Unit?)? = null,
    iconName: String,
    attrs: AttrBuilderContext<*> = {}
) {
    A(
        attrs = {
            classes("waves-effect", "waves-teal", "btn-flat")
            style {
                width(48.px)
                height(48.px)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
            }
            onClick?.let { this.onClick { it() } }
            attrs()
        }
    ) {
        MaterialIcon(name = iconName)
    }
}

@Composable
fun MaterialIcon(
    name: String,
    attrs: AttrBuilderContext<*> = {}
) {
    I(attrs = {
        classes("material-icons")
        attrs()
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