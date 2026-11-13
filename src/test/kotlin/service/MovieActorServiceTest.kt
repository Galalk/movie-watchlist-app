package service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import persistance.JSONSerializer
import java.io.File

class MovieActorServiceTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var movieActorService: MovieActorService

    @BeforeEach
    fun setup() {
        movieActorService =
            MovieActorService(
                JSONSerializer(File(tempDir, "movie-actors.json")),
            )
    }

    @Nested
    inner class AddMovieActors {
        @Test
        fun `adding an actor to a movie returns true`() {
            val result = movieActorService.addActorToMovie(1, 1)

            assertTrue(result)
        }

        @Test
        fun `storing and loading associations preserves them and duplicate protection`() {
            assertTrue(movieActorService.addActorToMovie(12, 34))
            assertTrue(movieActorService.addActorToMovie(56, 78))

            movieActorService.store()
            val loadedService =
                MovieActorService(
                    JSONSerializer(File(tempDir, "movie-actors.json")),
                )
            loadedService.load()

            assertFalse(loadedService.addActorToMovie(12, 34))
            assertFalse(loadedService.addActorToMovie(56, 78))
            assertTrue(loadedService.addActorToMovie(12, 78))
        }

        @Test
        fun `storing and loading an empty association list allows adding`() {
            movieActorService.store()
            val loadedService =
                MovieActorService(
                    JSONSerializer(File(tempDir, "movie-actors.json")),
                )

            loadedService.load()

            assertTrue(loadedService.addActorToMovie(12, 34))
        }

        @Test
        fun `deleting associations for a movie removes only that movie links`() {
            assertTrue(movieActorService.addActorToMovie(1, 10))
            assertTrue(movieActorService.addActorToMovie(1, 11))
            assertTrue(movieActorService.addActorToMovie(2, 10))

            assertTrue(movieActorService.deleteAssociationsForMovie(1))

            assertFalse(movieActorService.deleteAssociationsForMovie(1))
            assertTrue(movieActorService.addActorToMovie(1, 10))
            assertFalse(movieActorService.addActorToMovie(2, 10))
        }

        @Test
        fun `deleting associations for an actor removes only that actor links`() {
            assertTrue(movieActorService.addActorToMovie(1, 10))
            assertTrue(movieActorService.addActorToMovie(2, 10))
            assertTrue(movieActorService.addActorToMovie(1, 11))

            assertTrue(movieActorService.deleteAssociationsForActor(10))

            assertFalse(movieActorService.deleteAssociationsForActor(10))
            assertTrue(movieActorService.addActorToMovie(1, 10))
            assertFalse(movieActorService.addActorToMovie(1, 11))
        }
    }

    @Nested
    inner class ValidationTest {
        @Test
        fun `rejects adding the same actor to the same movie twice`() {
            assertTrue(movieActorService.addActorToMovie(1, 1))

            assertFalse(movieActorService.addActorToMovie(1, 1))
        }

        @Test
        fun `allows different actors to be added to the same movie`() {
            assertTrue(movieActorService.addActorToMovie(1, 1))

            assertTrue(movieActorService.addActorToMovie(1, 2))
        }

        @Test
        fun `allows the same actor to be added to different movies`() {
            assertTrue(movieActorService.addActorToMovie(1, 1))

            assertTrue(movieActorService.addActorToMovie(2, 1))
        }
    }

    @Nested
    inner class ReportTests {
        @Test
        fun `reports zero associations and zero average when no movies exist`() {
            assertEquals(0, movieActorService.numberOfMoviesPerActor(7))
            assertNull(movieActorService.movieWithMostActors())
            assertEquals(0.0, movieActorService.averageActorsPerMovie(0))
        }

        @Test
        fun `reports actor counts per movie and movie with most actors`() {
            assertTrue(movieActorService.addActorToMovie(10, 1))
            assertTrue(movieActorService.addActorToMovie(10, 2))
            assertTrue(movieActorService.addActorToMovie(20, 1))

            assertEquals(2, movieActorService.numberOfMoviesPerActor(1))
            assertEquals(1, movieActorService.numberOfMoviesPerActor(2))
            assertEquals(0, movieActorService.numberOfMoviesPerActor(99))
            assertEquals(10, movieActorService.movieWithMostActors()?.key)
            assertEquals(2, movieActorService.movieWithMostActors()?.value)
        }

        @Test
        fun `selects later movie when it has more actors and retains first on tie`() {
            assertTrue(movieActorService.addActorToMovie(10, 1))
            assertTrue(movieActorService.addActorToMovie(20, 1))
            assertTrue(movieActorService.addActorToMovie(20, 2))
            assertTrue(movieActorService.addActorToMovie(20, 3))
            assertTrue(movieActorService.addActorToMovie(30, 1))
            assertTrue(movieActorService.addActorToMovie(30, 2))
            assertTrue(movieActorService.addActorToMovie(30, 3))

            val report = movieActorService.movieWithMostActors()

            assertEquals(20, report?.key)
            assertEquals(3, report?.value)
        }

        @Test
        fun `reports the only movie that has an actor association`() {
            assertTrue(movieActorService.addActorToMovie(42, 7))

            val report = movieActorService.movieWithMostActors()

            assertEquals(42, report?.key)
            assertEquals(1, report?.value)
        }

        @Test
        fun `reports average actor associations per movie`() {
            assertTrue(movieActorService.addActorToMovie(10, 1))
            assertTrue(movieActorService.addActorToMovie(10, 2))
            assertTrue(movieActorService.addActorToMovie(20, 3))

            assertEquals(1.5, movieActorService.averageActorsPerMovie(2))
        }
    }
}
