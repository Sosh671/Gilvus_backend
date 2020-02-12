package server

import di.kModule
import org.koin.core.context.startKoin
import util.Constants

fun main() {
    startKoin {
        modules(kModule)

        val server = Server(Constants.port)
        server.start()
    }
}