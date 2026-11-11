import model.Actor
import model.Movie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {

    @Test
    fun `addMovie reports and rejects a duplicate title and release year`() {
        val title = "DuplicateFromMenu-${System.nanoTime()}"
        assertTrue(movieService.addMovie(Movie(title = title, releaseYear = 1982)))
        val movieCountBefore = movieService.getMovies().size

        val output = runAddMovie("${title.uppercase()}\nAction\n1982\n2.0\n")

        assertTrue(output.contains("This movie already exists in your backlog!"))
        assertEquals(movieCountBefore, movieService.getMovies().size)
    }

    @Test
    fun `addMovie accepts same title with a different year as a remake`() {
        val title = "RemakeFromMenu-${System.nanoTime()}"
        assertTrue(movieService.addMovie(Movie(title = title, releaseYear = 1982)))
        val movieCountBefore = movieService.getMovies().size

        val output = runAddMovie("$title\nDrama\n2018\n2.0\n")

        assertTrue(output.contains("Movie successfully added to backlog"))
        assertEquals(movieCountBefore + 1, movieService.getMovies().size)
    }

    @Test
    fun `submenu option 1 lists all movies`() {
        val title = "ListAllMovie-${System.nanoTime()}"
        assertTrue(movieService.addMovie(Movie(title = title)))

        val output = runListAll("1\n0\n")

        assertTrue(output.contains(title))
    }

    @Test
    fun `submenu option 2 lists all actors`() {
        val name = "ListAllActor-${System.nanoTime()}"
        actorService.addActor(Actor(name = name))

        val output = runListAll("2\n0\n")

        assertTrue(output.contains(name))
    }

    @Test
    fun `submenu option 3 lists watched movies only`() {
        val watchedTitle = "Watched-${System.nanoTime()}"
        val unwatchedTitle = "Unwatched-${System.nanoTime()}"
        val watchedMovie = Movie(title = watchedTitle)
        assertTrue(movieService.addMovie(watchedMovie))
        watchedMovie.finished = true
        assertTrue(movieService.addMovie(Movie(title = unwatchedTitle)))

        val output = runListAll("3\n0\n")

        assertTrue(output.contains(watchedTitle))
        assertFalse(output.contains(unwatchedTitle))
    }

    @Test
    fun `invalid submenu option reports the option and continues until back`() {
        val output = runListAll("42\n0\n")

        assertTrue(output.contains("Invalid option entered: 42"))
        assertTrue(output.contains("Returning to main menu"))
        assertTrue(output.contains("View All Movies"))
    }

    private fun runListAll(input: String): String {
        return captureOutput(input) { listAll() }
    }

    private fun runAddMovie(input: String): String {
        return captureOutput(input) { addMovie() }
    }

    private fun captureOutput(input: String, action: () -> Unit): String {
        val originalIn = System.`in`
        val originalOut = System.out
        val output = ByteArrayOutputStream()
        try {
            System.setIn(ByteArrayInputStream(input.toByteArray()))
            System.setOut(PrintStream(output))
            action()
        } finally {
            System.setIn(originalIn)
            System.setOut(originalOut)
        }
        return output.toString()
    }
}
