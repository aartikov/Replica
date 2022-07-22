package me.aartikov.replica.sample.app

import android.app.Application
import me.aartikov.replica.sample.BuildConfig
import me.aartikov.replica.sample.core.ComponentFactory
import me.aartikov.replica.sample.core.debug_tools.DebugTools
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import timber.log.Timber

class App : Application() {

    lateinit var koin: Koin
        private set

    override fun onCreate() {
        super.onCreate()
        initLogger()
        koin = createKoin()
        launchDebugTools()
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun createKoin(): Koin {
        return koinApplication {
            androidContext(this@App)
            modules(allModules)
            koin.declare(ComponentFactory(koin))
        }.koin
    }

    private fun launchDebugTools() {
        koin.get<DebugTools>().launch()
    }
}

val Application.koin get() = (this as App).koin