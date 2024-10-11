package integration.steps

import common.model.event.Event
import io.cucumber.java8.En
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals

class EventPublisherStepDefinitions : En {
    init {
        When("The following event are posted to the event-publisher:") { event: Event ->
            runBlocking {
                val res =
                    EnvironmentStepDefinitions.client
                        .post("/v1/publish") {
                            contentType(ContentType.Application.Json)
                            setBody(listOf(event))
                        }
                assertEquals(HttpStatusCode.OK, res.status)
            }
        }
    }
}
