package me.aartikov.replica.advanced_sample.features.fruits.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.features.fruits.ui.all.FruitsAllUi
import me.aartikov.replica.advanced_sample.features.fruits.ui.favourites.FruitsFavouritesUi

@Composable
fun FruitsUi(
    component: FruitsComponent,
    modifier: Modifier = Modifier
) {
    val stack by component.stack.collectAsState()
    val selectedTab by component.selectedTab.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            FruitsTopBar(
                onTabClick = component::onTabClick,
                selectedTab = selectedTab,
                modifier = Modifier.statusBarsPadding()
            )
        },
        content = { paddingValues ->
            Children(
                stack = stack,
                modifier = Modifier.padding(paddingValues)
            ) {
                when (val instance = it.instance) {
                    is FruitsComponent.Child.All -> FruitsAllUi(instance.component)
                    is FruitsComponent.Child.Favourites -> FruitsFavouritesUi(instance.component)
                }
            }
        }
    )
}

@Composable
private fun FruitsTopBar(
    onTabClick: (FruitsComponent.Tab) -> Unit,
    selectedTab: FruitsComponent.Tab,
    modifier: Modifier
) {
    Row(
        modifier = modifier
    ) {
        FruitsComponent.Tab.entries.forEach { tab ->
            Text(
                text = stringResource(tab.resId),
                style = MaterialTheme.typography.h5,
                color = when (selectedTab == tab) {
                    true -> MaterialTheme.colors.primary
                    false -> Color.Unspecified // Default
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .clickable { onTabClick(tab) }
            )
        }
    }
}

@Preview
@Composable
private fun FruitsUiPreview() {
    AppTheme {
        FruitsUi(FakeFruitsComponent())
    }
}
