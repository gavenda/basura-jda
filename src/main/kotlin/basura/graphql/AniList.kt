package basura.graphql

import basura.graphql.anilist.*

interface AniList {

    /**
     * Finds an AniList user by their name, returning only their id and name.
     * @param name name to look up
     */
    suspend fun findUserByName(name: String): User?

    /**
     * Find user statistics by their name.
     * @param name name to look up
     */
    suspend fun findUserStatisticsByName(name: String): User?

    /**
     * Finds a media based on query.
     * @param query query name for media
     */
    suspend fun findMedia(query: String): List<Media>?

    /**
     * Finds a media based on query and type.
     * @param query query name for media
     * @param type type of media
     */
    suspend fun findMediaByType(query: String, type: MediaType): List<Media>?

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
    ): List<Media>?

    /**
     * Finds an AniList user score based on the given users and medias.
     * @param userIds user ids for related users
     * @param mediaIds media ids for related medias
     */
    suspend fun findScoreByUsersAndMedias(userIds: List<Long>?, mediaIds: List<Long>?): List<MediaList>?

    /**
     * Finds a character based on query.
     * @param query the character query
     */
    suspend fun findCharacter(query: String?): List<Character>?

    /**
     * Finds an anime/media staff based on query.
     * @param query the character query
     */
    suspend fun findStaff(query: String?): List<Staff>?
}