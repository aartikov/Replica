package me.aartikov.replica.sample.core.ui.error_handing

import me.aartikov.replica.sample.core.ui.message.MessageData
import me.aartikov.replica.sample.core.ui.message.MessageService
import timber.log.Timber

class ErrorHandler(
    private val messageService: MessageService
) {
    fun handleError(exception: Exception, showError: Boolean) {
        Timber.e(exception)

        if (showError) {
            messageService.showMessage(MessageData(text = exception.errorMessage))
        }
    }
}