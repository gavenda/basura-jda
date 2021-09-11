package basura.command

import basura.*
import basura.db.guilds
import basura.db.users
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateMediaEmbed
import basura.graphql.AniList
import basura.graphql.anilist.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.first
import org.ktorm.entity.map

internal fun List<Media>.filterHentai(allowHentai: Boolean): List<Media> {
    if (allowHentai) return this
    return filter {
        it.genres.containsAll(
            listOf("Hentai", "Yuri", "Yaoi")
        )
    }
}

fun JDA.handleFind(): JDA {
    val log by Log4j2("Find")
    val db by basura.instance<Database>()
    val aniList by basura.instance<AniList>()

    onCommand(Command.FIND, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up anime/manga media: $this")
        }
        val guild = event.guild
        val allowHentai = if (guild != null) {
            db.guilds.first { it.discordGuildId eq guild.idLong }.hentai
        } else false
        val media = aniList.findMedia(query)
            ?.filterHentai(allowHentai)

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)
        val aniToDiscordName = aniListToDiscordNameMap(guild)

        event.sendMediaResults(media, mediaList, aniToDiscordName)
    }
    onCommand(Command.ANIME, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up anime media: $this")
        }
        val guild = event.guild
        val allowHentai = if (guild != null) {
            db.guilds.first { it.discordGuildId eq guild.idLong }.hentai
        } else false
        val media = aniList.findMediaByType(query, MediaType.ANIME)
            ?.filterHentai(allowHentai)

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)
        val aniToDiscordName = aniListToDiscordNameMap(guild)

        event.sendMediaResults(media, mediaList, aniToDiscordName)
    }
    onCommand(Command.MANGA, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up manga media: $this")
        }
        val guild = event.guild
        val allowHentai = if (guild != null) {
            db.guilds.first { it.discordGuildId eq guild.idLong }.hentai
        } else false
        val media = aniList.findMediaByType(query, MediaType.MANGA)
            ?.filterHentai(allowHentai)

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)
        val aniToDiscordName = aniListToDiscordNameMap(guild)

        event.sendMediaResults(media, mediaList, aniToDiscordName)
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
        val guild = event.guild
        val allowHentai = if (guild != null) {
            db.guilds.first { it.discordGuildId eq guild.idLong }.hentai
        } else false
        val media = aniList.findMediaByRanking(
            amount = amount,
            formatIn = formats,
            season = mediaSeason,
            seasonYear = seasonYear
        )?.filterHentai(allowHentai)

        if (media == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
            return@onCommand
        }

        val mediaList = lookupMediaList(media, event.guild?.idLong)
        val aniToDiscordName = aniListToDiscordNameMap(guild)

        event.sendMediaResults(media, mediaList, aniToDiscordName)
    }

    return this
}

internal fun aniListToDiscordNameMap(guild: Guild?): Map<Long, String?> {
    val db by basura.instance<Database>()
    if (guild == null) return mapOf()
    return db.users
        .filter { it.discordGuildId eq guild.idLong }
        .map {
            it.aniListId to guild.getMemberById(it.discordId)?.effectiveName
        }.toMap()
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

internal suspend fun SlashCommandEvent.sendMediaResults(
    media: List<Media>,
    mediaList: List<MediaList>?,
    aniToDiscordName: Map<Long, String?>
) {
    val embeds = media.mapIndexed { i, m ->
        generateMediaEmbed(m, mediaList, aniToDiscordName, (i + 1), media.size)
    }.toTypedArray()

    if (embeds.isEmpty()) {
        sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
        return
    }

    hook.sendPaginator(*embeds).await()
}
