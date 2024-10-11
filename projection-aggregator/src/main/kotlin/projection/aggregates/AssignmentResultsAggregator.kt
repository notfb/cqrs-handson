package projection.aggregates

import arrow.core.firstOrNone
import arrow.core.getOrElse
import arrow.core.raise.result
import common.model.event.AddStudentEvent
import common.model.event.AssignmentEvent
import common.model.event.Event
import common.model.event.ExerciseFinishedEvent
import common.model.event.RemoveStudentEvent
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

class AssigmentResultsAggregator :
    BaseAggregator<AssigmentResultsSnapshot>(
        name = "group_members",
        version = 1,
        principalTypes = listOf(Principal.Group::class),
        eventTypes =
        setOf(
            // TODO: needed? for validation?
            AddStudentEvent::class,
            // TODO: Remove Result for userId
            RemoveStudentEvent::class,
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
            //is AddStudentEvent -> snapshot.copy(members = snapshot.members + event.userId)
            //is RemoveStudentEvent -> snapshot.copy(members = snapshot.members - event.userId)
            is AssignmentEvent -> {
                // TODO: check if assigment already exists?
                snapshot.copy(
                    assignments = snapshot.assignments + Pair(
                        event.assignmentId,
                        Assigment(assignmentId = event.assignmentId)
                    )
                )
            }

            is ExerciseFinishedEvent -> {
                val assigment = snapshot.assignments
                    .getOrElse(event.assignmentId) {
                        throw InvalidStateException(
                            "Failed to find assigment with id ${event.assignmentId} in snapshot ${snapshot}. " +
                                    "Missing AssignmentEvent for given assignmentId?"
                        )
                    }

                snapshot.copy(
                    assignments = snapshot.assignments + Pair(
                        event.assignmentId, assigment.copy(
                            results = assigment.results + Pair(
                                event.assignmentId, AssigmentResult(
                                    userId = event.userId,
                                    numbErrors = event.numErrors,
                                    maxErrors = event.maxErrors
                                )
                            )
                        )
                    )
                )
            }

            else -> throw UnsupportedEventTypeException(event::class)
        }
}

class InvalidStateException(msg: String) : RuntimeException(msg) {

}
