package basura

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import java.util.*

/**
 * Message key constants.
 */
object LocaleMessage {
    object User {
        const val UsernameRequiredError = "user-username-required-error"
        const val NotLinkedError = "user-account-not-linked"
        const val NotFoundError = "user-not-found-error"
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
    }

    const val DirectMessageCleared = "direct-message-cleared"
    const val DirectMessageOnly = "direct-message-only"
    const val UnknownError = "unknown-error"
}

/**
 * Simple messages helper for players with different locales.
 */
object Messages {

    /**
     * Use user locale.
     */
    fun whenApplicableFor(user: User? = null, guild: Guild? = null): MessageContext {
        if(user != null) return MessageUserContext(user)
        if(guild != null) return MessageGuildContext(guild)
        return MessageDefaultContext()
    }
}

interface MessageContext {
    /**
     * Get the message given the key.
     * @param key the key
     */
    fun get(key: String): String

    /**
     * Get a list of messages given the key.
     * @param key the key
     */
    fun getList(key: String): List<String>
}

class MessageDefaultContext : MessageContext {
    private val messages = ResourceBundle.getBundle("i18n.messages", Locale.getDefault())
    override fun get(key: String): String = messages.getString(key)
    override fun getList(key: String) = messages.getStringArray(key).toList()
}

/**
 * Messages in a guild context.
 * @param guild the guild to get messages from
 */
class MessageGuildContext(guild: Guild) : MessageContext {
    private val messages = ResourceBundle.getBundle("i18n.messages", Locale.getDefault())
    override fun get(key: String): String = messages.getString(key)
    override fun getList(key: String) = messages.getStringArray(key).toList()
}

/**
 * Messages in a user context.
 * @param user the user to get messages from
 */
class MessageUserContext(user: User) : MessageContext {
    private val locale = Locale.getDefault()
    private val messages = ResourceBundle.getBundle("i18n.messages", locale)
    override fun get(key: String): String = messages.getString(key)
    override fun getList(key: String) = messages.getStringArray(key).toList()
}