package me.aartikov.replica.advanced_sample.features.dudes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.utils.OnEndReached
import me.aartikov.replica.advanced_sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.advanced_sample.core.widget.PagedLoadingProgress
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.core.widget.SwipeRefreshLceWidget
import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesPage
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedLoadingStatus

@Composable
fun DudesUi(
    component: DudesComponent,
    modifier: Modifier = Modifier
) {
    val dudesState by component.dudesState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colors.background,
                elevation = 4.dp
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    text = stringResource(R.string.dudes_list_description),
                    style = MaterialTheme.typography.h6
                )
            }

            SwipeRefreshLceWidget(
                state = dudesState,
                onRefresh = component::onRefresh,
                onRetryClick = component::onRetryClick
            ) { dudes, refreshing ->
                if (dudes.items.isNotEmpty()) {
                    DudesListContent(
                        loading = dudesState.loadingStatus,
                        dudes = dudes,
                        hasError = dudesState.error != null,
                        onLoadNext = component::onLoadNext
                    )
                } else {
                    EmptyPlaceholder(
                        description = stringResource(R.string.dudes_empty_description)
                    )
                }

                RefreshingProgress(refreshing)
            }
        }
    }
}

@Composable
private fun DudesListContent(
    loading: PagedLoadingStatus,
    dudes: PagedData<Dude, Page<Dude>>,
    hasError: Boolean,
    onLoadNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    if (dudes.hasNextPage && loading == PagedLoadingStatus.None) {
        lazyListState.OnEndReached(
            callback = onLoadNext,
            itemCountGap = 3,
            scrollingToEndRequired = hasError
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(
            items = dudes.items,
            key = { it.id }
        ) { dude ->
            DudeItem(dude)

            if (dude !== dudes.items.lastOrNull()) {
                Divider()
            }
        }

        if (loading == PagedLoadingStatus.LoadingNextPage) {
            item {
                PagedLoadingProgress()
            }
        }
    }
}

@Composable
private fun DudeItem(
    dude: Dude,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            contentDescription = null,
            model = ImageRequest.Builder(LocalContext.current)
                .data(dude.photoUrl)
                .apply {
                    with(LocalDensity.current) {
                        size(42.dp.roundToPx())
                        transformations(RoundedCornersTransformation(8.dp.toPx()))
                    }
                }
                .crossfade(true)
                .build(),
            modifier = Modifier.size(42.dp)
        )

        Text(
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = 16.dp),
            text = dude.name,
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview
@Composable
fun DudesUiPreview() {
    AppTheme {
        DudesUi(FakeDudesComponent())
    }
}

class FakeDudesComponent : DudesComponent {
    override val dudesState = MutableStateFlow(
        Paged(
            loadingStatus = PagedLoadingStatus.LoadingFirstPage,
            data = PagedData(
                pages = listOf(
                    DudesPage(
                        items = Dude.FAKE_LIST,
                        nextPageCursor = "12345"
                    )
                )
            )
        )
    )

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit

    override fun onLoadNext() = Unit
}