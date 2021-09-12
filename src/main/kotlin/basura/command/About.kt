package basura.command

import basura.*
import basura.discord.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

suspend fun onAbout(event: SlashCommandEvent) {
    val java = System.getProperty("java.vendor")
    val javaVersion = System.getProperty("java.version")
    val sys = System.getProperty("os.name")
    val sysArch = System.getProperty("os.arch")
    val sysVersion = System.getProperty("os.version")

    event.replyEmbeds(
        Embed {
            title = event.messageContext.get(LocaleMessage.About.Title)
            url = "https://github.com/gavenda/basura"
            description = event.messageContext.get(LocaleMessage.About.Description)
            field {
                name = event.messageContext.get(LocaleMessage.About.Version)
                value = version
                inline = true
            }
            field {
                name = event.messageContext.get(LocaleMessage.About.Language)
                value = "[Kotlin](https://kotlinlang.org)"
                inline = true
            }
            field {
                name = event.messageContext.get(LocaleMessage.About.Framework)
                value = "[JDA](https://github.com/DV8FromTheWorld/JDA)"
                inline = true
            }
            field {
                name = event.messageContext.get(LocaleMessage.About.Shard)
                value = event.jda.shardInfo.shardString
                inline = true
            }
            field {
                name = event.messageContext.get(LocaleMessage.About.OperatingSystem)
                value = "$java Java $javaVersion on $sys $sysVersion ($sysArch)"
                inline = true
            }
            footer {
                name = event.messageContext.get(LocaleMessage.About.Note)
                iconUrl = "https://github.com/fluidicon.png"
            }
        }
    )
        .setEphemeral(true)
        .await()
}
