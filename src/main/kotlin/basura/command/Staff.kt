package basura.command

import basura.*
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateStaffEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.kodein.di.instance

fun JDA.handleStaff(): JDA {
    val log by Log4j2("Staff")
    val aniList by basura.instance<AniList>()

    onCommand(Command.STAFF, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val query = event.requiredOption("query").asString.apply {
            log.debug("Looking up staff: $this")
        }
        val characters = aniList.findStaff(query)

        if (characters == null) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingStaff)
            return@onCommand
        }

        val embeds = characters.mapIndexed { i, c ->
            generateStaffEmbed(c, (i + 1), characters.size)
        }.toTypedArray()

        if (embeds.isEmpty()) {
            event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingStaff)
            return@onCommand
        }

        event.hook.sendPaginator(*embeds).await()
    }

    return this
}