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
                        "get_rooms" -> {
                            requestedGetRooms(data)
                        }
                        "add_room" -> {
                            requestedAddRoom(data)
                        }
                        "get_messages" -> {
                            requestedGetMessages(data)
                        }
                        "send_message" -> {
                            requestedSendMessage(data)
                        }
                        "check_contacts" -> {
                            requestedCheckPhoneNumbers(data)
                        }
                    else -> {
                        respondToClient(null, Status(false, "Unknown request: $request\n"))
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                respondToClient(null, Status(false, "You should pass valid json"))
            } catch (e: Exception) {
                e.printStackTrace()
                respondToClient(null, Status(false, "Unknown error\n"))
            }

            line = reader.readLine()
        }
        clientSocket.close()
    }

    private fun requestedRegistrationConfirmation(obj: JSONObject) {
        try {
            val phone = obj.getString("phone")
            val code = obj.getInt("code")
            val result = actionsHandler.confirmRegistration(phone, code)
            respondToClient("confirm_registration", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("confirm_registration", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("confirm_registration", Status(false, "Confirmation error"))
        }
    }

    private fun requestedRegistration(obj: JSONObject) {
        try {
            val phone = obj.getString("phone")
            val result = actionsHandler.registration(phone, "New user")
            respondToClient("registration", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("registration", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            e.printStackTrace()
            respondToClient("registration", Status(false, "Registration error"))
        }
    }

    private fun requestedLoginConfirmation(obj: JSONObject) {
        try {
            val phone = obj.getString("phone")
            val code = obj.getInt("code")
            val result = actionsHandler.confirmLogin(phone, code)
            respondToClient("confirm_login", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("confirm_login", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("confirm_login", Status(false, "Confirmation error"))
        }
    }

    private fun requestedLogin(obj: JSONObject) {
        try {
            val phone = obj.getString("phone")
            val password = obj.getString("password")
            val result = actionsHandler.login(phone, password)
            respondToClient("login", result)
        } catch (e: JSONException) {
            val phone = obj.getString("phone")
            val result = actionsHandler.login(phone, null)
            respondToClient("login", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("login", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("login", Status(false, "Login error"))
        }
    }

    private fun requestedGetRooms(obj: JSONObject) {
        try {
            val token = obj.getString("token")
            val result = actionsHandler.getChatRoomsList(token)
            respondToClient("get_rooms", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("get_rooms", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("get_rooms", Status(false, "Login error"))
        }
    }

    private fun requestedAddRoom(obj: JSONObject) {
        try {
            val token = obj.getString("token")
            val name = obj.getString("title")
            val members = ArrayList<Long>()
            val jsonArray = obj.getJSONArray("members")
            for (i in 0 until jsonArray.length())
                members.add(jsonArray.getJSONObject(i).getLong("id"))

            val result = actionsHandler.createChatRoom(token, name, members.toTypedArray())
            respondToClient("add_room", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("add_room", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("add_room", Status(false, "Login error"))
        }
    }

    private fun requestedGetMessages(obj: JSONObject) {
        try {
            val token = obj.getString("token")
            val roomId = obj.getLong("room_id")
            val result = actionsHandler.getMessages(token, roomId)
            respondToClient("get_messages", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("get_messages", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("get_messages", Status(false, "Login error"))
        }
    }

    private fun requestedSendMessage(obj: JSONObject) {
        try {
            val token = obj.getString("token")
            val roomId = obj.getLong("room_id")
            val message = obj.getString("message")
            val result = actionsHandler.sendMessage(token, roomId, message)
            respondToClient("send_message", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("send_message", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("send_message", Status(false, "Login error"))
        }
    }

    private fun requestedCheckPhoneNumbers(obj: JSONObject) {
        try {
            val token = obj.getString("token")
            val numbers = ArrayList<String>()
            val jsonArray = obj.getJSONArray("phone_numbers")
            for (i in 0 until jsonArray.length())
                numbers.add(jsonArray.getJSONObject(i).getString("number"))

            val result = actionsHandler.checkContacts(token, numbers.toTypedArray())
            respondToClient("check_contacts", result)
        } catch (e: JSONException) {
            e.printStackTrace()
            respondToClient("check_contacts", Status(false, "Wrong arguments"))
        } catch (e: Exception) {
            respondToClient("check_contacts", Status(false, "Login error"))

        }
    }

    private fun respondToClient(request: String?, status: Status) {
        val obj = JSONObject()
        obj.put("request", request)
        obj.put("status", status.status)
        (status.data as? JSONObject)?.let { obj.put("data", it) }
        status.errorMessage?.let { obj.put("message", it) }

        outputStream?.write("$obj\n".toByteArray())

        println("my response: $obj")
    }
}