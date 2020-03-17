package db

import data.models.IdAndPhone
import data.models.Message
import data.models.Room
import data.models.User
import util.Status
import java.sql.*

@Suppress("LiftReturnOrAssignment")
class DbRepository(private val dbConnection: Connection) {

    fun insertUserAndToken(user: User, token: String): Status {
        try {
            val insertUserStatement = dbConnection.prepareStatement(INSERT_USER)
            val selectUserIdStatement = dbConnection.prepareStatement(SELECT_USER_ID_BY_PHONE)
            val insertTokenStatement = dbConnection.prepareStatement(INSERT_USER_TOKEN)

            // begin transaction
            dbConnection.autoCommit = false

            insertUserStatement.setInt(1, 0)
            insertUserStatement.setString(2, user.name)
            insertUserStatement.setString(3, user.phone)
            insertUserStatement.setString(4, user.password)
            insertUserStatement.setString(5, user.avatarUrl)
            val insertUserResult = insertUserStatement.executeUpdate()

            // get user id by phone
            selectUserIdStatement.setString(1, user.phone)
            val idResult = selectUserIdStatement.executeQuery().apply { next() }
            val userId = idResult.getLong(1)

            // insert authorization token
            insertTokenStatement.setInt(1, 0)
            insertTokenStatement.setLong(2, userId)
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
            val statement = dbConnection.prepareStatement(CHECK_PHONE)
            statement.setString(1, phone)
            val result = statement.executeQuery().apply { next() }
            
            val exists = result.getLong(result.metaData.getColumnName(1))
            return exists > 0
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }

    fun validateCredentials(phone: String, password: String?): Status {
        try {
            val statement: PreparedStatement
            if (password != null )
                statement = dbConnection.prepareStatement(CHECK_PHONE_AND_PASSWORD)
            else
                statement = dbConnection.prepareStatement(CHECK_PHONE)
            
            statement.setString(1, phone)
            // set password if it's not null
            password?.let { statement.setString(1, password) }
            val result = statement.executeQuery().apply { next() }
            
            val exists = result.getLong(result.metaData.getColumnName(1))
            return if (exists > 0)
                Status(true)
            else
                Status(false)
        } catch (e: Exception) {
            return Status(false)
        }
    }

    // todo refactor sms
    fun confirmLogin(phone: String, smsCode: Int, token: String): Status {
        try {
//            val validateSmsStatement = dbConnection.prepareStatement(CHECK_SMS_AND_PHONE_EXISTS)
            val selectUserIdStatement = dbConnection.prepareStatement(SELECT_USER_ID_BY_PHONE)
            val insertUserTokenStatement = dbConnection.prepareStatement(INSERT_USER_TOKEN)

            // begin transaction
            dbConnection.autoCommit = false

            /*// first validate if the phone and the sms code are connected
            validateSmsStatement.setInt(1, phone)
            validateSmsStatement.setInt(2, smsCode)
            val result = validateSmsStatement.executeQuery().apply { next() }
            // if > 0  - validated connection
            val exists = result.getInt(result.metaData.getColumnName(1))*/
            val exists = 1

            if (exists > 0) {
                // get user id by phone
                selectUserIdStatement.setString(1, phone)
                val idResult = selectUserIdStatement.executeQuery().apply { next() }
                val userId = idResult.getLong(1)

                // insert authorization token
                insertUserTokenStatement.setInt(1, 0)
                insertUserTokenStatement.setLong(2, userId)
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

    fun createNewRoom(token: String, roomName: String, array: Array<Long>): Status {
        try {
            val getTokenStatement = dbConnection.prepareStatement(SELECT_USER_ID_BY_TOKEN)
            val insertNewRoomStatement = dbConnection.prepareStatement(INSERT_NEW_ROOM, Statement.RETURN_GENERATED_KEYS)
            val insertMemberStatement = dbConnection.prepareStatement(INSERT_NEW_MEMBER)

            // begin transaction
            dbConnection.autoCommit = false

            // get user id
            getTokenStatement.setString(1, token)
            val result = getTokenStatement.executeQuery()
            if (!result.next()) return Status(false, "Wrong token")
            val userId = result.getLong(1)
            val roomMembers = array.plus(userId)

            // insert new room
            insertNewRoomStatement.setInt(1, 0)
            insertNewRoomStatement.setString(2, roomName)
            insertNewRoomStatement.setLong(3, System.currentTimeMillis())
            insertNewRoomStatement.executeUpdate()
            val keys = insertNewRoomStatement.generatedKeys.apply { next() }
            val insertedRoomId = keys.getLong(1)
            if (insertedRoomId < 1) throw SQLException("Error while inserting new room")

            // insert all the members of the room
            for (i in roomMembers.indices) {
                insertMemberStatement.setInt(1, 0)
                insertMemberStatement.setLong(2, insertedRoomId)
                insertMemberStatement.setLong(3, roomMembers[i])
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
            val getTokenStatement = dbConnection.prepareStatement(SELECT_USER_ID_BY_TOKEN)
            val getRoomsStatement = dbConnection.prepareStatement(SELECT_ROOMS_LIST)

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
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }

    fun insertMessage(token: String, roomId: Long, message: String): Status {
        try {
            val getTokenStatement = dbConnection.prepareStatement(SELECT_USER_ID_BY_TOKEN)
            val insertMessageStatement = dbConnection.prepareStatement(INSERT_MESSAGE)

            // begin transaction
            dbConnection.autoCommit = false

            // get user id
            getTokenStatement.setString(1, token)
            val result = getTokenStatement.executeQuery()
            if (!result.next()) return Status(false, "Wrong token")
            val userId = result.getLong(1)

            // insert new message
            insertMessageStatement.setInt(1, 0)
            insertMessageStatement.setLong(2, roomId)
            insertMessageStatement.setLong(3, userId)
            insertMessageStatement.setLong(4, System.currentTimeMillis())
            insertMessageStatement.setString(5, message)
            insertMessageStatement.setBoolean(6, false)
            insertMessageStatement.executeUpdate()

            dbConnection.commit()
            return Status(true)
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }

    fun getRoomMembers(roomId: Long): List<Long>? {
        try {
            val getMembersStatement = dbConnection.prepareStatement(SELECT_ROOM_MEMBERS)

            getMembersStatement.setLong(1, roomId)
            val result = getMembersStatement.executeQuery()
            val list = ArrayList<Long>()
            while (result.next()) {
                list.add(result.getLong(1))
            }

            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    private val test = "SELECT users.id FROM users " +
    "INNER JOIN users_tokens ON user_id = users.id " +
    "WHERE users_tokens.token = ?"
    fun getUsersTokens(users: List<Long>): List<String>? {
        try {
            val getMembersStatement = dbConnection.prepareStatement(SELECT_ROOM_MEMBERS)

            getMembersStatement.setLong(1, 1L)
            val result = getMembersStatement.executeQuery()
            val list = ArrayList<Long>()
            while (result.next()) {
                list.add(result.getLong(1))
            }

            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getMessages(token: String, roomId: Long): Status {
        try {
            val getTokenStatement = dbConnection.prepareStatement(SELECT_USER_ID_BY_TOKEN)
            val getMessagesStatement = dbConnection.prepareStatement(SELECT_MESSAGES_BY_ROOM_ID)

            // begin transaction
            dbConnection.autoCommit = false

            // get user id
            getTokenStatement.setString(1, token)
            val result = getTokenStatement.executeQuery()
            if (!result.next()) return Status(false, "Wrong token")
            val userId = result.getLong(1)
            // todo check if this id is in the room members

            // get messages
            getMessagesStatement.setLong(1, roomId)
            val messagesResult = getMessagesStatement.executeQuery()
            val list = ArrayList<Message>()
            while (messagesResult.next()) {
                val message = Message(
                    messagesResult.getLong(1),
                    messagesResult.getLong(2),
                    messagesResult.getLong(3),
                    messagesResult.getString(4),
                    userId == messagesResult.getLong(2),
                    messagesResult.getBoolean(5)
                )
                list.add(message)
            }

            dbConnection.commit()
            return Status(true, data = list)
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }

    fun checkPhoneNumbersExist(token: String, numbers: Array<String>): Status {
        try {
            val getTokenStatement = dbConnection.prepareStatement(SELECT_USER_ID_BY_TOKEN)
            val checkPhone = dbConnection.prepareStatement(SELECT_USER_ID_BY_PHONE)

            // begin transaction
            dbConnection.autoCommit = false

            // get user id
            getTokenStatement.setString(1, token)
            val result = getTokenStatement.executeQuery()
            if (!result.next()) return Status(false, "Wrong token")

            val listOfExistingNumbers = ArrayList<IdAndPhone>()
            // check numbers
            for (number in numbers) {
                checkPhone.setString(1, number)
                val phoneResultSet = checkPhone.executeQuery()
                if (phoneResultSet.next())
                    listOfExistingNumbers.add(IdAndPhone(phoneResultSet.getLong(1), number))
            }

            dbConnection.commit()
            return Status(true, data = listOfExistingNumbers)
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false)
        } finally {
            dbConnection.rollback()
            dbConnection.autoCommit = true
        }
    }
}

private const val SELECT_USER_ID_BY_PHONE = "SELECT id FROM users WHERE phone = ?"
private const val SELECT_USER_ID_BY_TOKEN = "SELECT user_id FROM users_tokens WHERE token = ?"
private const val SELECT_MESSAGES_BY_ROOM_ID = "SELECT id, user_id, date, text, isRead FROM messages WHERE room_id = ?"
private const val SELECT_USER_TOKEN_BY_ID = "SELECT users.id FROM users " +
        "INNER JOIN users_tokens ON user_id = users.id " +
        "WHERE users_tokens.token = ?"
private const val SELECT_ROOMS_LIST = "SELECT rooms.id, rooms.name, rooms.date_created FROM rooms " +
        "INNER JOIN members ON room_id = rooms.id " +
        "WHERE members.user_id = ?"
private const val SELECT_ROOM_MEMBERS = "SELECT user_id FROM members " +
        "INNER JOIN rooms ON room_id = rooms.id " +
        "WHERE members.room_id = ?"

private const val INSERT_USER = "INSERT INTO users VALUES(?,?,?,?,?)"
private const val INSERT_SMS_CODE = "INSERT INTO sms_codes VALUES(?, ?, ?)"
private const val INSERT_USER_TOKEN = "INSERT INTO users_tokens VALUES(?,?,?)"
private const val INSERT_NEW_ROOM = "INSERT INTO rooms VALUES(?,?,?)"
private const val INSERT_NEW_MEMBER = "INSERT INTO members VALUES(?,?,?)"
private const val INSERT_MESSAGE = "INSERT INTO messages VALUES(?,?,?,?,?, ?)"

private const val CHECK_PHONE = "SELECT EXISTS(SELECT id FROM users WHERE phone = ?)"
private const val CHECK_PHONE_AND_PASSWORD = "SELECT EXISTS(SELECT id FROM users WHERE phone = ? AND password = ?)"
private const val CHECK_SMS_AND_PHONE_EXISTS = "SELECT EXISTS(" +
        "SELECT id " +
        "FROM sms_codes " +
        "WHERE phone = ? AND sms_code = ?" +
        ")"