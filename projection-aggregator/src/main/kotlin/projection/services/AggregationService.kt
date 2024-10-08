package projection.services

import common.model.event.eventTypeName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import projection.aggregates.BaseAggregator
import projection.aggregates.Snapshot
import projection.aggregates.UnsupportedEventTypeException
import projection.dao.IEventDao
import projection.dao.ISnapshotDao
import projection.model.Principal
import projection.model.SnapshotEnvelope
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class AggregationService : KoinComponent {
    val eventDao: IEventDao by inject()
    val snapshotDao: ISnapshotDao by inject()

    suspend fun <S : Snapshot> updateSnapshot(
        aggregator: BaseAggregator<S>,
        principal: Principal,
    ): S {
        val envelope =
            snapshotDao.findSnapshot(aggregator.name, principal, aggregator.version, aggregator.snapshotSerializer)
        var snapshot = envelope?.snapshot ?: aggregator.initialSnapshot(principal)
        var timestamp = envelope?.timestamp ?: OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        var changed = false
        val eventIds = envelope?.eventIds?.toMutableSet() ?: mutableSetOf()
        val eventTypes = aggregator.eventTypes.map { eventTypeName(it) }

        eventDao.collectEvents(timestamp, eventTypes, principal) { event ->
            if (!eventIds.contains(event.id!!)) {
                try {
                    snapshot = aggregator.update(principal, event, snapshot)
                    changed = true
                } catch (e: UnsupportedEventTypeException) {
                    logger.error(e.message, e)
                }
                timestamp = if (event.timestamp > timestamp) event.timestamp else timestamp
                eventIds.add(event.id!!)
            }
        }

        if (changed) {
            snapshotDao.storeSnapshot(
                aggregator.name,
                principal,
                aggregator.version,
                SnapshotEnvelope(timestamp, eventIds, snapshot),
                aggregator.snapshotSerializer,
            )
        }
        return snapshot
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AggregationService::class.java)
    }
}
