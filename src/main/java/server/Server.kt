package server

import db.DbRepository
import db.DbRepositoryImpl
import db.models.User
import util.Constants
import util.Status
import java.io.IOException
import java.net.ServerSocket
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

    override fun registration(phoneNumber: String, name: String): Status {
        return dbRepository?.insertUser(User(null, name, phoneNumber, null))?: Status(false)
    }
}