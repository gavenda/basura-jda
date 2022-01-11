package basura.command

import basura.*
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

suspend fun onClear(event: SlashCommandEvent) {
    val log by Log4j2("Clear")

    event.deferReply(true).await()

    // Ensure direct message
    if (event.isFromGuild) {
        event.sendLocalized(LocaleMessage.DirectMessageOnly)
        return
    }

    log.debug("Clearing direct messages for user: ${event.user.name}")

    var messages: List<Message>

    do {
        messages = event.privateChannel.history
            .retrievePast(100)
            .await()
        event.privateChannel.deleteMessages(messages)
    } while (messages.isNotEmpty())

    event.sendLocalized(LocaleMessage.DirectMessageCleared)
}

