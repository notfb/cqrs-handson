@file:UseSerializers(OffsetDateTimeSerializer::class)

package projection.dao

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import common.util.OffsetDateTimeSerializer
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import projection.aggregates.Snapshot
import projection.model.Principal
import projection.model.SnapshotEnvelope
import java.time.OffsetDateTime

class SnapshotDao :
    KoinComponent,
    ISnapshotDao {
    val database: MongoDatabase by inject()

    override suspend fun <S : Snapshot> findSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        serializer: KSerializer<S>,
    ): SnapshotEnvelope<S>? {
        val projectionCollection = database.getCollection<MongoEnvelope>("projections")
        val id = idFromPrincipal(aggregatorName, principal)

        return projectionCollection.find(Filters.eq("_id", id)).firstOrNull()?.let { envelope ->
            if (envelope.version == version) {
                val snapshot = json.decodeFromString(serializer, envelope.snapshot)
                SnapshotEnvelope(envelope.timestamp, envelope.eventIds, snapshot)
            } else {
                null
            }
        }
    }

    override suspend fun <S : Snapshot> storeSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        envelope: SnapshotEnvelope<S>,
        serializer: KSerializer<S>,
    ) {
        val projectionCollection = database.getCollection<MongoEnvelope>("projections")
        val id = idFromPrincipal(aggregatorName, principal)

        val doc =
            MongoEnvelope(
                id,
                aggregatorName,
                envelope.timestamp,
                version,
                envelope.eventIds,
                json.encodeToString(serializer, envelope.snapshot),
            )

        projectionCollection.replaceOne(Filters.eq("_id", id), doc, ReplaceOptions().upsert(true))
    }

    companion object {
        val json =
            Json {
                encodeDefaults = true
            }

        @Serializable
        data class MongoEnvelope(
            val _id: String,
            val aggregatorName: String,
            val timestamp: OffsetDateTime,
            val version: Int,
            val eventIds: Set<Long>,
            val snapshot: String,
        )

        private fun idFromPrincipal(
            aggregatorName: String,
            principal: Principal,
        ): String =
            when (principal) {
                is Principal.User -> "$aggregatorName-user-${principal.userId}"
                is Principal.Group -> "$aggregatorName-group-${principal.groupId}"
            }
    }
}
