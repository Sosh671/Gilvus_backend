package server

import java.io.IOException
import java.net.ServerSocket
import java.util.*

class Server(private val serverPort: Int) : Thread() {

    private val workerList = ArrayList<ServerWorker>()

    fun getWorkerList(): List<ServerWorker> {
        return workerList
    }

    fun removeWorker(serverWorker: ServerWorker?) {
        workerList.remove(serverWorker)
    }

    override fun run() {
        try {
            val serverSocket = ServerSocket(serverPort)
            while (true) {
                println("Ready to accept client...")
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
}