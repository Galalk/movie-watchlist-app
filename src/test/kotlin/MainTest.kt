import model.Actor
import model.Movie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files

class MainTest {

    @Test
    fun `watch status submenu marks a movie watched`() {
        val title = "WatchFromMenu-${System.nanoTime()}"
        val movie = Movie(title = title, releaseYear = 2020)
        assertTrue(movieService.addMovie(movie))

        val output = withMoviesFileRestored {
            runStatusMenu("1\n${movie.id}\n0\n")
        }

        assertTrue(movie.finished)
        assertTrue(output.contains("Movie successfully added to watched list"))
        assertTrue(output.contains("Returning to main menu"))
    }

    @Test
    fun `watch status submenu reverts a movie to to watch`() {
        val title = "RevertFromMenu-${System.nanoTime()}"
        val movie = Movie(title = title, releaseYear = 2018)
        assertTrue(movieService.addMovie(movie))
        movie.finished = true

        val output = withMoviesFileRestored {
            runStatusMenu("2\n${movie.id}\n0\n")
        }

        assertFalse(movie.finished)
        assertTrue(output.contains("Movie successfully added back into to your backlog!"))
    }

    @Test
    fun `watch status submenu reports invalid option and accepts back option`() {
        val output = runStatusMenu("42\n0\n")

        assertTrue(output.contains("Invalid option!"))
        assertTrue(output.contains("Returning to main menu"))
    }

    @Test
    fun `runMenu opens watch status submenu for option 5`() {
        val output = captureOutput("5\n0\n0\n") { runMenu() }

        assertTrue(output.contains("Change Movie to Watched"))
        assertTrue(output.contains("Exiting App"))
    }

    @Test
    fun `addActor reads date of birth and adds actor`() {
        val name = "ActorFromMenu-${System.nanoTime()}"
        val dateOfBirth = "25-03-1990"

        val output = runAddActor("$name\n$dateOfBirth\n")
        val actor = actorService.findActorByName(name)

        assertTrue(output.contains("Date of Birth (DD-MM-YYYY)"))
        assertTrue(output.contains("Actor successfully added!"))
        assertEquals(dateOfBirth, actor?.dateOfBirth)
    }

    @Test
    fun `addActor reports a duplicate actor instead of success`() {
        val name = "DuplicateActorFromMenu-${System.nanoTime()}"
        val dateOfBirth = "25-03-1990"
        assertTrue(actorService.addActor(Actor(name = name, dateOfBirth = dateOfBirth)))

        val output = runAddActor("$name\n$dateOfBirth\n")

        assertTrue(output.contains("This actor already exists!"))
        assertFalse(output.contains("Actor successfully added!"))
    }

    @Test
    fun `save and load persist movie actor associations`() {
        val idBase = (System.nanoTime() % 1_000_000).toInt() + 1_000_000
        val savedMovieId = idBase
        val savedActorId = idBase + 1
        val unsavedMovieId = idBase + 2
        val unsavedActorId = idBase + 3

        withApplicationFilesRestored {
            assertTrue(movieActorService.addActorToMovie(savedMovieId, savedActorId))
            save()
            assertTrue(movieActorService.addActorToMovie(unsavedMovieId, unsavedActorId))

            load()

            assertFalse(movieActorService.addActorToMovie(savedMovieId, savedActorId))
            assertTrue(movieActorService.addActorToMovie(unsavedMovieId, unsavedActorId))
        }
    }

    @Test
    fun `addMovieToWatched reports a movie that cannot be found`() {
        val output = runAddMovieToWatched("2147483647\n")

        assertTrue(output.contains("Movie not found!"))
    }

    @Test
    fun `addMovieToWatched reports when movie is already watched`() {
        val title = "AlreadyWatched-${System.nanoTime()}"
        val movie = Movie(title = title, releaseYear = 2020)
        assertTrue(movieService.addMovie(movie))
        movie.finished = true

        val output = runAddMovieToWatched("${movie.id}\n")

        assertTrue(output.contains("This movie is already marked as watched!"))
    }

    @Test
    fun `revertMovieStatus reports a movie that cannot be found`() {
        val output = runRevertMovieStatus("2147483647\n")

        assertTrue(output.contains("Movie not found!"))
    }

    @Test
    fun `revertMovieStatus reports when movie is already to watch`() {
        val title = "AlreadyToWatch-${System.nanoTime()}"
        val movie = Movie(title = title, releaseYear = 2020)
        assertTrue(movieService.addMovie(movie))

        val output = runRevertMovieStatus("${movie.id}\n")

        assertTrue(output.contains("This movie is already marked as To Watch!"))
    }

    @Test
    fun `watch status submenu changes only the movie with the selected ID`() {
        val title = "SameTitleRemake-${System.nanoTime()}"
        val original = Movie(title = title, releaseYear = 1982)
        val remake = Movie(title = title, releaseYear = 2018)
        assertTrue(movieService.addMovie(original))
        assertTrue(movieService.addMovie(remake))

        withMoviesFileRestored {
            runStatusMenu("1\n${remake.id}\n0\n")
        }

        assertFalse(original.finished)
        assertTrue(remake.finished)
    }

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
        val movie = Movie(title = title)
        assertTrue(movieService.addMovie(movie))

        val output = runListAll("1\n0\n")

        assertTrue(output.contains(title))
        assertTrue(output.contains("Movie ID: ${movie.id}"))
    }

    @Test
    fun `submenu option 2 lists all actors`() {
        val name = "ListAllActor-${System.nanoTime()}"
        val dateOfBirth = "25-03-1990"
        assertTrue(actorService.addActor(Actor(name = name, dateOfBirth = dateOfBirth)))

        val output = runListAll("2\n0\n")

        assertTrue(output.contains(name))
        assertTrue(output.contains("Date of Birth: $dateOfBirth"))
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

    private fun runAddActor(input: String): String {
        return captureOutput(input) { addActor() }
    }

    private fun runStatusMenu(input: String): String {
        return captureOutput(input) { changeMovieStatus() }
    }

    private fun runAddMovieToWatched(input: String): String {
        return captureOutput(input) { addMovieToWatched() }
    }

    private fun runRevertMovieStatus(input: String): String {
        return captureOutput(input) { revertMovieStatus() }
    }

    private fun <T> withMoviesFileRestored(action: () -> T): T {
        return withFilesRestored(listOf("movies.json"), action)
    }

    private fun <T> withApplicationFilesRestored(action: () -> T): T {
        return withFilesRestored(
            listOf("movies.json", "actors.json", "movieActors.json"),
            action
        )
    }

    private fun <T> withFilesRestored(fileNames: List<String>, action: () -> T): T {
        val originalContents = fileNames.associateWith { name ->
            File(name).takeIf { it.exists() }?.readBytes()
        }
        return try {
            action()
        } finally {
            originalContents.forEach { (name, contents) ->
                val file = File(name)
                if (contents == null) {
                    Files.deleteIfExists(file.toPath())
                } else {
                    Files.write(file.toPath(), contents)
                }
            }
        }
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
