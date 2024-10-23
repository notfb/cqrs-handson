package projection.aggregates

import common.model.event.AssignmentEvent
import common.model.event.Event
import common.model.event.ExerciseFinishedEvent
import kotlinx.serialization.Serializable
import projection.model.Principal

@Serializable
data class AssigmentResult(
    val userId: Long,
    val numbErrors: Int = 0,
    val maxErrors: Int = 0,
    val completed: Boolean = false,
)

@Serializable
data class Assigment(
    val assignmentId: Long,
    val results: Map<Long, AssigmentResult> = emptyMap(),
)

@Serializable
data class AssigmentResultsSnapshot(
    val assignments: Map<Long, Assigment> = emptyMap(),
) : Snapshot

class InvalidStateException(msg: String) : RuntimeException(msg)

// TODO: should never fail and validation should be done by publisher
class AssigmentResultsAggregator :
    BaseAggregator<AssigmentResultsSnapshot>(
        name = "assignment_results",
        version = 2,
        principalTypes = listOf(Principal.Group::class),
        eventTypes =
            setOf(
                // TODO: needed? for validation?
                // AddStudentEvent::class,
                // TODO: Remove Result for userId?
                // RemoveStudentEvent::class,
                ExerciseFinishedEvent::class,
                AssignmentEvent::class,
            ),
        snapshotSerializer = AssigmentResultsSnapshot.serializer(),
    ) {
    override fun initialSnapshot(principal: Principal): AssigmentResultsSnapshot = AssigmentResultsSnapshot()

    override suspend fun update(
        principal: Principal,
        event: Event,
        snapshot: AssigmentResultsSnapshot,
    ): AssigmentResultsSnapshot =
        when (event) {
            is AssignmentEvent -> aggregateAssigmentEvent(snapshot, event)
            is ExerciseFinishedEvent -> aggregateExerciseFinishedEvent(snapshot, event)
            else -> throw UnsupportedEventTypeException(event::class)
        }

    private fun aggregateAssigmentEvent(
        snapshot: AssigmentResultsSnapshot,
        event: AssignmentEvent,
    ): AssigmentResultsSnapshot {
        val existingResults = snapshot.assignments[event.assignmentId]?.results ?: emptyMap()
        return snapshot.copy(
            assignments = snapshot.assignments + makeAssigmentPair(event, existingResults),
        )
    }

    private fun makeAssigmentPair(
        event: AssignmentEvent,
        existingResults: Map<Long, AssigmentResult>,
    ) = Pair(
        event.assignmentId,
        Assigment(
            assignmentId = event.assignmentId,
            results =
                existingResults +
                    Pair(
                        event.userId,
                        AssigmentResult(userId = event.userId),
                    ),
        ),
    )

    private fun aggregateExerciseFinishedEvent(
        snapshot: AssigmentResultsSnapshot,
        event: ExerciseFinishedEvent,
    ): AssigmentResultsSnapshot {
        val assigment =
            snapshot.assignments
                .getOrElse(event.assignmentId) {
                    throw InvalidStateException(
                        "Failed to find assigment with id ${event.assignmentId} in snapshot $snapshot. " +
                            "Missing AssignmentEvent for given assignmentId?",
                    )
                }

        return snapshot.copy(
            assignments =
                snapshot.assignments +
                    Pair(
                        event.assignmentId,
                        assigment.copy(results = assigment.results + makeResultPair(event)),
                    ),
        )
    }

    private fun makeResultPair(event: ExerciseFinishedEvent): Pair<Long, AssigmentResult> {
        return Pair(
            event.userId,
            AssigmentResult(
                userId = event.userId,
                numbErrors = event.numErrors,
                maxErrors = event.maxErrors,
                completed = true,
            ),
        )
    }
}
