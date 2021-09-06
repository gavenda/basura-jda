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

fun JDA.addAboutCommand() {
    onCommand(Command.ABOUT) { event ->
        event.replyEmbeds(
            Embed {
                title = "What is Basura?"
                url = "https://gavenda.work"
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
                footer {
                    name = "You can help with the development by dropping by on GitHub."
                    iconUrl = "https://github.com/fluidicon.png"
                }
            }
        )
            .setEphemeral(true)
            .await()
    }
}

val VERSION: String
    get() =
        Properties().apply {
            load(object {}.javaClass.getResourceAsStream("/version.properties"))
        }.getProperty("version")