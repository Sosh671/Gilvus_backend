package server

import db.DbRepository
import db.DbRepositoryImpl
import db.PhoneStatus
import db.models.User
import org.json.JSONObject
import util.Constants
import util.Status
import java.io.IOException
import java.net.ServerSocket
import java.security.SecureRandom
import java.sql.DriverManager
import java.util.*

class Server(private val serverPort: Int) : Thread(), Actions {

    private val workerList = ArrayList<ServerWorker>()
    private var dbRepository: DbRepository? = null

    fun getWorkerList(): List<ServerWorker> {
        return workerList
    }

    fun removeWorker(serverWorker: ServerWorker?) {
        workerList.remove(serverWorker)
    }

    override fun run() {
        connectToDb()
        println("${confirmAuthorization(7, 1959)}")
        try {
            val serverSocket = ServerSocket(serverPort)
            while (true) {
                println("Ready to accept a client...")
                val clientSocket = serverSocket.accept()
                println("Accepted connection from $clientSocket")
                val worker = ServerWorker(this, clientSocket)
                workerList.add(worker)
                worker.start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun connectToDb() {
        val sqlConnection = DriverManager.getConnection(Constants.dbPath, Constants.dbUser, Constants.dbPassword)
        dbRepository = DbRepositoryImpl(sqlConnection)
    }

    override fun registration(phoneNumber: Int, name: String): Status {
        var generatedSms = ""
        for (i in 1..4)
            generatedSms += (1..9).random()
        return dbRepository?.insertUser(
            User(null, name, phoneNumber, null, null),
            generatedSms.toInt()
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
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return String(bytes)
    }
}