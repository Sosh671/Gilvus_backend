package server

import util.Status

interface Actions {

    fun registration(phoneNumber: String, name: String): Status
    fun confirmRegistration(phoneNumber: String, smsCode: Int): Status
    fun login(phoneNumber: String, password: String?): Status
    fun confirmLogin(phoneNumber: String, smsCode: Int): Status
    fun createChatRoom(token: String, roomName: String, members: Array<Long>): Status
    fun getChatRoomsList(token: String): Status
    fun getRoomInfo(token: String, roomId: Long): Status
    fun sendMessage(token: String, roomId: Long, message: String): Status
    fun getMessages(token: String, roomId: Long, offset: Int = 0, limit: Int = 0): Status
    fun checkContacts(token: String, numbers: Array<String>): Status
}