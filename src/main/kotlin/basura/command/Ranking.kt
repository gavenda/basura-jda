package basura.command

import basura.*
import basura.db.guilds
import basura.graphql.AniList
import basura.graphql.anilist.MediaFormat
import basura.graphql.anilist.MediaSeason
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first

suspend fun onRanking(event: SlashCommandEvent) {
    val log by Log4j2("Ranking")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

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
        return
    }

    val mediaList = lookupMediaList(media, event.guild?.idLong)
    val aniToDiscordName = aniListToDiscordNameMap(guild)

    event.sendMediaResults(media, mediaList, aniToDiscordName)
}