package basura.command

import basura.*
import basura.db.users
import basura.discord.await
import basura.discord.onCommand
import basura.embed.generateUserEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.apache.logging.log4j.LogManager
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

fun JDA.handleUser(): JDA {
    val log = LogManager.getLogger("User")
    val db by basura.instance<Database>()
    val aniList by basura.instance<AniList>()

    onCommand(Command.USER, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val usernameOpt = event.getOption("username")?.asString?.apply {
            log.debug("Looking up user: $this")
        }
        val guild = event.guild
        // Assure not direct message
        if (guild == null && usernameOpt == null) {
            event.sendLocalizedMessage(LocaleMessage.User.UsernameRequiredError)
            return@onCommand
        }

        val guildId = guild?.idLong!!

        val username = usernameOpt
            ?: db.users.firstOrNull {
                (it.discordId eq event.user.idLong) and (it.discordGuildId eq guildId)
            }?.aniListUsername

        if (username == null) {
            event.sendLocalizedMessage(LocaleMessage.User.NotLinkedError)
            return@onCommand
        }

        log.debug("Lookup user: $username")

        val user = aniList.findUserStatisticsByName(username)

        if (user == null) {
            event.sendLocalizedMessage(LocaleMessage.User.NotFoundError)
            return@onCommand
        }

        val embed = generateUserEmbed(user)
        event.hook
            .sendMessageEmbeds(embed)
            .await()

    }

    return this
}