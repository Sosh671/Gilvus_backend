package db

import data.models.Room
import data.models.User
import util.Status
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Statement

private const val INSERT_USER = "INSERT INTO users VALUES(?,?,?,?,?)"
private const val GET_USER_ID_BY_PHONE = "SELECT id FROM users WHERE phone = ?"
//private const val INSERT_SMS_CODE = "INSERT INTO sms_codes VALUES(?, ?, ?)"
private const val CHECK_SMS_AND_PHONE_EXISTS = "SELECT EXISTS(" +
        "SELECT id " +
        "FROM sms_codes " +
        "WHERE phone = ? AND sms_code = ?" +
        ")"
private const val INSERT_USER_TOKEN = "INSERT INTO users_tokens VALUES(?,?,?)"
private const val INSERT_NEW_ROOM = "INSERT INTO rooms VALUES(?,?,?)"
private const val INSERT_NEW_MEMBER = "INSERT INTO members VALUES(?,?,?)"
private const val GET_USER_ID_BY_TOKEN = "SELECT user_id FROM users_tokens WHERE token = ?"
private const val GET_ROOMS_LIST = "SELECT rooms.id, rooms.name, rooms.date_created FROM rooms " +
        "INNER JOIN members ON room_id = rooms.id " +
        "WHERE members.user_id = ?"

@Suppress("LiftReturnOrAssignment")
class DbRepository(private val dbConnection: Connection) {

    fun insertUserAndToken(user: User, token: String): Status {
        try {
            val insertUserStatement = dbConnection.prepareStatement(INSERT_USER)
            val selectUserIdStatement = dbConnection.prepareStatement(GET_USER_ID_BY_PHONE)
            val insertTokenStatement = dbConnection.prepareStatement(INSERT_USER_TOKEN)

            // begin transaction
            dbConnection.autoCommit = false

            insertUserStatement.setInt(1, 0)
            insertUserStatement.setString(2, user.name)
            insertUserStatement.setInt(3, user.phone)
            insertUserStatement.setString(4, user.password)
            insertUserStatement.setString(5, user.avatarUrl)
            val insertUserResult = insertUserStatement.executeUpdate()

            // get user id by phone
            selectUserIdStatement.setInt(1, user.phone)
            val idResult = selectUserIdStatement.executeQuery().also { it.next() }
            val userId = idResult.getInt(1)

            // insert authorization token
            insertTokenStatement.setInt(1, 0)
            insertTokenStatement.setInt(2, userId)
            insertTokenStatement.setString(3, token)
            val insertTokenResult = insertTokenStatement.executeUpdate()

            dbConnection.commit()

            if (insertUserResult > 0 && insertTokenResult > 0)
                return Status(true)
            else
                return Status(false)
        } catch (e: SQLIntegrityConstraintViolationException) {
            e.printStackTrace()
            return Status(false, "Phone already registered")
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }


    fun isPhoneNumberExists(phone: String): Boolean {
        try {
            val query = "SELECT EXISTS(SELECT phone FROM users WHERE phone = ?)"
            val statement = dbConnection.prepareStatement(query)
            statement.setString(1, phone)
            val result = statement.executeQuery().also { it.next() }
            val exists = result.getInt(result.metaData.getColumnName(1))
            return exists > 0
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }

    fun validateCredentials(phone: Int, password: String?): Status {
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

    fun confirmLogin(phone: Int, smsCode: Int, token: String): Status {
        try {
            val validateSmsStatement = dbConnection.prepareStatement(CHECK_SMS_AND_PHONE_EXISTS)
            val selectUserIdStatement = dbConnection.prepareStatement(GET_USER_ID_BY_PHONE)
            val insertUserTokenStatement = dbConnection.prepareStatement(INSERT_USER_TOKEN)

            // begin transaction
            dbConnection.autoCommit = false

            // first validate if the phone and the sms code are connected
            validateSmsStatement.setInt(1, phone)
            validateSmsStatement.setInt(2, smsCode)
            val result = validateSmsStatement.executeQuery().also { it.next() }
            // if > 0  - validated connection
            val exists = result.getInt(result.metaData.getColumnName(1))

            if (exists > 0) {
                // get user id by phone
                selectUserIdStatement.setInt(1, phone)
                val idResult = selectUserIdStatement.executeQuery().also { it.next() }
                val userId = idResult.getInt(1)

                // insert authorization token
                insertUserTokenStatement.setInt(1, 0)
                insertUserTokenStatement.setInt(2, userId)
                insertUserTokenStatement.setString(3, token)
                val insertTokenResult = insertUserTokenStatement.executeUpdate()

                dbConnection.commit()

                if (insertTokenResult > 0)
                    return Status(true)
                else
                    return Status(false)
            } else
                throw SQLException()
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }

    fun createNewRoom(token: String, roomName: String, roomMembers: Array<Int>): Status {
        try {
            val insertNewRoomStatement = dbConnection.prepareStatement(INSERT_NEW_ROOM, Statement.RETURN_GENERATED_KEYS)
            val insertMemberStatement = dbConnection.prepareStatement(INSERT_NEW_MEMBER)

            // begin transaction
            dbConnection.autoCommit = false

            // insert new room
            insertNewRoomStatement.setInt(1, 0)
            insertNewRoomStatement.setString(2, roomName)
            insertNewRoomStatement.setLong(3, System.currentTimeMillis())
            insertNewRoomStatement.executeUpdate()
            val keys = insertNewRoomStatement.generatedKeys.also { it.next() }
            val insertedRoomId = keys.getInt(1)
            if (insertedRoomId < 1) throw SQLException("Error while inserting new room")

            // insert all the members of the room
            for (i in roomMembers.indices) {
                insertMemberStatement.setInt(1, 0)
                insertMemberStatement.setInt(2, insertedRoomId)
                insertMemberStatement.setInt(3, roomMembers[i])
                if (insertMemberStatement.executeUpdate() != 1) throw SQLException("Error while inserting new room members")
            }

            dbConnection.commit()
            return Status(true)
        } catch (e: SQLIntegrityConstraintViolationException) {
            e.printStackTrace()
            return Status(false, "Some of the members are not existing users")
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }

    fun getAvailableRooms(token: String): Status {
        try {
            val getTokenStatement = dbConnection.prepareStatement(GET_USER_ID_BY_TOKEN)
            val getRoomsStatement = dbConnection.prepareStatement(GET_ROOMS_LIST)

            // begin transaction
            dbConnection.autoCommit = false

            // get user id
            getTokenStatement.setString(1, token)
            val result = getTokenStatement.executeQuery()
            if (!result.next()) return Status(false, "Wrong token")
            val userId = result.getLong(1)

            // get rooms where the user is a member
            getRoomsStatement.setLong(1, userId)
            val roomsResult = getRoomsStatement.executeQuery()
            val list = ArrayList<Room>()
            while (roomsResult.next()) {
                val room = Room(roomsResult.getLong(1), roomsResult.getString(2), roomsResult.getLong(3))
                list.add(room)
            }

            dbConnection.commit()
            return Status(true, data = list)
        } catch (e: SQLIntegrityConstraintViolationException) {
            e.printStackTrace()
            return Status(false, "Some of the members are not existing users")
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }
}