package me.aartikov.replica.advanced_sample.features.project.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.features.project.domain.Project

@Composable
fun ProjectUi(
    component: ProjectComponent,
    modifier: Modifier = Modifier,
) {
    val projectState by component.projectState.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullRefreshLceWidget(
            state = projectState,
            onRefresh = component::onRefresh,
            onRetryClick = component::onRetryClick,
            contentWindowInsets = WindowInsets.systemBars
        ) { project, refreshing, paddingValues ->
            ProjectContent(
                project = project,
                onUrlClick = component::onUrlClick,
                contentPadding = paddingValues
            )

            RefreshingProgress(
                modifier = Modifier.padding(top = 4.dp),
                active = refreshing
            )
        }
    }
}

@Composable
private fun ProjectContent(
    project: Project,
    onUrlClick: (url: String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 32.dp),
            textAlign = TextAlign.Center,
            text = project.name,
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            modifier = Modifier
                .clickable { onUrlClick(project.url) }
                .padding(2.dp),
            textAlign = TextAlign.Center,
            text = project.url,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = TextDecoration.Underline
            )
        )

        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "⭐ ${project.starsCount}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "🍴 ${project.forksCount}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "👀 ${project.subscribersCount}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ProjectUiPreview() {
    AppTheme {
        ProjectUi(FakeProjectComponent())
    }
}
