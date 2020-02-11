package sms

class SmsController {

    fun generateSms(): Int {
        var digits = ""
        for (i in 1..4)
            digits += (1..9).random()
        return digits.toInt()
    }
}