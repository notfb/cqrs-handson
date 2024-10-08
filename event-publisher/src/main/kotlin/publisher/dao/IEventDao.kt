package publisher.dao

import common.model.event.Event

interface IEventDao {
    suspend fun storeEvents(events: List<Event>): List<Event>
}
