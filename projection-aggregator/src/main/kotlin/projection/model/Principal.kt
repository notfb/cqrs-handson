package projection.model

sealed class Principal {
    data class User(
        val userId: Long,
    ) : Principal()

    data class Group(
        val groupId: Long,
    ) : Principal()
}
