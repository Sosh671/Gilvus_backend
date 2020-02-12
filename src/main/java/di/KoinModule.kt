package di

import db.DbRepository
import org.koin.dsl.module
import server.ClientRequestsHandler
import sms.SmsController
import util.Constants
import java.sql.DriverManager

val kModule = module {
    factory { DriverManager.getConnection(Constants.dbPath, Constants.dbUser, Constants.dbPassword) }
    factory { DbRepository(get()) }
    factory { SmsController() }
    factory { ClientRequestsHandler(get(), get()) }
}