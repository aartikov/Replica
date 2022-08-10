package me.aartikov.replica.simple_sample.core.error_handling

import me.aartikov.replica.advanced_sample.core.message.data.MessageService
import me.aartikov.replica.simple_sample.core.message.domain.Message
import timber.log.Timber

/**
 * Executes error processing: shows error messages and logs exceptions.
 */
class ErrorHandler(private val messageService: MessageService) {

    // Used to not handle the same exception more than one time.
    private var lastHandledException: Exception? = null

    fun handleError(exception: Exception, showError: Boolean = true) {
        if (lastHandledException === exception) return
        lastHandledException = exception

        Timber.e(exception)
        if (showError) {
            messageService.showMessage(Message(text = exception.errorMessage))
        }
    }
}