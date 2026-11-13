
package service

import model.Actor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import persistance.JSONSerializer
import persistance.Serializer
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
        emptyActorService =
            ActorService(
                JSONSerializer(File(tempDir, "empty-actors.json")),
            )

        populatedActorService =
            ActorService(
                JSONSerializer(File(tempDir, "actors.json")),
            )

        actor1 = Actor(0, "Russell Crowe", "07-04-1964")
        actor2 = Actor(0, "Tom Cruise", "03-07-1962")

        assertTrue(populatedActorService.addActor(actor1))
        assertTrue(populatedActorService.addActor(actor2))
    }

    @Nested
    inner class AddActorTests {
        @Test
        fun `adding actor increases collection size`() {
            assertEquals(0, emptyActorService.getActors().size)

            assertTrue(emptyActorService.addActor(Actor(0, "Brad Pitt")))

            assertEquals(1, emptyActorService.getActors().size)
        }

        @Test
        fun `adding actor assigns an ID`() {
            assertTrue(emptyActorService.addActor(actor1))

            assertEquals(0, actor1.id)
        }

        @Test
        fun `adding multiple actors assigns unique IDs`() {
            assertEquals(0, actor1.id)
            assertEquals(1, actor2.id)
        }

        @Test
        fun `adding actor stores correct information`() {
            assertTrue(emptyActorService.addActor(actor1))

            val retrievedActor = emptyActorService.getActors().first()

            assertEquals("Russell Crowe", retrievedActor.name)
        }
    }

    @Nested
    inner class SearchActorTests {
        @Test
        fun `finds actor by ID`() {
            assertSame(actor2, populatedActorService.findActorById(1))
        }

        @Test
        fun `returns null for unknown ID`() {
            assertNull(populatedActorService.findActorById(99))
        }

        @Test
        fun `searches actor by case insensitive name and exact date of birth`() {
            assertSame(actor1, populatedActorService.searchActor("rUsSeLl cRoWe", "07-04-1964"))
        }

        @Test
        fun `returns null for unknown actor or mismatched date of birth`() {
            assertNull(populatedActorService.searchActor("Unknown Actor", "01-01-1900"))
            assertNull(populatedActorService.searchActor("Russell Crowe", "01-01-1900"))
        }
    }

    @Nested
    inner class FilterActorsTests {
        @Test
        fun `filters actors by partial name ignoring case`() {
            val secondRussell = Actor(name = "Russell Westbrook", dateOfBirth = "12-11-1988")
            assertTrue(populatedActorService.addActor(secondRussell))

            assertEquals(
                listOf(actor1, secondRussell),
                populatedActorService.filterActorsByName("rUsSeLl"),
            )
        }

        @Test
        fun `returns empty list when no actor name matches`() {
            assertTrue(populatedActorService.filterActorsByName("Unknown").isEmpty())
        }
    }

    @Nested
    inner class UpdateActorTests {
        @Test
        fun `updates actor name and date of birth`() {
            val service = ActorService(JSONSerializer(File(tempDir, "update-actor.json")))
            val actor = Actor(name = "Old Name", dateOfBirth = "01-01-1980")
            assertTrue(service.addActor(actor))

            assertTrue(service.updateActor(actor.id, " New Name ", "02-02-1981"))

            assertEquals("New Name", actor.name)
            assertEquals("02-02-1981", actor.dateOfBirth)
            assertEquals(0, actor.id)
        }

        @Test
        fun `updating unknown actor returns false`() {
            val service = ActorService(JSONSerializer(File(tempDir, "missing-actor-update.json")))

            assertFalse(service.updateActor(99, "Name", "01-01-1980"))
        }
    }

    @Nested
    inner class DeleteActorTests {
        @Test
        fun `deletes existing actor`() {
            val service = ActorService(JSONSerializer(File(tempDir, "delete-actor.json")))
            val actor = Actor(name = "Delete Me")
            assertTrue(service.addActor(actor))

            assertTrue(service.deleteActor(actor.id))

            assertNull(service.findActorById(actor.id))
            assertTrue(service.getActors().isEmpty())
        }

        @Test
        fun `deleting unknown actor returns false`() {
            val service = ActorService(JSONSerializer(File(tempDir, "missing-actor-delete.json")))

            assertFalse(service.deleteActor(99))
        }
    }

    @Nested
    inner class ValidationTest {
        @Test
        fun `adding actor with same name and date of birth is rejected ignoring case`() {
            val service = ActorService(JSONSerializer(File(tempDir, "duplicates.json")))
            val original = Actor(name = "Morgan Freeman", dateOfBirth = "01-06-1937")
            val duplicate = Actor(name = "  mOrGaN fReEmAn  ", dateOfBirth = "01-06-1937")
            assertTrue(service.addActor(original))

            assertFalse(service.addActor(duplicate))

            assertEquals(1, service.getActors().size)
            val nextActor = Actor(name = "Another actor")
            assertTrue(service.addActor(nextActor))
            assertEquals(1, nextActor.id)
        }

        @Test
        fun `same actor name with different dates of birth is accepted`() {
            val service = ActorService(JSONSerializer(File(tempDir, "different-dobs.json")))
            val original = Actor(name = "Chris Evans", dateOfBirth = "13-06-1981")
            val differentPerson = Actor(name = "Chris Evans", dateOfBirth = "20-02-1990")

            assertTrue(service.addActor(original))
            assertTrue(service.addActor(differentPerson))

            assertEquals(2, service.getActors().size)
            assertEquals(1, differentPerson.id)
        }

        @Test
        fun `rejects update that would duplicate another actor`() {
            val service = ActorService(JSONSerializer(File(tempDir, "duplicate-actor-update.json")))
            val original = Actor(name = "First Person", dateOfBirth = "01-01-1980")
            val other = Actor(name = "Second Person", dateOfBirth = "02-02-1981")
            assertTrue(service.addActor(original))
            assertTrue(service.addActor(other))

            assertFalse(service.updateActor(original.id, " second person ", "02-02-1981"))

            assertEquals("First Person", original.name)
            assertEquals("01-01-1980", original.dateOfBirth)
        }

        @Test
        fun `allows update when another actor has the same name but different date of birth`() {
            val service = ActorService(JSONSerializer(File(tempDir, "same-name-different-dob.json")))
            val original = Actor(name = "Chris Evans", dateOfBirth = "13-06-1981")
            assertTrue(service.addActor(original))
            assertTrue(service.addActor(Actor(name = "Chris Evans", dateOfBirth = "20-02-1990")))

            assertTrue(service.updateActor(original.id, "Chris Evans", "15-07-1981"))

            assertEquals("15-07-1981", original.dateOfBirth)
        }

        @Test
        fun `allows update when another actor has a different name`() {
            val service = ActorService(JSONSerializer(File(tempDir, "different-name-update.json")))
            val original = Actor(name = "First Person", dateOfBirth = "01-01-1980")
            assertTrue(service.addActor(original))
            assertTrue(service.addActor(Actor(name = "Second Person", dateOfBirth = "02-02-1981")))

            assertTrue(service.updateActor(original.id, "Third Person", "03-03-1982"))

            assertEquals("Third Person", original.name)
        }
    }

    @Nested
    inner class ReportTests {
        @Test
        fun `empty actor collection reports zero and no oldest or youngest actor`() {
            assertEquals(0, emptyActorService.numberOfActors())
            assertNull(emptyActorService.oldestActor())
            assertNull(emptyActorService.youngestActor())
        }

        @Test
        fun `reports oldest and youngest actors from their dates of birth`() {
            val service = ActorService(JSONSerializer(File(tempDir, "actor-report.json")))
            val oldest = Actor(name = "Oldest", dateOfBirth = "10-05-1940")
            val middle = Actor(name = "Middle", dateOfBirth = "20-06-1970")
            val youngest = Actor(name = "Youngest", dateOfBirth = "30-07-2000")
            assertTrue(service.addActor(middle))
            assertTrue(service.addActor(youngest))
            assertTrue(service.addActor(oldest))

            assertEquals(3, service.numberOfActors())
            assertSame(oldest, service.oldestActor())
            assertSame(youngest, service.youngestActor())
        }
    }

    @Nested
    inner class PersistenceTests {
        @Test
        fun `saving and loading empty actors does not crash`() {
            emptyActorService.store()

            val loadedService =
                ActorService(
                    JSONSerializer(File(tempDir, "empty-actors.json")),
                )

            loadedService.load()

            assertEquals(0, loadedService.getActors().size)
        }

        @Test
        fun `saving and loading actors preserves data`() {
            populatedActorService.store()

            val loadedService =
                ActorService(
                    JSONSerializer(File(tempDir, "actors.json")),
                )

            loadedService.load()

            assertEquals(2, loadedService.getActors().size)
            assertEquals("Russell Crowe", loadedService.getActors()[0].name)
            assertEquals("07-04-1964", loadedService.getActors()[0].dateOfBirth)
            assertEquals("Tom Cruise", loadedService.getActors()[1].name)
            assertEquals("03-07-1962", loadedService.getActors()[1].dateOfBirth)
        }

        @Test
        fun `loading actors restores next available ID`() {
            populatedActorService.store()

            val loadedService =
                ActorService(
                    JSONSerializer(File(tempDir, "actors.json")),
                )

            loadedService.load()

            val newActor = Actor(0, "Brad Pitt", "18-12-1963")
            assertTrue(loadedService.addActor(newActor))

            assertEquals(2, newActor.id)
        }

        @Test
        fun `loading one actor restores ID after that actor`() {
            val serializer = FixedReadSerializer(arrayOf(Actor(id = 42, name = "Loaded")))
            val service = ActorService(serializer)

            service.load()
            val nextActor = Actor(name = "Next")
            assertTrue(service.addActor(nextActor))

            assertEquals(43, nextActor.id)
        }

        @Test
        fun `loading actors in descending ID order restores ID after the maximum`() {
            val loadedActors =
                arrayOf(
                    Actor(id = 42, name = "Higher ID"),
                    Actor(id = 7, name = "Lower ID"),
                )
            val service = ActorService(FixedReadSerializer(loadedActors))

            service.load()
            val nextActor = Actor(name = "Next")
            assertTrue(service.addActor(nextActor))

            assertEquals(43, nextActor.id)
        }

        @Test
        fun `store passes current actors to serializer`() {
            val serializer = CapturingSerializer()
            val service = ActorService(serializer)
            assertTrue(service.addActor(Actor(0, "Viola Davis")))

            service.store()

            assertSame(service.getActors(), serializer.writtenObject)
        }
    }

    private class CapturingSerializer : Serializer {
        var writtenObject: Any? = null

        override fun write(obj: Any?) {
            writtenObject = obj
        }

        override fun <T> read(classType: Class<T>): T = error("read should not be called")
    }

    private class FixedReadSerializer(
        private val actors: Array<Actor>,
    ) : Serializer {
        override fun write(obj: Any?) = Unit

        @Suppress("UNCHECKED_CAST")
        override fun <T> read(classType: Class<T>): T = actors as T
    }
}
