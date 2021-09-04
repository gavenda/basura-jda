package basura.graphql.anilist

import kotlinx.serialization.Serializable

@Serializable
data class CharacterConnection(
    val nodes: List<Character?>?,
    val edges: List<CharacterEdge?>?
)
