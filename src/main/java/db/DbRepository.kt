package db

import data.models.User
import util.Status

interface DbRepository {

    fun insertUserAndToken(user: User, token: String): Status

    fun phoneNumberExists(phone: String): Boolean

    fun confirmAuthorization(phone: Int, smsCode: Int, token: String): Status

    fun login(phone: Int, password: String?): Status
}