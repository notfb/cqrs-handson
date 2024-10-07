package projection.model

sealed class Principal {
    class User(
        val userId: Long,
    ) : Principal()

    class Group(
        val groupId: Long,
    ) : Principal()
}
