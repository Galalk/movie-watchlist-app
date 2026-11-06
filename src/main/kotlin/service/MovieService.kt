package service

import Persistance.Serializer
import model.Movie

class MovieService(serializerType: Serializer) {

    private var serializer: Serializer = serializerType
    private var movies = ArrayList<Movie>()

    private var lastId = 0
    private fun getId(): Int = lastId++

    fun getMovies(): List<Movie> = movies

    fun addMovie(movie: Movie) {
        movie.id = getId()
        movie.finished = false
        movies.add(movie)
    }

    fun findMovieById(movieId: Int): Movie? {
        return movies.find { it.id == movieId }
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