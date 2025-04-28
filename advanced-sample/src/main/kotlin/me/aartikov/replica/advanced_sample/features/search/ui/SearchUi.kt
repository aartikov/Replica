package me.aartikov.replica.advanced_sample.features.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.advanced_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.features.search.domain.WikiSearchItem
import ru.mobileup.kmm_form_validation.control.InputControl
import ru.mobileup.kmm_form_validation.toCompose

@Composable
fun SearchUi(
    component: SearchComponent,
    modifier: Modifier = Modifier,
) {
    val wikiItems by component.wikiSearchItems.collectAsState()
    val debouncedQuery by component.debouncedQuery.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(wikiItems.data) {
        scrollState.animateScrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        SearchTopBar(component.queryInputControl)

        PullRefreshLceWidget(
            state = wikiItems,
            onRetryClick = component::onRetryClick,
            onRefresh = component::onRetryClick
        ) { items, refreshing ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        state = scrollState,
                        enabled = debouncedQuery.isNotBlank()
                    )
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    debouncedQuery.isBlank() -> {
                        EmptyPlaceholder(
                            description = stringResource(R.string.search_query_placeholder)
                        )
                    }

                    items.isEmpty() && !wikiItems.loading && !refreshing -> {
                        EmptyPlaceholder(
                            description = stringResource(
                                R.string.no_results_placeholder,
                                debouncedQuery
                            )
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(vertical = 16.dp),
                        ) {
                            items.forEach {
                                WikiItem(wikiItem = it, onClick = { component.onItemClick(it) })

                                if (it !== items.lastOrNull()) HorizontalDivider()
                            }
                        }
                    }
                }
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
