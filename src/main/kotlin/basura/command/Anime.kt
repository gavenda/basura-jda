package basura.command

import basura.LocaleMessage
import basura.Log4j2
import basura.bot
import basura.db.guilds
import basura.graphql.AniList
import basura.graphql.anilist.MediaType
import basura.sendLocalized
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first

suspend fun onAnime(event: SlashCommandEvent) {
    val log by Log4j2("Anime")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

    event.deferReply().await()

    val query = event.getOption("query")!!.asString.apply {
        log.debug("Looking up anime media: $this")
    }
    val allowHentai = if (event.isFromGuild) {
        db.guilds.first { it.discordGuildId eq event.guildContext.guild.idLong }.hentai
    } else false
    val media = aniList.findMediaByType(query, MediaType.ANIME)
        ?.filterHentai(allowHentai)

    if (media == null) {
        event.sendLocalized(LocaleMessage.Find.NoMatchingMedia)
        return
    }

    val mediaList = lookupMediaList(media, event.guild?.idLong)
    val aniToDiscordName = aniListToDiscordNameMap(event.guild)

    event.sendMediaResults(media, mediaList, aniToDiscordName)
}
