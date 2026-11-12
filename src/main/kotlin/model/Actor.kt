package model

data class Actor(
    var id: Int = 0,
    var name: String = "",
    var dateOfBirth: String = ""
)

{
    override fun toString(): String {
        return """
        ------------------------------
        | Name: $name
        | Date of Birth: $dateOfBirth
        ------------------------------
    """.trimIndent()
    }
}
