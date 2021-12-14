package me.aartikov.replica.sample.features.message.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.aartikov.replica.sample.core.ui.message.MessageData
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.utils.resolve
import me.aartikov.sesame.localizedstring.LocalizedString

@Composable
fun MessageUi(
    component: MessageComponent,
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
) {
    val inverseIsDarkTheme = MaterialTheme.colors.isLight
    AppTheme(inverseIsDarkTheme) {
        Box(modifier = modifier.fillMaxSize()) {
            component.visibleMessageData?.let {
                MessagePopup(
                    messageData = it,
                    bottomPadding = bottomPadding
                )
            }
        }
    }
}

@Composable
private fun MessagePopup(
    messageData: MessageData,
    bottomPadding: Dp
) {
    Popup(
        alignment = Alignment.BottomCenter,
        properties = PopupProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 6.dp,
            modifier = Modifier
                .padding(bottom = bottomPadding, start = 8.dp, end = 8.dp)
                .wrapContentSize()
        ) {
            Text(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                text = messageData.text.resolve(),
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MessageUiPreview() {
    AppTheme {
        MessageUi(FakeMessageComponent(), Modifier, 40.dp)
    }
}

class FakeMessageComponent : MessageComponent {

    override val visibleMessageData = MessageData(LocalizedString.raw("Message"))
}