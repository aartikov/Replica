package me.aartikov.replica.sample.features.menu.ui

import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import me.aartikov.replica.sample.core.theme.AppTheme

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = colors
    ) {
        Text(
            text = text.uppercase(),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun MenuButtonPreview() {
    AppTheme {
        MenuButton(text = "Menu item", onClick = {})
    }
}