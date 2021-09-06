package basura.command

import basura.*
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateStaffEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.apache.logging.log4j.LogManager
import org.kodein.di.instance

fun JDA.addStaffCommand() {
    val log = LogManager.getLogger("Staff")
    val aniList by basura.instance<AniList>()

    onCommand(Command.STAFF, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up staff: $this")
        }
        val characters = aniList.findStaff(query)

        if (characters == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingCharacter)
            return@onCommand
        }

        val embeds = characters.mapIndexed { i, c ->
            generateStaffEmbed(c, (i + 1), characters.size)
        }.toTypedArray()

        if(embeds.isEmpty()) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingCharacter)
            return@onCommand
        }

        event.hook.sendPaginator(*embeds).await()
    }
}