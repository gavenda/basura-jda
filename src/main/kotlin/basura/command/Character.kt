package basura.command

import basura.*
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateCharacterEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.kodein.di.instance

fun JDA.addCharacterCommand() {
    val aniList by basura.instance<AniList>()

    onCommand(Command.CHARACTER, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString
        val characters = aniList.findCharacter(query)

        if (characters == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingCharacter)
            return@onCommand
        }

        log.debug("Creating media embed...")

        val embeds = characters.mapIndexed { i, c ->
            generateCharacterEmbed(c, (i + 1), characters.size)
        }.toTypedArray()

        event.hook.sendPaginator(*embeds).await()
    }
}