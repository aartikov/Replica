package me.aartikov.replica.sample.features.pokemons.ui.list

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.sample.core.theme.AppTheme
import me.aartikov.replica.sample.features.pokemons.domain.PokemonType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PokemonTypeItem(
    type: PokemonType,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(48.dp),
        color = when (isSelected) {
            true -> MaterialTheme.colors.primary
            else -> MaterialTheme.colors.surface
        },
        elevation = 6.dp
    ) {
        Text(
            text = type.name,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Preview
@Composable
fun PokemonTypeItemPreview() {
    var isSelected by remember { mutableStateOf(false) }
    AppTheme {
        PokemonTypeItem(
            type = PokemonType.Fire,
            isSelected = isSelected,
            onClick = {
                isSelected = !isSelected
            }
        )
    }
}