package me.aartikov.replica.sample.features.dudes.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import me.aartikov.replica.sample.R
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.widget.EmptyPlaceholder
import me.aartikov.replica.sample.core.ui.widget.RefreshingProgress
import me.aartikov.replica.sample.core.ui.widget.SwipeRefreshLceWidget
import me.aartikov.replica.sample.features.dudes.domain.Dude
import me.aartikov.replica.sample.features.dudes.domain.DudeId
import me.aartikov.replica.single.Loadable

@Composable
fun DudesUi(
    component: DudesComponent,
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
                    text = stringResource(R.string.dudes_list_description),
                    style = MaterialTheme.typography.h6
                )
            }

            SwipeRefreshLceWidget(
                state = component.dudesState,
                onRefresh = component::onRefresh,
                onRetryClick = component::onRetryClick
            ) { dudes, refreshing ->
                if (dudes.isNotEmpty()) {
                    DudesListContent(dudes)
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
    dudes: List<Dude>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(
            items = dudes,
            key = { it.id }
        ) { dude ->
            DudeItem(dude)

            if (dude !== dudes.lastOrNull()) {
                Divider()
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
        Image(
            modifier = Modifier.size(42.dp),
            painter = rememberImagePainter(dude.photoUrl) {
                crossfade(true)
                with(LocalDensity.current) {
                    size(42.dp.roundToPx())
                    transformations(RoundedCornersTransformation(8.dp.toPx()))
                }
            },
            contentDescription = null
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
    override val dudesState: Loadable<List<Dude>> = Loadable(
        loading = true,
        data = listOf(
            Dude(
                id = DudeId("1"),
                name = "Leanne Graham",
                photoUrl = ""
            ),
            Dude(
                id = DudeId("2"),
                name = "Ervin Howell",
                photoUrl = ""
            ),
            Dude(
                id = DudeId("3"),
                name = "Clementine Bauch",
                photoUrl = ""
            )
        )
    )

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit
}