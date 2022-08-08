package me.aartikov.replica.advanced_sample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.aartikov.replica.advanced_sample.core.debug_tools.DebugTools
import me.aartikov.replica.advanced_sample.core.debug_tools.RealDebugTools
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.external_app_service.ExternalAppService
import me.aartikov.replica.advanced_sample.core.external_app_service.ExternalAppServiceImpl
import me.aartikov.replica.advanced_sample.core.message.data.MessageService
import me.aartikov.replica.advanced_sample.core.message.data.MessageServiceImpl
import me.aartikov.replica.advanced_sample.core.network.BaseUrl
import me.aartikov.replica.advanced_sample.core.network.NetworkApiFactory
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.network.AndroidNetworkConnectivityProvider
import me.aartikov.replica.network.NetworkConnectivityProvider
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    single(named(BaseUrl.Github)) { NetworkApiFactory(BaseUrl.Github.url, get()) }
    single(named(BaseUrl.Pokemons)) { NetworkApiFactory(BaseUrl.Pokemons.url, get()) }
    single(named(BaseUrl.RandomData)) { NetworkApiFactory(BaseUrl.RandomData.url, get()) }
    single<NetworkConnectivityProvider> { AndroidNetworkConnectivityProvider(androidApplication()) }
    single { ReplicaClient(get()) }
    single<DebugTools> { RealDebugTools(androidContext(), get()) }
    single<MessageService> { MessageServiceImpl() }
    single { ErrorHandler(get()) }
    single<ExternalAppService> { ExternalAppServiceImpl(androidContext()) }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
}