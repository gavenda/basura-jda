package basura.command

import basura.*
import basura.db.User
import basura.db.users
import basura.discord.onCommand
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.apache.logging.log4j.LogManager
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.removeIf

fun JDA.addLinkCommands() {
    val log = LogManager.getLogger("Link")
    val db by basura.instance<Database>()
    val aniList by basura.instance<AniList>()

    onCommand(Command.LINK, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val guild = event.guild
        // Assure not direct message
        if (guild == null) {
            event.sendLocalizedMessage(LocaleMessage.Link.ServerOnlyError)
            return@onCommand
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
            return@onCommand
        }

        val user = aniList.findUserByName(username)

        if (user == null) {
            event.sendLocalizedMessage(LocaleMessage.User.NotFoundError)
            return@onCommand
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

    onCommand(Command.UNLINK, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val guild = event.guild
        // Assure not direct message
        if (guild == null) {
            event.sendLocalizedMessage(LocaleMessage.Unlink.ServerOnlyError)
            return@onCommand
        }

        val guildId = guild.idLong
        val existingUser = db.users.firstOrNull {
            (it.discordId eq event.user.idLong) and (it.discordGuildId eq guildId)
        }

        if (existingUser == null) {
            event.sendLocalizedMessage(LocaleMessage.Unlink.NotLinked)
            return@onCommand
        }

        log.debug("Unlinking AniList user [ ${existingUser.aniListUsername} ] from Discord [ user = ${event.user.name}, guild = ${guild.name} ]")

        db.users.removeIf {
            (it.discordId eq event.user.idLong) and (it.discordGuildId eq guildId)
        }

        event.sendLocalizedMessage(LocaleMessage.Unlink.Successful)
    }
}