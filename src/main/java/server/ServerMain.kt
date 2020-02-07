package server

import util.Constants

fun main() {
    val server = Server(Constants.port)
    server.start()
}