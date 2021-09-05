package basura

import basura.db.users
import basura.discord.await
import basura.discord.onCommand
import basura.embed.generateUserEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.JDA
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

fun JDA.addUserCommand() {
    val db by basura.instance<Database>()
    val aniList by basura.instance<AniList>()

    onCommand(Command.USER, basuraExceptionHandler) { event ->
        event.awaitDeferReply()

        val usernameOpt = event.getOption("username")?.asString
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

        log.debug("Creating media embed...")

        val embed = generateUserEmbed(user)
        event.hook
            .sendMessageEmbeds(embed)
            .await()

    }
}