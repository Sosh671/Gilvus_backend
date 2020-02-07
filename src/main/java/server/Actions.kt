package server

import util.Status

interface Actions {

    fun registration(phoneNumber: String, name: String): Status
}