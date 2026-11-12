package service

import Persistance.Serializer
import model.Actor

class ActorService(serializerType: Serializer){

    private var serializer: Serializer = serializerType
    private var actors = ArrayList<Actor>()

    private var lastId = 0
    private fun getId(): Int = lastId++

    fun getActors(): List<Actor> = actors

    fun addActor(actor: Actor): Boolean {

        if (actors.any {
                it.name.equals(actor.name.trim(), ignoreCase = true) &&
                        it.dateOfBirth == actor.dateOfBirth
            }) {
            return false
        }

        actor.id = getId()
        actors.add(actor)

        return true
    }

    fun findActorById(actorId: Int): Actor? {
        return actors.find { it.id == actorId }
    }

    fun findActorByName(name: String): Actor? {
        return actors.find { it.name.equals(name, ignoreCase = true) }
    }

    fun load() {
        val array = serializer.read(Array<Actor>::class.java)
        actors = array.toCollection(ArrayList())
        lastId = (actors.maxOfOrNull { it.id } ?: -1) + 1
    }

    fun store() {
        serializer.write(actors)
    }
}