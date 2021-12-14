package me.aartikov.replica.sample.features.message.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import me.aartikov.replica.sample.core.ui.message.MessageData
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.utils.ShowDialog
import me.aartikov.replica.sample.core.ui.utils.resolve
import me.aartikov.sesame.dialog.DialogControl
import me.aartikov.sesame.localizedstring.LocalizedString

@Composable
fun MessageUi(
    component: MessageComponent,
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
) {
    Box(modifier = modifier.fillMaxSize()) {
        MessageDialog(component.dialogControl, bottomPadding)
    }
}

@Composable
fun MessageDialog(dialog: DialogControl<MessageData, Unit>, bottomPadding: Dp) {
    ShowDialog(dialog) { data ->
        MessagePopup(
            messageData = data,
            bottomPadding = bottomPadding,
            dismiss = dialog::dismiss
        )
    }
}

@Composable
private fun MessagePopup(
    messageData: MessageData,
    bottomPadding: Dp,
    dismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = dismiss,
        properties = PopupProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
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

    LaunchedEffect(messageData) {
        delay(4000L)
        dismiss()
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

    override val dialogControl = DialogControl<MessageData, Unit>().apply {
        show(MessageData(text = LocalizedString.raw("Message")))
    }
}