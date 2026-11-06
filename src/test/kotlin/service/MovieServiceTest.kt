
package service

import Persistance.JSONSerializer
import model.Movie
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MovieServiceTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var emptyMovieService: MovieService
    private lateinit var populatedMovieService: MovieService

    private lateinit var movie1: Movie
    private lateinit var movie2: Movie

    @BeforeEach
    fun setUp() {
        emptyMovieService = MovieService(
            JSONSerializer(File(tempDir, "empty-movies.json"))
        )

        populatedMovieService = MovieService(
            JSONSerializer(File(tempDir, "movies.json"))
        )

        movie1 = Movie(0, "Gladiator")
        movie2 = Movie(0, "Inception")

        populatedMovieService.addMovie(movie1)
        populatedMovieService.addMovie(movie2)
    }

    @Nested
    inner class AddMovieTests {

        @Test
        fun `adding movie increases collection size`() {
            assertEquals(0, emptyMovieService.getMovies().size)

            emptyMovieService.addMovie(Movie(0, "Interstellar"))

            assertEquals(1, emptyMovieService.getMovies().size)
        }

        @Test
        fun `adding movie assigns an ID`() {
            emptyMovieService.addMovie(movie1)

            assertEquals(0, movie1.id)
        }

        @Test
        fun `adding multiple movies assigns unique IDs`() {
            assertEquals(0, movie1.id)
            assertEquals(1, movie2.id)
        }

        @Test
        fun `adding movie stores correct information`() {
            emptyMovieService.addMovie(movie1)

            val retrievedMovie = emptyMovieService.getMovies().first()

            assertEquals("Gladiator", retrievedMovie.title)
        }
    }

    @Nested
    inner class PersistenceTests {

        @Test
        fun `saving and loading empty movies does not crash`() {
            emptyMovieService.store()

            val loadedService = MovieService(
                JSONSerializer(File(tempDir, "empty-movies.json"))
            )

            loadedService.load()

            assertEquals(0, loadedService.getMovies().size)
        }

        @Test
        fun `saving and loading movies preserves data`() {
            populatedMovieService.store()

            val loadedService = MovieService(
                JSONSerializer(File(tempDir, "movies.json"))
            )

            loadedService.load()

            assertEquals(2, loadedService.getMovies().size)
            assertEquals("Gladiator", loadedService.getMovies()[0].title)
            assertEquals("Inception", loadedService.getMovies()[1].title)
        }

        @Test
        fun `loading movies restores next available ID`() {
            populatedMovieService.store()

            val loadedService = MovieService(
                JSONSerializer(File(tempDir, "movies.json"))
            )

            loadedService.load()

            val newMovie = Movie(0, "Interstellar")
            loadedService.addMovie(newMovie)

            assertEquals(2, newMovie.id)
        }
    }
}
