package server

import data.models.RegistrationData
import data.models.User
import db.DbRepository
import org.json.JSONObject
import sms.SmsController
import util.Status
import java.security.SecureRandom
import java.util.*

class ClientRequestsHandler(
    private val dbRepository: DbRepository,
    private val smsController: SmsController) : Actions {

    var registrationData: RegistrationData? = null

    override fun registration(phoneNumber: Int, name: String): Status {
        val alreadyRegistered = dbRepository.phoneNumberExists("$phoneNumber")
        if (alreadyRegistered) return Status(false, "Phone already registered")

        val generateSms = smsController.generateSms()
        smsController.sendSms(generateSms)

        registrationData = RegistrationData(phoneNumber, name, generateSms)
        return Status(true)
    }

    override fun confirmRegistration(phoneNumber: Int, smsCode: Int): Status {
        try {
            if (phoneNumber != registrationData!!.phone) return Status(false, "Wrong sms code")
            if (smsCode != registrationData!!.smsCode) return Status(false, "Wrong sms code")
            val token = generateToken()

            val result = dbRepository.insertUserAndToken(
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
        val result = dbRepository.confirmAuthorization(phoneNumber, smsCode, token)
        if (result.status) {
            val obj = JSONObject()
            obj.put("token", token)
            result.data = obj
        }
        return result
    }

    override fun createChatRoom(token: String, members: Array<Int>): Status {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChatRoomsList(token: String): Status {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendMessage(token: String, roomId: Int): Status {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessages(token: String, roomId: Int, offset: Int, limit: Int): Status {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun login(phoneNumber: Int, password: String?): Status =
        dbRepository.login(phoneNumber, password) ?: Status(false)

    private fun generateToken(): String {
        val randomBytes = ByteArray(30).also { SecureRandom().nextBytes(it) }
        val builder = StringBuilder(40)
        builder
            .append(Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes))
            .setLength(30)
        return builder.toString()
    }
}