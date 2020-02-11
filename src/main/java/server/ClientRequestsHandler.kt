package server

import db.DbRepository
import db.DbRepositoryImpl
import db.PhoneStatus
import db.models.User
import org.json.JSONObject
import sms.SmsController
import util.Constants
import util.Status
import java.security.SecureRandom
import java.sql.DriverManager
import java.util.*

class ClientRequestsHandler : Actions {

    private var dbRepository: DbRepository? = null
    private var smsController: SmsController? = null

    init {
        val sqlConnection = DriverManager.getConnection(Constants.dbPath, Constants.dbUser, Constants.dbPassword)
        dbRepository = DbRepositoryImpl(sqlConnection)
        smsController = SmsController()
    }

    override fun registration(phoneNumber: Int, name: String): Status {
        val generateSms = smsController?.generateSms() ?: return Status(false)

        return dbRepository?.insertUser(
            User(null, name, phoneNumber, null, null),
            generateSms
        ) ?: Status(false)
    }

    override fun checkPhoneNumber(phoneNumber: String): PhoneStatus =
        dbRepository?.checkPhoneNumber(phoneNumber) ?: PhoneStatus.UNKNOWN_ERROR

    override fun confirmAuthorization(phoneNumber: Int, smsCode: Int): Status {
        val token = generateToken()
        val result = dbRepository?.confirmAuthorization(phoneNumber, smsCode, token) ?: Status(false)
        if (result.status) {
            val obj = JSONObject()
            obj.put("token", token)
            result.data = obj
        }
        return result
    }

    override fun login(phoneNumber: Int, password: String?): Status =
        dbRepository?.login(phoneNumber, password) ?: Status(false)

    private fun generateToken(): String {
        val randomBytes = ByteArray(30).also { SecureRandom().nextBytes(it) }
        val builder = StringBuilder(40)
        builder
            .append(Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes))
            .setLength(30)
        return builder.toString()
    }
}