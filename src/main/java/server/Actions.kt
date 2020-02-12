package server

import util.Status

interface Actions {

    fun registration(phoneNumber: Int, name: String): Status
    fun confirmRegistration(phoneNumber: Int, smsCode: Int): Status
    fun login(phoneNumber: Int, password: String?): Status
    fun confirmAuthorization(phoneNumber: Int, smsCode: Int): Status
    fun createChatRoom(token: String, members: Array<Int>): Status
    fun getChatRoomsList(token: String): Status
    fun sendMessage(token: String, roomId: Int): Status
    fun getMessages(token: String, roomId: Int, offset: Int = 0, limit: Int = 0): Status
}