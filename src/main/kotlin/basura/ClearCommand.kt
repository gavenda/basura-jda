package basura

import basura.discord.await
import basura.discord.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message

fun JDA.addClearCommand() {
    onCommand(Command.CLEAR) { event ->
        event.deferReply()
            .setEphemeral(true)
            .await()

        val guild = event.guild
        // Assure direct message
        if (guild != null) {
            event.sendLocalizedMessage(LocaleMessage.DirectMessageOnly)
            return@onCommand
        }

        var messages: List<Message>

        do {
            messages = event.privateChannel.history
                .retrievePast(100)
                .await()
            event.privateChannel.deleteMessages(messages)
        } while (messages.isNotEmpty())

        event.sendLocalizedMessage(LocaleMessage.DirectMessageCleared)
    }
}

