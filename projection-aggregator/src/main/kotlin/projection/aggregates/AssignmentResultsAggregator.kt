package projection.aggregates

import common.model.event.AssignmentEvent
import common.model.event.Event
import common.model.event.ExerciseFinishedEvent
import kotlinx.serialization.Serializable
import projection.model.Principal

@Serializable
data class AssigmentResult(
    val userId: Long,
    val numbErrors: Int,
    val maxErrors: Int,
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

// TODO: unit test
// TODO: should never fail and validation should be done by publisher
class AssigmentResultsAggregator :
    BaseAggregator<AssigmentResultsSnapshot>(
        name = "assignment_results",
        version = 1,
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
            // is AddStudentEvent -> snapshot.copy(members = snapshot.members + event.userId)
            // is RemoveStudentEvent -> snapshot.copy(members = snapshot.members - event.userId)
            is AssignmentEvent -> {
                if (snapshot.assignments[event.assignmentId] == null) {
                    snapshot.copy(
                        assignments =
                        snapshot.assignments +
                                Pair(
                                    event.assignmentId,
                                    Assigment(assignmentId = event.assignmentId),
                                ),
                    )
                } else {
                    snapshot
                }
            }

            is ExerciseFinishedEvent -> {
                val assigment =
                    snapshot.assignments
                        .getOrElse(event.assignmentId) {
                            throw InvalidStateException(
                                "Failed to find assigment with id ${event.assignmentId} in snapshot $snapshot. " +
                                    "Missing AssignmentEvent for given assignmentId?",
                            )
                        }

                snapshot.copy(
                    assignments =
                        snapshot.assignments +
                            Pair(
                                event.assignmentId,
                                assigment.copy(
                                    results =
                                        assigment.results +
                                            Pair(
                                                event.userId,
                                                AssigmentResult(
                                                    userId = event.userId,
                                                    numbErrors = event.numErrors,
                                                    maxErrors = event.maxErrors,
                                                ),
                                            ),
                                ),
                            ),
                )
            }

            else -> throw UnsupportedEventTypeException(event::class)
        }
}

class InvalidStateException(msg: String) : RuntimeException(msg)
