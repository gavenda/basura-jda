package basura.command

import basura.LocaleMessage
import basura.Log4j2
import basura.bot
import basura.db.users
import basura.sendLocalized
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.removeIf

suspend fun onUnlink(event: SlashCommandEvent) {
    val log by Log4j2("Link")
    val db by bot.instance<Database>()

    event.deferReply().await()

    // Ensure in guild
    if (event.isDirectMessage) {
        event.sendLocalized(LocaleMessage.Unlink.ServerOnlyError, true)
        return
    }

    val guild = event.guildContext.guild
    val existingUser = db.users.firstOrNull {
        (it.discordId eq event.user.idLong) and (it.discordGuildId eq guild.idLong)
    }

    if (existingUser == null) {
        event.sendLocalized(LocaleMessage.Unlink.NotLinked)
        return
    }

    log.debug("Unlinking AniList user [ ${existingUser.aniListUsername} ] from Discord [ user = ${event.user.name}, guild = ${guild.name} ]")

    db.users.removeIf {
        (it.discordId eq event.user.idLong) and (it.discordGuildId eq guild.idLong)
    }

    event.sendLocalized(LocaleMessage.Unlink.Successful)
}