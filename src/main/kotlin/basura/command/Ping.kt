package basura.command

import basura.Command
import basura.Log4j2
import basura.basuraExceptionHandler
import basura.discord.await
import basura.discord.onCommand
import net.dv8tion.jda.api.JDA

fun JDA.handlePing(): JDA {
    val log by Log4j2("Ping")

    onCommand(Command.PING, basuraExceptionHandler) { event ->
        val ping = event.jda.gatewayPing

        log.debug("Ping request received, ping: $ping")

        event.replyFormat("%d ms", ping)
            .setEphemeral(true)
            .await()
    }

    return this
}