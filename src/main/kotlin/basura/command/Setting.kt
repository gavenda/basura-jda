package basura.command

import basura.*
import basura.db.guilds
import basura.discord.await
import basura.discord.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first
import java.util.*

fun JDA.handleSetting(): JDA {
    onCommand(Command.SETTING) { event ->
        event.awaitDeferReply(true)

        val guild = event.guild

        // Assure not direct message
        if (guild == null) {
            event.sendLocalizedMessage(LocaleMessage.ServerOnlyError)
            return@onCommand
        }

        // Should be administrator or owner
        val isAdmin = event.context.invoker.hasPermission(
            Permission.ADMINISTRATOR,
        )
        val isOwner = event.context.invoker.isOwner
        val isAllowed = isOwner || isAdmin

        if(isAllowed.not()) {
            event.sendLocalizedMessage(LocaleMessage.ServerAdminOnly)
            return@onCommand
        }

        when (event.subcommandName) {
            Command.Setting.CURRENT -> onListSettings(event, guild)
            Command.Setting.HENTAI -> onHentai(event, guild)
            Command.Setting.LANGUAGE -> onLanguage(event, guild)
        }
    }
    return this
}

internal suspend fun onListSettings(event: SlashCommandEvent, guild: Guild) {
    val db by basura.instance<Database>()
    val dbGuild = db.guilds.first { it.discordGuildId eq guild.idLong }
    val locale = Locale.forLanguageTag(dbGuild.locale)
    val embed = Embed {
        title = "Settings"
        description = "The current settings for this guild."
        field {
            name = "Language"
            value = locale.displayName
            inline = false
        }

        field {
            name = "Hentai"
            value = dbGuild.hentai.toYesNo()
        }
    }

    event.hook.sendMessageEmbeds(embed)
        .await()
}

internal suspend fun onHentai(event: SlashCommandEvent, guild: Guild) {
    val db by basura.instance<Database>()
    val display = event.requiredOption("display").asBoolean
    val dbGuild = db.guilds.first { it.discordGuildId eq guild.idLong }

    dbGuild.hentai = display
    dbGuild.flushChanges()

    event.sendLocalizedMessage(LocaleMessage.SettingUpdated)
}

internal suspend fun onLanguage(event: SlashCommandEvent, guild: Guild) {
    val db by basura.instance<Database>()
    val language = event.requiredOption("language").asString
    val dbGuild = db.guilds.first { it.discordGuildId eq guild.idLong }

    dbGuild.locale = language
    dbGuild.flushChanges()

    event.sendLocalizedMessage(LocaleMessage.SettingUpdated)
}