/*
 * Copyright 2020 Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package basura.paginator

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonInteraction
import net.dv8tion.jda.api.requests.ErrorResponse
import java.security.SecureRandom
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

const val DEFAULT_DURATION = 300L
val DEFAULT_PREV = Button.primary("prev", "Previous")
val DEFAULT_NEXT = Button.primary("next", "Next")

data class AniPage(
    val message: Message,
    val urlName: String = "",
    val urlHref: String = ""
)

class AniPaginator internal constructor(
    private val nonce: String,
    val duration: Duration
): EventListener {
    private var expiresAt: Long = System.currentTimeMillis() + duration.inWholeMilliseconds
    private var index = 0
    private val pageCache = mutableListOf<AniPage>()
    private val nextPage: AniPage
        get() {
            val nextIndex = ++index
            if (nextIndex > pageCache.lastIndex) {
                index = 0
                return pageCache.first()
            }
            return pageCache[nextIndex]
        }

    private val prevPage: AniPage
        get() {
            val nextIndex = --index
            if (nextIndex < 0) {
                index = pageCache.lastIndex
                return pageCache.last()
            }
            return pageCache[nextIndex]
        }

    private val currPage get() = pageCache[index]

    var filter: (ButtonInteraction) -> Boolean = { true }

    fun filterBy(filter: (ButtonInteraction) -> Boolean): AniPaginator {
        this.filter = filter
        return this
    }

    private var prev = DEFAULT_PREV
    private var next = DEFAULT_NEXT

    internal val controls: ActionRow
        get() = if (pageCache.size > 1) {
            ActionRow.of(
                prev.withId("$nonce:prev"),
                next.withId("$nonce:next"),
                Button.link(currPage.urlHref, currPage.urlName)
            )
        } else {
            ActionRow.of(
                Button.link(currPage.urlHref, currPage.urlName)
            )
        }

    val pages: List<AniPage> get() = pageCache.toList()

    fun addPages(vararg page: AniPage) {
        pageCache.addAll(page)
    }

    override fun onEvent(event: GenericEvent) {
        if (expiresAt < System.currentTimeMillis())
            return unregister(event.jda)
        if (event !is ButtonClickEvent) return
        val buttonId = event.componentId
        if (!buttonId.startsWith(nonce) || !filter(event)) return
        expiresAt = System.currentTimeMillis() + duration.inWholeMilliseconds
        val (_, operation) = buttonId.split(":")
        when (operation) {
            "prev" -> {
                event.editMessage(prevPage.message)
                    .setActionRows(controls)
                    .queue(null, ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE) { unregister(event.jda) })
            }
            "next" -> {
                event.editMessage(nextPage.message)
                    .setActionRows(controls)
                    .queue(null, ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE) { unregister(event.jda) })
            }
        }
    }

    private fun unregister(jda: JDA) {
        jda.removeEventListener(this)
    }
}

fun aniPaginator(
    vararg pages: AniPage,
    expireAfter: Duration = DEFAULT_DURATION.seconds
): AniPaginator {
    val nonce = ByteArray(32)
    SecureRandom().nextBytes(nonce)
    return AniPaginator(Base64.getEncoder().encodeToString(nonce), expireAfter)
        .also { it.addPages(*pages) }
}

fun InteractionHook.sendAniPaginator(
    aniPaginator: AniPaginator,
) = sendMessage(aniPaginator.also { jda.addEventListener(it) }.pages.first().message)
    .addActionRows(aniPaginator.controls)
    .delay(aniPaginator.duration.toJavaDuration())
    .flatMap { it.editMessageComponents() }

fun InteractionHook.sendAniPaginator(
    vararg pages: AniPage,
    expireAfter: Duration = DEFAULT_DURATION.seconds,
    filter: (ButtonInteraction) -> Boolean = { true }
) = sendAniPaginator(
    aniPaginator = aniPaginator(*pages, expireAfter = expireAfter).filterBy(filter)
)
