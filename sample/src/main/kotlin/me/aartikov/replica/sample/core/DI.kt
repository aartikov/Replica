package me.aartikov.replica.sample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.DevToolsSettings
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.network.AndroidNetworkConnectivityProvider
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.sample.core.data.network.BaseUrl
import me.aartikov.replica.sample.core.data.network.NetworkApiFactory
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.external_app_service.ExternalAppService
import me.aartikov.replica.sample.core.ui.external_app_service.ExternalAppServiceImpl
import me.aartikov.replica.sample.core.ui.message.MessageService
import me.aartikov.replica.sample.core.ui.message.MessageServiceImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    single(named(BaseUrl.Github)) { NetworkApiFactory(BaseUrl.Github.url) }
    single(named(BaseUrl.Pokemons)) { NetworkApiFactory(BaseUrl.Pokemons.url) }
    single(named(BaseUrl.RandomData)) { NetworkApiFactory(BaseUrl.RandomData.url) }
    single<NetworkConnectivityProvider> { AndroidNetworkConnectivityProvider(androidApplication()) }
    single { ReplicaClient(get()) }
    single { ReplicaDevTools(get(), DevToolsSettings(), androidApplication()) }
    single<MessageService> { MessageServiceImpl() }
    single { ErrorHandler(get()) }
    single<ExternalAppService> { ExternalAppServiceImpl(androidContext()) }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
}