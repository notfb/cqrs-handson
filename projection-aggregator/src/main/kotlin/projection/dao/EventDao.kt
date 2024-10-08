package projection.dao

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import common.model.event.Event
import kotlinx.coroutines.flow.FlowCollector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import projection.model.Principal
import java.time.OffsetDateTime

class EventDao :
    KoinComponent,
    IEventDao {
    val database: MongoDatabase by inject()

    override suspend fun collectEvents(
        startDateTime: OffsetDateTime,
        eventTypes: List<String>,
        principal: Principal,
        collector: FlowCollector<Event>,
    ) {
        val eventCollection = database.getCollection<Event>("events")
        val filters =
            mutableListOf(
                Filters.gt("timestamp", startDateTime.toInstant()),
                Filters.`in`("event_type", eventTypes),
            )

        when (principal) {
            is Principal.User -> filters.add(Filters.eq("userId", principal.userId))
            is Principal.Group -> filters.add(Filters.eq("groupId", principal.groupId))
        }

        eventCollection.find(Filters.and(filters)).sort(Sorts.ascending("timestamp")).collect(collector)
    }
}
