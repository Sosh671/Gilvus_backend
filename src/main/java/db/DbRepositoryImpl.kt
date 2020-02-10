package db

import db.models.User
import util.Status
import java.sql.Connection
import java.sql.SQLException

private const val INSERT_USER = "INSERT INTO users VALUES(?,?,?,?,?)"
private const val GET_USER_ID_BY_PHONE = "SELECT id FROM users WHERE phone = ?"
private const val INSERT_SMS_CODE = "INSERT INTO sms_codes VALUES(?, ?, ?)"
private const val CHECK_SMS_AND_PHONE_EXISTS = "SELECT EXISTS(" +
        "SELECT phone " +
        "FROM sms_codes " +
        "INNER JOIN users ON sms_codes.user_id = users.id " +
        "WHERE phone = ? AND sms_code = ?" +
        ")"

@Suppress("LiftReturnOrAssignment")
class DbRepositoryImpl(private val dbConnection: Connection) : DbRepository {

    override fun insertUser(user: User, smsCode: Int): Status {
        try {
            // this array is needed for retrieving inserted user id
            val ar = arrayOf("ID")
            val insertUserStatement = dbConnection.prepareStatement(INSERT_USER, ar)
            val insertSmsStatement = dbConnection.prepareStatement(INSERT_SMS_CODE)
            dbConnection.autoCommit = false

            insertUserStatement.setInt(1, 0)
            insertUserStatement.setString(2, user.name)
            insertUserStatement.setInt(3, user.phone)
            insertUserStatement.setString(4, user.password)
            insertUserStatement.setString(5, user.avatarUrl)
            insertUserStatement.executeUpdate()

            // get id of newly inserted user
            val set = insertUserStatement.generatedKeys
            val insertedUserId: Long
            if (set.next())
                insertedUserId = set.getLong(1)
            else
                insertedUserId = 0
            if (insertedUserId == 0L)
                throw SQLException()

            // insert sms code into staging table
            insertSmsStatement.setInt(1, 0)
            insertSmsStatement.setLong(2, insertedUserId)
            insertSmsStatement.setInt(3, smsCode)
            insertSmsStatement.executeUpdate()

            dbConnection.commit()

            return Status(false)
        } catch (e: java.sql.SQLIntegrityConstraintViolationException) {
            e.printStackTrace()
            dbConnection.rollback()
            return Status(false, "Phone already registered")
        } catch (e: Exception) {
            e.printStackTrace()
            dbConnection.rollback()
            return Status(false)
        } finally {
            dbConnection.autoCommit = true
        }
    }

    override fun checkPhoneNumber(phone: String): PhoneStatus {
        try {
            val query = "SELECT EXISTS(SELECT phone FROM users WHERE phone = ?)"
            val statement = dbConnection.prepareStatement(query)
            statement.setString(1, phone)
            val result = statement.executeQuery().also { it.next() }
            val exists = result.getInt(result.metaData.getColumnName(1))
            return if (exists > 0)
                PhoneStatus.PHONE_EXISTS
            else
                PhoneStatus.OK
        } catch (e: Exception) {
            return PhoneStatus.UNKNOWN_ERROR
        }
    }

    override fun confirmAuthorization(phone: Int, smsCode: Int, token: String): Status {
        try {
            val query = "INSERT INTO users_tokens VALUES(?,?,?)"
            val idQuery = "SELECT id FROM users WHERE phone = ?"
            val selectStatement = dbConnection.prepareStatement(CHECK_SMS_AND_PHONE_EXISTS)
            val selectIdStatement = dbConnection.prepareStatement(idQuery)
            val insertStatement = dbConnection.prepareStatement(query)
            dbConnection.autoCommit = false

            selectStatement.setInt(1, phone)
            selectStatement.setInt(2, smsCode)
            val result = selectStatement.executeQuery()
            result.next()
            // get result by column id
            val exists = result.getInt(result.metaData.getColumnName(1))

            if (exists != 0) {
                selectIdStatement.setInt(1, phone)
                val idResult = selectIdStatement.executeQuery().also { it.next() }
                val id = idResult.getInt(1)
                println("id $id")

                return Status(true)
            } else
                throw SQLException()
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.autoCommit = true
        }
    }

    override fun login(phone: Int, password: String?): Status {
        try {
            val query = "SELECT EXISTS(SELECT id FROM users WHERE phone = ? AND password = ?)"
            val statement = dbConnection.prepareStatement(query)
            statement.setInt(1, phone)
            statement.setString(1, password)
            val result = statement.executeQuery()
            result.next()
            val exists = result.getInt(result.metaData.getColumnName(1))
            return if (exists > 0)
                Status(true)
            else
                Status(false)
        } catch (e: Exception) {
            return Status(false)
        }
    }
}