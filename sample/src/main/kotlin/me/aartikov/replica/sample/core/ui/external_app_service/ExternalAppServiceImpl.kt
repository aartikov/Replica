package me.aartikov.replica.sample.core.ui.external_app_service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import me.aartikov.replica.sample.core.domain.MatchingAppNotFoundException

class ExternalAppServiceImpl(private val applicationContext: Context) : ExternalAppService {

    override fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ContextCompat.startActivity(applicationContext, intent, null)
        } catch (e: ActivityNotFoundException) {
            throw MatchingAppNotFoundException(e)
        }
    }
}