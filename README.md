# Assignment 6 – Database Localization

## Project Overview

This project is a JavaFX temperature converter and measurement application supporting English, Finnish, Swedish, Japanese and Arabic. Users can convert temperatures, calculate speed, save conversions and view their five most recent conversions.

## Localization Design

The project uses the Translation Table approach. The `units` table stores language-independent unit information, while `unit_translation` stores translated unit names and descriptions.

This approach avoids duplicating unit records for each language and makes it easier to add new languages. When a translation is unavailable, English is used as the fallback language.

The JavaFX interface uses ResourceBundle property files. Changing the selected language updates both the interface and database unit translations. Arabic uses right-to-left layout.

## Database Design

- `units`: stores unit identifiers and categories.
- `unit_translation`: stores translated names and descriptions.
- `conversions`: stores conversion inputs, outputs, language and timestamps.
- `measurements`: stores distance, time and calculated speed, related to conversions.

## Technologies

- Java 17
- JavaFX
- Maven
- MariaDB
- JUnit 5
- JaCoCo
- Jenkins
- Docker
- Kubernetes and Minikube

## Running the Application

Initialize the MariaDB database using `src/main/resources/init.sql`. Configure the database connection using environment variables and run:

`mvn clean javafx:run`

Run tests and generate the coverage report:

`mvn clean verify`

## Docker and Kubernetes

Jenkins builds and tests the project, creates a Docker image and pushes it to Docker Hub. The Kubernetes manifests in the `k8s` directory deploy the application and MariaDB to Minikube.

## Screenshots

Add screenshots showing:
- English interface
- Japanese interface
- Arabic interface
- Successful Jenkins pipeline
- JaCoCo coverage report
- Kubernetes pods running