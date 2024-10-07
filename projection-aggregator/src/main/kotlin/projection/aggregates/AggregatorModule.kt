package projection.aggregates

object AggregatorModule {
    val aggregators: Map<String, BaseAggregator<out Snapshot>> =
        listOf(
            CoinsAndStarsAggregator(),
        ).associateBy { it.name }
}
