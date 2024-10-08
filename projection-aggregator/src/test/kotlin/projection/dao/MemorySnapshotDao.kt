package projection.dao

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import projection.aggregates.Snapshot
import projection.model.Principal
import projection.model.SnapshotEnvelope

class MemorySnapshotDao : ISnapshotDao {
    private val snapshots = mutableMapOf<String, String>()

    override suspend fun <S : Snapshot> findSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        serializer: KSerializer<S>,
    ): SnapshotEnvelope<S>? =
        snapshots[idFromPrincipal(aggregatorName, principal)]?.let {
            json.decodeFromString(SnapshotEnvelope.serializer(serializer), it)
        }

    override suspend fun <S : Snapshot> storeSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        envelope: SnapshotEnvelope<S>,
        serializer: KSerializer<S>,
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
