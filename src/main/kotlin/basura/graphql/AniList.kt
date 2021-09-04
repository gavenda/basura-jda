package basura.graphql

import basura.findResourceAsText
import basura.graphql.anilist.*
import kotlinx.serialization.Serializable

class AniList {
    private val graphUri = "https://graphql.anilist.co"

    /**
     * Finds an AniList user by their name, returning only their id and name.
     * @param name name to look up
     */
    suspend fun findUserByName(name: String): User? {
        val gqlQuery = findResourceAsText("/gql/FindUserByName.graphql")
        val variables = FindUser(name)
        val result = gqlQuery<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    /**
     * Find user statistics by their name.
     * @param name name to look up
     */
    suspend fun findUserStatisticsByName(name: String): User? {
        val gqlQuery = findResourceAsText("/gql/FindStatisticsByUserName.graphql")
        val variables = FindUser(name)
        val result = gqlQuery<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    /**
     * Finds a media based on query.
     * @param query query name for media
     */
    suspend fun findMedia(query: String): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMedia.graphql")
        val variables = FindMedia(query)
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    /**
     * Finds a media based on query and type.
     * @param query query name for media
     * @param type type of media
     */
    suspend fun findMediaByType(query: String, type: MediaType): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaByType.graphql")
        val variables = FindMedia(query, type)
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    /**
     * Finds a media based on its ranking.
     * @param amount the amount of results to return
     * @param formatIn the media formats to filter
     * @param season the season to filter
     * @param seasonYear the season year to filter
     */
    suspend fun findMediaByRanking(
        amount: Int,
        formatIn: List<MediaFormat>?,
        season: MediaSeason?,
        seasonYear: Int?
    ): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaByRanking.graphql")
        val variables = FindMedia(
            perPage = amount,
            sort = listOf(MediaSort.SCORE_DESC),
            formatIn = formatIn,
            season = season,
            seasonYear = seasonYear
        )
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    /**
     * Finds an AniList user score based on the given users and medias.
     * @param userIds user ids for related users
     * @param mediaIds media ids for related medias
     */
    suspend fun findScoreByUsersAndMedias(userIds: List<Long>?, mediaIds: List<Long>?): List<MediaList>? {
        val gqlQuery = findResourceAsText("/gql/FindScoreByMediaIdAndUserId.graphql")
        val variables = FindScore(userIds, mediaIds)
        val result = gqlQuery<FindScore, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.mediaList
    }

    /**
     * Finds a character based on query.
     * @param query the character query
     */
    suspend fun findCharacter(query: String?): List<Character>? {
        val gqlQuery = findResourceAsText("/gql/FindCharacter.graphql")
        val variables = FindCharacter(query)
        val result = gqlQuery<FindCharacter, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.characters
    }

    @Serializable
    data class FindUser(
        val name: String? = null
    )

    @Serializable
    data class FindCharacter(
        val query: String? = null
    )

    @Serializable
    data class FindMedia(
        val query: String? = null,
        val type: MediaType? = null,
        val page: Int = 1,
        val perPage: Int = 25,
        val sort: List<MediaSort>? = null,
        val formatIn: List<MediaFormat>? = null,
        val season: MediaSeason? = null,
        val seasonYear: Int? = null
    )

    @Serializable
    data class FindScore(
        val userId: List<Long>?,
        val mediaId: List<Long>?,
        val page: Int = 1,
        val perPage: Int = 25
    )

    @Serializable
    data class UserResult(
        val User: User?
    )

    @Serializable
    data class PageResult(
        val Page: Page?
    )

}