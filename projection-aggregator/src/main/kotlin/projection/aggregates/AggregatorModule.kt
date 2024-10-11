package projection.aggregates

object AggregatorModule {
    val aggregators: Map<String, BaseAggregator<out Snapshot>> =
        listOf(
            CoinsAndStarsAggregator(),
            GroupMembersAggregator(),
            AssigmentResultsAggregator(),
        ).associateBy { it.name }
}
