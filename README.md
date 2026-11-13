# Movie Backlog Tracker

## About

Movie Backlog Tracker is a Kotlin console application that allows users to keep track of movies and their associated actors.

The idea behind the project is to make it easier to organise a movie collection, keep track of films and manage the actors connected to each movie.

This project was developed as part of my Software Development Tools module at SETU.

## Features

- Add movies to the collection
- Add actors to the collection
- Associate actors with movies
- Remove actors from movies
- Assign unique IDs to movies and actors.
- Update movie watch status
- Search and filter entries
- Update and delete entries
- View multiple statistics reports for every data model
- Validate user input to prevent invalid entries
- Save and load data using JSON persistence
- JUnit tests to check that the main functionality works correctly

## Technologies Used

- Kotlin
- Gradle
- JUnit 5
- JSON persistence
- Git and GitHub

## Project Structure

The project is organised into separate packages to keep the code organised:

- **Model:** Contains the Movie, Actor and MovieActor classes.
- **Service:** Handles the application's main functionality and manages the data.
- **Persistence:** Handles saving and loading data.
- **Main:** Contains the console menu and allows users to interact with the application.
- **Test:** Handles all of the JUnit testing

## Future Improvements

Further improvements will be made as development continues, with additional functionality, testing and refinements planned for future versions.
