package db

import db.models.User
import util.Status

interface DbRepository {

    fun insertUser(user: User, smsCode: Int): Status

    fun checkPhoneNumber(phone: String): PhoneStatus

    fun confirmAuthorization(phone: Int, smsCode: Int, token: String): Status

    fun login(phone: Int, password: String?): Status
}

enum class PhoneStatus(code: Int) {
    OK(0),
    PHONE_EXISTS(1),
    UNKNOWN_ERROR(2)
}