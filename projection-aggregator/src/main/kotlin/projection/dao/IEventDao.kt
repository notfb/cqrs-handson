package projection.dao

import common.model.event.Event
import kotlinx.coroutines.flow.FlowCollector
import projection.model.Principal
import java.time.OffsetDateTime

interface IEventDao {
    suspend fun collectEvents(
        startDateTime: OffsetDateTime,
        eventTypes: List<String>,
        principal: Principal,
        collector: FlowCollector<Event>,
    )
}
