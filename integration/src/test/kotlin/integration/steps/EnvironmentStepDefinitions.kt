package integration.steps

import common.model.BuildInfo
import common.model.event.Event
import common.model.event.eventJson
import integration.dao.MemoryEventDao
import integration.dao.MemorySnapshotDao
import io.cucumber.docstring.DocStringType
import io.cucumber.java8.En
import io.cucumber.java8.Scenario
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import projection.api.projectionRoutes
import projection.services.serviceModule
import publisher.api.eventRoutes
import publisher.api.statusRoutes
import java.net.ServerSocket
import java.time.Instant
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class EnvironmentStepDefinitions : En {
    var testServer: ApplicationEngine? = null

    init {
        After { scenario: Scenario ->
            testServer?.stop()
            testServer = null
        }

        Given("A running event-publisher and projection-aggregator") {
            val env =
                applicationEngineEnvironment {
                    connector {
                        host = "localhost"
                        port = testServerPort
                    }
                    module {
                        install(Koin) {
                            slf4jLogger()
                            modules(
                                org.koin.dsl.module {
                                    single { BuildInfo.getBuildInfo() }
                                    single(named("startTime"), createdAtStart = true) { Instant.now() }
                                    single(named("mongo")) { environment.config.config("mongo") }

                                    single { MemoryEventDao() } binds
                                        arrayOf(publisher.dao.IEventDao::class, projection.dao.IEventDao::class)
                                    single { MemorySnapshotDao() } bind projection.dao.ISnapshotDao::class
                                },
                                serviceModule,
                            )
                        }
                        install(Routing) {
                            install(ContentNegotiation) {
                                json(eventJson)
                            }

                            statusRoutes()
                            eventRoutes()
                            projectionRoutes()
                        }
                    }
                }
            testServer = embeddedServer(Netty, env) {}
            testServer?.start(wait = false)

            Thread.sleep(100)
        }

        DocStringType("event") { text: String -> eventJson.decodeFromString<Event>(text) }

        DocStringType("json") { text: String -> Json.decodeFromString<JsonElement>(text) }
    }

    companion object {
        val testServerPort: Int by lazy { ServerSocket(0).use { it.localPort } }
        val client: HttpClient by lazy {
            HttpClient(CIO) {
                defaultRequest {
                    url {
                        protocol = URLProtocol.HTTP
                        host = "localhost"
                        port = testServerPort
                    }
                }
                install(ClientContentNegotiation) {
                    json(eventJson)
                }
            }
        }
    }
}
