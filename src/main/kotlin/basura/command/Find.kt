package basura.command

import basura.*
import basura.db.users
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateMediaEmbed
import basura.graphql.AniList
import basura.graphql.anilist.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.map

fun JDA.handleFind(): JDA {
    val log by Log4j2("Find")
    val aniList by basura.instance<AniList>()

    onCommand(Command.FIND, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up anime/manga media: $this")
        }
        val media = aniList.findMedia(query)

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)

        event.sendMediaResults(media, mediaList)
    }
    onCommand(Command.ANIME, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up anime media: $this")
        }
        val media = aniList.findMediaByType(query, MediaType.ANIME)

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)

        event.sendMediaResults(media, mediaList)
    }
    onCommand(Command.MANGA, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up manga media: $this")
        }
        val media = aniList.findMediaByType(query, MediaType.MANGA)

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)

        event.sendMediaResults(media, mediaList)
    }
    onCommand(Command.RANKING, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val amount = event.requiredOption("amount").asLong.toInt()
        val format = event.getOption("format")?.asString
        val season = event.getOption("season")?.asString
        val seasonYear = event.getOption("year")?.asLong?.toInt()

        log.debug("Looking up rankings: [ amount = $amount, format = $format, season = $season, seasonYear = $seasonYear ] ")

        val mediaSeason = season?.let { MediaSeason.valueOf(it) }
        val mediaFormat = format?.let { MediaFormat.valueOf(it) } ?: MediaFormat.TV

        val formats = listOf(mediaFormat)
        val media = aniList.findMediaByRanking(
            amount = amount,
            formatIn = formats,
            season = mediaSeason,
            seasonYear = seasonYear
        )

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)

        event.sendMediaResults(media, mediaList)
    }

    return this
}

internal suspend fun lookupMediaList(medias: List<Media>?, guildId: Long?): List<MediaList>? {
    val db by basura.instance<Database>()
    val aniList by basura.instance<AniList>()

    return aniList.findScoreByUsersAndMedias(
        userIds = db.users
            .filter { it.discordGuildId eq (guildId ?: -1) }
            .map { it.aniListId },
        mediaIds = medias?.map { it.id }
    )
}

internal suspend fun SlashCommandEvent.sendMediaResults(media: List<Media>, mediaList: List<MediaList>?) {
    val embeds = media.mapIndexed { i, m ->
        generateMediaEmbed(m, mediaList, (i + 1), media.size)
    }.toTypedArray()

    if (embeds.isEmpty()) {
        sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
        return
    }

    hook.sendPaginator(*embeds).await()
}

