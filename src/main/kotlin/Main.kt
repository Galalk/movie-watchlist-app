import io.github.oshai.kotlinlogging.KotlinLogging
import model.Actor
import model.Movie
import persistance.JSONSerializer
import service.ActorService
import service.MovieActorService
import service.MovieService
import utils.readNextDOB
import utils.readNextDouble
import utils.readNextInt
import utils.readNextLine
import utils.readNextYear
import java.io.File

val movieService = MovieService(JSONSerializer(File("movies.json")))
val actorService = ActorService(JSONSerializer(File("actors.json")))
val movieActorService = MovieActorService(JSONSerializer(File("movieActors.json")))

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Movie Backlog Tracker Started" }

    runMenu()
}

fun mainMenu(): Int {
    print(
        """  
         > ------------------------------------------
         >           Movies Backlog Tracker ☺          
         > ------------------------------------------
         > |     1) Search and Filter               |
         > |     2) Add New                         |
         > |     3) View All                        |
         > |     4) Change Movie Watch Status       |
         > |     5) Update Movie or Actor           |
         > |     6) Delete or Remove                |
         > |     7) View Reports                    |
         > ------------------------------------------
         > |     8) Save                            |
         > |     9) Load                            |
         > ------------------------------------------   
         > |     0) Exit                            |
         > ------------------------------------------
         > """.trimMargin(">"),
    )
    return readNextInt("==>> ")
}

fun runMenu() {
    var input: Int
    do {
        input = mainMenu()
        when (input) {
            1 -> {
                searchMenu()
            }

            2 -> {
                addMenu()
            }

            3 -> {
                listAll()
            }

            4 -> {
                changeMovieStatus()
            }

            5 -> {
                updateMenu()
            }

            6 -> {
                deleteMenu()
            }

            7 -> {
                reportMenu()
            }

            8 -> {
                save()
            }

            9 -> {
                load()
            }

            0 -> {
                println("Exiting App")
                logger.info { " Exiting Movie Backlog Tracker" }
            }

            else -> {
                println("Invalid Option")
            }
        }
    } while (input != 0)
}

fun searchMenu() {
    logger.info { "Search menu opened" }

    var option: Int

    do {
        println(
            """
            > -------- Search and Filter --------
            > |   1) Search for Movie           |
            > |   2) Search for Actor           | 
            > |   3) Filter Movies by Genre     |
            > |   4) Filter Actors by Name      |
            > -----------------------------------
            > |   0) Back to Main Menu          |
            > -----------------------------------
            """.trimIndent(),
        )

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> {
                searchMovie()
            }

            2 -> {
                searchActor()
            }

            3 -> {
                filterMoviesByGenre()
            }

            4 -> {
                filterActorsByName()
            }

            0 -> {
                logger.info { "Exiting search menu" }
                println("Returning to Main Menu")
            }

            else -> {
                println("Invalid option")
            }
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

fun addMenu() {
    logger.info { "Add menu opened" }

    var option: Int

    do {
        println(
            """
            > ------------ Add New --------------
            > |   1) Add a Movie                |
            > |   2) Add an Actor               | 
            > |   3) Add an Actor to a Movie    |
            > -----------------------------------
            > |   0) Back to Main Menu          |
            > -----------------------------------
            """.trimIndent(),
        )

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> {
                addMovie()
            }

            2 -> {
                addActor()
            }

            3 -> {
                addActorToMovie()
            }

            0 -> {
                logger.info { "Exiting add menu" }
                println("Returning to Main Menu")
            }

            else -> {
                println("Invalid option")
            }
        }
    } while (option != 0)
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

fun addActor() {
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

fun removeActorFromMovie() {
    val movieId = readNextInt("Enter Movie ID: ")
    val movie = movieService.findMovieById(movieId)

    if (movie == null) {
        println("Movie not found.")
        return
    }

    val actorId = readNextInt("Enter Actor ID: ")
    val actor = actorService.findActorById(actorId)

    if (actor == null) {
        println("Actor not found.")
        return
    }

    if (movieActorService.removeActorFromMovie(movieId, actorId)) {
        println("Removed ${actor.name} from ${movie.title}.")
    } else {
        println("${actor.name} is not associated with ${movie.title}.")
    }
}

fun listAll() {
    logger.info { "View menu opened" }

    var option: Int

    do {
        option =
            readNextInt(
                """
            > ---------- View All -------------
            > |   1) View All Movies          |
            > |   2) View All Actors          |
            > |   3) View Watched Movies      |
            > |   4) View Actors in a Movie   |
            > |   5) View Movies for an Actor |
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
            > ==>>
            """.trimMargin(">"),
            )

        when (option) {
            1 -> {
                movieService.getMovies().forEach { println(it) }
            }

            2 -> {
                actorService.getActors().forEach { println(it) }
            }

            3 -> {
                movieService
                    .getMovies()
                    .filter { it.finished }
                    .forEach { println(it) }
            }

            4 -> {
                viewActorsInMovie()
            }

            5 -> {
                viewMoviesForActor()
            }

            0 -> {
                logger.info { "Exiting view menu" }
                println("Returning to Main Menu")
            }

            else -> {
                println("Invalid option entered: $option")
            }
        }
    } while (option != 0)
}

fun viewActorsInMovie() {
    val movieId = readNextInt("Enter Movie ID: ")
    val movie = movieService.findMovieById(movieId)

    if (movie == null) {
        println("Movie not found.")
        return
    }

    val actorIds = movieActorService.getActorIdsForMovie(movieId)

    if (actorIds.isEmpty()) {
        println("No actors associated with ${movie.title}.")
        return
    }

    println("Actors in ${movie.title}:")
    for (actorId in actorIds) {
        println(actorService.findActorById(actorId))
    }
}

fun viewMoviesForActor() {
    val actorId = readNextInt("Enter Actor ID: ")
    val actor = actorService.findActorById(actorId)

    if (actor == null) {
        println("Actor not found.")
        return
    }

    val movieIds = movieActorService.getMovieIdsForActor(actorId)

    if (movieIds.isEmpty()) {
        println("No movies associated with ${actor.name}.")
        return
    }

    println("Movies featuring ${actor.name}:")
    for (movieId in movieIds) {
        println(movieService.findMovieById(movieId))
    }
}

fun changeMovieStatus() {
    logger.info { "Movie status menu opened" }

    var option: Int

    do {
        println(
            """
            > --------- Movie Status ----------
            > |   1) Change Movie to Watched  |       
            > |   2) Revert Movie to To Watch |         
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
            """.trimIndent(),
        )

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> {
                addMovieToWatched()
            }

            2 -> {
                revertMovieStatus()
            }

            0 -> {
                logger.info { "Exiting movie status menu" }
                println("Returning to Main Menu")
            }

            else -> {
                println("Invalid option!")
            }
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
    logger.info { "Update menu opened" }

    var option: Int

    do {
        println(
            """

            > ------------ Update -------------
            > |   1) Update Movie             |
            > |   2) Update Actor             |
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
            """.trimIndent(),
        )

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> {
                updateMovie()
            }

            2 -> {
                updateActor()
            }

            0 -> {
                logger.info { "Exiting update menu" }
                println("Returning to Main Menu")
            }

            else -> {
                println("Invalid option.")
            }
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

    if (service.updateMovie(movieId, title, genre, releaseYear, duration)) {
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
    logger.info { "Delete menu opened" }

    var option: Int

    do {
        println(
            """

            > -------------  Delete ---------------
            > |   1) Delete a Movie               |
            > |   2) Delete an Actor              | 
            > |   3) Remove an Actor from a Movie |
            > -----------------------------------
            > |   0) Back to Main Menu            |
            > -------------------------------------
            """.trimIndent(),
        )

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> {
                deleteMovie()
            }

            2 -> {
                deleteActor()
            }

            3 -> {
                removeActorFromMovie()
            }

            0 -> {
                logger.info { "Exiting delete menu" }
                println("Returning to Main Menu")
            }

            else -> {
                println("Invalid option.")
            }
        }
    } while (option != 0)
}

fun deleteMovie(
    service: MovieService = movieService,
    associationService: MovieActorService = movieActorService,
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
    associationService: MovieActorService = movieActorService,
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

fun reportMenu() {
    logger.info { "Report menu opened" }

    var option: Int

    do {
        println(
            """

            > ------------ Reports ------------
            > |   1) Movie Report             |
            > |   2) Actor Report             |
            > |   3) Movie and Actor Report   |
            > --------------------------------
            > |   0) Back to Main Menu        |
            > ---------------------------------
            """.trimIndent(),
        )

        option = readNextInt("Enter option: ")

        when (option) {
            1 -> {
                movieReport()
            }

            2 -> {
                actorReport()
            }

            3 -> {
                movieActorReport()
            }

            0 -> {
                logger.info { "Exiting report menu" }
                println("Returning to Main Menu")
            }

            else -> {
                println("Invalid option.")
            }
        }
    } while (option != 0)
}

fun movieReport() {
    println(
        """
        ----- Movie Report -----
          Total Movies:      ${movieService.numberOfMovies()}
          Watched Movies:  ${movieService.numberOfWatchedMovies()}
          Unwatched Movies: ${movieService.numberOfToWatchMovies()}
          Percentage Watched: ${"%.2f".format(movieService.percentageWatched())}%
        ------------------------        
        """.trimIndent(),
    )
}

fun actorReport() {
    val oldest = actorService.oldestActor()
    val youngest = actorService.youngestActor()

    println(
        """
        ----- Actor Report -----
          Total Actors: ${actorService.numberOfActors()}
          Oldest Actor: ${oldest?.name ?: "N/A"} (${oldest?.dateOfBirth ?: "N/A"})
          Youngest Actor: ${youngest?.name ?: "N/A"} (${youngest?.dateOfBirth ?: "N/A"})
        ------------------------
        """.trimIndent(),
    )
}

fun movieActorReport() {
    val movieWithMostActors = movieActorService.movieWithMostActors()
    val averageActors = movieActorService.averageActorsPerMovie(movieService.numberOfMovies())
    val movie =
        movieWithMostActors?.key?.let {
            movieService.findMovieById(it)
        }

    println(
        """
        ----- Movie Actor Report -----
        Movie With Most Actors: ${movie?.title ?: "N/A"}
        Number Of Actors: ${movieWithMostActors?.value ?: 0}
        Average Actors Per Movie: ${"%.2f".format(averageActors)}
        ------------------------------
        """.trimIndent(),
    )
}

fun save() {
    try {
        movieService.store()
        actorService.store()
        movieActorService.store()
        logger.info { "Data saved successfully" }
        println("Data saved successfully.")
    } catch (e: Exception) {
        logger.error(e) { "Error saving data" }
        println("Error writing to file: ${e.message}")
    }
}

fun load() {
    try {
        movieService.load()
        actorService.load()
        movieActorService.load()
        logger.info { "Data loaded successfully" }
        println("Data loaded successfully.")
    } catch (e: Exception) {
        logger.error(e) { "Error loading data" }
        println("Error reading from file: ${e.message}")
    }
}
