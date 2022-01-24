package me.aartikov.replica.devtools.internal

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebServer(private val coroutineScope: CoroutineScope) {

    companion object {
        private const val LOG_TAG = "Replica WebServer"
    }

    //TODO Установка порта и хоста
    private val server by lazy {
        embeddedServer(Netty, port = 8080, host = "192.168.0.8") {
            routing {
                get("/") {
                    call.respondText("Hello, world!")
                }
            }
        }
    }

    fun start() = coroutineScope.launch(Dispatchers.IO) {
        server.start(true)
    }
}