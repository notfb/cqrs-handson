package projection.dao

import kotlinx.serialization.KSerializer
import projection.aggregates.Snapshot
import projection.model.Principal
import projection.model.SnapshotEnvelope

interface ISnapshotDao {
    suspend fun <S : Snapshot> findSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        serializer: KSerializer<S>,
    ): SnapshotEnvelope<S>?

    suspend fun <S : Snapshot> storeSnapshot(
        aggregatorName: String,
        principal: Principal,
        version: Int,
        envelope: SnapshotEnvelope<S>,
        serializer: KSerializer<S>,
    )
}
