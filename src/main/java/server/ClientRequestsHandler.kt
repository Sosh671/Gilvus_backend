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
        val alreadyRegistered = dbRepository.isPhoneNumberExists("$phoneNumber")
        if (alreadyRegistered) return Status(false, "Phone already registered")

        val generatedSms = smsController.generateSms()
        smsController.sendSms(generatedSms)

        registrationData = RegistrationData(phoneNumber, name, generatedSms)
        return Status(true, "Code is: $generatedSms")
    }

    override fun confirmRegistration(phoneNumber: Int, smsCode: Int): Status {
        try {
            if (phoneNumber != registrationData?.phone) return Status(false, "Wrong sms code")
            if (smsCode != registrationData?.smsCode) return Status(false, "Wrong sms code")
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
            )

            if (result.status) {
                val obj = JSONObject()
                obj.put("token", token)
                result.data = obj

                registrationData = null
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

    var loginData: RegistrationData? = null
    override fun login(phoneNumber: Int, password: String?): Status {
        val validate = dbRepository.validateCredentials(phoneNumber, password)
        if (!validate.status) return Status(false, "Wrong phone or password")

        val generatedSms = smsController.generateSms()
        smsController.sendSms(generatedSms)

        loginData = RegistrationData(phoneNumber, password?:"", generatedSms)
        return Status(true, "Code is: $generatedSms")
    }

    override fun confirmAuthorization(phoneNumber: Int, smsCode: Int): Status {
        if (phoneNumber != loginData?.phone) return Status(false, "Wrong sms code")
        if (smsCode != loginData?.smsCode) return Status(false, "Wrong sms code")
        val token = generateToken()
        val result = dbRepository.confirmLogin(phoneNumber, smsCode, token)
        if (result.status) {
            val obj = JSONObject()
            obj.put("token", token)
            result.data = obj
        }
        return result
    }

    override fun createChatRoom(token: String, members: Array<Long>): Status {
       return dbRepository.createNewRoom(token, "room", members)
    }

    override fun getChatRoomsList(token: String): Status {
        return dbRepository.getAvailableRooms(token)
    }

    override fun sendMessage(token: String, roomId: Long, message: String): Status {
        return dbRepository.insertMessage(token, roomId, message)
    }

    override fun getMessages(token: String, roomId: Long, offset: Int, limit: Int): Status {
        return dbRepository.getMessages(token, roomId)
    }

    private fun generateToken(): String {
        val randomBytes = ByteArray(30).also { SecureRandom().nextBytes(it) }
        val builder = StringBuilder(40)
        builder
            .append(Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes))
            .setLength(30)
        return builder.toString()
    }
}