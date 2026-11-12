package service

import Persistance.JSONSerializer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MovieActorServiceTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var movieActorService: MovieActorService

    @BeforeEach
    fun setup() {
        movieActorService = MovieActorService(
            JSONSerializer(File(tempDir, "movie-actors.json"))
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
        fun `adding the same actor to the same movie twice returns false`() {
            movieActorService.addActorToMovie(1, 1)

            val result = movieActorService.addActorToMovie(1, 1)

            assertFalse(result)
        }

        @Test
        fun `different actors can be added to the same movie`() {
            assertTrue(movieActorService.addActorToMovie(1, 1))
            assertTrue(movieActorService.addActorToMovie(1, 2))
        }

        @Test
        fun `the same actor can be added to different movies`() {
            assertTrue(movieActorService.addActorToMovie(1, 1))
            assertTrue(movieActorService.addActorToMovie(2, 1))
        }

        @Test
        fun `storing and loading associations preserves them and duplicate protection`() {
            assertTrue(movieActorService.addActorToMovie(12, 34))
            assertTrue(movieActorService.addActorToMovie(56, 78))

            movieActorService.store()
            val loadedService = MovieActorService(
                JSONSerializer(File(tempDir, "movie-actors.json"))
            )
            loadedService.load()

            assertFalse(loadedService.addActorToMovie(12, 34))
            assertFalse(loadedService.addActorToMovie(56, 78))
            assertTrue(loadedService.addActorToMovie(12, 78))
        }

        @Test
        fun `storing and loading an empty association list allows adding`() {
            movieActorService.store()
            val loadedService = MovieActorService(
                JSONSerializer(File(tempDir, "movie-actors.json"))
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
}
