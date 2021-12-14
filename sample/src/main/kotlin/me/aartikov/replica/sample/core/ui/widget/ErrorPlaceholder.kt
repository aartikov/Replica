package me.aartikov.replica.sample.core.ui.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.aartikov.replica.sample.R


@Composable
fun ErrorPlaceholder(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            Text(
                text = errorMessage,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.body2
            )
            TextButton(
                onClick = onRetryClick
            ) {
                Text(
                    text = stringResource(R.string.common_retry).uppercase()
                )
            }
        }
    }
}