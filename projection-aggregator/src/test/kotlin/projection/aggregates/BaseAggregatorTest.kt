package projection.aggregates

import au.com.origin.snapshots.Expect
import au.com.origin.snapshots.junit5.SnapshotExtension
import common.EventFixtures
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import projection.dao.IEventDao
import projection.dao.ISnapshotDao
import projection.dao.MemoryEventDao
import projection.dao.MemorySnapshotDao
import projection.model.Principal
import projection.services.AggregationService
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExtendWith(SnapshotExtension::class)
abstract class BaseAggregatorTest : KoinTest {
    abstract val aggregator: BaseAggregator<*>
    abstract val principals: List<Principal>

    val aggregationService: AggregationService by inject()
    lateinit var expect: Expect

    @BeforeTest
    fun setup() {
        startKoin {
            modules(
                module {
                    single { AggregationService() }
                    single { MemoryEventDao(EventFixtures.testEvents()) } bind IEventDao::class
                    single { MemorySnapshotDao() } bind ISnapshotDao::class
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testFixture() =
        runBlocking {
            val snapshots =
                principals
                    .map { principal ->
                        principal.toString() to aggregationService.updateSnapshot(aggregator, principal)
                    }.toMap()
            expect.serializer("orderedJson").toMatchSnapshot(snapshots)
        }
}
