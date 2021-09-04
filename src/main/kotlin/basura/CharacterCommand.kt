package basura

import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateCharacterEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import org.kodein.di.instance

fun JDA.addCharacterCommand() {
    val aniList by kodein.instance<AniList>()

    onCommand(Command.CHARACTER) { event ->
        event.awaitDeferReply()

        try {
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
        } catch (e: ErrorResponseException) {
            if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                log.debug("Received unknown message error, but is being ignored.")
            } else {
                log.error("Error occurred during character lookup", e)
            }
        } catch (e: Exception) {
            log.error("Error occurred during character lookup", e)
            event.sendUnknownError()
        }
    }
}