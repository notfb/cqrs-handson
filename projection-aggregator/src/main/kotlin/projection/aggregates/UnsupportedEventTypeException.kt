package projection.aggregates

import common.model.event.Event
import java.lang.RuntimeException
import kotlin.reflect.KClass

class UnsupportedEventTypeException(
    val eventType: KClass<out Event>,
) : RuntimeException("Unsupported event type: $eventType")
