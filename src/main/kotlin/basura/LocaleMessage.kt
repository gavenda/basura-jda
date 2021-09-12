package basura

import basura.db.guilds
import basura.db.userLocales
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first
import org.ktorm.entity.firstOrNull
import java.util.*

/**
 * Message key constants.
 */
object LocaleMessage {
    object User {
        const val UsernameRequiredError = "user-username-required-error"
        const val NotLinkedError = "user-account-not-linked"
        const val NotFoundError = "user-not-found-error"
        const val LinkedNotFoundError = "user-linked-not-found"
    }

    object Link {
        const val ServerOnlyError = "link-server-only-error"
        const val AlreadyLinked = "link-already-linked"
        const val Successful = "link-successful"
    }

    object Unlink {
        const val ServerOnlyError = "unlink-server-only-error"
        const val NotLinked = "unlink-not-linked"
        const val Successful = "unlink-successful"
    }

    object Find {
        const val NoMatchingMedia = "find-no-matching-media"
        const val NoMatchingCharacter = "find-no-matching-character"
        const val NoMatchingStaff = "find-no-matching-staff"
    }

    object About {
        const val Title = "about-title"
        const val Description = "about-description"
        const val Version = "about-version"
        const val Language = "about-language"
        const val Framework = "about-framework"
        const val Shard = "about-shard"
        const val OperatingSystem = "about-operating-system"
        const val Note = "about-note"
    }

    const val DirectMessageCleared = "direct-message-cleared"
    const val DirectMessageOnly = "direct-message-only"
    const val ServerOnlyError = "server-only-error"
    const val SettingUpdated = "setting-updated"
    const val ServerAdminOnly = "server-admin-only"
    const val LanguageChanged = "locale-changed"
}

/**
 * Simple messaging helper for players with different locales.
 */
object Messages {

    /**
     * With the given context. Will use whatever is available with priority to user setting.
     * @param user user to use as context
     * @param guild guild to use as context
     */
    fun withContext(user: User? = null, guild: Guild? = null): MessageContext {
        val db by bot.instance<Database>()
        val locale = if (user != null) {
            val localeStr = db.userLocales.firstOrNull { it.discordId eq user.idLong }?.locale
            if(localeStr != null) {
                Locale.forLanguageTag(localeStr)
            } else {
                Locale.getDefault()
            }
        } else if (guild != null) {
            val localeStr = db.guilds.first { it.discordGuildId eq guild.idLong }.locale
            Locale.forLanguageTag(localeStr)
        } else {
            Locale.getDefault()
        }
        return MessageContext(locale)
    }
}

class MessageContext(locale: Locale) {
    private val messages = ResourceBundle.getBundle("i18n.messages", locale)

    /**
     * Get the message given the key.
     * @param key the key
     */
    fun get(key: String): String = messages.getString(key)

    /**
     * Get the message given the key.
     * @param key the key
     */
    fun get(key: String, vararg args: Any?): String = String.format(get(key), args)
}
