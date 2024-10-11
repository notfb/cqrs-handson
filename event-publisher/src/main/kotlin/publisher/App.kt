package publisher

import common.model.BuildInfo
import common.model.ErrorDetails
import common.model.event.eventJson
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import publisher.api.eventRoutes
import publisher.api.statusRoutes
import publisher.dao.daoModule
import java.time.Instant

fun Application.appModule() {
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { BuildInfo.getBuildInfo() }
                single(named("startTime"), createdAtStart = true) { Instant.now() }
                single(named("mongo")) { environment.config.config("mongo") }
            },
            daoModule,
        )
    }
    install(DefaultHeaders)
    install(CallLogging)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error(
                "Internal server error ${call.request.uri}",
                cause,
            )
            call.respond(
                ErrorDetails.response(HttpStatusCode.InternalServerError, cause.message),
            )
        }
        exception<NotFoundException> { call, cause ->
            call.application.log.warn("Not found ${call.request.uri}", cause)
            call.respond(
                ErrorDetails.response(HttpStatusCode.NotFound, cause.message),
            )
        }
        exception<BadRequestException> { call, cause ->
            call.application.log.warn("Bad request ${call.request.uri}", cause)
            call.respond(
                ErrorDetails.response(HttpStatusCode.BadRequest, cause.message),
            )
        }
        status(HttpStatusCode.UnsupportedMediaType) { call, status ->
            call.respond(
                ErrorDetails.response(status, status.description),
            )
        }
    }

    install(Routing) {
        install(ContentNegotiation) {
            json(eventJson)
        }

        statusRoutes()
        eventRoutes()
    }
}

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args),
        configure = {
            responseWriteTimeoutSeconds = 0
            shareWorkGroup = true
        },
    ).start(wait = true)
}
