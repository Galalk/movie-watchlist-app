import Persistance.JSONSerializer
import utils.readNextDOB
import utils.readNextDouble
import utils.readNextInt
import utils.readNextLine
import utils.readNextYear
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
    logger.info { "Movie Backlog Tracker Started" }

    runMenu()
}

fun mainMenu(): Int {
    print(""" 
         > ------------------------------------------
         >           Movies Backlog Tracker ☺          
         > ------------------------------------------
         > |     1) Search and Filter               |
         > |     2) Add a Movie                     |
         > |     3) Add an Actor                    |
         > |     4) Add an Actor to a Movie         |
         > |     5) List All Menu                   |
         > |     6) Change Movie Watch Status       |
         > |     7) Update Movie or Actor           |
         > |     8) Delete Movie or Actor           |
         > ------------------------------------------
         > |    20) Save                            |
         > |    21) Load                            |
         > ------------------------------------------   
         > |     0) Exit                            |
         > ------------------------------------------
         > """.trimMargin(">"))
    return readNextInt("==>> ")
}

fun runMenu (){
    var input : Int
    do {
        input = mainMenu()
        when(input) {
            1 -> searchMenu()
            2 -> addMovie()
            3 -> addActor()
            4 -> addActorToMovie()
            5 -> listAll()
            6 -> changeMovieStatus()
            7 -> updateMenu()
            8 -> deleteMenu()
            20 -> save()
            21 -> load()
            0 ->  {
                println("Exiting App")
                logger.info { "Movie Backlog Tracker Exiting" }
            }
            else -> println("Invalid Option")
        }
    } while (input != 0)
}

fun searchMenu() {

    var option: Int

    do {
        println("""
            > ---------- Search Menu ------------
            > |   1) Search for Movie           |
            > |   2) Search for Actor           | 
            > |   3) Filter Movies by Genre     |
            > |   4) Filter Actors by Name      |
            > -----------------------------------
            > |   0) Back to Main Menu          |
            > -----------------------------------
        """.trimIndent())

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> searchMovie()
            2 -> searchActor()
            3 -> filterMoviesByGenre()
            4 -> filterActorsByName()
            0 -> println("Returning to main menu...")
            else -> println("Invalid option")
        }

    } while (option != 0)
}

fun searchMovie() {

    val title = readNextLine("Enter movie title: ")
    val releaseYear = readNextYear("Enter release year: ")

    val movie = movieService.searchMovie(title, releaseYear)

    if (movie != null) {
        println(movie.toString())
    }
}

fun searchActor() {

    val name = readNextLine("Enter actor name: ")
    val dateOfBirth = readNextDOB("Enter date of birth: ")

    val actor = actorService.searchActor(name, dateOfBirth)

    if (actor != null) {
        println(actor.toString())
    }
}

fun filterMoviesByGenre() {

    val genre = readNextLine("Enter genre: ")

    val filteredMovies = movieService.filterMoviesByGenre(genre)

    if (filteredMovies.isNotEmpty()) {
        filteredMovies.forEach { println(it) }
    } else {
        println("No movies found for this genre.")
    }
}

fun filterActorsByName() {

    val name = readNextLine("Enter actor name: ")

    val filteredActors = actorService.filterActorsByName(name)

    if (filteredActors.isNotEmpty()) {
        filteredActors.forEach { println(it) }
    } else {
        println("No actors found.")
    }
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

    val actorId = readNextInt("Enter Actor ID: ")
    val actor = actorService.findActorById(actorId)

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
            > --------- Movie Status ----------
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

fun updateMenu() {
    var option: Int

    do {
        println("""

            > ---------- Update Menu ----------
            > |   1) Update Movie             |
            > |   2) Update Actor             |
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
        """.trimIndent())

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> updateMovie()
            2 -> updateActor()
            0 -> println("Returning to main menu...")
            else -> println("Invalid option.")
        }

    } while (option != 0)
}

fun updateMovie(service: MovieService = movieService) {
    if (service.getMovies().isEmpty()) {
        println("There are no movies to update.")
        return
    }

    println("\n--- Movies ---")
    service.getMovies().forEach { println(it) }

    val movieId = readNextInt("Enter the Movie ID to update: ")
    val movie = service.findMovieById(movieId)

    if (movie == null) {
        println("Movie not found.")
        return
    }

    println("\nEnter the updated movie details:")

    val title = readNextLine("Enter title: ")
    val genre = readNextLine("Enter genre: ")
    val releaseYear = readNextYear("Enter release year: ")
    val duration = readNextDouble("Enter duration in hours: ")

    if (service.updateMovie(
            movieId,
            title,
            genre,
            releaseYear,
            duration
        )
    ) {
        println("Movie updated successfully.")
    } else {
        println("Update failed. A movie with that title and release year may already exist.")
    }
}

fun updateActor(service: ActorService = actorService) {
    if (service.getActors().isEmpty()) {
        println("There are no actors to update.")
        return
    }

    println("\n--- Actors ---")
    service.getActors().forEach { println(it) }

    val actorId = readNextInt("Enter the Actor ID to update: ")
    val actor = service.findActorById(actorId)

    if (actor == null) {
        println("Actor not found.")
        return
    }

    println("\nEnter the updated actor details:")

    val name = readNextLine("Enter name: ")
    val dateOfBirth = readNextDOB("Enter date of birth: ")

    if (service.updateActor(actorId, name, dateOfBirth)) {
        println("Actor updated successfully.")
    } else {
        println("Update failed. An actor with that name and date of birth may already exist.")
    }
}

fun deleteMenu() {
    var option: Int

    do {
        println("""

            > ---------- Delete Menu ----------
            > |   1) Delete Movie             |
            > |   2) Delete Actor             |
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
        """.trimIndent())

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> deleteMovie()
            2 -> deleteActor()
            0 -> println("Returning to main menu...")
            else -> println("Invalid option.")
        }

    } while (option != 0)
}

fun deleteMovie(
    service: MovieService = movieService,
    associationService: MovieActorService = movieActorService
) {
    if (service.getMovies().isEmpty()) {
        println("There are no movies to delete.")
        return
    }

    println("\n--- Movies ---")
    service.getMovies().forEach { println(it) }

    val movieId = readNextInt("Enter the Movie ID to delete: ")
    val movie = service.findMovieById(movieId)

    if (movie == null) {
        println("Movie not found.")
        return
    }

    println("Are you sure you want to delete '${movie.title}'?")
    val confirmation = readNextLine("Type YES to confirm: ")

    if (confirmation.equals("YES", ignoreCase = true)) {
        service.deleteMovie(movieId)
        associationService.deleteAssociationsForMovie(movieId)

        println("Movie and its actor associations deleted successfully.")
    } else {
        println("Deletion cancelled.")
    }
}

fun deleteActor(
    service: ActorService = actorService,
    associationService: MovieActorService = movieActorService
) {
    if (service.getActors().isEmpty()) {
        println("There are no actors to delete.")
        return
    }

    println("\n--- Actors ---")
    service.getActors().forEach { println(it) }

    val actorId = readNextInt("Enter the Actor ID to delete: ")
    val actor = service.findActorById(actorId)

    if (actor == null) {
        println("Actor not found.")
        return
    }

    println("Are you sure you want to delete '${actor.name}'?")
    val confirmation = readNextLine("Type YES to confirm: ")

    if (confirmation.equals("YES", ignoreCase = true)) {
        service.deleteActor(actorId)
        associationService.deleteAssociationsForActor(actorId)

        println("Actor and their movie associations deleted successfully.")
    } else {
        println("Deletion cancelled.")
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
