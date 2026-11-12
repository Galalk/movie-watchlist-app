import Persistance.JSONSerializer
import Utils.readNextDOB
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
         >           Movies Backlog Tracker ☺          
         > ------------------------------------------    
         > |        1) Add a Movie                  |
         > |        2) Add an Actor                 |
         > |        3) Add an Actor to a Movie      |
         > |        4) List All Menu                |
         > |        5) Change Movie Watch Status    |
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
            3 -> addActorToMovie()
            4 -> listAll()
            5 -> changeMovieStatus()
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

fun addMovie() {
    val title = readNextLine("Title: ")
    val genre = readNextLine("Genre: ")
    val releaseYear = readNextYear("Release Year: ")
    val duration = readNextDouble("Movie duration (Hours): ")

    val movie = Movie(0, title, genre, releaseYear, duration, false)

    if (movieService.addMovie(movie)) {
        println("Movie successfully added to backlog!")
    } else {
        println("This movie already exists in your backlog!")
    }
}

fun addActor(){
    val name = readNextLine("Full name: ")
    val dateOfBirth = readNextDOB("Date of Birth (DD-MM-YYYY): ")

    if (actorService.addActor(Actor(0, name, dateOfBirth))) {
        println("Actor successfully added!")
    } else {
        println("This actor already exists!")
    }
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

fun listAll() {

    var option: Int

    do {
        option = readNextInt(
            """
            > ---------- List Menu ------------
            > |   1) View All Movies          |
            > |   2) View All Actors          |
            > |   3) View Watched Movies      |
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
            > ==>>
            """.trimMargin(">")
        )

        when (option) {
            1 -> movieService.getMovies().forEach { println(it) }
            2 -> actorService.getActors().forEach { println(it) }
            3 -> movieService.getMovies()
                .filter { it.finished }
                .forEach { println(it) }

            0 -> println("Returning to main menu")

            else -> println("Invalid option entered: $option")
        }

    } while (option != 0)
}

fun changeMovieStatus() {
    var option: Int

    do {
        println("""
            > ---------- List Menu ------------
            > |   1) Change Movie to Watched  |       
            > |   2) Revert Movie to To Watch |         
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
        """.trimIndent())

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> addMovieToWatched()
            2 -> revertMovieStatus()

            0 -> println("Returning to main menu...")

            else -> println("Invalid option!")
        }
    } while (option != 0)
}

fun addMovieToWatched() {

    val id = readNextInt("Enter Movie ID: ")


    val movie = movieService.findMovieById(id)

    if (movie == null) {
        println("Movie not found!")
        return
    }

    if (movie.finished) {
        println("This movie is already marked as watched!")
        return
    }

    if (movieService.markMovieAsWatched(movie.id)) {
        println("Movie successfully added to watched list")
    }
}

fun revertMovieStatus() {
    val id = readNextInt("Enter Movie ID: ")

    val movie = movieService.findMovieById(id)

    if (movie == null) {
        println("Movie not found!")
        return
    }

    if (!movie.finished) {
        println("This movie is already marked as To Watch!")
        return
    }

    if (movieService.markMovieAsToWatch(movie.id)) {
        println("Movie successfully added back into to your backlog!")
    }
}

fun save() {
    try {
        movieService.store()
        actorService.store()
        movieActorService.store()
    } catch (e: Exception) {
        System.err.println("Error writing to file: $e")
    }
}

fun load() {
    try {
        movieService.load()
        actorService.load()
        movieActorService.load()
    } catch (e: Exception) {
        System.err.println("Error reading from file: $e")
    }
}
