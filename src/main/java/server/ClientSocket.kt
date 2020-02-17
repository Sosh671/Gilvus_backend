package server

import org.json.JSONException
import org.json.JSONObject
import util.Status
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

class ClientSocket(private val actionsHandler: Actions, private val clientSocket: Socket) : Thread() {

    private var outputStream: OutputStream? = null

    override fun run() {
        try {
            handleClientSocket()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleClientSocket() {
        val inputStream = clientSocket.getInputStream()
        outputStream = clientSocket.getOutputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String? = reader.readLine()
        while (line != null) {
            println("received: $line")
            try {
                val obj = JSONObject(line)
                val request = obj.getString("request")
                val data = obj.getJSONObject("data")

                when (request) {
                    "registration" -> {
                        requestedRegistration(data)
                    }
                    "login" -> {
                        requestedLogin(data)
                    }
                    "confirm_login" -> {
                        requestedLoginConfirmation(data)
                    }
                    "confirm_registration" -> {
                        requestedRegistrationConfirmation(data)
                    }
                    else -> {
                        respondToClient(Status(false, "Unknown request: $request\n"))
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                respondToClient(Status(false, "You should pass valid json\n"))
            } catch (e: Exception) {
                e.printStackTrace()
                respondToClient(Status(false, "Unknown error\n"))
            }

            line = reader.readLine()
        }
        clientSocket.close()
    }

    private fun requestedRegistrationConfirmation(obj: JSONObject) {
        try {
            val phone = obj.getInt("phone")
            val code = obj.getInt("code")
            val result = actionsHandler.confirmRegistration(phone, code)
            respondToClient(result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient(Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient(Status(false, "Confirmation error"))
        }
    }

    private fun requestedRegistration(obj: JSONObject) {
        try {
            val phone = obj.getInt("phone")
            val result = actionsHandler.registration(phone, "New user")
            respondToClient(result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient(Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            e.printStackTrace()
            respondToClient(Status(false, "Registration error"))
        }
    }

    private fun requestedLoginConfirmation(obj: JSONObject) {
        try {
            val phone = obj.getInt("phone")
            val code = obj.getInt("code")
            val result = actionsHandler.confirmAuthorization(phone, code)
            respondToClient(result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient(Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient(Status(false, "Confirmation error"))
        }
    }

    private fun requestedLogin(obj: JSONObject) {
        try {
            val phone = obj.getInt("phone")
            val password = obj.getString("password")
            val result = actionsHandler.login(phone, password)
            respondToClient(result)
        } catch (e: JSONException) {
            val phone = obj.getInt("phone")
            val result = actionsHandler.login(phone, null)
            respondToClient(result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient(Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient(Status(false, "Login error"))
        }
    }

    private fun respondToClient(status: Status) {
        val obj: JSONObject = status.data as? JSONObject ?: JSONObject()
        obj.put("status", status.status)
        status.errorMessage?.let { obj.put("message", it) }

        outputStream?.write("$obj\n".toByteArray())

        println("my response: $obj")
    }
}