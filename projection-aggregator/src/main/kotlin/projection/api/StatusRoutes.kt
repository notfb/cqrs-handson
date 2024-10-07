package projection.api

import common.model.ApplicationStatus
import common.model.BuildInfo
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.time.Duration
import java.time.Instant

fun Route.statusRoutes() {
    val startTime: Instant by inject(named("startTime"))
    val buildInfo: BuildInfo by inject()

    get("/status") {
        val status =
            ApplicationStatus(
                buildInfo = buildInfo,
                upTime = Duration.between(startTime, Instant.now()).toString(),
            )
        call.respond(status)
    }
}
