package basura.command

import basura.*
import basura.db.guilds
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first
import java.util.*

suspend fun onSetting(event: SlashCommandEvent) {
    event.deferReply(true).await()

    // Ensure in guild
    if (event.isDirectMessage) {
        event.sendLocalized(LocaleMessage.ServerOnlyError)
        return
    }

    // Should be administrator or owner
    val isAdmin = event.guildContext.invoker.hasPermission(
        Permission.ADMINISTRATOR,
    )
    val isOwner = event.guildContext.invoker.isOwner
    val isAllowed = isOwner || isAdmin

    if (isAllowed.not()) {
        event.sendLocalized(LocaleMessage.ServerAdminOnly)
        return
    }

    when (event.subcommandName) {
        Command.Setting.CURRENT -> onSettingCurrent(event)
        Command.Setting.HENTAI -> onSettingHentai(event)
        Command.Setting.LANGUAGE -> onSettingLanguage(event)
    }
}

internal suspend fun onSettingCurrent(event: SlashCommandEvent) {
    val db by bot.instance<Database>()
    val dbGuild = db.guilds.first { it.discordGuildId eq event.guildContext.guild.idLong }
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

internal suspend fun onSettingHentai(event: SlashCommandEvent) {
    val db by bot.instance<Database>()
    val display = event.getOption("display")!!.asBoolean
    val dbGuild = db.guilds.first { it.discordGuildId eq event.guildContext.guild.idLong }

    dbGuild.hentai = display
    dbGuild.flushChanges()

    event.sendLocalized(LocaleMessage.SettingUpdated)
}

internal suspend fun onSettingLanguage(event: SlashCommandEvent) {
    val db by bot.instance<Database>()
    val language = event.getOption("language")!!.asString
    val dbGuild = db.guilds.first { it.discordGuildId eq event.guildContext.guild.idLong }

    dbGuild.locale = language
    dbGuild.flushChanges()

    event.sendLocalized(LocaleMessage.SettingUpdated)
}