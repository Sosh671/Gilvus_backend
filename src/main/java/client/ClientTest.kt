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
        var i = 0
        val stream = socket.getOutputStream()
        stream.write(registerRequest())
        println("info written")
        while(true) {
            i = 1
        }
        println("after")
    } catch (e: Exception) {
        System.err.println(e)
    }
}

fun registerRequest(): ByteArray {
    val obj = JSONObject()
    obj.put("phone", "123123123")
    val request = "registration $obj\n".toByteArray()
    return request
}

class Reader(private val stream: InputStream) : Thread() {
    override fun run() {
        super.run()
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String
//        val file = File("vasya")
//        if (!file.exists())
//            file.createNewFile()
        while (reader.readLine().also { line = it } != null) {
            println("new cycle")
            println(line)
//            file.writeText(line)
        }
    }
}