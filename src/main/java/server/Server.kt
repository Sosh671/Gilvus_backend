package server

import org.koin.core.KoinComponent
import org.koin.core.inject
import util.Status
import java.net.ServerSocket
import java.util.*

class Server(private val serverPort: Int) : Thread(), KoinComponent {

    private val testToken = "mKewwUQxGeycoUuNcrmQt3gpgyyMkA"

    private fun testRegistration() {
        val phone = "12"
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
        cc.createChatRoom(testToken,"room 1", longArrayOf(4,5, 21).toTypedArray())
    }

    private fun testGetRooms() {
        val cc by inject<ClientRequestsHandler>()
        var dd: Status
        dd = cc.getChatRoomsList("1234")
        println("test $dd")
        dd = cc.getChatRoomsList(testToken)
        println("test $dd")
    }

    private fun testSendMessage() {
        val cc by inject<ClientRequestsHandler>()
        var dd: Status
        dd = cc.sendMessage("1234", 15, "test message")
        println(dd)
        dd = cc.sendMessage(testToken,15, "test b")
        dd = cc.sendMessage(testToken,16, "test a")
        println(dd)
    }

    private fun testGetMessages() {
        val cc by inject<ClientRequestsHandler>()
        var dd: Status
        dd = cc.getMessages("1234", 15)
        println(dd)
        dd = cc.getMessages(testToken,15)
        println(dd)
    }

    private fun testCheckNumbers() {
        val cc by inject<ClientRequestsHandler>()
        val status = cc.checkContacts(testToken, arrayOf("2", "10", "0", "99"))
        println(status)
    }

    override fun run() {
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