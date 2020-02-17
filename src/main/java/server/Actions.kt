package server

import util.Status

interface Actions {

    fun registration(phoneNumber: Int, name: String): Status
    fun confirmRegistration(phoneNumber: Int, smsCode: Int): Status
    fun login(phoneNumber: Int, password: String?): Status
    fun confirmAuthorization(phoneNumber: Int, smsCode: Int): Status
    fun createChatRoom(token: String, members: Array<Long>): Status
    fun getChatRoomsList(token: String): Status
    fun sendMessage(token: String, roomId: Long, message: String): Status
    fun getMessages(token: String, roomId: Long, offset: Int = 0, limit: Int = 0): Status
}