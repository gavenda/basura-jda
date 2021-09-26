package basura.command

import basura.LocaleMessage
import basura.bot
import basura.db.users
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.embed.pagedMediaEmbed
import basura.graphql.AniList
import basura.graphql.anilist.Media
import basura.graphql.anilist.MediaList
import basura.sendLocalized
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.map

/**
 * Filter hentai from a list of medias.
 */
internal fun List<Media>.filterHentai(allowHentai: Boolean): List<Media> {
    if (allowHentai) return this
    return filter {
        it.genres.any { genre ->
            genre == "Hentai" || genre == "Yuri" || genre == "Yaoi"
        }.not()
    }
}

/**
 * Map AniList identifier to the proper discord name
 */
internal fun aniListToDiscordNameMap(guild: Guild?): Map<Long, String?> {
    val db by bot.instance<Database>()
    if (guild == null) return mapOf()
    return db.users
        .filter { it.discordGuildId eq guild.idLong }
        .map {
            it.aniListId to guild.getMemberById(it.discordId)?.effectiveName
        }.toMap()
}

internal suspend fun lookupMediaList(medias: List<Media>?, guildId: Long?): List<MediaList>? {
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

    return aniList.findScoreByUsersAndMedias(
        userIds = db.users
            .filter { it.discordGuildId eq (guildId ?: -1) }
            .map { it.aniListId },
        mediaIds = medias?.map { it.id }
    )
}

internal suspend fun SlashCommandEvent.sendMediaResults(
    media: List<Media>,
    mediaList: List<MediaList>?,
    aniToDiscordName: Map<Long, String?>
) {
    val paginated = media.mapIndexed { i, m ->
        pagedMediaEmbed(m, mediaList, aniToDiscordName, (i + 1), media.size)
    }.toTypedArray()

    if (paginated.isEmpty()) {
        sendLocalized(LocaleMessage.Find.NoMatchingMedia)
        return
    }

    hook.sendPaginator(*paginated).await()
}
