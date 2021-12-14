package me.aartikov.replica.sample.features.root.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.Children
import com.arkivanov.decompose.router.RouterState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.utils.createFakeRouterState
import me.aartikov.replica.sample.core.ui.utils.resolve
import me.aartikov.replica.sample.features.menu.ui.FakeMenuComponent
import me.aartikov.replica.sample.features.menu.ui.MenuUi
import me.aartikov.replica.sample.features.message.ui.FakeMessageComponent
import me.aartikov.replica.sample.features.message.ui.MessageUi
import me.aartikov.replica.sample.features.project.ui.ProjectUi
import me.aartikov.sesame.localizedstring.LocalizedString

@Composable
fun RootUi(
    component: RootComponent,
    modifier: Modifier = Modifier
) {

    SystemBarColors()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(component.title.resolve()) }
            )
        }
    ) {
        Content(component.routerState)
    }

    MessageUi(
        component = component.messageComponent,
        modifier = modifier,
        bottomPadding = 16.dp
    )
}

@Composable
private fun SystemBarColors() {
    val systemUiController = rememberSystemUiController()

    val statusBarColor = if (MaterialTheme.colors.isLight) {
        MaterialTheme.colors.primaryVariant
    } else {
        MaterialTheme.colors.surface
    }
    LaunchedEffect(statusBarColor) {
        systemUiController.setStatusBarColor(statusBarColor)
    }

    val navigationBarColor = MaterialTheme.colors.surface
    LaunchedEffect(navigationBarColor) {
        systemUiController.setNavigationBarColor(navigationBarColor)
    }
}

@Composable
private fun Content(routerState: RouterState<*, RootComponent.Child>) {
    Children(routerState) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Menu -> MenuUi(instance.component)
            is RootComponent.Child.Project -> ProjectUi(instance.component)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun RootUiPreview() {
    AppTheme {
        RootUi(FakeRootComponent())
    }
}

class FakeRootComponent : RootComponent {

    override val routerState = createFakeRouterState(
        RootComponent.Child.Menu(FakeMenuComponent())
    )

    override val title = LocalizedString.raw("Replica sample")

    override val messageComponent = FakeMessageComponent()
}