package basura.command

import basura.Command
import basura.Embed
import basura.discord.await
import basura.discord.onCommand
import net.dv8tion.jda.api.JDA
import java.util.*

internal const val ABOUT =
    """
Basura literally means trash. 

Trash includes isekai bullshit, power leveling xianxia, wuxia, otome politics, 
and any that heightens your level of retardedness.
"""

fun JDA.handleAbout(): JDA {
    val java = System.getProperty("java.vendor")
    val javaVersion = System.getProperty("java.version")
    val sys = System.getProperty("os.name")
    val sysArch = System.getProperty("os.arch")
    val sysVersion = System.getProperty("os.version")

    onCommand(Command.ABOUT) { event ->
        event.replyEmbeds(
            Embed {
                title = "What is Basura?"
                url = "https://github.com/gavenda/basura"
                description = ABOUT
                field {
                    name = "Version"
                    value = VERSION
                    inline = true
                }
                field {
                    name = "Language"
                    value = "[Kotlin](https://kotlinlang.org)"
                    inline = true
                }
                field {
                    name = "Framework"
                    value = "[JDA](https://github.com/DV8FromTheWorld/JDA)"
                    inline = true
                }
                field {
                    name = "Shard"
                    value = shardInfo.shardString
                    inline = true
                }
                field {
                    name = "Operating System"
                    value = "$java Java $javaVersion on $sys $sysVersion ($sysArch)"
                    inline = true
                }
                footer {
                    name = "You can help with the development by dropping by on GitHub."
                    iconUrl = "https://github.com/fluidicon.png"
                }
            }
        )
            .setEphemeral(true)
            .await()
    }

    return this
}

val VERSION: String
    get() =
        Properties().apply {
            load(object {}.javaClass.getResourceAsStream("/version.properties"))
        }.getProperty("version") ?: "-"