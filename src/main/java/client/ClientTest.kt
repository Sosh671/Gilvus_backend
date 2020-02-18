package client

import org.json.JSONArray
import org.json.JSONObject
import util.Constants
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class Reader(private val stream: InputStream) : Thread() {
    override fun run() {
        super.run()
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String? = reader.readLine()
        while (line != null) {
            println("received: $line")
            line = reader.readLine()
        }
    }
}

var oStream: OutputStream? = null

val phone = "131311231213"
val password = "password"
//val userToken = "2132"
val userToken = "lEdE6rHnpbCZlOEmHnrxqQ4G9R6F0w"
val userId1 = 4
val userId2 = 5
val userId3 = 15
val roomId = 19

fun main() {

    val pcLocalhostAddress = Constants.pcLocalhostAddress
    val port = Constants.port

    try {
        val socket = Socket()
        socket.connect(InetSocketAddress(pcLocalhostAddress, port))
        println("connected")
        Reader(socket.getInputStream()).start()
        oStream = socket.getOutputStream()

//        testRegistration()
//        testRegistrationConfirm()
//        testLogin()
//        testLoginConfirm()
//        testAddRoom()
//        testGetRooms()
//        testSendMessage()
//        testGetMessages()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun testRegistration(password: Boolean = false) {
    val dataObject = JSONObject()
    dataObject.put("phone", phone)
    if (password)
        dataObject.put("password", password)

    formRequestObject("registration", dataObject)
}

fun testRegistrationConfirm() {
    val dataObject = JSONObject()
    dataObject.put("phone", phone)
    dataObject.put("code", 1111)

    formRequestObject("confirm_registration", dataObject)
}

fun testLogin(password: Boolean = false) {
    val dataObject = JSONObject()
    dataObject.put("phone", phone)
    if (password)
        dataObject.put("password", password)

    formRequestObject("login", dataObject)
}

fun testLoginConfirm() {
    val dataObject = JSONObject()
    dataObject.put("phone", phone)
    dataObject.put("code", 1111)

    formRequestObject("confirm_login", dataObject)
}

fun testGetRooms() {
    val dataObject = JSONObject()
    dataObject.put("token", userToken)

    formRequestObject("get_rooms", dataObject)
}

fun testAddRoom() {
    val membersArray = JSONArray()
    membersArray.put(JSONObject().apply { put("id", userId1); put("id", userId2); put("id", userId3);})

    val dataObject = JSONObject()
    dataObject.put("token", userToken)
    dataObject.put("title", "New room 1")
    dataObject.put("members", membersArray)

    formRequestObject("add_room", dataObject)
}

fun testGetMessages() {
    val dataObject = JSONObject()
    dataObject.put("token", userToken)
    dataObject.put("room_id", roomId)

    formRequestObject("get_messages", dataObject)
}

fun testSendMessage() {
    val dataObject = JSONObject()
    dataObject.put("token", userToken)
    dataObject.put("room_id", roomId)
    dataObject.put("message", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

    formRequestObject("send_message", dataObject)
}

fun formRequestObject(request: String, data: JSONObject) {
    val obj = JSONObject()
    obj.put("request", request)
    obj.put("data", data)

    sendToServer(obj)
}

fun sendToServer(data: JSONObject) {
    println("requested $data")
    oStream?.write("$data\n".toByteArray())
}