package me.aartikov.replica.simple_sample.core.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.simple_sample.core.theme.AppTheme

@Composable
fun EmptyPlaceholder(
    description: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = description,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.Center)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
private fun EmptyPlaceholderPreview() {
    AppTheme {
        EmptyPlaceholder(
            description = "Description"
        )
    }
}
