@file:UseSerializers(OffsetDateTimeSerializer::class)

package projection.model

import common.util.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.OffsetDateTime

@Serializable
data class SnapshotEnvelope<S>(
    val timestamp: OffsetDateTime,
    val eventIds: Set<Long>,
    val snapshot: S,
)
