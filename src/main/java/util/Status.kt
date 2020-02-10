package util

import org.json.JSONObject

data class Status(val status: Boolean, val errorMessage: String? = null, var data: JSONObject? = null)