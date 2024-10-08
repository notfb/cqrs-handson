package projection.dao

import common.model.event.Event
import common.model.event.eventTypeName
import kotlinx.coroutines.flow.FlowCollector
import projection.model.Principal
import java.time.OffsetDateTime

class MemoryEventDao(
    val events: List<Event>,
) : IEventDao {
    override suspend fun collectEvents(
        startDateTime: OffsetDateTime,
        eventTypes: List<String>,
        principal: Principal,
        collector: FlowCollector<Event>,
    ) {
        for (event in events) {
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
