package me.aartikov.replica.simple_sample.core

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.network.AndroidNetworkConnectivityProvider
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.simple_sample.BuildConfig
import me.aartikov.replica.simple_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.simple_sample.core.message.data.MessageService
import me.aartikov.replica.simple_sample.core.message.data.MessageServiceImpl
import me.aartikov.replica.simple_sample.core.message.ui.MessagePopup
import me.aartikov.replica.simple_sample.core.network.NetworkApiFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreModule = module {
    single { NetworkApiFactory(BuildConfig.BACKEND_URL) }
    single<NetworkConnectivityProvider> { AndroidNetworkConnectivityProvider(androidApplication()) }
    single { ReplicaClient(get()) }
    single { ReplicaDevTools(get(), get()) }
    single<MessageService> { MessageServiceImpl() }
    single { ErrorHandler(get()) }
    factory { MessagePopup(get()) }
}