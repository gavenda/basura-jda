package basura.ext

import basura.check.anyGuild
import basura.db.DbUser
import basura.db.users
import basura.graphql.AniList
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.removeIf

class Link : Extension() {
    override val name: String = "link"
    override val bundle: String = "link"

    private val db by inject<Database>()
    private val aniList by inject<AniList>()
    private val log = KotlinLogging.logger { }

    override suspend fun setup() {
        ephemeralSlashCommand(::UserArgs) {
            name = "link"
            description = "Link your Discord account to your AniList account."
            check {
                anyGuild(translate("link.error.serverOnly"))
            }
            action {
                val userIdLong = user.id.value.toLong()
                val guildIdLong = guild!!.id.value.toLong()

                val existingUser = db.users.firstOrNull {
                    (it.discordId eq userIdLong) and (it.discordGuildId eq guildIdLong)
                }

                if (existingUser != null) {
                    respond {
                        content = translate("link.error.alreadyLinked")
                    }
                    return@action
                }

                val user = aniList.findUserByName(arguments.username)

                if (user == null) {
                    respond {
                        content = translate("link.successful")
                    }
                    return@action
                }

                db.users.add(
                    DbUser {
                        aniListId = user.id
                        aniListUsername = user.name
                        discordId = userIdLong
                        discordGuildId = guildIdLong
                    }
                )

                respond {
                    content = translate("link.successful")
                }
            }
        }
        ephemeralSlashCommand {
            name = "unlink"
            description = "Unlink your AniList account from your Discord account."
            check {
                anyGuild(translate("unlink.error.serverOnly"))
            }
            action {
                val userIdLong = user.id.value.toLong()
                val guildIdLong = guild!!.id.value.toLong()

                val existingUser = db.users.firstOrNull {
                    (it.discordId eq userIdLong) and (it.discordGuildId eq guildIdLong)
                }

                if (existingUser == null) {
                    respond {
                        content = translate("unlink.error.notLinked")
                    }
                    return@action
                }

                log.debug { "Unlinking AniList user [ ${existingUser.aniListUsername} ] from Discord [ user = $userIdLong, guild = $guildIdLong ]" }

                db.users.removeIf {
                    (it.discordId eq userIdLong) and (it.discordGuildId eq guildIdLong)
                }

                respond {
                    content = translate("unlink.successful")
                }
            }
        }
    }

    inner class UserArgs : Arguments() {
        val username by string {
            name = "username"
            description = "AniList username, defaults to your own if linked."
        }
    }

}