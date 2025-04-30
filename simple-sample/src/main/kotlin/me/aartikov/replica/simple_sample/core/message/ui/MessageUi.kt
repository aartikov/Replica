package me.aartikov.replica.simple_sample.core.message.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.aartikov.replica.simple_sample.core.message.domain.Message
import me.aartikov.replica.simple_sample.core.theme.AppTheme
import me.aartikov.replica.simple_sample.core.utils.resolve
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

/**
 * Displays a [Message] as a popup at the bottom of screen.
 */
@Composable
fun MessageUi(
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = koinViewModel<DefaultMessageViewModel>(),
    bottomPadding: Dp = 16.dp,
) {
    val message by viewModel.visibleMessage.collectAsState()
    val bottomPaddingPx = LocalDensity.current.run { bottomPadding.toPx().roundToInt() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        message?.let {
            MessagePopup(
                message = it,
                bottomPaddingPx = bottomPaddingPx,
            )
        }
    }
}

@Composable
private fun MessagePopup(
    message: Message,
    bottomPaddingPx: Int,
) {
    Popup(
        offset = IntOffset(0, -bottomPaddingPx),
        alignment = Alignment.BottomCenter,
        properties = PopupProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
            ),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(vertical = 13.dp, horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = message.text.resolve(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun MessageUiPreview() {
    AppTheme {
        MessageUi(viewModel = PreviewMessageViewModel())
    }
}
