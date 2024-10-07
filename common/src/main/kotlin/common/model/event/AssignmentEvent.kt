@file:UseSerializers(OffsetDateTimeSerializer::class)

package common.model.event

import common.util.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.OffsetDateTime

@Serializable
@SerialName("AssignmentEvent")
data class AssignmentEvent(
    override val id: Long? = null,
    override val userId: Long,
    override val groupId: Long? = null,
    override val timestamp: OffsetDateTime,
    val assignmentId: Long,
    val exerciseId: String,
) : Event {
    override fun withId(id: Long): Event = copy(id = id)
}
