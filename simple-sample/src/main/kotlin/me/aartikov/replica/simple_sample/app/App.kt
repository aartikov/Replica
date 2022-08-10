package me.aartikov.replica.simple_sample.app

import android.app.Application
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.simple_sample.BuildConfig
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogger()
        initKoin()
        launchReplicaDevTools()
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
    }

    private fun launchReplicaDevTools() {
        get<ReplicaDevTools>().launch()
    }
}