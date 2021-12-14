package me.aartikov.replica.sample.core

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.message.MessageService
import me.aartikov.replica.sample.core.ui.message.MessageServiceImpl
import org.koin.dsl.module

val coreModule = module {
    single<MessageService> { MessageServiceImpl() }
    single { ErrorHandler(get()) }
    single { ReplicaClient() }
}