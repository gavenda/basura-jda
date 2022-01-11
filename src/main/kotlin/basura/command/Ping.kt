package basura.command

import basura.Log4j2
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

suspend fun onPing(event: SlashCommandEvent) {
    val log by Log4j2("Ping")
    val ping = event.jda.gatewayPing

    log.debug("Ping request received, ping: $ping")

    event.replyFormat("%d ms", ping)
        .setEphemeral(true)
        .await()
}
