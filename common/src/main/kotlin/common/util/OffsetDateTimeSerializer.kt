package common.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonDateTime
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("java.time.OffsetDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OffsetDateTime =
        when (decoder) {
            is BsonDecoder ->
                when (val value = decoder.decodeBsonValue()) {
                    is BsonDateTime -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(value.value), ZoneOffset.UTC)
                    else -> throw RuntimeException("Expected bson date-time")
                }
            else -> OffsetDateTime.parse(decoder.decodeString())
        }

    override fun serialize(
        encoder: Encoder,
        value: OffsetDateTime,
    ) = when (encoder) {
        is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.toInstant().toEpochMilli()))
        else -> encoder.encodeString(value.toString())
    }
}
