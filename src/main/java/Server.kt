import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class Server {

    // Vector to store active clients
    private val ar: Vector<ClientHandler?>? = Vector()
    // counter for clients
    private var i = 0
    val port = Constants.port


    fun start() {
        val ss = ServerSocket(port)
        var s: Socket
        // running infinite loop for getting
// client request
        while (true) { // Accept the incoming request
            println("listening...")
            s = ss.accept()
            println("New client request received : $s")
            // obtain input and output streams
            val dis = DataInputStream(s.getInputStream())
            val dos = DataOutputStream(s.getOutputStream())
            println("Creating a new handler for this client...")
            // Create a new handler object for handling this request.
            val mtch = ClientHandler(s, "client $i", dis, dos, ar)
            // Create a new Thread with this object.
            val t = Thread(mtch)
            println("Adding this client to active client list")
            // add this client to active clients list
            ar?.add(mtch)
            // start the thread.
            t.start()
            // increment i for new client.
// i is used for naming only, and can be replaced
// by any naming scheme
            i++

            dos.writeBytes("yo client ${i-1}!\n")
        }
    }

    // ClientHandler class
    internal class ClientHandler(
        var s: Socket, private val name: String,
        val dis: DataInputStream, val dos: DataOutputStream,
        val ar: Vector<ClientHandler?>?
    ) : Runnable {
        var scn = Scanner(System.`in`)
        var isloggedin = true
        override fun run() {
            val reader = BufferedReader(InputStreamReader(dis))
            var receivedString = reader.readLine()
            while (receivedString != null) {
                try {
                    println("hey ${receivedString}")

                    if (receivedString == "logout") {
                        isloggedin = false
                        println("$name decided to logout")
                        s.close()
                        break
                    }

                    receivedString = reader.readLine()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try { // closing resources
                dis.close()
                dos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}