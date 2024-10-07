package projection.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject
import projection.aggregates.AggregatorModule
import projection.aggregates.BaseAggregator
import projection.aggregates.Snapshot
import projection.model.Principal
import projection.services.AggregationService

fun Route.projectionRoutes() {
    val aggregationService: AggregationService by inject()

    fun aggregatorFromCall(call: ApplicationCall): BaseAggregator<out Snapshot> {
        val aggregatorName =
            call.parameters["aggregator"] ?: throw BadRequestException(
                "aggregator not set",
            )
        return AggregatorModule.aggregators[aggregatorName] ?: throw NotFoundException("Aggregator $aggregatorName does not exists")
    }

    get("/v1/projections/{aggregator}/user/{userId}") {
        val userId =
            call.parameters["userId"]?.toLong() ?: throw BadRequestException(
                "userId not set",
            )
        val aggregator = aggregatorFromCall(call)
        if (!aggregator.principalTypes.contains(
                Principal.User::class,
            )
        ) {
            throw BadRequestException("Aggregator ${aggregator.name} not available for users")
        }

        call.respond(HttpStatusCode.OK, aggregationService.updateSnapshot(aggregator, Principal.User(userId)))
    }

    get("/v1/projections/{aggregator}/group/{groupId}") {
        val aggregator = aggregatorFromCall(call)
        val groupId =
            call.parameters["groupId"]?.toLong() ?: throw BadRequestException(
                "groupId not set",
            )
        if (!aggregator.principalTypes.contains(
                Principal.Group::class,
            )
        ) {
            throw BadRequestException("Aggregator ${aggregator.name} not available for groups")
        }

        call.respond(HttpStatusCode.OK, aggregationService.updateSnapshot(aggregator, Principal.Group(groupId)))
    }
}
