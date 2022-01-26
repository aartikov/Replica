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
fun MaterialIcon(name: String) {
    I(attrs = { classes("material-icons") }) { Text(value = name) }
}

@Composable
fun NavBar(
    title: String,
    navigationIcon: NavBarIcon? = null
) {
    Nav {
        Div(attrs = { classes("nav-wrapper") }) {
            if (navigationIcon != null) {
                Ul(attrs = { classes("left") }) {
                    NavBarIcon(icon = navigationIcon)
                }
            }

            A(
                attrs = {
                    classes("brand-logo")
                    style {
                        paddingLeft(16.px)
                    }
                }
            ) {
                Text(value = title)
            }
        }
    }
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
fun Divider() {
    Div(
        attrs = {
            classes("divider")
            style {
                height(1.px)
                width(100.percent)
            }
        }
    )
}