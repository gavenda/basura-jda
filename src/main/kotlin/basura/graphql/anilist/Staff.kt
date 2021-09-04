package basura.graphql.anilist

import kotlinx.serialization.Serializable

/**
 * Voice actors or production staff
 */
@Serializable
data class Staff(
    /**
     * The id of the staff member
     */
    val id: Long,
    /**
     * The names of the staff member
     */
    val name: StaffName?,
    /**
     * The primary language of the staff member. Current values: Japanese, English, Korean, Italian,
     * Spanish, Portuguese, French, German, Hebrew, Hungarian, Chinese, Arabic, Filipino, Catalan
     */
    val languageV2: String?,
    /**
     * The staff images
     */
    val image: StaffImage?,
    /**
     * A general description of the staff member
     */
    val description: String?,
    /**
     * The url for the staff page on the AniList website
     */
    val siteUrl: String?,
    /**
     * Media where the staff member has a production role
     */
    val staffMedia: MediaConnection?,
    /**
     * Characters voiced by the actor
     */
    val characters: CharacterConnection?,
    /**
     * The amount of user's who have favourited the staff member
     */
    val favourites: Int?
)
