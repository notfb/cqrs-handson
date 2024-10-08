package common

import common.model.event.Event
import common.model.event.eventBsonCodec
import common.model.event.eventJson
import common.model.event.eventTypeName
import kotlinx.serialization.encodeToString
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class EventModelTest {
    @ParameterizedTest(name = "Event serialization: {0} line {1}")
    @MethodSource("testEvents")
    fun testEventSerialization(
        eventType: String,
        line: Int,
        event: Event,
    ) {
        val jsonCompat = eventJson.decodeFromString<Event>(eventJson.encodeToString(event))

        assertEquals(event, jsonCompat)

        val bson = BsonDocument()
        eventBsonCodec.encode(BsonDocumentWriter(bson), event, EncoderContext.builder().build())
        val bsonCompat = eventBsonCodec.decode(BsonDocumentReader(bson), DecoderContext.builder().build())

        assertEquals(event, bsonCompat)
    }

    companion object {
        @JvmStatic
        fun testEvents(): List<Arguments> =
            EventFixtures.testEvents().withIndex().map { (idx, event) ->
                Arguments.of(
                    eventTypeName(event::class),
                    idx,
                    event,
                )
            }
    }
}
