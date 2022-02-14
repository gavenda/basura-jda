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

    override suspend fun findMedia(query: String, allowHentai: Boolean): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMedia.graphql")
        val variables = FindMedia(query, allowHentai = allowHentai)
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaByType(query: String, type: MediaType, allowHentai: Boolean): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaByType.graphql")
        val variables = FindMedia(query, type, allowHentai = allowHentai)
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaByRanking(
        amount: Int,
        formatIn: List<MediaFormat>?,
        season: MediaSeason?,
        seasonYear: Int?,
        allowHentai: Boolean
    ): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaByRanking.graphql")
        val variables = FindMedia(
            perPage = amount,
            sort = listOf(MediaSort.SCORE_DESC),
            formatIn = formatIn,
            season = season,
            seasonYear = seasonYear,
            allowHentai = allowHentai
        )
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaTitles(query: String, type: MediaType?): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindMediaName.graphql")
        val variables = FindMedia(
            query = query,
            type = type
        )
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.media?.forEach {
                it.title?.native?.let { title -> add(title) }
                it.title?.romaji?.let { title -> add(title) }
            }
        }.distinct()
    }

    override suspend fun findCharacterNames(query: String): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindCharacterName.graphql")
        val variables = Find(query)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.characters?.forEach {
                it.name?.native?.let { title -> add(title) }
                it.name?.alternative?.let { titles -> addAll(titles.filterNotNull()) }
                it.name?.full?.let { title -> add(title) }
            }
        }.distinct()
    }

    override suspend fun findStaffNames(query: String): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindStaffName.graphql")
        val variables = Find(query)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.staff?.forEach {
                it.name?.native?.let { title -> add(title) }
                it.name?.alternative?.let { titles -> addAll(titles.filterNotNull()) }
                it.name?.full?.let { title -> add(title) }
            }
        }.distinct()
    }

    override suspend fun findUserNames(query: String): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindUserName.graphql")
        val variables = Find(query)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.users?.forEach {
                add(it.name)
            }
        }
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
        val perPage: Int = 10,
    )

    @Serializable
    data class FindMedia(
        val query: String? = null,
        val type: MediaType? = null,
        val page: Int = 1,
        val perPage: Int = 10,
        val sort: List<MediaSort>? = null,
        val formatIn: List<MediaFormat>? = null,
        val season: MediaSeason? = null,
        val seasonYear: Int? = null,
        val allowHentai: Boolean = false
    )

    @Serializable
    data class FindScore(
        val userId: List<Long>?,
        val mediaId: List<Long>?,
        val page: Int = 1,
        val perPage: Int = 10
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