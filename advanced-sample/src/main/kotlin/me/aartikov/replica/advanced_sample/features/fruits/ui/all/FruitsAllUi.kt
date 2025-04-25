package me.aartikov.replica.advanced_sample.features.fruits.ui.all

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.advanced_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.advanced_sample.features.fruits.ui.widget.FruitItem

@Composable
fun FruitsAllUi(
    component: FruitsAllComponent,
    modifier: Modifier = Modifier
) {
    val fruitsState by component.fruitsState.collectAsState()

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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    text = stringResource(R.string.fruits_like_question),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            PullRefreshLceWidget(
                state = fruitsState,
                onRefresh = component::onRefresh,
                onRetryClick = component::onRetryClick
            ) { fruits, refreshing ->
                if (fruits.isNotEmpty()) {
                    FruitsListContent(
                        fruits = fruits,
                        onFruitClick = component::onFruitClick
                    )
                } else {
                    EmptyPlaceholder(
                        modifier = Modifier.navigationBarsPadding(),
                        description = stringResource(R.string.fruits_empty_description)
                    )
                }

                RefreshingProgress(refreshing)
            }
        }
    }
}

@Composable
private fun FruitsListContent(
    fruits: List<Fruit>,
    onFruitClick: (FruitId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(
            items = fruits,
            key = { fruit -> fruit.id.value }
        ) { fruit ->
            FruitItem(
                fruit = fruit,
                onClick = { onFruitClick(fruit.id) }
            )

            if (fruit !== fruits.lastOrNull()) {
                HorizontalDivider()
            }
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Preview
@Composable
private fun FruitsUiPreview() {
    AppTheme {
        FruitsAllUi(FakeFruitsAllComponent())
    }
}