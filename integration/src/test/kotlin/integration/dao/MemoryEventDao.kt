package integration.dao

import common.model.event.Event
import common.model.event.eventTypeName
import kotlinx.coroutines.flow.FlowCollector
import org.koin.core.component.KoinComponent
import projection.model.Principal
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicLong

class MemoryEventDao :
    KoinComponent,
    publisher.dao.IEventDao,
    projection.dao.IEventDao {
    private val idCounter = AtomicLong(0)
    private val eventStore: MutableList<Event> = mutableListOf()

    override suspend fun storeEvents(events: List<Event>): List<Event> {
        eventStore += events.map { it.withId(idCounter.getAndIncrement()) }
        return events
    }

    override suspend fun collectEvents(
        startDateTime: OffsetDateTime,
        eventTypes: List<String>,
        principal: Principal,
        collector: FlowCollector<Event>,
    ) {
        for (event in eventStore) {
            if (!eventTypes.contains(eventTypeName(event::class))) {
                continue
            }
            when (principal) {
                is Principal.User -> if (event.userId != principal.userId) continue
                is Principal.Group -> if (event.groupId != principal.groupId) continue
            }
            collector.emit(event)
        }
    }
}
