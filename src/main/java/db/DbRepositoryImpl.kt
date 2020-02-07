package db

import db.models.User
import util.Status
import java.sql.Connection

@Suppress("LiftReturnOrAssignment")
class DbRepositoryImpl(private val dbConnection: Connection) : DbRepository {

    override fun insertUser(user: User): Status {
        try {
            val statement = dbConnection.createStatement()
            val query = "INSERT INTO users VALUES('0','${user.name}','${user.phone}','${user.avatarUrl}')"
            val result = statement.executeUpdate(query)
            statement.closeOnCompletion()
            return Status(result > 0)
        } catch (e: java.sql.SQLIntegrityConstraintViolationException) {
            e.printStackTrace()
            return Status(false, "Phone already registered")
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        }
    }
}