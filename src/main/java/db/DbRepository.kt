package db

import db.models.User
import util.Status

interface DbRepository {

    fun insertUser(user: User): Status
}