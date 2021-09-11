package basura

import basura.discord.await
import io.github.furstenheim.CopyDown
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import java.util.*

/**
 * Loads any file in resources and returns them as a string.
 */
fun findResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path)!!.readText()
}

/**
 * Send a localized message.
 */
suspend fun SlashCommandEvent.sendLocalizedMessage(key: String, ephemeral: Boolean = false): Message =
    hook.sendMessage(
        Messages.whenApplicableFor(user, guild)
            .get(key)
    )
        .setEphemeral(ephemeral)
        .await()

/**
 * Delete the messages in bulk.
 * @param messages messages to delete
 */
suspend fun PrivateChannel.deleteMessages(messages: List<Message>) {
    val messageIds = messages.map { it.idLong }
    val sortedMessageIds = TreeSet<Long>(Comparator.reverseOrder()).apply {
        addAll(messageIds)
    }

    sortedMessageIds.forEach {
        deleteMessageById(it).await()
    }
}

fun Boolean.toYesNo(): String {
    if(this) return "Yes"
    return "No"
}

/**
 * Cleans down any html and converts them into a Markdown format.
 */
fun String.htmlClean(): String {
    val converter = CopyDown()
    return converter.convert(this)
}

fun String.weirdHtmlClean(): String {
    return this
        .replace("<i>", "_")
        .replace("</i>", "_")
        .replace("<b>", "**")
        .replace("</b>", "**")
}

fun StringBuilder.appendIfNotMax(text: String, max: Int) {
    val sum = length + text.length
    if (sum < max) {
        append(text)
    }
}

fun String.abbreviate(max: Int): String {
    if (length <= max) return this
    return take(max).substring(0, max - 3) + Typography.ellipsis
}

/**
 * Clean AniList profile description.
 */
fun String.aniClean(): String {
    return this
        .replace("~!", "")
        .replace("!~", "")
        .replace("~~~", "")
}

/**
 * Converts AniList colors to their appropriate hex color.
 */
fun String.toHexColor() = when (this) {
    "blue" -> 0x3DB4F2
    "purple" -> 0xC063FF
    "green" -> 0x4CCA51
    "orange" -> 0xEF881A
    "red" -> 0xE13333
    "pink" -> 0xFC9DD6
    "gray" -> 0x677B94
    else -> 0x000000
}

/**
 * Converts this float to a star rating system.
 *
 * 0 to 29: 1 star
 * 30 to 49: 2 stars
 * 50 to 69: 3 stars
 * 70 to 89: 4 stars
 * 90 and beyond: 5 stars
 */
fun Float.toStars(): String {
    return this.toInt().toStars()
}

/**
 * Converts this integer to a star rating system.
 *
 * 0 to 29: 1 star
 * 30 to 49: 2 stars
 * 50 to 69: 3 stars
 * 70 to 89: 4 stars
 * 90 and beyond: 5 stars
 */
fun Int.toStars(): String {
    if (this >= 90) {
        // 5 star
        return "★".repeat(5)
    } else if (this in 70..89) {
        // 4 star
        return "★".repeat(4)
    } else if (this in 50..69) {
        // 3 star
        return "★".repeat(3)
    } else if (this in 30..49) {
        // 2 star
        return "★".repeat(2)
    } else if (this in 1..29) {
        // 1 star
        return "★".repeat(1)
    }
    return "-"
}