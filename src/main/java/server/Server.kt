package server

import org.koin.core.KoinComponent
import org.koin.core.inject
import java.net.ServerSocket
import java.util.*

class Server(private val serverPort: Int) : Thread(), KoinComponent {

    private fun testRegistration() {
        val phone = 12
        val test by inject<ClientRequestsHandler>()
        val status1 = test.registration(phone, "vasiliy")
        val status2 = test.confirmRegistration(phone, test.registrationData!!.smsCode)
        println(status1)
        println(status2)
    }

    private fun testLogin() {

    }

    private fun testCreateNewRoom() {
        val cc by inject<ClientRequestsHandler>()
        cc.createChatRoom("", intArrayOf(4,5).toTypedArray())
    }

    override fun run() {
        testCreateNewRoom()
        try {
            val serverSocket = ServerSocket(serverPort)
            while (true) {
                println("Listening...")
                val clientSocket = serverSocket.accept()
                println("Accepted connection from $clientSocket")

                val handler by inject<ClientRequestsHandler>()
                val client = ClientSocket(handler, clientSocket)

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