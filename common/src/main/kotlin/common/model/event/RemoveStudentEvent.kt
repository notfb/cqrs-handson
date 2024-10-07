@file:UseSerializers(OffsetDateTimeSerializer::class)

package common.model.event

import common.util.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.OffsetDateTime

@Serializable
@SerialName("RemoveStudentEvent")
data class RemoveStudentEvent(
    override val id: Long? = null,
    override val userId: Long,
    override val groupId: Long?,
    override val timestamp: OffsetDateTime,
) : Event {
    override fun withId(id: Long): Event = copy(id = id)
}
