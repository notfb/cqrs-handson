package projection.aggregates

import common.model.event.AwardCoinsAndStarsEvent
import common.model.event.Event
import kotlinx.serialization.Serializable
import projection.model.Principal

@Serializable
data class CoinsAndStarsSnapshot(
    val coins: Int = 0,
    val stars: Int = 0,
) : Snapshot

class CoinsAndStarsAggregator :
    BaseAggregator<CoinsAndStarsSnapshot>(
        name = "coins_and_stars",
        version = 2,
        principalTypes = listOf(Principal.User::class),
        eventTypes = setOf(AwardCoinsAndStarsEvent::class),
        snapshotSerializer = CoinsAndStarsSnapshot.serializer(),
    ) {
    override fun initialSnapshot(principal: Principal): CoinsAndStarsSnapshot = CoinsAndStarsSnapshot()

    override suspend fun update(
        principal: Principal,
        event: Event,
        snapshot: CoinsAndStarsSnapshot,
    ): CoinsAndStarsSnapshot =
        when (event) {
            is AwardCoinsAndStarsEvent ->
                snapshot.copy(
                    coins = snapshot.coins + event.coins,
                    stars = snapshot.stars + event.stars,
                )
            else -> throw UnsupportedEventTypeException(event::class)
        }
}
