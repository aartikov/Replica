package me.aartikov.replica.advanced_sample.features.project.ui

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.features.project.domain.Project
import me.aartikov.replica.single.Loadable

class FakeProjectComponent : ProjectComponent {

    override val projectState = MutableStateFlow(
        Loadable(
            loading = true,
            data = Project(
                name = "Replica",
                url = "https://github.com/aartikov/Replica",
                starsCount = 605,
                forksCount = 5,
                subscribersCount = 15
            )
        )
    )

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit

    override fun onUrlClick(url: String) = Unit
}
