package basura.command

import basura.*
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateCharacterEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.apache.logging.log4j.LogManager
import org.kodein.di.instance

fun JDA.handleCharacter(): JDA {
    val log = LogManager.getLogger("Character")
    val aniList by basura.instance<AniList>()

    onCommand(Command.CHARACTER, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up character: $this")
        }
        val characters = aniList.findCharacter(query)

        if (characters == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingCharacter)
            return@onCommand
        }

        val embeds = characters.mapIndexed { i, c ->
            generateCharacterEmbed(c, (i + 1), characters.size)
        }.toTypedArray()

        if(embeds.isEmpty()) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingCharacter)
            return@onCommand
        }

        event.hook.sendPaginator(*embeds).await()
    }

    return this
}