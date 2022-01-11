package basura.command

import basura.LocaleMessage
import basura.Log4j2
import basura.bot
import basura.db.users
import basura.embed.generateUserEmbed
import basura.graphql.AniList
import basura.sendLocalized
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

suspend fun onUser(event: SlashCommandEvent) {
    val log by Log4j2("User")
    val db by bot.instance<Database>()
    val aniList by bot.instance<AniList>()

    event.deferReply().await()

    val usernameOpt = event.getOption("username")?.asString?.apply {
        log.debug("Looking up user: $this")
    }
    // Ensure in guild when option for username is empty
    if (event.isDirectMessage && usernameOpt == null) {
        event.sendLocalized(LocaleMessage.User.UsernameRequiredError)
        return
    }

    if (usernameOpt != null) {
        val user = aniList.findUserStatisticsByName(usernameOpt)

        if (user == null) {
            event.sendLocalized(LocaleMessage.User.NotFoundError)
            return
        }

        val embed = generateUserEmbed(user)
        event.hook
            .sendMessageEmbeds(embed)
            .addActionRows(
                ActionRow.of(
                    Button.link(user.siteUrl, "View on AniList")
                )
            )
            .await()
    } else {
        // We are sure in a guild

        val username = db.users.firstOrNull {
            (it.discordId eq event.user.idLong) and (it.discordGuildId eq event.guildContext.guild.idLong)
        }?.aniListUsername

        if (username == null) {
            event.sendLocalized(LocaleMessage.User.NotLinkedError)
            return
        }

        val user = aniList.findUserStatisticsByName(username)

        // Linked, but not found
        if (user == null) {
            event.sendLocalized(LocaleMessage.User.LinkedNotFoundError)
            return
        }

        val embed = generateUserEmbed(user)
        event.hook
            .sendMessageEmbeds(embed)
            .addActionRows(
                ActionRow.of(
                    Button.link(user.siteUrl, "View on AniList")
                )
            )
            .await()
    }
}