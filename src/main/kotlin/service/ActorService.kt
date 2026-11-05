package service

import Persistance.Serializer
import model.Actor

class ActorService(serializerType: Serializer){

    private var serializer: Serializer = serializerType
    private var actors = ArrayList<Actor>()

    private var lastId = 0
    private fun getId(): Int = lastId++

    fun addActor(actor: Actor) {
        actor.id = getId()
        actors.add(actor)
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