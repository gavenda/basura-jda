package basura.command

import basura.LocaleMessage
import basura.Log4j2
import basura.bot
import basura.db.UserLocale
import basura.db.userLocales
import basura.discord.interaction.deferReplyAwait
import basura.discord.interaction.requiredOption
import basura.sendLocalized
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull

suspend fun onLanguage(event: SlashCommandEvent) {
    val log by Log4j2("Language")
    val db by bot.instance<Database>()

    event.deferReplyAwait(true)

    val languageLocale = event.requiredOption("language").asString

    // Find existing
    val userLocale = db.userLocales.firstOrNull { it.discordId eq event.user.idLong }

    if (userLocale != null) {
        userLocale.locale = languageLocale
        userLocale.flushChanges()
    } else {
        db.userLocales.add(UserLocale {
            discordId = event.user.idLong
            locale = languageLocale
        })
    }

    log.debug("User [ ${event.user.name} ] locale set to [ $languageLocale ]")

    event.sendLocalized(LocaleMessage.LanguageChanged)
}