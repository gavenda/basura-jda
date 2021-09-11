package basura.command

import basura.*
import basura.db.guilds
import basura.graphql.AniList
import basura.graphql.anilist.MediaType
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first

suspend fun onAnime(event: SlashCommandEvent) {
    val log by Log4j2("Anime")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

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
        return
    }

    val mediaList = lookupMediaList(media, event.guild?.idLong)
    val aniToDiscordName = aniListToDiscordNameMap(guild)

    event.sendMediaResults(media, mediaList, aniToDiscordName)
}
