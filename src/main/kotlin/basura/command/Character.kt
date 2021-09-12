package basura.command

import basura.*
import basura.discord.await
import basura.discord.interaction.deferReplyAwait
import basura.discord.interaction.requiredOption
import basura.discord.interaction.sendPaginator
import basura.embed.generateCharacterEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance

suspend fun onCharacter(event: SlashCommandEvent) {
    val log by Log4j2("Character")
    val aniList by bot.instance<AniList>()

    event.deferReplyAwait()

    val query = event.requiredOption("query").asString.apply {
        log.debug("Looking up character: $this")
    }
    val characters = aniList.findCharacter(query)

    if (characters == null) {
        event.sendLocalized(LocaleMessage.Find.NoMatchingCharacter)
        return
    }

    val embeds = characters.mapIndexed { i, c ->
        generateCharacterEmbed(c, (i + 1), characters.size)
    }.toTypedArray()

    if (embeds.isEmpty()) {
        event.sendLocalized(LocaleMessage.Find.NoMatchingCharacter)
        return
    }

    event.hook.sendPaginator(*embeds).await()
}