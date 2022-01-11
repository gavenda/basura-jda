package basura.command

import basura.LocaleMessage
import basura.Log4j2
import basura.bot
import basura.db.User
import basura.db.users
import basura.graphql.AniList
import basura.sendLocalized
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull

suspend fun onLink(event: SlashCommandEvent) {
    val log by Log4j2("Link")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

    event.deferReply().await()

    // Ensure in guild
    if (event.isDirectMessage) {
        event.sendLocalized(LocaleMessage.Link.ServerOnlyError, true)
        return
    }

    val guild = event.guildContext.guild
    val username = event.getOption("username")!!.asString.apply {
        log.debug("Linking AniList user [ $this ] to Discord [ user = ${event.user.name}, guild = ${guild.name} ]")
    }
    val existingUser = db.users.firstOrNull {
        (it.discordId eq event.user.idLong) and (it.discordGuildId eq guild.idLong)
    }

    if (existingUser != null) {
        event.sendLocalized(LocaleMessage.Link.AlreadyLinked)
        return
    }

    val user = aniList.findUserByName(username)

    if (user == null) {
        event.sendLocalized(LocaleMessage.User.NotFoundError)
        return
    }

    db.users.add(
        User {
            aniListId = user.id
            aniListUsername = user.name
            discordId = event.user.idLong
            discordGuildId = guild.idLong
        }
    )

    event.sendLocalized(LocaleMessage.Link.Successful)
}
