package basura.graphql

import basura.findResourceAsText
import basura.graphql.anilist.*
import kotlinx.serialization.Serializable

class AniListGraphQL : AniList {
    private val graphUri = "https://graphql.anilist.co"

    override suspend fun findUserByName(name: String): User? {
        val gqlQuery = findResourceAsText("/gql/FindUserByName.graphql")
        val variables = FindUser(name)
        val result = gqlQuery<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    override suspend fun findUserStatisticsByName(name: String): User? {
        val gqlQuery = findResourceAsText("/gql/FindStatisticsByUserName.graphql")
        val variables = FindUser(name)
        val result = gqlQuery<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    override suspend fun findMedia(query: String): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMedia.graphql")
        val variables = FindMedia(query)
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaByType(query: String, type: MediaType): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaByType.graphql")
        val variables = FindMedia(query, type)
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaByRanking(
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

    override suspend fun findScoreByUsersAndMedias(userIds: List<Long>?, mediaIds: List<Long>?): List<MediaList>? {
        val gqlQuery = findResourceAsText("/gql/FindScoreByMediaIdAndUserId.graphql")
        val variables = FindScore(userIds, mediaIds)
        val result = gqlQuery<FindScore, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.mediaList
    }

    override suspend fun findCharacter(query: String?): List<Character>? {
        val gqlQuery = findResourceAsText("/gql/FindCharacter.graphql")
        val variables = Find(query)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.characters
    }

    override suspend fun findStaff(query: String?): List<Staff>? {
        val gqlQuery = findResourceAsText("/gql/FindStaff.graphql")
        val variables = Find(query)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.staff
    }

    @Serializable
    data class FindUser(
        val name: String? = null
    )

    @Serializable
    data class Find(
        val query: String? = null,
        val page: Int = 1,
        val perPage: Int = 20,
    )

    @Serializable
    data class FindMedia(
        val query: String? = null,
        val type: MediaType? = null,
        val page: Int = 1,
        val perPage: Int = 20,
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
        val perPage: Int = 20
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