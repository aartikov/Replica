package me.aartikov.replica.sample.core

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.sample.core.data.network.BaseUrl
import me.aartikov.replica.sample.core.data.network.NetworkApiFactory
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.external_app_service.ExternalAppService
import me.aartikov.replica.sample.core.ui.external_app_service.ExternalAppServiceImpl
import me.aartikov.replica.sample.core.ui.message.MessageService
import me.aartikov.replica.sample.core.ui.message.MessageServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    single(named(BaseUrl.Github)) { NetworkApiFactory(BaseUrl.Github.url) }
    single<MessageService> { MessageServiceImpl() }
    single { ErrorHandler(get()) }
    single<ExternalAppService> { ExternalAppServiceImpl(androidContext()) }
    single { ReplicaClient() }
}