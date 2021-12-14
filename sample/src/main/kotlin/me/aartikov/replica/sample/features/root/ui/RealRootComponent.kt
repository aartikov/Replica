package me.aartikov.replica.sample.features.root.ui

import android.os.Parcelable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import kotlinx.parcelize.Parcelize
import me.aartikov.replica.sample.R
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.core.ui.utils.toComposeState
import me.aartikov.replica.sample.features.menu.createMenuComponent
import me.aartikov.replica.sample.features.menu.ui.MenuComponent
import me.aartikov.replica.sample.features.menu.ui.MenuItem
import me.aartikov.replica.sample.features.message.createMessagesComponent
import me.aartikov.replica.sample.features.project.createProjectComponent
import me.aartikov.sesame.localizedstring.LocalizedString

class RealRootComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory
) : ComponentContext by componentContext, RootComponent {

    private val router = router<ChildConfig, RootComponent.Child>(
        initialConfiguration = ChildConfig.Menu,
        handleBackButton = true,
        childFactory = ::createChild
    )

    override val routerState: RouterState<*, RootComponent.Child>
        by router.state.toComposeState(lifecycle)

    override val title by derivedStateOf {
        getTitle(routerState)
    }

    override val messageComponent = componentFactory.createMessagesComponent(
        childContext(key = "message")
    )

    private fun createChild(config: ChildConfig, componentContext: ComponentContext) =
        when (config) {
            is ChildConfig.Menu -> {
                RootComponent.Child.Menu(
                    componentFactory.createMenuComponent(componentContext, ::onMenuOutput)
                )
            }

            is ChildConfig.Project -> {
                RootComponent.Child.Project(
                    componentFactory.createProjectComponent(componentContext)
                )
            }
        }

    private fun onMenuOutput(output: MenuComponent.Output): Unit = when (output) {
        is MenuComponent.Output.OpenScreen -> when (output.menuItem) {
            MenuItem.Project -> router.push(ChildConfig.Project)
        }
    }

    private fun getTitle(routerState: RouterState<*, RootComponent.Child>): LocalizedString =
        when (routerState.activeChild.instance) {
            is RootComponent.Child.Menu -> LocalizedString.resource(R.string.app_name)
            is RootComponent.Child.Project -> LocalizedString.resource(R.string.project_title)
        }

    private sealed interface ChildConfig : Parcelable {

        @Parcelize
        object Menu : ChildConfig

        @Parcelize
        object Project : ChildConfig
    }
}

