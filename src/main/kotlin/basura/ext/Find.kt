package basura.ext

import basura.PAGINATOR_TIMEOUT
import basura.aniListToDiscordNameMap
import basura.db.guilds
import basura.embed.createMediaEmbed
import basura.graphql.AniList
import basura.graphql.anilist.MediaType
import basura.lookupMediaList
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.focusedOption
import dev.kord.core.behavior.interaction.suggestString
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import respondingStandardPaginator

class Find : Extension() {
    override val name: String = "find"
    override val bundle: String = "find"

    private val aniList by inject<AniList>()
    private val db by inject<Database>()

    private val log = KotlinLogging.logger {  }

    override suspend fun setup() {
        publicSlashCommand(::FindArgs) {
            name = "find"
            description = "Looks up the name of the anime/manga."
            action {
                val allowHentai = if (guild != null) {
                    val guildIdLong = guild!!.id.value.toLong()
                    db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
                } else false
                val media = aniList.findMedia(arguments.query, allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        media.forEach {
                            page {
                                apply(createMediaEmbed(it, mediaList, aniToDiscordName))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }

        publicSlashCommand(::FindAnimeArgs) {
            name = "anime"
            description = "Looks up the name of the anime."
            action {
                val allowHentai = if (guild != null) {
                    val guildIdLong = guild!!.id.value.toLong()
                    db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
                } else false
                val media = aniList.findMediaByType(arguments.query, MediaType.ANIME, allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        media.forEach {
                            page {
                                apply(createMediaEmbed(it, mediaList, aniToDiscordName))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }

        publicSlashCommand(::FindMangaArgs) {
            name = "manga"
            description = "Looks up the name of the manga."
            action {
                val allowHentai = if (guild != null) {
                    val guildIdLong = guild!!.id.value.toLong()
                    db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
                } else false
                val media = aniList.findMediaByType(arguments.query, MediaType.MANGA, allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        media.forEach {
                            page {
                                apply(createMediaEmbed(it, mediaList, aniToDiscordName))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }

        publicMessageCommand {
            name = "Search Trash"
            action {
                val allowHentai = if (guild != null) {
                    val guildIdLong = guild!!.id.value.toLong()
                    db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
                } else false
                val media = aniList.findMedia(targetMessages.first().content, allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        media.forEach {
                            page {
                                apply(createMediaEmbed(it, mediaList, aniToDiscordName))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }
    }

    inner class FindArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime/manga."
            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value as String
                val mediaTitles = aniList.findMediaTitles(typed).take(25)

                suggestString {
                    for (media in mediaTitles) {
                        choice(media, media)
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
                val typed = focusedOption.value as String
                val mediaTitles = aniList.findMediaTitles(typed, MediaType.ANIME).take(25)

                suggestString {
                    for (media in mediaTitles) {
                        choice(media, media)
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
                val typed = focusedOption.value as String
                val mediaTitles = aniList.findMediaTitles(typed, MediaType.MANGA).take(25)

                suggestString {
                    for (media in mediaTitles) {
                        choice(media, media)
                    }
                }
            }
        }
    }
}