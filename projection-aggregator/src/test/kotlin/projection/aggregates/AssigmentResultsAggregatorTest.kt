package projection.aggregates

import projection.model.Principal

// TODO: what does this test???
class AssigmentResultsAggregatorTest : BaseAggregatorTest() {
    override val aggregator = AssigmentResultsAggregator()
    override val principals = listOf(1000, 2000).map { groupId -> Principal.Group(groupId.toLong()) }
}