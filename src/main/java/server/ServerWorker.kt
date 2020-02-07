package server

import org.json.simple.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

class ServerWorker(private val server: Server, private val clientSocket: Socket) : Thread() {

    var login: String? = null
        private set

    private var outputStream: OutputStream? = null
    override fun run() {
        try {
            handleClientSocket()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun handleClientSocket() {
        val inputStream = clientSocket.getInputStream()
        outputStream = clientSocket.getOutputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println("received: $line")
            val params: Array<String>? = line?.split(" ")?.toTypedArray()
            if (params != null && params.isNotEmpty()) {
                val cmd = params[0]
                if ("logout" == cmd || "quit".equals(cmd, ignoreCase = true)) {
                    handleLogout()
                    break
                } else if ("login".equals(cmd, ignoreCase = true)) {
                    requestedLogin(params)
                } else {
                    val msg = "Unknown command: $cmd\n"
                    outputStream?.write(msg.toByteArray())
                }
            }
        }
        clientSocket.close()
    }

    @Throws(IOException::class)
    private fun handleLogout() {
        val workerList = server.getWorkerList()
        // send other online users current user's status
        val onlineMsg = "offline $login\n"
        for (worker in workerList) {
            if (login != worker.login) {
                worker.send(onlineMsg)
            }
        }
        clientSocket.close()
    }

    @Throws(IOException::class)
    private fun requestedLogin(params: Array<String>) {
        if (params.size == 3) {

            val login = params[1]
            val password = params[2]
            if (login == "guest" && password == "guest" || login == "jim" && password == "jim") {
                val msg = "ok login\n"
                outputStream!!.write(msg.toByteArray())
                this.login = login
                println("User logged in succesfully: $login")
            } else {
                val msg = "error login\n"
                outputStream!!.write(msg.toByteArray())
            }
        } else {
            sendStatus(false)
        }
    }

    private fun sendStatus(success: Boolean) {
        val obj = JSONObject()
        obj["status"] = success

        val string = StringBuilder()
        for (i in 1..1000000)
            string.append("sdfsdfdsfsdf")
        obj["sdf"] = string.toString()
        obj["xczv"] = "\n"

        send(obj.toJSONString())
    }

    private fun send(msg: String) {
        outputStream?.write("$msg\n".toByteArray())
    }
}