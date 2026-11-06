package service

import Persistance.JSONSerializer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class MovieActorServiceTest {

    private lateinit var movieActorService: MovieActorService

    @BeforeEach
    fun setup() {
        movieActorService = MovieActorService(
            JSONSerializer(File("test-movie-actors.json"))
        )
    }

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
}
