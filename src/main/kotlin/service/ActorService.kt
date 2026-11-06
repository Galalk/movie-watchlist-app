package service

import Persistance.Serializer
import model.Actor

class ActorService(serializerType: Serializer){

    private var serializer: Serializer = serializerType
    private var actors = ArrayList<Actor>()

    private var lastId = 0
    private fun getId(): Int = lastId++

    fun getActors(): List<Actor> = actors

    fun addActor(actor: Actor) {
        actor.id = getId()
        actors.add(actor)
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