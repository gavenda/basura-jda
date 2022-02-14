package basura.paginator

import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.modify.embed
import java.util.*

/**
 * Class representing a button-based paginator that operates by editing the given ephemeral interaction response.
 *
 * @param interaction Interaction response behaviour to work with.
 */
class EphemeralResponseStandardPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
    val interaction: EphemeralInteractionResponseBehavior,
) : StandardPaginator(pages, owner, timeoutSeconds, true, switchEmoji, bundle, locale) {
    /** Whether this paginator has been set up for the first time. **/
    var isSetup: Boolean = false

    override suspend fun send() {
        if (!isSetup) {
            isSetup = true

            setup()
        } else {
            updateButtons()
        }

        interaction.edit {
            embed { applyPage() }

            with(this@EphemeralResponseStandardPaginator.components) {
                this@edit.applyToMessage()
            }
        }
    }

    override suspend fun destroy() {
        if (!active) {
            return
        }

        active = false

        interaction.edit {
            embed { applyPage() }

            this.components = mutableListOf()
        }

        super.destroy()
    }
}

/** Convenience function for creating an interaction button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
fun EphemeralResponseStandardPaginator(
    builder: MutablePaginatorBuilder,
    interaction: EphemeralInteractionResponseBehavior
): EphemeralResponseStandardPaginator = EphemeralResponseStandardPaginator(
    pages = builder.pages,
    owner = builder.owner,
    timeoutSeconds = builder.timeoutSeconds,
    bundle = builder.bundle,
    locale = builder.locale,
    interaction = interaction,

    switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
)

/**
 * Create a paginator that edits the original interaction. This is the only option for an ephemeral interaction, as
 * it's impossible to edit an ephemeral follow-up.
 */
suspend inline fun EphemeralInteractionContext.editingStandardPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (MutablePaginatorBuilder).() -> Unit
): EphemeralResponseStandardPaginator {
    val pages = MutablePaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return EphemeralResponseStandardPaginator(pages, interactionResponse)
}