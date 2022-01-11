package basura

import basura.db.Guild
import basura.db.guilds
import basura.db.users
import dev.minn.jda.ktx.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.removeIf

fun JDA.bindGuildEvents(): JDA {
    val db by bot.instance<Database>()

    listener<GuildLeaveEvent> { event ->
        db.users.removeIf { it.discordGuildId eq event.guild.idLong }
        db.guilds.removeIf { it.discordGuildId eq event.guild.idLong }
    }

    listener<GuildMemberRemoveEvent> { event ->
        db.users.removeIf { it.discordId eq event.user.idLong }
    }

    listener<GuildJoinEvent> { event ->
        db.guilds.add(Guild {
            discordGuildId = event.guild.idLong
            hentai = false
            locale = "en-US"
        })
    }

    return this
}