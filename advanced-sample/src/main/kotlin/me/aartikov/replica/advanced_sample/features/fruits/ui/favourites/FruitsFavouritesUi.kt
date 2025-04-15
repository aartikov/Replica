package me.aartikov.replica.advanced_sample.features.fruits.ui.favourites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.core.widget.SwipeRefreshLceWidget
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit
import me.aartikov.replica.advanced_sample.features.fruits.domain.FruitId
import me.aartikov.replica.advanced_sample.features.fruits.ui.widget.FruitItem

@Composable
fun FruitsFavouritesUi(
    component: FruitsFavouritesComponent,
    modifier: Modifier = Modifier
) {
    val fruitsState by component.fruitsState.collectAsState()
    val removingInProgress by component.removingInProgress.collectAsState()

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
                    text = stringResource(R.string.fruits_like_question),
                    style = MaterialTheme.typography.h6
                )
            }

            SwipeRefreshLceWidget(
                state = fruitsState,
                onRefresh = component::onRefresh,
                onRetryClick = component::onRetryClick
            ) { fruits, refreshing ->
                if (fruits.isNotEmpty()) {
                    FruitsListContent(
                        fruits = fruits,
                        removingInProgress = removingInProgress,
                        onFruitClick = component::onRemoveFruitClick
                    )
                } else {
                    EmptyPlaceholder(
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
    removingInProgress: Set<FruitId>,
    onFruitClick: (FruitId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(
            items = fruits,
            key = { fruit -> fruit.id }
        ) { fruit ->
            FruitItem(
                fruit = fruit,
                isEnabled = fruit.id !in removingInProgress,
                onClick = { onFruitClick(fruit.id) }
            )

            if (fruit !== fruits.lastOrNull()) {
                Divider()
            }
        }
    }
}

@Preview
@Composable
fun FruitsUiPreview() {
    AppTheme {
        FruitsFavouritesUi(FakeFruitsFavouritesComponent())
    }
}