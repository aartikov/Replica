package me.aartikov.replica.advanced_sample.features.project.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.core.widget.SwipeRefreshLceWidget
import me.aartikov.replica.advanced_sample.features.project.domain.Project
import me.aartikov.replica.single.Loadable

@Composable
fun ProjectUi(
    component: ProjectComponent,
    modifier: Modifier = Modifier
) {
    val projectState by component.projectState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        SwipeRefreshLceWidget(
            state = projectState,
            onRefresh = component::onRefresh,
            onRetryClick = component::onRetryClick
        ) { project, refreshing ->
            ProjectContent(
                project = project,
                onUrlClick = component::onUrlClick,
                onTestClick = component::onTestClick
            )
            RefreshingProgress(
                refreshing,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProjectContent(
    project: Project,
    onUrlClick: (url: String) -> Unit,
    onTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 32.dp),
            textAlign = TextAlign.Center,
            text = project.name,
            style = MaterialTheme.typography.h5,
        )

        Text(
            modifier = Modifier
                .clickable { onUrlClick(project.url) }
                .padding(2.dp),
            textAlign = TextAlign.Center,
            text = project.url,
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.body1.copy(
                textDecoration = TextDecoration.Underline
            )
        )

        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "⭐ ${project.starsCount}",
                style = MaterialTheme.typography.h5
            )
            Text(
                text = "🍴 ${project.forksCount}",
                style = MaterialTheme.typography.h5
            )
            Text(
                text = "👀 ${project.subscribersCount}",
                style = MaterialTheme.typography.h5
            )
        }

        Button(
            modifier = Modifier.padding(top = 48.dp),
            onClick = onTestClick
        ) {
            Text(
                text = "TEST",
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ProjectUiPreview() {
    AppTheme {
        ProjectUi(FakeProjectComponent())
    }
}


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

    override fun onTestClick() = Unit
}
