package projection.aggregates

import projection.model.Principal

class GroupMembersAggregatorTest : BaseAggregatorTest() {
    override val aggregator = GroupMembersAggregator()
    override val principals = listOf(1000, 2000).map { groupId -> Principal.Group(groupId.toLong()) }
}
