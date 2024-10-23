package projection.aggregates

import common.model.event.AssignmentEvent
import common.model.event.ExerciseFinishedEvent
import kotlinx.coroutines.runBlocking
import projection.model.Principal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class AssigmentResultsAggregatorTest : BaseAggregatorTest() {
    override val aggregator = AssigmentResultsAggregator()
    override val principals = listOf(1000, 2000).map { groupId -> Principal.Group(groupId.toLong()) }

    private val assignmentEvent =
        AssignmentEvent(
            id = 1,
            userId = 10,
            groupId = 11,
            timestamp = OffsetDateTime.now(),
            assignmentId = 12,
            exerciseId = "exerciseId",
        )

    private val exerciseFinishedEvent =
        ExerciseFinishedEvent(
            id = 1,
            userId = 10,
            groupId = 11,
            timestamp = OffsetDateTime.now(),
            assignmentId = 12,
            exerciseId = "exerciseId",
            numErrors = 5,
            maxErrors = 8,
        )

    @Test
    fun testInitialSnapshot() {
        assertEquals(AssigmentResultsSnapshot(), aggregator.initialSnapshot(Principal.Group(23)))
    }

    @Test
    fun testAssigmentEventShouldAssignStudentToGroup() {
        val expected =
            AssigmentResultsSnapshot(
                mapOf(
                    assignmentEvent.assignmentId to
                        Assigment(
                            assignmentId = assignmentEvent.assignmentId,
                            results =
                                mapOf(
                                    assignmentEvent.userId to
                                        AssigmentResult(
                                            userId = assignmentEvent.userId,
                                            numbErrors = 0,
                                            maxErrors = 0,
                                            completed = false,
                                        ),
                                ),
                        ),
                ),
            )

        runBlocking {
            assertEquals(
                expected,
                aggregator.update(
                    Principal.Group(assignmentEvent.groupId!!),
                    assignmentEvent,
                    AssigmentResultsSnapshot(),
                ),
            )
        }
    }

    @Test
    fun testExerciseFinishedEventShouldMarkExerciseAsFinishedWithErrorCounts() {
        val expected =
            AssigmentResultsSnapshot(
                mapOf(
                    assignmentEvent.assignmentId to
                        Assigment(
                            assignmentId = assignmentEvent.assignmentId,
                            results =
                                mapOf(
                                    assignmentEvent.userId to
                                        AssigmentResult(
                                            userId = assignmentEvent.userId,
                                            numbErrors = exerciseFinishedEvent.numErrors,
                                            maxErrors = exerciseFinishedEvent.maxErrors,
                                            completed = true,
                                        ),
                                ),
                        ),
                ),
            )

        runBlocking {
            val updatedSnapshot =
                aggregator.update(
                    Principal.Group(assignmentEvent.groupId!!),
                    assignmentEvent,
                    AssigmentResultsSnapshot(),
                )
            assertEquals(
                expected,
                aggregator.update(Principal.Group(assignmentEvent.groupId!!), exerciseFinishedEvent, updatedSnapshot),
            )
        }
    }
}
