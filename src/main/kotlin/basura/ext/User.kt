package basura.ext

import basura.db.users
import basura.embed.createUserEmbed
import basura.graphql.AniList
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicUserCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.focusedOption
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

class User : Extension() {
    override val name: String = "user"
    override val bundle: String = "user"

    private val aniList by inject<AniList>()
    private val db by inject<Database>()

    override suspend fun setup() {
        publicSlashCommand(::UserArgs) {
            name = "user"
            description = "Looks up the statistics of a user's AniList."
            action {
                val username = arguments.username

                if (username != null) {
                    val user = aniList.findUserStatisticsByName(username)
                    if (user == null) {
                        respond {
                            content = translate("user.error.userNotFound")
                        }
                    } else if (user.statistics == null) {
                        respond {
                            content = translate("user.error.noUserStatistics")
                        }
                    } else {
                        respond {
                            embed {
                                apply(createUserEmbed(user))
                            }
                            components {
                                linkButton {
                                    label = "Follow on AniList"
                                    url = user.siteUrl
                                }
                            }
                        }
                    }
                } else {
                    if (guild == null) {
                        respond {
                            content = translate("user.error.usernameRequired")
                        }
                        return@action
                    }

                    val userIdLong = user.id.value.toLong()
                    val guildIdLong = guild!!.id.value.toLong()
                    val dbUsername = db.users.firstOrNull {
                        (it.discordId eq userIdLong) and (it.discordGuildId eq guildIdLong)
                    }?.aniListUsername

                    if (dbUsername == null) {
                        respond {
                            content = translate("user.error.accountNotLinked")
                        }
                        return@action
                    }

                    val user = aniList.findUserStatisticsByName(dbUsername)

                    // Linked, but not found
                    if (user == null) {
                        respond {
                            content = translate("user.error.linkNotFound")
                        }
                    } else if (user.statistics == null) {
                        respond {
                            content = translate("user.error.noUserStatistics")
                        }
                    } else {
                        respond {
                            embed {
                                apply(createUserEmbed(user))
                            }
                            components {
                                linkButton {
                                    label = "Follow on AniList"
                                    url = user.siteUrl
                                }
                            }
                        }
                    }
                }
            }
        }

        publicUserCommand {
            name = "Show AniList"
            check {
                anyGuild()
            }
            action {
                val userIdLong = targetUsers.first().id.value.toLong()
                val guildIdLong = guild!!.id.value.toLong()
                val dbUsername = db.users.firstOrNull {
                    (it.discordId eq userIdLong) and (it.discordGuildId eq guildIdLong)
                }?.aniListUsername

                if (dbUsername == null) {
                    respond {
                        content = translate("user.error.userNotLinked")
                    }
                    return@action
                }

                val user = aniList.findUserStatisticsByName(dbUsername)

                // Linked, but not found
                if (user == null) {
                    respond {
                        content = translate("user.error.linkNotFound")
                    }
                } else if (user.statistics == null) {
                    respond {
                        content = translate("user.error.noUserStatistics")
                    }
                } else {
                    respond {
                        embed {
                            apply(createUserEmbed(user))
                        }
                        components {
                            linkButton {
                                label = "Follow on AniList"
                                url = user.siteUrl
                            }
                        }
                    }
                }
            }
        }
    }

    inner class UserArgs : Arguments() {
        val username by optionalString {
            name = "username"
            description = "AniList username, defaults to your own if linked."

            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value as String
                val usernames = aniList.findUserNames(typed).take(25)

                suggestString {
                    for (username in usernames) {
                        choice(username, username)
                    }
                }
            }
        }
    }

}