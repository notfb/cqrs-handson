package common

import common.model.event.Event
import common.model.event.eventJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream

object EventFixtures {
    @OptIn(ExperimentalSerializationApi::class)
    fun testEvents(): List<Event> =
        EventFixtures::class.java.classLoader.getResourceAsStream("fixtures/events.json")!!.use {
            eventJson.decodeFromStream<List<Event>>(it).mapIndexed { index: Int, event: Event -> event.withId(index.toLong()) }
        }
}
