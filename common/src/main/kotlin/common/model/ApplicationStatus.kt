package common.model

import kotlinx.serialization.Serializable
import java.util.Properties

@Serializable
data class BuildInfo(
    val baseCommit: String,
    val baseBranch: String,
    val baseBuild: String,
) {
    companion object {
        fun getBuildInfo(): BuildInfo {
            val buildInfo = Properties()
            buildInfo.load(BuildInfo::class.java.getResourceAsStream("/buildInfo.properties"))
            val build = buildInfo.getProperty("baseBuild", "unknown")

            return BuildInfo(
                baseCommit = buildInfo.getProperty("baseCommit", "unknown"),
                baseBranch = buildInfo.getProperty("baseBranch", "unknown"),
                baseBuild = build,
            )
        }
    }
}

@Serializable
data class ApplicationStatus(
    val buildInfo: BuildInfo,
    val upTime: String,
)
