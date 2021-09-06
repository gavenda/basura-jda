package basura.command

import basura.Command
import basura.basuraExceptionHandler
import basura.discord.await
import basura.discord.onCommand
import net.dv8tion.jda.api.JDA

fun JDA.addPingCommand() {
    onCommand(Command.PING, basuraExceptionHandler) { event ->
        event.replyFormat("%d ms", event.jda.gatewayPing)
            .setEphemeral(true)
            .await()
    }
}