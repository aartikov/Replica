package me.aartikov.replica.sample.features.menu.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.sample.R
import me.aartikov.replica.sample.core.ui.theme.AppTheme

@Composable
fun MenuUi(
    component: MenuComponent,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(modifier = Modifier.padding(32.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(IntrinsicSize.Max)
            ) {
                MenuButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.project_title),
                    onClick = { component.onMenuItemClick(MenuItem.Project) }
                )
            }
        }
    }
}

@Preview
@Composable
fun MenuUiPreview() {
    AppTheme {
        MenuUi(FakeMenuComponent())
    }
}

class FakeMenuComponent : MenuComponent {
    override fun onMenuItemClick(item: MenuItem) = Unit
}