package me.aartikov.replica.advanced_sample.features.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.core.theme.AppTheme

@Composable
fun MenuUi(
    component: MenuComponent,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .width(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MenuItem.entries.forEach {
                    MenuButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = it.displayName,
                        onClick = { component.onMenuItemClick(it) }
                    )
                }
            }
        }
    }
}

private val MenuItem.displayName: String
    @Composable
    get() = when (this) {
        MenuItem.Project -> R.string.project_title
        MenuItem.Pokemons -> R.string.pokemons_title
        MenuItem.Fruits -> R.string.fruits_title
        MenuItem.Dudes -> R.string.dudes_title
        MenuItem.Search -> R.string.search_title
    }.let { stringResource(it) }

@Preview
@Composable
private fun MenuUiPreview() {
    AppTheme {
        MenuUi(FakeMenuComponent())
    }
}
