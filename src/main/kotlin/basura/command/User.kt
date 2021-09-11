package basura.command

import basura.*
import basura.db.users
import basura.discord.await
import basura.discord.interaction.deferReplyAwait
import basura.embed.generateUserEmbed
import basura.graphql.AniList
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

suspend fun onUser(event: SlashCommandEvent) {
    val log by Log4j2("User")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

    event.deferReplyAwait()

    val usernameOpt = event.getOption("username")?.asString?.apply {
        log.debug("Looking up user: $this")
    }
    val guild = event.guild

    // Assure in guild when option for username is empty
    if (event.isDirectMessage && usernameOpt == null) {
        event.sendLocalizedMessage(LocaleMessage.User.UsernameRequiredError)
        return
    }

    val guildId = guild?.idLong!!

    val username = usernameOpt
        ?: db.users.firstOrNull {
            (it.discordId eq event.user.idLong) and (it.discordGuildId eq guildId)
        }?.aniListUsername

    if (username == null) {
        event.sendLocalizedMessage(LocaleMessage.User.NotLinkedError)
        return
    }

    log.debug("Lookup user: $username")

    val user = aniList.findUserStatisticsByName(username)

    if (user == null) {
        event.sendLocalizedMessage(LocaleMessage.User.NotFoundError)
        return
    }

    val embed = generateUserEmbed(user)
    event.hook
        .sendMessageEmbeds(embed)
        .await()
}