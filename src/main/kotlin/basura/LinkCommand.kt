package basura

import basura.db.User
import basura.db.users
import basura.discord.onCommand
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.removeIf

fun JDA.addLinkCommands() {
    val db by kodein.instance<Database>()
    val aniList by kodein.instance<AniList>()

    onCommand(Command.LINK) { event ->
        event.awaitDeferReply()

        val guild = event.guild
        // Assure not direct message
        if (guild == null) {
            event.sendLocalizedMessage(LocaleMessage.Link.ServerOnlyError)
            return@onCommand
        }

        val guildId = guild.idLong
        val username = event.requiredOption("username").asString

        try {

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
        } catch (e: Exception) {
            log.error("An error occurred during account linking", e)
            event.sendUnknownError()
        }
    }

    onCommand(Command.UNLINK) { event ->
        event.awaitDeferReply()

        val guild = event.guild
        // Assure not direct message
        if (guild == null) {
            event.sendLocalizedMessage(LocaleMessage.Unlink.ServerOnlyError)
            return@onCommand
        }

        val guildId = guild.idLong

        try {
            val existingUser = db.users.firstOrNull {
                (it.discordId eq event.user.idLong) and (it.discordGuildId eq guildId)
            }

            if (existingUser == null) {
                event.sendLocalizedMessage(LocaleMessage.Unlink.NotLinked)
                return@onCommand
            }

            db.users.removeIf {
                (it.discordId eq event.user.idLong) and (it.discordGuildId eq guildId)
            }

            event.sendLocalizedMessage(LocaleMessage.Unlink.Successful)
        } catch (e: Exception) {
            log.error("An error occurred during account un-linking", e)
            event.sendUnknownError()
        }
    }
}