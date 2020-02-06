package db

import java.sql.*


/**
 * Simple Java program to connect to MySQL database running on localhost and
 * running SELECT and INSERT query to retrieve and add data.
 * @author Javin Paul
 */
object JavaToMySQL {
    // JDBC URL, username and password of MySQL server
    private const val url = "jdbc:mysql://localhost:3306/test"
    private const val user = "popeye"
    private const val password = "password"
    // JDBC variables for opening and managing connection
    private var con: Connection? = null
    private var stmt: Statement? = null
    private var rs: ResultSet? = null
    @JvmStatic
    fun main(args: Array<String>) {
        val query = "select count(*) from books"
        try { // opening database connection to MySQL server
            con = DriverManager.getConnection(url, user, password)
            // getting Statement object to execute query
            stmt = con!!.createStatement()
            // executing SELECT query
            rs = stmt!!.executeQuery(query)
            while (rs!!.next()) {
                val count = rs!!.getInt(1)
                println("Total number of books in the table : $count")
            }
        } catch (sqlEx: SQLException) {
            sqlEx.printStackTrace()
        } finally { //close connection ,stmt and resultset here
            try {
                con!!.close()
            } catch (se: SQLException) { /*can't do anything */
            }
            try {
                stmt!!.close()
            } catch (se: SQLException) { /*can't do anything */
            }
            try {
                rs!!.close()
            } catch (se: SQLException) { /*can't do anything */
            }
        }
    }
}