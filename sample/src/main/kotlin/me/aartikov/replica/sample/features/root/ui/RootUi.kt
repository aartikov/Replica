package me.aartikov.replica.sample.features.root.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.Children
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.utils.createFakeRouterState
import me.aartikov.replica.sample.features.fruits.ui.FruitsUi
import me.aartikov.replica.sample.features.menu.ui.FakeMenuComponent
import me.aartikov.replica.sample.features.menu.ui.MenuUi
import me.aartikov.replica.sample.features.message.ui.FakeMessageComponent
import me.aartikov.replica.sample.features.message.ui.MessageUi
import me.aartikov.replica.sample.features.pokemons.ui.PokemonsUi
import me.aartikov.replica.sample.features.project.ui.ProjectUi

@Composable
fun RootUi(
    component: RootComponent,
    modifier: Modifier = Modifier
) {

    SystemBarColors()

    Children(component.routerState) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Menu -> MenuUi(instance.component, modifier)
            is RootComponent.Child.Project -> ProjectUi(instance.component, modifier)
            is RootComponent.Child.Pokemons -> PokemonsUi(instance.component, modifier)
            is RootComponent.Child.Fruits -> FruitsUi(instance.component, modifier)
        }
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

    val statusBarColor = MaterialTheme.colors.surface
    LaunchedEffect(statusBarColor) {
        systemUiController.setStatusBarColor(statusBarColor)
    }

    val navigationBarColor = MaterialTheme.colors.surface
    LaunchedEffect(navigationBarColor) {
        systemUiController.setNavigationBarColor(navigationBarColor)
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

    override val messageComponent = FakeMessageComponent()
}