
package service

import Persistance.JSONSerializer
import Persistance.Serializer
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

        assertTrue(populatedMovieService.addMovie(movie1))
        assertTrue(populatedMovieService.addMovie(movie2))
    }

    @Nested
    inner class AddMovieTests {

        @Test
        fun `adding movie increases collection size`() {
            assertEquals(0, emptyMovieService.getMovies().size)

            assertTrue(emptyMovieService.addMovie(Movie(0, "Interstellar")))

            assertEquals(1, emptyMovieService.getMovies().size)
        }

        @Test
        fun `adding movie assigns an ID`() {
            assertTrue(emptyMovieService.addMovie(movie1))

            assertEquals(0, movie1.id)
        }

        @Test
        fun `adding multiple movies assigns unique IDs`() {
            assertEquals(0, movie1.id)
            assertEquals(1, movie2.id)
        }

        @Test
        fun `adding movie stores correct information`() {
            assertTrue(emptyMovieService.addMovie(movie1))

            val retrievedMovie = emptyMovieService.getMovies().first()

            assertEquals("Gladiator", retrievedMovie.title)
        }

        @Test
        fun `adding movie resets finished status`() {
            val finishedMovie = Movie(0, "Gladiator", finished = true)

            assertTrue(emptyMovieService.addMovie(finishedMovie))

            assertFalse(finishedMovie.finished)
        }

        @Test
        fun `adding movie with same title and release year is rejected ignoring case`() {
            val original = Movie(title = "The Thing", releaseYear = 1982)
            val duplicate = Movie(title = "  tHe ThInG  ", releaseYear = 1982)
            val service = MovieService(JSONSerializer(File(tempDir, "duplicates.json")))
            assertTrue(service.addMovie(original))

            assertFalse(service.addMovie(duplicate))

            assertEquals(1, service.getMovies().size)
            val nextMovie = Movie(title = "Another movie", releaseYear = 2020)
            assertTrue(service.addMovie(nextMovie))
            assertEquals(1, nextMovie.id)
        }

        @Test
        fun `same title with different release year is accepted as a remake`() {
            val original = Movie(title = "A Star Is Born", releaseYear = 1954)
            val remake = Movie(title = "A Star Is Born", releaseYear = 2018)
            val service = MovieService(JSONSerializer(File(tempDir, "remakes.json")))

            assertTrue(service.addMovie(original))
            assertTrue(service.addMovie(remake))

            assertEquals(2, service.getMovies().size)
            assertEquals(0, original.id)
            assertEquals(1, remake.id)
        }
    }

    @Nested
    inner class SearchMovieTests {

        @Test
        fun `finds movie by its ID`() {
            assertSame(movie2, populatedMovieService.findMovieById(1))
        }

        @Test
        fun `returns null when movie ID does not exist`() {
            assertNull(populatedMovieService.findMovieById(99))
        }

        @Test
        fun `searches movie by case insensitive title and release year`() {
            val movie = Movie(title = "The Matrix", releaseYear = 1999)
            val service = MovieService(JSONSerializer(File(tempDir, "find-movie.json")))
            assertTrue(service.addMovie(movie))

            assertSame(movie, service.searchMovie("tHe MaTrIx", 1999))
        }

        @Test
        fun `returns null when title or release year does not match`() {
            val movie = Movie(title = "The Matrix", releaseYear = 1999)
            val service = MovieService(JSONSerializer(File(tempDir, "find-movie.json")))
            assertTrue(service.addMovie(movie))

            assertNull(service.searchMovie("Unknown", 1999))
            assertNull(service.searchMovie("The Matrix", 2003))
        }

        @Test
        fun `searches for a movie with no release year`() {
            val movie = Movie(title = "Untitled Release")
            val service = MovieService(JSONSerializer(File(tempDir, "no-release-year.json")))
            assertTrue(service.addMovie(movie))

            assertSame(movie, service.searchMovie("Untitled Release", null))
        }
    }

    @Nested
    inner class FilterMoviesTests {

        @Test
        fun `filters movies by exact genre ignoring case`() {
            val movie = Movie(title = "Arrival", genre = "Science Fiction")
            assertTrue(emptyMovieService.addMovie(movie))
            assertTrue(emptyMovieService.addMovie(Movie(title = "Alien", genre = "Horror")))

            assertEquals(listOf(movie), emptyMovieService.filterMoviesByGenre("science fiction"))
        }

        @Test
        fun `returns empty list when no movie has the requested genre`() {
            assertTrue(emptyMovieService.addMovie(Movie(title = "Arrival", genre = "Science Fiction")))

            assertTrue(emptyMovieService.filterMoviesByGenre("Comedy").isEmpty())
        }
    }

    @Nested
    inner class MovieStatusTests {

        @Test
        fun `marking an existing movie watched updates it and stores the list`() {
            val serializer = CapturingSerializer()
            val service = MovieService(serializer)
            val movie = Movie(title = "Arrival")
            assertTrue(service.addMovie(movie))

            assertTrue(service.markMovieAsWatched(movie.id))

            assertTrue(movie.finished)
            assertSame(service.getMovies(), serializer.writtenObject)
            assertEquals(1, serializer.writeCalls)
        }

        @Test
        fun `marking unknown movie watched returns false without storing`() {
            val serializer = CapturingSerializer()
            val service = MovieService(serializer)

            assertFalse(service.markMovieAsWatched(99))

            assertEquals(0, serializer.writeCalls)
        }

        @Test
        fun `reverting existing movie to watch updates it and stores the list`() {
            val serializer = CapturingSerializer()
            val service = MovieService(serializer)
            val movie = Movie(title = "Arrival")
            assertTrue(service.addMovie(movie))
            movie.finished = true

            assertTrue(service.markMovieAsToWatch(movie.id))

            assertFalse(movie.finished)
            assertSame(service.getMovies(), serializer.writtenObject)
            assertEquals(1, serializer.writeCalls)
        }

        @Test
        fun `reverting unknown movie returns false without storing`() {
            val serializer = CapturingSerializer()
            val service = MovieService(serializer)

            assertFalse(service.markMovieAsToWatch(99))

            assertEquals(0, serializer.writeCalls)
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
            assertTrue(loadedService.addMovie(newMovie))

            assertEquals(2, newMovie.id)
        }

        @Test
        fun `loading empty movies resets next ID to zero`() {
            val serializer = FixedReadSerializer(emptyArray())
            val service = MovieService(serializer)

            service.load()
            val newMovie = Movie(0, "First movie")
            assertTrue(service.addMovie(newMovie))

            assertEquals(0, newMovie.id)
            assertEquals(1, service.getMovies().size)
        }

        @Test
        fun `store passes current movies to serializer`() {
            val serializer = CapturingSerializer()
            val service = MovieService(serializer)
            val movie = Movie(0, "Arrival")
            assertTrue(service.addMovie(movie))

            service.store()

            assertSame(service.getMovies(), serializer.writtenObject)
        }
    }

    private class CapturingSerializer : Serializer {
        var writtenObject: Any? = null
        var writeCalls: Int = 0

        override fun write(obj: Any?) {
            writtenObject = obj
            writeCalls++
        }

        override fun <T> read(classType: Class<T>): T = error("read should not be called")
    }

    private class FixedReadSerializer(private val movies: Array<Movie>) : Serializer {
        override fun write(obj: Any?) = Unit

        @Suppress("UNCHECKED_CAST")
        override fun <T> read(classType: Class<T>): T = movies as T
    }
}
