package integration.steps

import io.cucumber.java8.En
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals

class ProjectionAggregatorStepDefinitions : En {
    init {
        Then("The projection {string} for group {long} should be:") { aggregatorName: String, groupId: Long, expected: JsonElement ->
            runBlocking {
                val res =
                    EnvironmentStepDefinitions.client
                        .get("/v1/projections/$aggregatorName/group/$groupId")
                assertEquals(HttpStatusCode.OK, res.status)
                val body = res.body<JsonElement>()
                assertEquals(expected, body)
            }
        }
    }
}
