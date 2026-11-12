package service

import Persistance.Serializer
import model.Movie

class MovieService(serializerType: Serializer) {

    private var serializer: Serializer = serializerType
    private var movies = ArrayList<Movie>()

    private var lastId = 0
    private fun getId(): Int = lastId++

    fun getMovies(): List<Movie> = movies

    fun addMovie(movie: Movie): Boolean {

        if (movies.any {
                it.title.equals(movie.title.trim(), ignoreCase = true) &&
                        it.releaseYear == movie.releaseYear
            }) {
            return false
        }

        movie.id = getId()
        movie.finished = false
        movies.add(movie)

        return true
    }

    fun findMovieById(movieId: Int): Movie? {
        return movies.find { it.id == movieId }
    }

    fun searchMovie(title: String, releaseYear: Int?): Movie? {
        return movies.firstOrNull {
            it.title.equals(title, ignoreCase = true) &&
                    it.releaseYear == releaseYear
        }
    }

    fun filterMoviesByGenre(genre: String): List<Movie> {
        return movies.filter {
            it.genre.equals(genre, ignoreCase = true)
        }
    }

    fun markMovieAsWatched(movieId: Int): Boolean {

        val movie = movies.find { it.id == movieId } ?: return false

        movie.finished = true
        store()

        return true
    }

    fun markMovieAsToWatch(movieId: Int): Boolean {
        val movie = movies.find { it.id == movieId } ?: return false

        movie.finished = false
        store()
        return true
    }

    fun updateMovie(movieId: Int, title: String, genre: String, releaseYear: Int?, duration: Double): Boolean {
        val movie = findMovieById(movieId) ?: return false

        if (movies.any {
                it.id != movieId &&
                        it.title.equals(title.trim(), ignoreCase = true) &&
                        it.releaseYear == releaseYear
            }) {
            return false
        }

        movie.title = title.trim()
        movie.genre = genre.trim()
        movie.releaseYear = releaseYear
        movie.duration = duration

        return true
    }

    fun deleteMovie(movieId: Int): Boolean {
        val movie = findMovieById(movieId) ?: return false
        movies.remove(movie)
        return true
    }

    fun load() {
        val array = serializer.read(Array<Movie>::class.java)
        movies = array.toCollection(ArrayList())
        lastId = (movies.maxOfOrNull { it.id } ?: -1) + 1 // Bug fix
    }

    fun store() {
        serializer.write(movies)
    }
}