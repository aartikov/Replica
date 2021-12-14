package me.aartikov.replica.sample.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import me.aartikov.replica.sample.BuildConfig
import me.aartikov.replica.sample.core.ui.ComponentFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import timber.log.Timber

class App : Application() {

    lateinit var koin: Koin
        private set

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initLogger()
        koin = createKoin().also {
            it.declare(ComponentFactory(it))
        }
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
        }.koin
    }
}

val Application.koin get() = (this as App).koin