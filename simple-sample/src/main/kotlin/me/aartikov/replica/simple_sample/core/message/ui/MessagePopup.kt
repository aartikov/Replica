package me.aartikov.replica.simple_sample.core.message.ui

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import me.aartikov.replica.simple_sample.core.message.data.MessageService

class MessagePopup(private val messageService: MessageService) {

    fun setup(activity: AppCompatActivity) {
        activity.lifecycle.coroutineScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageService.messageFlow.collect { message ->
                    val messageText = message.text.resolve(activity)
                    Toast.makeText(activity, messageText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}