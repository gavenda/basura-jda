package basura.ext

import basura.AppDispatchers
import basura.action
import basura.db.DbGuild
import basura.db.guilds
import basura.db.users
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.removeIf

class Guild : Extension() {
    override val name: String = "guild"

    private val db by inject<Database>()

    override suspend fun setup() {
        event<GuildDeleteEvent> {
            action(AppDispatchers.IO) {
                if (!event.unavailable) {
                    db.users.removeIf { it.discordGuildId eq event.guildId.value.toLong() }
                    db.guilds.removeIf { it.discordGuildId eq event.guildId.value.toLong() }
                }
            }
        }

        event<GuildCreateEvent> {
            action(AppDispatchers.IO) {
                val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq event.guild.id.value.toLong() }

                if (dbGuild == null) {
                    db.guilds.add(DbGuild {
                        discordGuildId = event.guild.id.value.toLong()
                        hentai = false
                        locale = "en-US"
                    })
                }
            }
        }

    }
}