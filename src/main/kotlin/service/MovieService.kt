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

    fun findMovie(title: String, releaseYear: Int): Movie? {
        return movies.find {
            it.title.equals(title.trim(), ignoreCase = true) &&
                    it.releaseYear == releaseYear
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

    fun load() {
        val array = serializer.read(Array<Movie>::class.java)
        movies = array.toCollection(ArrayList())
        lastId = (movies.maxOfOrNull { it.id } ?: -1) + 1 // Bug fix
    }

    fun store() {
        serializer.write(movies)
    }
}