package basura.ext

import basura.aniListToDiscordNameMap
import basura.db.guilds
import basura.embed.createMediaEmbed
import basura.filterHentai
import basura.graphql.AniList
import basura.graphql.anilist.MediaType
import basura.lookupMediaList
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicUserCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.flow.first
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

    override suspend fun setup() {
        publicSlashCommand(::FindArgs) {
            name = "find"
            description = "Looks up the name of the anime/manga."
            action {
                val allowHentai = if (guild != null) {
                    val guildIdLong = guild!!.id.value.toLong()
                    db.guilds.firstOrNull { it.discordGuildId eq guildIdLong }?.hentai ?: false
                } else false
                val media = aniList.findMedia(arguments.query)
                    ?.filterHentai(allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
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
                val media = aniList.findMediaByType(arguments.query, MediaType.ANIME)
                    ?.filterHentai(allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
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
                val media = aniList.findMediaByType(arguments.query, MediaType.MANGA)
                    ?.filterHentai(allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
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
                val media = aniList.findMedia(targetMessages.first().content)
                    ?.filterHentai(allowHentai)

                if (media == null) {
                    respond {
                        content = translate("find.error.noMatchingMedia")
                    }
                } else {
                    val mediaList = lookupMediaList(media, guild?.id?.value?.toLong())
                    val aniToDiscordName = aniListToDiscordNameMap(guild?.fetchGuildOrNull())
                    val paginator = respondingStandardPaginator {
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
        }
    }

    inner class FindAnimeArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime."
        }
    }

    inner class FindMangaArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the manga."
        }
    }
}