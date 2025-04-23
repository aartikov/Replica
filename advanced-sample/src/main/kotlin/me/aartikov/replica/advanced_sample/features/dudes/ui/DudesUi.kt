package me.aartikov.replica.advanced_sample.features.dudes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import me.aartikov.replica.advanced_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.features.dudes.domain.Dude
import me.aartikov.replica.advanced_sample.features.dudes.domain.DudesContent
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedLoadingStatus

@Composable
fun DudesUi(
    component: DudesComponent,
    modifier: Modifier = Modifier,
) {
    val dudesState by component.dudesState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 4.dp
            ) {
                Text(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    text = stringResource(R.string.dudes_list_description),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            PullRefreshLceWidget(
                state = dudesState,
                onRefresh = component::onRefresh,
                onRetryClick = component::onRetryClick
            ) { dudes, refreshing ->
                if (dudes.items.isNotEmpty()) {
                    DudesListContent(
                        loadingStatus = dudesState.loadingStatus,
                        dudes = dudes,
                        hasError = dudesState.error != null,
                        onLoadNext = component::onLoadNext
                    )
                } else {
                    EmptyPlaceholder(
                        modifier = Modifier.navigationBarsPadding(),
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
    loadingStatus: PagedLoadingStatus,
    dudes: DudesContent,
    hasError: Boolean,
    onLoadNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    if (dudes.hasNextPage && loadingStatus == PagedLoadingStatus.None) {
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
            key = { it.id.value }
        ) { dude ->
            DudeItem(dude)

            if (dude !== dudes.items.lastOrNull()) {
                HorizontalDivider()
            }
        }

        if (loadingStatus == PagedLoadingStatus.LoadingNextPage) {
            item {
                PagedLoadingProgress()
            }
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun DudeItem(
    dude: Dude,
    modifier: Modifier = Modifier,
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
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
private fun DudesUiPreview() {
    AppTheme {
        DudesUi(FakeDudesComponent())
    }
}

class FakeDudesComponent : DudesComponent {
    override val dudesState = MutableStateFlow(
        Paged(
            loadingStatus = PagedLoadingStatus.LoadingFirstPage,
            data = DudesContent(Dude.FAKE_LIST, hasNextPage = false)
        )
    )

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit

    override fun onLoadNext() = Unit
}