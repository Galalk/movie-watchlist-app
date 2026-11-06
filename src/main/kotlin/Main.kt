import Persistance.JSONSerializer
import Utils.readNextDouble
import Utils.readNextInt
import Utils.readNextLine
import Utils.readNextYear
import io.github.oshai.kotlinlogging.KotlinLogging
import model.Actor
import model.Movie
import service.ActorService
import service.MovieActorService
import service.MovieService
import java.io.File

val movieService = MovieService(JSONSerializer(File("movies.json")))
val actorService = ActorService(JSONSerializer(File("actors.json")))
val movieActorService = MovieActorService(JSONSerializer(File("movieActors.json")))

private val logger = KotlinLogging.logger {}

fun main(){
    logger.info { "Notes App Started" }

    runMenu()
}

fun mainMenu(): Int {
    print(""" 
         > ------------------------------------------
         >            Movies Backlog App ☺          
         > ------------------------------------------    
         > |        1) Add a Movie                  |
         > |        2) Add an Actor                 |
         > |        3) Add an Actor to a Movie      |
         > ------------------------------------------
         > |       20) Save                         |
         > |       21) Load                         |
         > ------------------------------------------   
         > |        0) Exit                         |
         > ------------------------------------------
         > """.trimMargin(">"))
    return readNextInt("==>> ")
}

fun runMenu (){
    var input : Int
    do {
        input = mainMenu()
        when(input) {
            1 -> addMovie()
            2 -> addActor()
            20 -> save()
            21 -> load()
            0 ->  {
                println("Exiting App")
                logger.info { "Notes App Exiting" }
            }
            else -> println("Invalid Option")
        }
    } while (input != 0)
}

fun addMovie(){
    val title = readNextLine("Title: ")
    val genre = readNextLine("Genre: ")
    val releaseYear = readNextYear("Release Year: ")
    val duration = readNextDouble("Movie duration (Hours): ")

    movieService.addMovie(
        Movie(0, title, genre, releaseYear, duration, false)
    )

    println("Movie successfully added to backlog")
}

fun addActor(){
    val name = readNextLine("Full name: ")

    actorService.addActor(
        Actor(0, name)
    )

    println("Actor successfully added")
}

fun addActorToMovie() {
    val movieId = readNextInt("Enter Movie ID: ")

    if (movieService.findMovieById(movieId) == null) {
        println("Movie not found")
        return
    }

    val actorName = readNextLine("Enter Actor Name: ")

    val actor = actorService.findActorByName(actorName)

    if (actor == null) {
        println("Actor not found")
        return
    }

    if (movieActorService.addActorToMovie(movieId, actor.id)) {
        println("Actor successfully added to movie")
    } else {
        println("This actor is already associated with this movie")
    }
}

fun save() {
    try {
        movieService.store()
        actorService.store()
    } catch (e: Exception) {
        System.err.println("Error writing to file: $e")
    }
}

fun load() {
    try {
        movieService.load()
        actorService.load()
    } catch (e: Exception) {
        System.err.println("Error reading from file: $e")
    }
}