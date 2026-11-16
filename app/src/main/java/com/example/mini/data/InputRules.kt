// #member1
// Input validation shared by registration, profile editing and visitor forms.
package com.example.mini.data

object InputRules {
    const val PASSWORD_MESSAGE = "Password must be at least 6 characters with letters and numbers"
    const val PHONE_MESSAGE = "Phone number must be 10 or 11 digits"
    const val IC_MESSAGE = "IC number must be 12 digits"
    const val EMAIL_MESSAGE = "Enter a valid email address"

    fun isValidPassword(password: String): Boolean =
        password.length >= 6 && password.any { it.isLetter() } && password.any { it.isDigit() }

    // dashes and spaces don't count, so 012-345 6789 is fine
    fun isValidPhone(phone: String): Boolean = phone.count { it.isDigit() } in 10..11

    fun isValidIc(icNo: String): Boolean = icNo.count { it.isDigit() } == 12

    fun isValidEmail(email: String): Boolean =
        email.contains("@") && email.substringAfter("@").contains(".")

    // keeps only digits and dashes while typing
    fun sanitizePhone(input: String): String = input.filter { it.isDigit() || it == '-' }.take(13)

    fun sanitizeIc(input: String): String = input.filter { it.isDigit() || it == '-' }.take(14)
}
