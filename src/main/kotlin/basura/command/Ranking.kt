package basura.command

import basura.LocaleMessage
import basura.Log4j2
import basura.bot
import basura.db.guilds
import basura.discord.interaction.asInt
import basura.discord.interaction.deferReplyAwait
import basura.discord.interaction.requiredOption
import basura.graphql.AniList
import basura.graphql.anilist.MediaFormat
import basura.graphql.anilist.MediaSeason
import basura.sendLocalizedMessage
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first

suspend fun onRanking(event: SlashCommandEvent) {
    val log by Log4j2("Ranking")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

    event.deferReplyAwait()

    val amount = event.requiredOption("amount").asInt
    val format = event.getOption("format")?.asString
    val season = event.getOption("season")?.asString
    val seasonYear = event.getOption("year")?.asInt

    log.debug("Looking up rankings: [ amount = $amount, format = $format, season = $season, seasonYear = $seasonYear ] ")

    val mediaSeason = season?.let { MediaSeason.valueOf(it) }
    val mediaFormat = format?.let { MediaFormat.valueOf(it) } ?: MediaFormat.TV

    val formats = listOf(mediaFormat)
    val allowHentai = if (event.isFromGuild) {
        db.guilds.first { it.discordGuildId eq event.guildContext.guild.idLong }.hentai
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
    val aniToDiscordName = aniListToDiscordNameMap(event.guild)

    event.sendMediaResults(media, mediaList, aniToDiscordName)
}