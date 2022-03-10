package basura.ext

import basura.AppDispatchers
import basura.abbreviate
import basura.action
import basura.db.guilds
import basura.graphql.AniList
import basura.graphql.anilist.MediaType
import basura.sendMediaResult
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.focusedOption
import dev.kord.core.behavior.interaction.suggestString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

class Find : Extension() {
    override val name: String = "find"
    override val bundle: String = "find"

    private val aniList by inject<AniList>()
    private val db by inject<Database>()
    private val log = KotlinLogging.logger { }

    override suspend fun setup() {
        publicSlashCommand(::FindArgs) {
            name = "find"
            description = "Looks up the name of the anime/manga."
            action(AppDispatchers.IO) {
                findMedia(arguments.query)
            }
        }

        publicSlashCommand(::FindAnimeArgs) {
            name = "anime"
            description = "Looks up the name of the anime."
            action(AppDispatchers.IO) {
                findMedia(arguments.query, MediaType.ANIME)
            }
        }

        publicSlashCommand(::FindMangaArgs) {
            name = "manga"
            description = "Looks up the name of the manga."
            action(AppDispatchers.IO) {
                findMedia(arguments.query, MediaType.MANGA)
            }
        }

        publicMessageCommand {
            name = "Search Trash"
            action(AppDispatchers.IO) {
                findMedia(targetMessages.first().content)
            }
        }
    }

    private suspend fun ApplicationCommandContext.findMedia(query: String, type: MediaType? = null) {
        if (this !is PublicInteractionContext) return

        log.info { "Looking up media [ query = $query, userId = ${user.id} ]" }

        val hentai = if (guild != null) {
            val guildIdLong = guild!!.id.value.toLong()
            db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
        } else false
        val media = aniList.findMedia(query, type, hentai)

        if (media == null || media.isEmpty()) {
            respond {
                content = translate("find.error.noMatchingMedia")
            }
            return
        }

        sendMediaResult(guild, media)
    }

    inner class FindArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime/manga."
            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value

                suggestString {
                    aniList.findMediaTitles(typed)
                        .take(25)
                        .forEach { media ->
                            choice(media.abbreviate(100), media)
                        }
                }
            }
        }
    }

    inner class FindAnimeArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime."

            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value

                suggestString {
                    aniList.findMediaTitles(typed, MediaType.ANIME)
                        .take(25)
                        .forEach { media ->
                            choice(media.abbreviate(100), media)
                        }
                }
            }
        }
    }

    inner class FindMangaArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the manga."

            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value

                suggestString {
                    aniList.findMediaTitles(typed, MediaType.MANGA).take(25).forEach { media ->
                        choice(media.abbreviate(100), media)
                    }
                }
            }
        }
    }
}