package service

import Persistance.Serializer
import model.MovieActor

class MovieActorService(serializerType: Serializer) {

    private var serializer: Serializer = serializerType

    private var movieActors = ArrayList<MovieActor>()

    fun addActorToMovie(movieId: Int, actorId: Int): Boolean {

        if (movieActors.any {
                it.movieId == movieId && it.actorId == actorId
            }) {
            return false
        }

        movieActors.add(
            MovieActor(movieId, actorId)
        )

        return true
    }


    fun load() {
        val array = serializer.read(Array<MovieActor>::class.java)
        movieActors = array.toCollection(ArrayList())
    }

    fun store() {
        serializer.write(movieActors)
    }
}

