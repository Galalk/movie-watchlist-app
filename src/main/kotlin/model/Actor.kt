package model

data class Actor(
    var id: Int = 0,
    var name: String = "",
)

{
    override fun toString(): String {
        return """
        ------------------------------
        | Name: $name
        ------------------------------
    """.trimIndent()
    }
}
