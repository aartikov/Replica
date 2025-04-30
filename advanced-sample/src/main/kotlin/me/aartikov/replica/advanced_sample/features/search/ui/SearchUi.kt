package me.aartikov.replica.advanced_sample.features.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.utils.plus
import me.aartikov.replica.advanced_sample.core.widget.ContentOrPlaceholder
import me.aartikov.replica.advanced_sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.advanced_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchItem
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchResult
import ru.mobileup.kmm_form_validation.control.InputControl
import ru.mobileup.kmm_form_validation.toCompose

@Composable
fun SearchUi(
    component: SearchComponent,
    modifier: Modifier = Modifier,
) {
    val wikiSearchResult by component.wikiSearchResultState.collectAsState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(wikiSearchResult.data) {
        lazyListState.animateScrollToItem(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        SearchTopBar(component.queryInputControl)

        PullRefreshLceWidget(
            state = wikiSearchResult,
            onRetryClick = component::onRefresh,
            onRefresh = component::onRefresh,
        ) { result, refreshing, paddingValues ->
            ContentOrPlaceholder(
                items = result.items,
                placeholder = {
                    when {
                        result.query.isBlank() -> {
                            EmptyPlaceholder(
                                modifier = Modifier.padding(paddingValues),
                                description = stringResource(R.string.search_query_placeholder)
                            )
                        }

                        result.items.isEmpty() -> {
                            EmptyPlaceholder(
                                modifier = Modifier.padding(paddingValues),
                                description = stringResource(
                                    R.string.no_results_placeholder,
                                    result.query
                                )
                            )
                        }
                    }
                }
            ) {
                WikiSearchList(
                    result = result,
                    lazyListState = lazyListState,
                    contentPadding = paddingValues,
                    onItemClick = component::onItemClick
                )
            }

            RefreshingProgress(refreshing)
        }
    }
}

@Composable
private fun SearchTopBar(
    queryInputControl: InputControl,
    modifier: Modifier = Modifier,
) {
    val query by queryInputControl.value.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                text = stringResource(R.string.search_top_bar_title),
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                value = query,
                onValueChange = { queryInputControl.onValueChange(it) },
                keyboardOptions = queryInputControl.keyboardOptions.toCompose(),
                maxLines = 1,
                label = { Text(stringResource(R.string.search_title)) }
            )
        }
    }
}

@Composable
private fun BoxScope.WikiSearchList(
    result: WikiSearchResult,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    onItemClick: (WikiSearchItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = contentPadding + PaddingValues(vertical = 16.dp),
        userScrollEnabled = result.query.isNotBlank()
    ) {
        items(items = result.items, key = { it.url }) {
            Column(
                Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
            ) {
                WikiItem(wikiItem = it, onClick = { onItemClick(it) })

                if (it !== result.items.lastOrNull()) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun WikiItem(
    wikiItem: WikiSearchItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = wikiItem.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (wikiItem.description.isNotBlank()) {
            Text(
                text = wikiItem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = wikiItem.url,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchUiPreview() {
    AppTheme {
        SearchUi(FakeSearchComponent())
    }
}
