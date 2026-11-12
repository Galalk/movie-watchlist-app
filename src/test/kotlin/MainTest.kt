import Persistance.JSONSerializer
import model.Actor
import model.Movie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import service.ActorService
import service.MovieService
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
    fun `runMenu opens watch status submenu for option 6`() {
        val output = captureOutput("6\n0\n0\n") { runMenu() }

        assertTrue(output.contains("Change Movie to Watched"))
        assertTrue(output.contains("Exiting App"))
    }

    @Test
    fun `runMenu opens search submenu for option 1`() {
        val output = captureOutput("1\n0\n0\n") { runMenu() }

        assertTrue(output.contains("Search for Movie"))
        assertTrue(output.contains("Exiting App"))
    }

    @Test
    fun `runMenu opens list submenu for option 5`() {
        val output = captureOutput("5\n0\n0\n") { runMenu() }

        assertTrue(output.contains("View All Movies"))
        assertTrue(output.contains("Exiting App"))
    }

    @Test
    fun `runMenu routes add movie and actor to their updated options`() {
        val title = "RunMenuMovie-${System.nanoTime()}"
        val actorName = "RunMenuActor-${System.nanoTime()}"
        val dateOfBirth = "17-08-1985"
        val input = listOf(
            "2", title, "Drama", "2021", "2.0",
            "3", actorName, dateOfBirth,
            "0"
        ).joinToString("\n") + "\n"

        val output = captureOutput(input) { runMenu() }

        assertTrue(output.contains("Movie successfully added to backlog!"))
        assertTrue(output.contains("Actor successfully added!"))
        assertTrue(movieService.searchMovie(title, 2021) != null)
        assertTrue(actorService.searchActor(actorName, dateOfBirth) != null)
    }

    @Test
    fun `runMenu routes actor association to option 4`() {
        val movie = Movie(title = "RunMenuLink-${System.nanoTime()}")
        val actorName = "RunMenuLinkActor-${System.nanoTime()}"
        val dateOfBirth = "17-08-1985"
        assertTrue(movieService.addMovie(movie))
        val actor = Actor(name = actorName, dateOfBirth = dateOfBirth)
        assertTrue(actorService.addActor(actor))
        val input = "4\n${movie.id}\n${actor.id}\n0\n"

        val output = captureOutput(input) { runMenu() }

        assertTrue(output.contains("Actor successfully added to movie"))
        assertTrue(output.contains("Exiting App"))
    }

    @Test
    fun `runMenu opens update and delete menus for options 7 and 8`() {
        val output = captureOutput("7\n0\n8\n0\n0\n") { runMenu() }

        assertTrue(output.contains("Update Movie"))
        assertTrue(output.contains("Delete Movie"))
        assertTrue(output.contains("Exiting App"))
    }

    @Test
    fun `updateMenu reports invalid option and returns`() {
        val output = runUpdateMenu("42\n0\n")

        assertTrue(output.contains("Invalid option."))
        assertTrue(output.contains("Returning to main menu"))
    }

    @Test
    fun `update and delete functions report when their collections are empty`() {
        val emptyMovieService = MovieService(JSONSerializer(File("unused-empty-movies.json")))
        val emptyActorService = ActorService(JSONSerializer(File("unused-empty-actors.json")))

        val updateMovieOutput = captureOutput("") { updateMovie(emptyMovieService) }
        val updateActorOutput = captureOutput("") { updateActor(emptyActorService) }
        val deleteMovieOutput = captureOutput("") { deleteMovie(emptyMovieService) }
        val deleteActorOutput = captureOutput("") { deleteActor(emptyActorService) }

        assertTrue(updateMovieOutput.contains("There are no movies to update."))
        assertTrue(updateActorOutput.contains("There are no actors to update."))
        assertTrue(deleteMovieOutput.contains("There are no movies to delete."))
        assertTrue(deleteActorOutput.contains("There are no actors to delete."))
    }

    @Test
    fun `updateMenu dispatches movie and actor updates`() {
        val movie = Movie(title = "MenuUpdateMovie-${System.nanoTime()}")
        val actor = Actor(name = "MenuUpdateActor-${System.nanoTime()}", dateOfBirth = "01-01-1980")
        assertTrue(movieService.addMovie(movie))
        assertTrue(actorService.addActor(actor))
        val input = listOf(
            "1", movie.id.toString(), "Menu Updated Movie", "Comedy", "2022", "2.0",
            "2", actor.id.toString(), "Menu Updated Actor", "02-02-1981",
            "0"
        ).joinToString("\n") + "\n"

        val output = runUpdateMenu(input)

        assertTrue(output.contains("Movie updated successfully."))
        assertTrue(output.contains("Actor updated successfully."))
        assertEquals("Menu Updated Movie", movie.title)
        assertEquals("Menu Updated Actor", actor.name)
    }

    @Test
    fun `updateMovie changes movie details`() {
        val movie = Movie(title = "Before-${System.nanoTime()}", genre = "Drama", releaseYear = 2000)
        assertTrue(movieService.addMovie(movie))

        val output = runUpdateMovie("${movie.id}\nUpdated title\nComedy\n2001\n2.5\n")

        assertEquals("Updated title", movie.title)
        assertEquals("Comedy", movie.genre)
        assertEquals(2001, movie.releaseYear)
        assertEquals(2.5, movie.duration)
        assertTrue(output.contains("Movie updated successfully."))
    }

    @Test
    fun `updateMovie reports unknown ID and does not update another movie`() {
        val original = Movie(title = "Existing-${System.nanoTime()}")
        assertTrue(movieService.addMovie(original))

        val output = runUpdateMovie("2147483647\nUpdated\nComedy\n2001\n2.5\n")

        assertTrue(output.contains("Movie not found."))
        assertTrue(original.title.startsWith("Existing-"))
    }

    @Test
    fun `updateMovie reports duplicate title and year`() {
        val originalTitle = "Original-${System.nanoTime()}"
        val original = Movie(title = originalTitle, releaseYear = 2000)
        val other = Movie(title = "DuplicateTarget-${System.nanoTime()}", releaseYear = 2020)
        assertTrue(movieService.addMovie(original))
        assertTrue(movieService.addMovie(other))

        val output = runUpdateMovie("${original.id}\n${other.title}\nDrama\n2020\n2.0\n")

        assertTrue(output.contains("Update failed."))
        assertEquals(originalTitle, original.title)
        assertEquals(2000, original.releaseYear)
    }

    @Test
    fun `updateActor changes actor name and date of birth`() {
        val actor = Actor(name = "Before-${System.nanoTime()}", dateOfBirth = "01-01-1980")
        assertTrue(actorService.addActor(actor))

        val output = runUpdateActor("${actor.id}\nUpdated Actor\n02-02-1981\n")

        assertEquals("Updated Actor", actor.name)
        assertEquals("02-02-1981", actor.dateOfBirth)
        assertTrue(output.contains("Actor updated successfully."))
    }

    @Test
    fun `updateActor reports unknown ID`() {
        val output = runUpdateActor("2147483647\nUpdated Actor\n02-02-1981\n")

        assertTrue(output.contains("Actor not found."))
    }

    @Test
    fun `updateActor reports duplicate name and date of birth`() {
        val original = Actor(name = "Original-${System.nanoTime()}", dateOfBirth = "01-01-1980")
        val other = Actor(name = "DuplicateTarget-${System.nanoTime()}", dateOfBirth = "02-02-1981")
        assertTrue(actorService.addActor(original))
        assertTrue(actorService.addActor(other))

        val output = runUpdateActor("${original.id}\n${other.name}\n02-02-1981\n")

        assertTrue(output.contains("Update failed."))
        assertTrue(original.name.startsWith("Original-"))
        assertEquals("01-01-1980", original.dateOfBirth)
    }

    @Test
    fun `deleteMenu reports invalid option and returns`() {
        val output = runDeleteMenu("42\n0\n")

        assertTrue(output.contains("Invalid option."))
        assertTrue(output.contains("Returning to main menu"))
    }

    @Test
    fun `deleteMenu dispatches movie and actor deletions`() {
        val movie = Movie(title = "MenuDeleteMovie-${System.nanoTime()}")
        val actor = Actor(name = "MenuDeleteActor-${System.nanoTime()}")
        assertTrue(movieService.addMovie(movie))
        assertTrue(actorService.addActor(actor))
        assertTrue(movieActorService.addActorToMovie(movie.id, actor.id))
        val input = "1\n${movie.id}\nYES\n2\n${actor.id}\nYES\n0\n"

        val output = runDeleteMenu(input)

        assertTrue(output.contains("Movie and its actor associations deleted successfully."))
        assertTrue(output.contains("Actor and their movie associations deleted successfully."))
        assertEquals(null, movieService.findMovieById(movie.id))
        assertEquals(null, actorService.findActorById(actor.id))
    }

    @Test
    fun `deleteMovie can be cancelled without removing movie or association`() {
        val movie = Movie(title = "CancelMovie-${System.nanoTime()}")
        val actor = Actor(name = "CancelActor-${System.nanoTime()}")
        assertTrue(movieService.addMovie(movie))
        assertTrue(actorService.addActor(actor))
        assertTrue(movieActorService.addActorToMovie(movie.id, actor.id))

        val output = runDeleteMovie("${movie.id}\nno\n")

        assertTrue(output.contains("Deletion cancelled."))
        assertTrue(movieService.findMovieById(movie.id) === movie)
        assertFalse(movieActorService.addActorToMovie(movie.id, actor.id))
    }

    @Test
    fun `deleteMovie confirmation removes movie and its associations`() {
        val movie = Movie(title = "DeleteMovie-${System.nanoTime()}")
        val actor = Actor(name = "KeepActor-${System.nanoTime()}")
        assertTrue(movieService.addMovie(movie))
        assertTrue(actorService.addActor(actor))
        assertTrue(movieActorService.addActorToMovie(movie.id, actor.id))

        val output = runDeleteMovie("${movie.id}\nYES\n")

        assertTrue(output.contains("Movie and its actor associations deleted successfully."))
        assertEquals(null, movieService.findMovieById(movie.id))
        assertTrue(movieActorService.addActorToMovie(movie.id, actor.id))
    }

    @Test
    fun `deleteMovie reports unknown ID`() {
        val output = runDeleteMovie("2147483647\n")

        assertTrue(output.contains("Movie not found."))
    }

    @Test
    fun `deleteActor confirmation removes actor and their associations`() {
        val movie = Movie(title = "KeepMovie-${System.nanoTime()}")
        val actor = Actor(name = "DeleteActor-${System.nanoTime()}")
        assertTrue(movieService.addMovie(movie))
        assertTrue(actorService.addActor(actor))
        assertTrue(movieActorService.addActorToMovie(movie.id, actor.id))

        val output = runDeleteActor("${actor.id}\nyes\n")

        assertTrue(output.contains("Actor and their movie associations deleted successfully."))
        assertEquals(null, actorService.findActorById(actor.id))
        assertTrue(movieActorService.addActorToMovie(movie.id, actor.id))
    }

    @Test
    fun `deleteActor can be cancelled and reports unknown IDs`() {
        val actor = Actor(name = "KeepActor-${System.nanoTime()}")
        assertTrue(actorService.addActor(actor))

        val cancelled = runDeleteActor("${actor.id}\nno\n")
        val missing = runDeleteActor("2147483647\n")

        assertTrue(cancelled.contains("Deletion cancelled."))
        assertTrue(actorService.findActorById(actor.id) === actor)
        assertTrue(missing.contains("Actor not found."))
    }

    @Test
    fun `searchMenu dispatches all search and filter options`() {
        val title = "SearchMenuMovie-${System.nanoTime()}"
        val genre = "SearchGenre-${System.nanoTime()}"
        val actorName = "SearchMenuActor-${System.nanoTime()}"
        val dateOfBirth = "17-08-1985"
        assertTrue(movieService.addMovie(Movie(title = title, genre = genre, releaseYear = 2021)))
        assertTrue(actorService.addActor(Actor(name = actorName, dateOfBirth = dateOfBirth)))
        val partialActorName = actorName.take(10)
        val input = listOf(
            "1", title, "2021",
            "2", actorName, dateOfBirth,
            "3", genre,
            "4", partialActorName,
            "0"
        ).joinToString("\n") + "\n"

        val output = runSearchMenu(input)

        assertTrue(output.contains(title))
        assertTrue(output.contains(actorName))
        assertTrue(output.contains("Search Menu"))
    }

    @Test
    fun `searchMenu handles invalid selection and returns to main menu`() {
        val output = runSearchMenu("99\n0\n")

        assertTrue(output.contains("Invalid option"))
        assertTrue(output.contains("Returning to main menu"))
    }

    @Test
    fun `search functions and filters handle no matches`() {
        val unique = "NoMatch-${System.nanoTime()}"
        val input = listOf(
            "1", unique, "2021",
            "2", unique, "17-08-1985",
            "3", unique,
            "4", unique,
            "0"
        ).joinToString("\n") + "\n"

        val output = runSearchMenu(input)

        assertTrue(output.contains("No movies found for this genre."))
        assertTrue(output.contains("No actors found."))
        assertFalse(output.contains("Movie ID:"))
        assertFalse(output.contains("Date of Birth:"))
    }

    @Test
    fun `addActorToMovie reports when movie ID is unknown`() {
        val output = runAddActorToMovie("2147483647\n")

        assertTrue(output.contains("Movie not found"))
    }

    @Test
    fun `addActorToMovie associates the actor selected by ID`() {
        val movie = Movie(title = "ActorLinkMovie-${System.nanoTime()}")
        val actorName = "ActorLink-${System.nanoTime()}"
        val firstActor = Actor(name = actorName, dateOfBirth = "17-08-1985")
        val selectedActor = Actor(name = actorName, dateOfBirth = "18-08-1986")
        assertTrue(movieService.addMovie(movie))
        assertTrue(actorService.addActor(firstActor))
        assertTrue(actorService.addActor(selectedActor))

        val input = "${movie.id}\n${selectedActor.id}\n"
        val firstOutput = runAddActorToMovie(input)
        val secondOutput = runAddActorToMovie(input)

        assertTrue(firstOutput.contains("Actor successfully added to movie"))
        assertTrue(secondOutput.contains("This actor is already associated with this movie"))
        assertFalse(movieActorService.addActorToMovie(movie.id, selectedActor.id))
        assertTrue(movieActorService.addActorToMovie(movie.id, firstActor.id))
    }

    @Test
    fun `addActorToMovie reports when actor ID is unknown`() {
        val movie = Movie(title = "ActorNotFoundMovie-${System.nanoTime()}")
        assertTrue(movieService.addMovie(movie))

        val output = runAddActorToMovie("${movie.id}\n2147483647\n")

        assertTrue(output.contains("Actor not found"))
    }

    @Test
    fun `addActor reads date of birth and adds actor`() {
        val name = "ActorFromMenu-${System.nanoTime()}"
        val dateOfBirth = "25-03-1990"

        val output = runAddActor("$name\n$dateOfBirth\n")
        val actor = actorService.searchActor(name, dateOfBirth)

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

    private fun runSearchMenu(input: String): String {
        return captureOutput(input) { searchMenu() }
    }

    private fun runAddActorToMovie(input: String): String {
        return captureOutput(input) { addActorToMovie() }
    }

    private fun runUpdateMenu(input: String): String {
        return captureOutput(input) { updateMenu() }
    }

    private fun runUpdateMovie(input: String): String {
        return captureOutput(input) { updateMovie() }
    }

    private fun runUpdateActor(input: String): String {
        return captureOutput(input) { updateActor() }
    }

    private fun runDeleteMenu(input: String): String {
        return captureOutput(input) { deleteMenu() }
    }

    private fun runDeleteMovie(input: String): String {
        return captureOutput(input) { deleteMovie() }
    }

    private fun runDeleteActor(input: String): String {
        return captureOutput(input) { deleteActor() }
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
