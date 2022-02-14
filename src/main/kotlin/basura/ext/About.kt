package basura.ext

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import java.util.*

class About : Extension() {
    override val name: String = "about"
    override val bundle: String = "about"
    override suspend fun setup() {

        ephemeralSlashCommand {
            name = "about"
            description = "Shows more information about this bot."
            action {
                val java = System.getProperty("java.vendor")
                val javaVersion = System.getProperty("java.version")
                val sys = System.getProperty("os.name")
                val sysArch = System.getProperty("os.arch")
                val sysVersion = System.getProperty("os.version")
                val version = Properties().apply {
                    load(object {}.javaClass.getResourceAsStream("/version.properties"))
                }.getProperty("version") ?: "-"

                respond {
                    embed {
                        title = translate("about.title")
                        url = "https://github.com/gavenda/basura"
                        description = translate("about.description")
                        field {
                            name = translate("about.version")
                            value = version
                            inline = true
                        }
                        field {
                            name = translate("about.language")
                            value = "[Kotlin](https://kotlinlang.org)"
                            inline = true
                        }
                        field {
                            name = translate("about.framework")
                            value = "[Kord](https://github.com/kordlib/kord)"
                            inline = true
                        }
                        field {
                            name = translate("about.operatingSystem")
                            value = "$java Java $javaVersion on $sys $sysVersion ($sysArch)"
                            inline = true
                        }
                        footer {
                            text = translate("about.note")
                            icon = "https://github.com/fluidicon.png"
                        }
                    }
                }
            }
        }
    }
}