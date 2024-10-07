package projection.aggregates

import common.model.event.AddStudentEvent
import common.model.event.Event
import common.model.event.RemoveStudentEvent
import kotlinx.serialization.Serializable
import projection.model.Principal

@Serializable
data class GroupMembersSnapshot(val members: Set<Long> = emptySet()) : Snapshot

class GroupMembersAggregator : BaseAggregator<GroupMembersSnapshot> (
    name = "group_members",
    version = 1,
    principalTypes = listOf(Principal.Group::class),
    eventTypes =
        setOf(
            AddStudentEvent::class,
            RemoveStudentEvent::class,
        ),
    snapshotSerializer = GroupMembersSnapshot.serializer(),
) {
    override fun initialSnapshot(principal: Principal): GroupMembersSnapshot = GroupMembersSnapshot()

    override suspend fun update(
        principal: Principal,
        event: Event,
        snapshot: GroupMembersSnapshot,
    ): GroupMembersSnapshot =
        when (event) {
            is AddStudentEvent -> snapshot.copy(members = snapshot.members + event.userId)
            is RemoveStudentEvent -> snapshot.copy(members = snapshot.members - event.userId)
            else -> throw UnsupportedEventTypeException(event::class)
        }
}
