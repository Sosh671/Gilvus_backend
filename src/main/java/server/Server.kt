package server

import java.io.IOException
import java.net.ServerSocket
import java.util.*

class Server(private val serverPort: Int) : Thread() {

    override fun run() {
        try {
            val serverSocket = ServerSocket(serverPort)
            while (true) {
                println("Listening...")
                val clientSocket = serverSocket.accept()
                println("Accepted connection from $clientSocket")
                val client = ClientSocket(ClientRequestsHandler(), clientSocket)

                clients.add(client)
                client.start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val clients = ArrayList<ClientSocket>()

    fun getClients(): List<ClientSocket> {
        return clients
    }

    fun removeClient(clientSocket: ClientSocket?) {
        clients.remove(clientSocket)
    }
}