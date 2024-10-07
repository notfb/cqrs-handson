# CQRS hands-on

## Background

* Users actions create different kind of events within the system that are collected by the "event-publisher" service
  * Main objective of the "event-publisher" is to validate the event-data and store it (currently mongo database)
* Events are aggregated into different views/snapshots within the "projection-aggregator" service to be displayed in the web-ui
  * Aggregation can be triggered on-demand or via a background process/task
* General business use-case:
  * Every user is identified by a "userId" and is part of a group/school-class identified by a "groupId"
  * Users are distinguished as teachers and students
  * A teacher acts a maintainer for a group/class, i.e. a teacher can add or remove students from a group. This will
    create "AddStudentEvent" resp. "RemoveStudentEvent" per student.
  * A teacher can assign an exercise (identified by an "exerciseId", which refers to a static-content file) to students within
   a group/class (e.g. homework). This will create an "AssignmentEvent" per student.
  * Students will work on assigned exercises which eventually creates an "ExerciseFinishedEvent" per student and assignment.
    The "ExerciseFinishedEvent" contains the number of errors made ("numErrors") and the maximum number of errors of
    the exercise, i.e. "numErrors == 0" is the perfect score and "numErrors == maxErrors" the worst possible score.
  * Based on performance the teacher will award students with coins (i.e. good job) and stars (i.e. extent job), which
    creates an "AwardCoinsAndStarsEvent" per student.

## Local development

* Requirements:
    * JDK == 17
    * docker/docker-compose are optional if a mongodb is available by some other means
    * curl or some alternative means to execute http requests

* Start a local mongodb (if not available otherwise)
  ```bash
  docker-compose up -d --wait
  ```

* Clean and build everything
  ```bash
  ./gradlew clean
  ./gradlew build
  ```

* Start event-publisher in dev-mode (on port 8123)
  ```bash
  ./gradlew :event-publisher:run
  ```

* Start projection-aggregator in dev-mode (on port 8124)
  ```bash
  ./gradlew :projection-aggregator:run
  ```

## Fixture

* Add the event fixtures to a running event-publisher
  ```bash
  curl -X POST -d @fixtures/events.json -H "Content-Type: application/json"  http://localhost:8123/v1/publish
  ```
* Simulated behaviour:
  * Teacher adds students "100, 101, 102, 103, 104" to group/class "1000" and students "200, 201, 202" to group/class "2000"
  * Correcting a mistake student "104" is removed from group/class "1000"
  * Teacher hands out an assignment of exercise "https://content.demo.nowhere/someexercise" to all students in group "1000"
  * Teacher hands out an assignment of exercise "https://content.demo.nowhere/someotherexercise" to all students in group "2000"
  * All students except "101" and "201" complete the assignment with different success
  * Teacher awards coins and stars to all students that have completed the assignment
* Students are then able to query their coins and stars from the projection-aggregator service:
  ```bash
  curl http://localhost:8124/v1/projections/coins_and_stars/user/$USER_ID
  ```
  e.g.
  ```bash
  > curl http://localhost:8124/v1/projections/coins_and_stars/user/100
  {"coins":3,"stars":1}
  ```

## Quiz

* What can be optimized?
* What needs to be refactored and/or added to be ready for production?


## Open discussion

* How can the projection-aggregator be extended so that aggregates/snapshots are updated
  in the background rather than on demand?
* How can coins and stars be awarded to the students automatically based on error percentage?
