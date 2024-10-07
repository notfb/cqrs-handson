package projection.aggregates

import common.model.event.Event
import kotlinx.serialization.KSerializer
import projection.model.Principal
import kotlin.reflect.KClass

interface Snapshot

abstract class BaseAggregator<S : Snapshot>(
    val name: String,
    val version: Int,
    val principalTypes: List<KClass<out Principal>>,
    val eventTypes: Set<KClass<out Event>>,
    val snapshotSerializer: KSerializer<S>,
) {
    abstract fun initialSnapshot(principal: Principal): S

    abstract suspend fun update(
        principal: Principal,
        event: Event,
        snapshot: S,
    ): S
}
