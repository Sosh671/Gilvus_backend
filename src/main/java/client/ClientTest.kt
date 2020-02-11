package client

import org.json.JSONObject
import util.Constants
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

fun main() {

    val pcLocalhostAddress = Constants.pcLocalhostAddress
    val port = Constants.port

    try {
        val socket = Socket()
        socket.connect(InetSocketAddress(pcLocalhostAddress, port))
        println("connected")
        Reader(socket.getInputStream()).start()
        val stream = socket.getOutputStream()

        stream.write(registerRequest())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun registerRequest(): ByteArray {
    val dataObject = JSONObject()
    dataObject.put("phone", "12312323")

    val obj = JSONObject()
    obj.put("request", "registration")
    obj.put("data", dataObject)
    println("requested $obj")
    return "$obj\n".toByteArray()
}

class Reader(private val stream: InputStream) : Thread() {
    override fun run() {
        super.run()
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String
        while (reader.readLine().also { line = it } != null) {
            println("server response: $line")
        }
    }
}