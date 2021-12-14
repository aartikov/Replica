package me.aartikov.replica.sample.features.root.ui

import com.arkivanov.decompose.router.RouterState
import me.aartikov.replica.sample.features.menu.ui.MenuComponent
import me.aartikov.replica.sample.features.message.ui.MessageComponent
import me.aartikov.replica.sample.features.profile.ui.ProfileComponent
import me.aartikov.sesame.localizedstring.LocalizedString

interface RootComponent {

    val routerState: RouterState<*, Child>

    val title: LocalizedString

    val messageComponent: MessageComponent

    sealed interface Child {
        class Menu(val component: MenuComponent) : Child
        class Profile(val component: ProfileComponent) : Child
    }
}