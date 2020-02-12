package server

import data.models.RegistrationData
import data.models.User
import db.DbRepository
import db.DbRepositoryImpl
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

    var registrationData: RegistrationData? = null

    override fun registration(phoneNumber: Int, name: String): Status {
        val alreadyRegistered = dbRepository?.phoneNumberExists("$phoneNumber") ?: return Status(false)
        if (alreadyRegistered) return Status(false, "Phone already registered")

        val generateSms = smsController?.generateSms() ?: return Status(false, "Couldn't send an sms")
        smsController?.sendSms(generateSms) ?: return Status(false, "Couldn't send an sms")

        registrationData = RegistrationData(phoneNumber, name, generateSms)
        return Status(true)
    }

    override fun confirmRegistration(phoneNumber: Int, smsCode: Int): Status {
        try {
            if (phoneNumber != registrationData!!.phone) return Status(false, "Wrong sms code")
            if (smsCode != registrationData!!.smsCode) return Status(false, "Wrong sms code")
            val token = generateToken()

            val result = dbRepository?.insertUserAndToken(
                User(
                    null,
                    registrationData!!.name,
                    registrationData!!.phone,
                    null,
                    null
                ),
                token
            ) ?: return Status(false)

            if (result.status) {
                val obj = JSONObject()
                obj.put("token", token)
                result.data = obj
            }
            return result
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return Status(false, "No registration data detected")
        } catch (e: Exception) {
            e.printStackTrace()
            return Status(false, "Unknown error")
        }
    }

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