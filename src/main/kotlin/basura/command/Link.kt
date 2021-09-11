package basura.command

import basura.*
import basura.db.User
import basura.db.users
import basura.graphql.AniList
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

    event.awaitDeferReply()

    val guild = event.guild
    // Assure not direct message
    if (guild == null) {
        event.sendLocalizedMessage(LocaleMessage.Link.ServerOnlyError, true)
        return
    }

    val guildId = guild.idLong
    val username = event.requiredOption("username").asString.apply {
        log.debug("Linking AniList user [ $this ] to Discord [ user = ${event.user.name}, guild = ${guild.name} ]")
    }
    val existingUser = db.users.firstOrNull {
        (it.discordId eq event.user.idLong) and (it.discordGuildId eq guildId)
    }

    if (existingUser != null) {
        event.sendLocalizedMessage(LocaleMessage.Link.AlreadyLinked)
        return
    }

    val user = aniList.findUserByName(username)

    if (user == null) {
        event.sendLocalizedMessage(LocaleMessage.User.NotFoundError)
        return
    }

    db.users.add(
        User {
            aniListId = user.id
            aniListUsername = user.name
            discordId = event.user.idLong
            discordGuildId = guildId
        }
    )

    event.sendLocalizedMessage(LocaleMessage.Link.Successful)
}
