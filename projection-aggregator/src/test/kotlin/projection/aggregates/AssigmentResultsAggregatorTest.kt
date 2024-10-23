package projection.aggregates

import common.model.event.AssignmentEvent
import common.model.event.ExerciseFinishedEvent
import common.model.event.RemoveStudentEvent
import kotlinx.coroutines.runBlocking
import projection.model.Principal
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

private const val EXERCISE_ID = "exerciseId"

private val assignmentEvent =
    AssignmentEvent(
        id = 1,
        userId = 10,
        groupId = 11,
        timestamp = OffsetDateTime.now(),
        assignmentId = 12,
        exerciseId = EXERCISE_ID,
    )

private val removeStudentEvent =
    RemoveStudentEvent(
        id = 1,
        userId = 10,
        groupId = 11,
        timestamp = OffsetDateTime.now(),
    )

private val exerciseFinishedEvent =
    ExerciseFinishedEvent(
        id = 1,
        userId = 10,
        groupId = 11,
        timestamp = OffsetDateTime.now(),
        assignmentId = 12,
        exerciseId = EXERCISE_ID,
        numErrors = 5,
        maxErrors = 8,
    )


class AssigmentResultsAggregatorTest : BaseAggregatorTest() {
    override val aggregator = AssigmentResultsAggregator()
    override val principals = listOf(1000, 2000).map { groupId -> Principal.Group(groupId.toLong()) }

    @Test
    fun testInitialSnapshot() {
        assertEquals(AssigmentResultsSnapshot(), aggregator.initialSnapshot(Principal.Group(23)))
    }

    @Test
    fun testAssigmentEventShouldAssignStudentToGroup() {
        val expected =
            makeSnapshot(
                mapOf(
                    assignmentEvent.userId to
                        AssigmentResult(
                            userId = assignmentEvent.userId,
                            numErrors = 0,
                            maxErrors = 0,
                            completed = false,
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
    fun testRemoveStudentEventShouldRemoveStudentResults() {
        val expected = makeSnapshot()

        runBlocking {
            val updatedSnapshot =
                aggregator.update(
                    Principal.Group(assignmentEvent.groupId!!),
                    assignmentEvent,
                    AssigmentResultsSnapshot(),
                )
            assertEquals(
                expected,
                aggregator.update(
                    Principal.Group(assignmentEvent.groupId!!),
                    removeStudentEvent,
                    updatedSnapshot,
                ),
            )
        }
    }

    @Test
    fun testAssigmentEventShouldAssignMultipleStudentsToGroup() {
        val assignmentEvent2 = assignmentEvent.copy(id = 2, userId = 20)
        val expected =
            makeSnapshot(
                mapOf(
                    assignmentEvent.userId to
                        AssigmentResult(
                            userId = assignmentEvent.userId,
                            numErrors = 0,
                            maxErrors = 0,
                            completed = false,
                        ),
                    assignmentEvent2.userId to
                        AssigmentResult(
                            userId = assignmentEvent2.userId,
                            numErrors = 0,
                            maxErrors = 0,
                            completed = false,
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
                aggregator.update(
                    Principal.Group(assignmentEvent.groupId!!),
                    assignmentEvent2,
                    updatedSnapshot,
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
                                            numErrors = exerciseFinishedEvent.numErrors,
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

    private fun makeSnapshot(results: Map<Long, AssigmentResult> = emptyMap()) =
        AssigmentResultsSnapshot(
            mapOf(
                assignmentEvent.assignmentId to
                    Assigment(
                        assignmentId = assignmentEvent.assignmentId,
                        results = results,
                    ),
            ),
        )
}
