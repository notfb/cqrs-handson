package publisher.dao

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import common.model.event.Event
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EventDao :
    KoinComponent,
    IEventDao {
    val database: MongoDatabase by inject()
    val idGenerator: IdGenerator by inject()

    override suspend fun storeEvents(events: List<Event>): List<Event> {
        val eventCollection = database.getCollection<Event>("events")

        val stored = mutableListOf<Event>()
        for (event in events) {
            val toStore = event.withId(idGenerator.generate())

            eventCollection.insertOne(toStore)

            stored.add(toStore)
        }

        return stored
    }
}
