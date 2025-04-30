package me.aartikov.replica.simple_sample.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.aartikov.replica.simple_sample.app.navigation.AppNavHost
import me.aartikov.replica.simple_sample.core.message.ui.MessageUi
import me.aartikov.replica.simple_sample.core.theme.AppTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PokemonApplication()
        }
    }
}

@Composable
private fun PokemonApplication() {
    AppTheme {
        AppNavHost()

        MessageUi()
    }
}
