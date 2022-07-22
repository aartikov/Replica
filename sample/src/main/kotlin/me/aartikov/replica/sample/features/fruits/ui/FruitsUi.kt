package me.aartikov.replica.sample.features.fruits.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import me.aartikov.replica.sample.R
import me.aartikov.replica.sample.core.theme.AppTheme
import me.aartikov.replica.sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.sample.core.widget.RefreshingProgress
import me.aartikov.replica.sample.core.widget.SwipeRefreshLceWidget
import me.aartikov.replica.sample.features.fruits.domain.Fruit
import me.aartikov.replica.sample.features.fruits.domain.FruitId
import me.aartikov.replica.single.Loadable

@Composable
fun FruitsUi(
    component: FruitsComponent,
    modifier: Modifier = Modifier
) {
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
                state = component.fruitsState,
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
            key = { fruit -> fruit.id }
        ) { fruit ->
            FruitItem(
                fruit = fruit,
                onClick = { onFruitClick(fruit.id) }
            )

            if (fruit !== fruits.lastOrNull()) {
                Divider()
            }
        }
    }
}

@Composable
private fun FruitItem(
    fruit: Fruit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            contentDescription = null,
            model = ImageRequest.Builder(LocalContext.current)
                .data(fruit.imageUrl)
                .apply {
                    with(LocalDensity.current) {
                        size(42.dp.roundToPx())
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
            text = fruit.name,
            style = MaterialTheme.typography.body1
        )

        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(
                if (fruit.liked) R.drawable.ic_liked else R.drawable.ic_unliked
            ),
            contentDescription = null
        )
    }
}

@Preview
@Composable
fun FruitsUiPreview() {
    AppTheme {
        FruitsUi(FakeFruitsComponent())
    }
}

class FakeFruitsComponent : FruitsComponent {
    override val fruitsState: Loadable<List<Fruit>> = Loadable(
        loading = true,
        data = listOf(
            Fruit(
                id = FruitId("1"),
                name = "Banana",
                imageUrl = "",
                liked = false
            ),
            Fruit(
                id = FruitId("2"),
                name = "Orange",
                imageUrl = "",
                liked = true
            ),
            Fruit(
                id = FruitId("3"),
                name = "Mango",
                imageUrl = "",
                liked = false
            )
        )
    )

    override fun onFruitClick(fruitId: FruitId) = Unit

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit
}
