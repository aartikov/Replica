package me.aartikov.replica.advanced_sample.features.root.ui

import me.aartikov.replica.advanced_sample.core.message.ui.FakeMessageComponent
import me.aartikov.replica.advanced_sample.core.utils.createFakeChildStackStateFlow
import me.aartikov.replica.advanced_sample.features.menu.ui.FakeMenuComponent

class FakeRootComponent : RootComponent {

    override val childStack = createFakeChildStackStateFlow(
        RootComponent.Child.Menu(FakeMenuComponent())
    )

    override val messageComponent = FakeMessageComponent()
}
