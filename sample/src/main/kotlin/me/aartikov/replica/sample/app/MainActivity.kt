package me.aartikov.replica.sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.defaultComponentContext
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.features.root.createRootComponent
import me.aartikov.replica.sample.features.root.ui.RootUi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val componentFactory = application.koin.get<ComponentFactory>()
        val rootComponent = componentFactory.createRootComponent(defaultComponentContext())

        installSplashScreen()
        setContent {
            AppTheme {
                RootUi(rootComponent)
            }
        }
    }
}