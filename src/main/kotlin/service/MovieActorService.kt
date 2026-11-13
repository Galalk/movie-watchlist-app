package service

import model.MovieActor
import persistance.Serializer

/**
 * This class manages a list of movie and actor associations and provides functionality for adding, deleting those
 * associations. It uses a [Serializer] to load and store the movies persistently.
 *
 * @property serializer A serializer instance for reading and writing the movies.
 * @constructor Initializes the MovieService with the specified [serializerType].
 */

class MovieActorService(
    serializerType: Serializer,
) {
    private var serializer: Serializer = serializerType

    private var movieActors = ArrayList<MovieActor>()

    fun addActorToMovie(
        movieId: Int,
        actorId: Int,
    ): Boolean {
        if (movieActors.any {
                it.movieId == movieId && it.actorId == actorId
            }
        ) {
            return false
        }

        movieActors.add(
            MovieActor(movieId, actorId),
        )

        return true
    }

    fun deleteAssociationsForMovie(movieId: Int): Boolean =
        movieActors.removeAll {
            it.movieId == movieId
        }

    fun deleteAssociationsForActor(actorId: Int): Boolean =
        movieActors.removeAll {
            it.actorId == actorId
        }

    fun numberOfMoviesPerActor(actorId: Int) = movieActors.count { it.actorId == actorId }

    fun movieWithMostActors() =
        movieActors
            .groupingBy { it.movieId }
            .eachCount()
            .maxByOrNull { it.value }

    fun averageActorsPerMovie(totalMovies: Int): Double {
        if (totalMovies == 0) return 0.0

        return movieActors.size.toDouble() / totalMovies
    }

    fun getActorIdsForMovie(movieId: Int): List<Int> =
        movieActors
            .filter { it.movieId == movieId }
            .map { it.actorId }

    fun getMovieIdsForActor(actorId: Int): List<Int> =
        movieActors
            .filter { it.actorId == actorId }
            .map { it.movieId }

    fun removeActorFromMovie(
        movieId: Int,
        actorId: Int,
    ): Boolean =
        movieActors.removeAll {
            it.movieId == movieId && it.actorId == actorId
        }

    fun load() {
        val array = serializer.read(Array<MovieActor>::class.java)
        movieActors = array.toCollection(ArrayList())
    }

    fun store() {
        serializer.write(movieActors)
    }
}
