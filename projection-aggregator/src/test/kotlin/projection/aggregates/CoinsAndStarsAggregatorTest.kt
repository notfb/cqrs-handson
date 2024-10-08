package projection.aggregates

import projection.model.Principal

class CoinsAndStarsAggregatorTest : BaseAggregatorTest() {
    override val aggregator = CoinsAndStarsAggregator()
    override val principals = listOf(100, 101, 102, 104, 200, 201, 202).map { userId -> Principal.User(userId.toLong()) }
}
