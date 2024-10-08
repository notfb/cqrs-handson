package publisher.api

import common.model.event.Event
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject
import publisher.dao.IEventDao

fun Route.eventRoutes() {
    val eventDao: IEventDao by inject()

    post("/v1/publish") {
        val events = call.receive<List<Event>>()

        call.respond(HttpStatusCode.OK, eventDao.storeEvents(events))
    }
}
