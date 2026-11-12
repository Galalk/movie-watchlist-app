
package utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class UserInputTest {

    private val originalInput = System.`in`

    @BeforeEach
    fun setup() {
        System.setIn(originalInput)
    }

    @AfterEach
    fun restoreInput() {
        System.setIn(originalInput)
    }

    private fun provideInput(input: String) {
        System.setIn(ByteArrayInputStream(input.toByteArray()))
    }

    @Nested
    inner class ReadNextLine {

        @Test
        fun `reading a line returns entered text`() {
            provideInput("Inception\n")

            val result = readNextLine()

            assertEquals("Inception", result)
        }
    }

    @Nested
    inner class ReadNextYear {

        @Test
        fun `accepting a four digit year`() {
            provideInput("2005\n")

            val result = readNextYear("Year: ")

            assertEquals(2005, result)
        }

        @Test
        fun `rejecting invalid year before accepting valid year`() {
            provideInput("abc\n25\n2005\n")

            val result = readNextYear("Year: ")

            assertEquals(2005, result)
        }
    }

    @Nested
    inner class ReadNextInt {

        @Test
        fun `accepting a valid integer`() {
            provideInput("25\n")

            val result = readNextInt()

            assertEquals(25, result)
        }

        @Test
        fun `rejecting non numeric input`() {
            provideInput("abc\n25\n")

            val result = readNextInt()

            assertEquals(25, result)
        }
    }

    @Nested
    inner class ReadNextDouble {

        @Test
        fun `accepting a valid decimal`() {
            provideInput("7.5\n")

            val result = readNextDouble()

            assertEquals(7.5, result)
        }

        @Test
        fun `rejecting invalid input`() {
            provideInput("hello\n7.5\n")

            val result = readNextDouble()

            assertEquals(7.5, result)
        }
    }

    @Nested
    inner class ReadNextBoolean {

        @Test
        fun `accepting yes as true`() {
            provideInput("yes\n")

            val result = readNextBoolean()

            assertEquals(true, result)
        }

        @Test
        fun `accepting no as false`() {
            provideInput("no\n")

            val result = readNextBoolean()

            assertEquals(false, result)
        }
    }

    @Nested
    inner class ReadNextDOB {

        @Test
        fun `accepting a valid date`() {
            provideInput("15-04-2000\n")

            val result = readNextDOB("DOB: ")

            assertEquals("15-04-2000", result)
        }

        @Test
        fun `rejecting incorrect date format`() {
            provideInput("2000-04-15\n15-04-2000\n")

            val result = readNextDOB("DOB: ")

            assertEquals("15-04-2000", result)
        }

        @Test
        fun `rejecting impossible dates`() {
            provideInput("35-19-2000\n15-04-2000\n")

            val result = readNextDOB("DOB: ")

            assertEquals("15-04-2000", result)
        }

        @Test
        fun `rejecting invalid leap year dates`() {
            provideInput("29-02-2023\n15-04-2000\n")

            val result = readNextDOB("DOB: ")

            assertEquals("15-04-2000", result)
        }

        @Test
        fun `accepting valid leap year dates`() {
            provideInput("29-02-2024\n")

            val result = readNextDOB("DOB: ")

            assertEquals("29-02-2024", result)
        }
    }
}
