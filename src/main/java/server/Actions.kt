package server

import util.Status

interface Actions {

    fun registration(phoneNumber: Int, name: String): Status
    fun confirmRegistration(phoneNumber: Int, smsCode: Int): Status
    fun login(phoneNumber: Int, password: String?): Status
    fun confirmAuthorization(phoneNumber: Int, smsCode: Int): Status
}