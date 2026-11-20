// #member1
// Input validation shared by registration, profile editing, account and visitor forms.
package com.example.mini.data

object InputRules {
    const val PASSWORD_MESSAGE = "Password must be at least 6 characters with letters and numbers"
    const val PHONE_MESSAGE = "Phone number must start with 0 and have 10 or 11 digits"
    const val IC_MESSAGE = "IC number must be 12 digits with a real birth date (YYMMDD)"
    const val EMAIL_MESSAGE = "Enter a valid email address, e.g. ali@example.com"
    const val USERNAME_MESSAGE = "User ID must be 4-20 letters, numbers, dots or underscores (no spaces)"
    const val NAME_MESSAGE = "Name must contain letters"

    // one "@", no spaces, a dot in the domain, and something either side: ali@mail.com
    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    private val usernamePattern = Regex("^[A-Za-z0-9._]{4,20}$")

    fun isValidPassword(password: String): Boolean =
        password.length >= 6 && password.any { it.isLetter() } && password.any { it.isDigit() }

    // dashes don't count, so 012-345 6789 is fine; Malaysian numbers always start with 0
    fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length in 10..11 && digits.startsWith("0")
    }

    // 12 digits, and the first 6 must be a real date (YYMMDD), e.g. 900101 = 1 Jan 1990
    fun isValidIc(icNo: String): Boolean {
        val digits = icNo.filter { it.isDigit() }
        if (digits.length != 12) return false
        val month = digits.substring(2, 4).toInt()
        val day = digits.substring(4, 6).toInt()
        return month in 1..12 && day in 1..31
    }

    fun isValidEmail(email: String): Boolean = emailPattern.matches(email.trim())

    // no spaces or symbols, so it's easy to type again at login
    fun isValidUsername(username: String): Boolean = usernamePattern.matches(username)

    // a name of only numbers or symbols ("12345", "???") is almost certainly a typo
    fun isValidName(name: String): Boolean = name.trim().any { it.isLetter() }

    // keeps only digits and dashes while typing
    fun sanitizePhone(input: String): String = input.filter { it.isDigit() || it == '-' }.take(13)

    fun sanitizeIc(input: String): String = input.filter { it.isDigit() || it == '-' }.take(14)
}
