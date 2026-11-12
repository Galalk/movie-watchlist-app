package model

data class Movie(
    var id: Int = 0,
    var title: String = "",
    var genre: String = "",
    var releaseYear: Int? = null,
    var duration: Double = 0.0,
    var finished: Boolean = false,
)

{
    override fun toString(): String {
        return """
        ------------------------------
        | Movie ID: $id
        | Title: $title
        | Genre: $genre
        | Release Year: $releaseYear
        | Duration: $duration Hours
        | Status: ${if (finished) "Watched" else "To Watch"}
        ------------------------------
    """.trimIndent()
    }
}
