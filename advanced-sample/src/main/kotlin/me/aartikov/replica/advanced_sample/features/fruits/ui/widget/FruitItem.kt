package me.aartikov.replica.advanced_sample.features.fruits.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.features.fruits.domain.Fruit

@Composable
fun FruitItem(
    fruit: Fruit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = isEnabled)
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            contentDescription = null,
            model = ImageRequest.Builder(LocalContext.current)
                .data(fruit.imageUrl)
                .size(
                    with(LocalDensity.current) {
                        42.dp.roundToPx()
                    }
                )
                .crossfade(true)
                .build(),
            modifier = Modifier.size(42.dp),
        )

        Text(
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = 16.dp),
            text = fruit.name,
            style = MaterialTheme.typography.body1
        )

        Image(
            modifier = Modifier
                .size(24.dp),
            painter = painterResource(
                if (fruit.isFavourite) R.drawable.ic_liked else R.drawable.ic_unliked
            ),
            contentDescription = null
        )
    }
}
