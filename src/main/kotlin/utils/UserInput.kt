package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

fun readNextLine(prompt: String = ""): String {
    print(prompt)
    return readln()
}

fun readNextYear(prompt: String): Int {
    while (true) {
        val input = readNextLine(prompt)

        if (input.matches(Regex("\\d{4}"))) {
            return input.toInt()
        }

        println("Invalid year. Please enter exactly 4 digits.")
    }
}

fun readNextInt(prompt: String = ""): Int {
    while (true) {
        print(prompt)

        val value = readln().trim().toIntOrNull()
        if (value != null) {
            return value
        }

        println("Please enter a valid integer.")
    }
}

fun readNextDouble(prompt: String = ""): Double {
    while (true) {
        print(prompt)

        val valueDouble = readln().trim().toDoubleOrNull()
        if (valueDouble != null) {
            return valueDouble
        }

        println("Please enter a valid Double..(e.g. 1.5")
    }
}

fun readNextBoolean(prompt: String = ""): Boolean {
    print(prompt)
    return when (readlnOrNull()?.lowercase()) {
        "y", "yes", "yeah", "yea", "true" -> true
        else -> false
    }
}

fun readNextDOB(prompt: String): String {
    val formatter =
        DateTimeFormatter
            .ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT)

    while (true) {
        val dob = readNextLine(prompt)

        try {
            LocalDate.parse(dob, formatter)
            return dob
        } catch (e: DateTimeParseException) {
            println("Invalid date! Please enter a valid date in DD-MM-YYYY format.")
        }
    }
}
