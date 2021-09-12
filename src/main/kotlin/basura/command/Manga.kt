package basura.command

import basura.*
import basura.db.guilds
import basura.discord.interaction.deferReplyAwait
import basura.discord.interaction.requiredOption
import basura.graphql.AniList
import basura.graphql.anilist.MediaType
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first

suspend fun onManga(event: SlashCommandEvent) {
    val log by Log4j2("Manga")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

    event.deferReplyAwait()

    val query = event.requiredOption("query").asString.apply {
        log.debug("Looking up manga media: $this")
    }
    val allowHentai = if (event.isFromGuild) {
        db.guilds.first { it.discordGuildId eq event.guildContext.guild.idLong }.hentai
    } else false
    val media = aniList.findMediaByType(query, MediaType.MANGA)
        ?.filterHentai(allowHentai)

    if (media == null) {
        event.sendLocalized(LocaleMessage.Find.NoMatchingMedia)
        return
    }

    val mediaList = lookupMediaList(media, event.guild?.idLong)
    val aniToDiscordName = aniListToDiscordNameMap(event.guild)

    event.sendMediaResults(media, mediaList, aniToDiscordName)
}
