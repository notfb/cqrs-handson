package integration.dao

import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import projection.aggregates.Snapshot
import projection.dao.ISnapshotDao
import projection.model.Principal
import projection.model.SnapshotEnvelope

class MemorySnapshotDao :
    KoinComponent,
    ISnapshotDao {
    private val snapshots: MutableMap<String, String> = mutableMapOf()

    override suspend fun <S : Snapshot> findSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        serializer: kotlinx.serialization.KSerializer<S>,
    ): SnapshotEnvelope<S>? =
        snapshots[idFromPrincipal(aggregatorName, principal)]?.let {
            json.decodeFromString(SnapshotEnvelope.serializer(serializer), it)
        }

    override suspend fun <S : Snapshot> storeSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        envelope: SnapshotEnvelope<S>,
        serializer: kotlinx.serialization.KSerializer<S>,
    ) {
        snapshots[idFromPrincipal(aggregatorName, principal)] = json.encodeToString(SnapshotEnvelope.serializer(serializer), envelope)
    }

    companion object {
        val json =
            Json {
                encodeDefaults = true
            }

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
