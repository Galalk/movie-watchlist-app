
package service

import Persistance.JSONSerializer
import model.Actor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ActorServiceTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var emptyActorService: ActorService
    private lateinit var populatedActorService: ActorService

    private lateinit var actor1: Actor
    private lateinit var actor2: Actor

    @BeforeEach
    fun setUp() {
        emptyActorService = ActorService(
            JSONSerializer(File(tempDir, "empty-actors.json"))
        )

        populatedActorService = ActorService(
            JSONSerializer(File(tempDir, "actors.json"))
        )

        actor1 = Actor(0, "Russell Crowe")
        actor2 = Actor(0, "Tom Cruise")

        populatedActorService.addActor(actor1)
        populatedActorService.addActor(actor2)
    }

    @Nested
    inner class AddActorTests {

        @Test
        fun `adding actor increases collection size`() {
            assertEquals(0, emptyActorService.getActors().size)

            emptyActorService.addActor(Actor(0, "Brad Pitt"))

            assertEquals(1, emptyActorService.getActors().size)
        }

        @Test
        fun `adding actor assigns an ID`() {
            emptyActorService.addActor(actor1)

            assertEquals(0, actor1.id)
        }

        @Test
        fun `adding multiple actors assigns unique IDs`() {
            assertEquals(0, actor1.id)
            assertEquals(1, actor2.id)
        }

        @Test
        fun `adding actor stores correct information`() {
            emptyActorService.addActor(actor1)

            val retrievedActor = emptyActorService.getActors().first()

            assertEquals("Russell Crowe", retrievedActor.name)
        }
    }

    @Nested
    inner class PersistenceTests {

        @Test
        fun `saving and loading empty actors does not crash`() {
            emptyActorService.store()

            val loadedService = ActorService(
                JSONSerializer(File(tempDir, "empty-actors.json"))
            )

            loadedService.load()

            assertEquals(0, loadedService.getActors().size)
        }

        @Test
        fun `saving and loading actors preserves data`() {
            populatedActorService.store()

            val loadedService = ActorService(
                JSONSerializer(File(tempDir, "actors.json"))
            )

            loadedService.load()

            assertEquals(2, loadedService.getActors().size)
            assertEquals("Russell Crowe", loadedService.getActors()[0].name)
            assertEquals("Tom Cruise", loadedService.getActors()[1].name)
        }

        @Test
        fun `loading actors restores next available ID`() {
            populatedActorService.store()

            val loadedService = ActorService(
                JSONSerializer(File(tempDir, "actors.json"))
            )

            loadedService.load()

            val newActor = Actor(0, "Brad Pitt")
            loadedService.addActor(newActor)

            assertEquals(2, newActor.id)
        }
    }
}
