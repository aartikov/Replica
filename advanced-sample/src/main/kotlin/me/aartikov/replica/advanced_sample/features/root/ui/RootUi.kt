package me.aartikov.replica.advanced_sample.features.root.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.aartikov.replica.advanced_sample.core.message.ui.FakeMessageComponent
import me.aartikov.replica.advanced_sample.core.message.ui.MessageUi
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.utils.createFakeChildStackStateFlow
import me.aartikov.replica.advanced_sample.features.dudes.ui.DudesUi
import me.aartikov.replica.advanced_sample.features.fruits.ui.FruitsUi
import me.aartikov.replica.advanced_sample.features.menu.ui.FakeMenuComponent
import me.aartikov.replica.advanced_sample.features.menu.ui.MenuUi
import me.aartikov.replica.advanced_sample.features.pokemons.ui.PokemonsUi
import me.aartikov.replica.advanced_sample.features.project.ui.ProjectUi

@Composable
fun RootUi(
    component: RootComponent,
    modifier: Modifier = Modifier
) {
    SystemBarColors()

    val childStack by component.childStack.collectAsState()

    Children(childStack, modifier) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Menu -> MenuUi(instance.component)
            is RootComponent.Child.Project -> ProjectUi(instance.component)
            is RootComponent.Child.Pokemons -> PokemonsUi(instance.component)
            is RootComponent.Child.Fruits -> FruitsUi(instance.component)
            is RootComponent.Child.Dudes -> DudesUi(instance.component)
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

    override val childStack = createFakeChildStackStateFlow(
        RootComponent.Child.Menu(FakeMenuComponent())
    )

    override val messageComponent = FakeMessageComponent()
}