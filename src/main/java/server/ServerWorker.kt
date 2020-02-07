package server

import org.json.simple.JSONObject
import util.Status
import util.validatePhoneFormat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

class ServerWorker(private val actionsHandler: Actions, private val clientSocket: Socket) : Thread() {

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

    @Suppress("MoveVariableDeclarationIntoWhen")
    private fun handleClientSocket() {
        val inputStream = clientSocket.getInputStream()
        outputStream = clientSocket.getOutputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println("received: $line")

            if (!line.isNullOrBlank() && !line.isNullOrEmpty()) {
                // remove multiple whitespaces in a row and put formatted params into an array
                val params: Array<String>? = line?.trim()?.replace("\\s+".toRegex(), " ")?.split(" ")?.toTypedArray()
                if (!params.isNullOrEmpty()) {
                    val request = params[0].toLowerCase()
                    when (request) {
                        "registration", "r" -> {
                            requestedRegistration(params)
                        }
                        "login" -> {
                            requestedLogin(params)
                        }
                        "logout" -> {

                        }
                        else -> {
                            sendStatus(Status(false, "Unknown request: $request\n"))
                        }
                    }
                }
            } else
                sendStatus(Status(false, "Unknown request: $line\n"))
        }
        clientSocket.close()
    }

    private fun requestedRegistration(params: Array<String>) {
        if (params.size == 2) {
            val phone = params[1]
            if (phone.validatePhoneFormat()) {
                val result = actionsHandler.registration(phone, "new user")
                sendStatus(result)
            } else {
                sendStatus(Status(false, "Invalid phone number format"))
            }
        } else {
            sendStatus(Status(false, "Invalid parameters number for registration"))
        }
    }

    @Throws(IOException::class)
    private fun handleLogout() {
        // send other online users current user's status
        val onlineMsg = "offline $login\n"
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
            sendStatus(Status(false, "Invalid parameters number for login"))
        }
    }

    private fun sendStatus(status: Status) {
        val obj = JSONObject()
        obj["status"] = status.status
        status.message?.let {
            if (it.isNotBlank() && it.isNotEmpty())
                obj["message"] = it
        }

        outputStream?.write("${obj.toJSONString()}\n".toByteArray())
    }
}