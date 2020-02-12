package server

import java.net.ServerSocket
import java.util.*

class Server(private val serverPort: Int) : Thread() {

    private fun testRegistration() {
        val phone = 10
        val test = ClientRequestsHandler()
        val status1 = test.registration(phone, "vasiliy")
        val status2 = test.confirmRegistration(phone, test.registrationData!!.smsCode)
        println(status1)
        println(status2)
    }

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
        } catch (e: Exception) {
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