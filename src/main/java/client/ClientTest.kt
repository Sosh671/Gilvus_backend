package client

import Constants
import java.net.InetSocketAddress
import java.net.Socket

fun main() {

//    val androidEmulatorLocalhostAddress = "10.0.2.2"
    val pcLocalhostAddress = "localhost"
    val port = Constants.port

    try {
        val socket = Socket()
        println("before")
        Thread.sleep(1000)
        socket.connect(InetSocketAddress(pcLocalhostAddress, port))
        println("after")
    } catch (e: Exception) {
        System.err.println(e)
    }
}