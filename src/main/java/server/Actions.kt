package server

import db.PhoneStatus
import util.Status

interface Actions {

    fun registration(phoneNumber: Int, name: String): Status
    fun checkPhoneNumber(phoneNumber: String): PhoneStatus
    fun confirmAuthorization(phoneNumber: Int, smsCode: Int): Status
    fun login(phoneNumber: Int, password: String?): Status
}