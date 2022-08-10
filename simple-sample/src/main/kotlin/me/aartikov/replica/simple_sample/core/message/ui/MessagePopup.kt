package me.aartikov.replica.simple_sample.core.message.ui

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import me.aartikov.replica.advanced_sample.core.message.data.MessageService

class MessagePopup(private val messageService: MessageService) {

    fun setup(activity: AppCompatActivity) {
        activity.lifecycle.coroutineScope.launchWhenStarted {
            messageService.messageFlow.collect { message ->
                val messageText = message.text.resolve(activity)
                Toast.makeText(activity, messageText, Toast.LENGTH_SHORT).show()
            }
        }
    }
}