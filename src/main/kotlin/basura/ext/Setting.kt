package basura.ext

import basura.check.anyGuild
import basura.db.guilds
import basura.toYesNo
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingStringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first

class Setting : Extension() {
    override val name: String = "setting"
    override val bundle: String = "setting"
    private val db by inject<Database>()

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "setting"
            description = "Setting for the current guild. (does not work for direct messages)"

            ephemeralSubCommand {
                name = "list"
                description = "The current overall settings for this guild."
                check {
                    anyGuild()
                    hasPermission(Permission.Administrator)
                }
                action {
                    val guildIdLong = guild!!.id.value.toLong()
                    val dbGuild = db.guilds.first { it.discordGuildId eq guildIdLong }
                    val locale = java.util.Locale.forLanguageTag(dbGuild.locale)

                    respond {
                        embed {
                            title = translate("setting.embed.title")
                            description = translate("setting.embed.description")

                            thumbnail {
                                url = guild!!.fetchGuild().getIconUrl(Image.Format.PNG).toString()
                            }

                            field {
                                name = translate("setting.embed.field.language")
                                value = locale.displayName
                                inline = false
                            }

                            field {
                                name = translate("setting.embed.field.hentai")
                                value = dbGuild.hentai.toYesNo()
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand(::LanguageArgs) {
                name = "language"
                description = "Allow hentai content for this guild."
                check {
                    anyGuild()
                    hasPermission(Permission.Administrator)
                }
                action {
                    val guildIdLong = guild!!.id.value.toLong()
                    val dbGuild = db.guilds.first { it.discordGuildId eq guildIdLong }

                    dbGuild.locale = arguments.language
                    dbGuild.flushChanges()

                    respond {
                        content = translate("setting.settingUpdated")
                    }
                }
            }

            ephemeralSubCommand(::HentaiArgs) {
                name = "hentai"
                description = "Configure the language for this guild."
                check {
                    anyGuild()
                    hasPermission(Permission.Administrator)
                }
                action {
                    val guildIdLong = guild!!.id.value.toLong()
                    val dbGuild = db.guilds.first { it.discordGuildId eq guildIdLong }

                    dbGuild.hentai = arguments.display
                    dbGuild.flushChanges()

                    respond {
                        content = translate("setting.settingUpdated")
                    }
                }
            }
        }
    }

    inner class LanguageArgs : Arguments() {
        val language by defaultingStringChoice {
            name = "language"
            description = "The language to use."
            defaultValue = "en-US"
            choice("English", "en-US")
        }
    }

    inner class HentaiArgs : Arguments() {
        val display by boolean {
            name = "display"
            description = "Display hentai or not."
        }
    }
}