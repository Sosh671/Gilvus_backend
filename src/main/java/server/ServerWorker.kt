package server

import org.json.JSONException
import org.json.JSONObject
import util.Status
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

class ServerWorker(private val actionsHandler: Actions, private val clientSocket: Socket) : Thread() {

    private var token: String? = null

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
                if (params != null && params.size == 2) {
                    try {
                        val request = params[0].toLowerCase()
                        val obj = JSONObject(params[1])

                        when (request) {
                            "registration", "r" -> {
                                requestedRegistration(obj)
                            }
                            "login" -> {
                                requestedLogin(obj)
                            }
                            "confirm", "c" -> {
                                requestedConfirmation(obj)
                            }
                            else -> {
                                sendStatus(Status(false, "Unknown request: $request\n"))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        sendStatus(Status(false, "Error"))
                    }
                }
            } else
                sendStatus(Status(false, "Unknown request: $line\n"))
        }
        clientSocket.close()
    }

    private fun requestedRegistration(obj: JSONObject) {
        try {
            val phone = obj.getInt("phone")
            val result = actionsHandler.registration(phone, "test")
            sendStatus(result)
        } catch (e: Exception) {
            e.printStackTrace()
            sendStatus(Status(false, "Registration error"))
        }
    }

    private fun requestedConfirmation(obj: JSONObject) {
        try {
            val phone = obj.getInt("phone")
            val code = obj.getInt("code")
            val result = actionsHandler.confirmAuthorization(phone, code)
            sendStatus(result)
        } catch (e: Exception) {
            sendStatus(Status(false, "Confirmation error"))
        }
    }

    private fun requestedLogin(obj: JSONObject) {
        try {
            val phone = obj.getInt("phone")
            val password = obj.getString("password")
            val result = actionsHandler.login(phone, password)
            sendStatus(result)
        } catch (e: JSONException) {
            val phone = obj.getInt("phone")
            val result = actionsHandler.login(phone, null)
            sendStatus(result)
        } catch (e: Exception) {
            sendStatus(Status(false, "Login error"))
        }
    }

    private fun handleLogout() {
        clientSocket.close()
    }

    private fun sendStatus(status: Status) {
        val obj: JSONObject = status.data ?: JSONObject()
        obj.put("status", status.status)

        status.errorMessage?.let {
            if (it.isNotBlank() && it.isNotEmpty())
                obj.put("message", it)
        }

        outputStream?.write("$obj\n".toByteArray())
    }
}