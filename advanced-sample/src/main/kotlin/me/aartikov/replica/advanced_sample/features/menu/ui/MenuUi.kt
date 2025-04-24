package me.aartikov.replica.advanced_sample.features.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

            Box(modifier = Modifier.weight(1.0f)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(IntrinsicSize.Max)
                ) {
                    MenuButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.project_title),
                        onClick = { component.onMenuItemClick(MenuItem.Project) }
                    )

                    MenuButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.pokemons_title),
                        onClick = { component.onMenuItemClick(MenuItem.Pokemons) }
                    )

                    MenuButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.fruits_title),
                        onClick = { component.onMenuItemClick(MenuItem.Fruits) }
                    )

                    MenuButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.dudes_title),
                        onClick = { component.onMenuItemClick(MenuItem.Dudes) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MenuUiPreview() {
    AppTheme {
        MenuUi(FakeMenuComponent())
    }
}
