package basura.paginator

import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.buttons.LinkInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButton
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.pagination.BasePaginator
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.EmbedBuilder
import java.util.*

/**
 * Abstract class containing some common functionality needed by interactive button-based paginators.
 */
abstract class StandardPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
) : BasePaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    /** [ComponentContainer] instance managing the buttons for this paginator. **/
    var components: ComponentContainer = ComponentContainer()

    /** Scheduler used to schedule the paginator's timeout. **/
    var scheduler: Scheduler = Scheduler()

    /** Scheduler used to schedule the paginator's timeout. **/
    var task: Task? = if (timeoutSeconds != null) {
        scheduler.schedule(timeoutSeconds) { destroy() }
    } else {
        null
    }

    private val emojiNext = ReactionEmoji.Custom(Snowflake(905638284468830229), "next", false)
    private val emojiPrev = ReactionEmoji.Custom(Snowflake(905638284074557461), "previous", false)

    /** Button builder representing the button that switches to the previous page. **/
    open var backButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches to the next page. **/
    open var nextButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches to the next page. **/
    open var viewOnAniList: LinkInteractionButton? = null

    override suspend fun destroy() {
        runTimeoutCallbacks()
        task?.cancel()
    }

    override suspend fun setup() {
        if (pages.groups.values.any { it.size > 1 }) {
            addNavigationButtons()
        }
        updateButtons()

        components.sort()
    }

    /**
     * Convenience function to switch to a specific group.
     */
    suspend fun switchGroup(group: String) {
        if (group == currentGroup) {
            return
        }

        // To avoid out-of-bounds
        currentPageNum = minOf(currentPageNum, pages.groups[group]!!.size)
        currentPage = pages.get(group, currentPageNum)
        currentGroup = group

        send()
    }

    override suspend fun nextGroup() {
        val current = currentGroup
        val nextIndex = allGroups.indexOf(current) + 1

        if (nextIndex >= allGroups.size) {
            switchGroup(allGroups.first())
        } else {
            switchGroup(allGroups[nextIndex])
        }
    }

    override suspend fun goToPage(page: Int) {
        if (page == currentPageNum) {
            return
        }

        if (page < 0 || page > pages.groups[currentGroup]!!.size - 1) {
            return
        }

        currentPageNum = page
        currentPage = pages.get(currentGroup, currentPageNum)

        send()
    }

    private suspend fun addNavigationButtons() {
        // Add navigation buttons...
        backButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Primary
            emoji(emojiPrev)

            action {
                if (currentPageNum == 0) {
                    goToPage(pages.groups[currentGroup]!!.size - 1)
                } else {
                    previousPage()
                }

                send()
                task?.restart()
            }
        }

        nextButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Primary
            emoji(emojiNext)

            action {
                if (currentPageNum >= pages.groups[currentGroup]!!.size - 1) {
                    goToPage(0)
                } else {
                    nextPage()
                }

                send()
                task?.restart()
            }
        }
    }

    /**
     * Convenience function that enables and disables buttons as necessary, depending on the current page number.
     */
    suspend fun updateButtons() {
        val embedBuilder = EmbedBuilder()
        currentPage.builder(embedBuilder)

        val embedUrl = embedBuilder.url

        if (embedUrl != null) {
            viewOnAniList?.let { components.remove(it) }
            viewOnAniList = components.linkButton {
                label = "View on AniList"
                url = embedUrl
            }
            components.sort()
        }

        if (pages.groups.values.any { it.size > 1 }) {
            if (backButton == null && nextButton == null) {
                addNavigationButtons()
                components.sort()
            }
        } else {
            backButton?.let { components.remove(it) }
            nextButton?.let { components.remove(it) }
            backButton = null
            nextButton = null
        }
    }
}