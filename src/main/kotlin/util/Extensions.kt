package util

fun String.validatePhoneFormat(): Boolean {
    if (!(length in 5..20))
        return false
    if (contains("[^0-9]".toRegex()))
        return false
    return true
}

fun String.dirtyStringToNumbers() = replace(Regex("[^0-9]+"), "")