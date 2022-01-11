package basura.command

import basura.*
import basura.embed.pagedStaffEmbed
import basura.graphql.AniList
import basura.paginator.sendAniPaginator
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance

suspend fun onStaff(event: SlashCommandEvent) {
    val log by Log4j2("Staff")
    val aniList by bot.instance<AniList>()

    event.deferReply().await()

    val query = event.getOption("query")!!.asString.apply {
        log.debug("Looking up staff: $this")
    }
    val characters = aniList.findStaff(query)

    if (characters == null) {
        event.sendLocalized(LocaleMessage.Find.NoMatchingStaff)
        return
    }

    val paginated = characters.mapIndexed { i, c ->
        pagedStaffEmbed(c, (i + 1), characters.size)
    }.toTypedArray()

    if (paginated.isEmpty()) {
        event.sendLocalized(LocaleMessage.Find.NoMatchingStaff)
        return
    }

    event.hook.sendAniPaginator(*paginated).await()
}