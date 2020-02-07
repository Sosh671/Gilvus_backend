package server

import Constants

fun main() {
    val server = Server(Constants.port)
    server.start()
}