package basura

import basura.db.users
import basura.embed.createMediaEmbed
import basura.graphql.AniList
import basura.graphql.anilist.Media
import basura.graphql.anilist.MediaList
import basura.graphql.anilist.MediaType
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import io.github.furstenheim.CopyDown
import org.koin.java.KoinJavaComponent.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.entity.filter
import org.ktorm.entity.map
import respondingStandardPaginator

/**
 * Loads any file in resources and returns them as a string.
 */
fun findResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path)!!.readText()
}

/**
 * Abbreviates the string based on the given max.
 */
fun String.abbreviate(max: Int): String {
    if (length <= max) return this
    return take(max).substring(0, max - 3) + Typography.ellipsis
}

/**
 * Clean AniList profile description.
 */
fun String.aniClean(): String {
    return this
        .replace("~!", "")
        .replace("!~", "")
        .replace("~~~", "")
}

/**
 * Cleans down any html and converts them into a Markdown format.
 */
fun String.htmlClean(): String {
    val converter = CopyDown()
    return converter.convert(this)
}

/**
 * Cleans down any weird html from anilist and converts them into a Markdown format.
 */
fun String.weirdHtmlClean(): String {
    return this
        .replace("<i>", "_")
        .replace("</i>", "_")
        .replace("<b>", "**")
        .replace("</b>", "**")
}

/**
 * Converts AniList colors to their appropriate hex color.
 */
fun String.toHexColor() = when (this) {
    "blue" -> Color(0x3DB4F2)
    "purple" -> Color(0xC063FF)
    "green" -> Color(0x4CCA51)
    "orange" -> Color(0xEF881A)
    "red" -> Color(0xE13333)
    "pink" -> Color(0xFC9DD6)
    "gray" -> Color(0x677B94)
    else -> Color(0x000000)
}

fun Boolean.toYesNo(): String {
    if (this) return "Yes"
    return "No"
}

fun StringBuilder.appendIfNotMax(text: String, max: Int) {
    val sum = length + text.length
    if (sum < max) {
        append(text)
    }
}

/**
 * Converts this float to a star rating system.
 *
 * 0 to 29: 1 star
 * 30 to 49: 2 stars
 * 50 to 69: 3 stars
 * 70 to 89: 4 stars
 * 90 and beyond: 5 stars
 */
fun Float.toStars(): String {
    return this.toInt().toStars()
}

/**
 * Converts this integer to a star rating system.
 *
 * 0 to 29: 1 star
 * 30 to 49: 2 stars
 * 50 to 69: 3 stars
 * 70 to 89: 4 stars
 * 90 and beyond: 5 stars
 */
fun Int.toStars(): String {
    if (this >= 90) {
        // 5 star
        return "★".repeat(5)
    } else if (this in 70..89) {
        // 4 star
        return "★".repeat(4)
    } else if (this in 50..69) {
        // 3 star
        return "★".repeat(3)
    } else if (this in 30..49) {
        // 2 star
        return "★".repeat(2)
    } else if (this in 1..29) {
        // 1 star
        return "★".repeat(1)
    }
    return "-"
}

/**
 * Map AniList identifier to the proper discord name
 */
internal suspend fun aniListToDiscordNameMap(guild: Guild?, userIds: List<Long>?): Map<Long, String?> {
    val db by inject<Database>(Database::class.java)
    if (guild == null) return mapOf()
    if (userIds == null || userIds.isEmpty()) return mapOf()
    return db.users
        .filter { it.aniListId inList userIds }
        .map {
            it.aniListId to guild.getMember(Snowflake(it.discordId)).displayName
        }.toMap()
}

internal suspend fun lookupMediaList(medias: List<Media>?, guildId: Long?): List<MediaList>? {
    val db by inject<Database>(Database::class.java)
    val aniList by inject<AniList>(AniList::class.java)
    val userIds = db.users
        .filter { it.discordGuildId eq (guildId ?: -1) }
        .map { it.aniListId }
    return aniList.findScoreByUsersAndMedias(
        userIds = userIds,
        mediaIds = medias?.map { it.id }
    )
}

internal suspend fun PublicInteractionContext.sendMediaResult(guild: GuildBehavior?, media: List<Media>) {
    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
    val userIds = mediaList?.mapNotNull { it.user?.id }
    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull(), userIds)
    val paginator = respondingStandardPaginator {
        timeoutSeconds = PAGINATOR_TIMEOUT
        media.forEach {
            page {
                apply(createMediaEmbed(it, mediaList, aniToDiscordName))
            }
        }
    }

    if (paginator.pages.groups.isNotEmpty()) {
        paginator.send()
    }
}
