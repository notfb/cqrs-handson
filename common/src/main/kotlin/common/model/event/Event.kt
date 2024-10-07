package common.model.event

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.bson.codecs.kotlinx.BsonConfiguration
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import java.time.OffsetDateTime
import kotlin.reflect.KClass

@Serializable
sealed interface Event {
    val id: Long?
    val userId: Long
    val groupId: Long?
    val timestamp: OffsetDateTime

    fun withId(id: Long): Event
}

val eventJson =
    Json {
        classDiscriminator = "event_type"
    }

val eventBsonCodec =
    KotlinSerializerCodec.create<Event>(
        bsonConfiguration =
            BsonConfiguration(
                explicitNulls = true,
                classDiscriminator = "event_type",
            ),
    )!!

@OptIn(ExperimentalSerializationApi::class)
fun eventTypeName(eventType: KClass<out Event>): String {
    val eventSerializer = serializer(eventType, emptyList(), false)
    return eventSerializer.descriptor.serialName
}
