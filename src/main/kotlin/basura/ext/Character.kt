package basura.ext

import basura.aniListToDiscordNameMap
import basura.db.guilds
import basura.embed.createCharacterEmbed
import basura.embed.createMediaEmbed
import basura.filterHentai
import basura.graphql.AniList
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
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import respondingStandardPaginator

class Character : Extension() {
    override val name: String = "character"
    override val bundle: String = "character"
    private val aniList by inject<AniList>()
    override suspend fun setup() {
        publicSlashCommand(::CharacterArgs) {
            name = "character"
            description = "Looks up the name of an anime/manga character."
            action {
                val characters = aniList.findCharacter(arguments.query)

                if (characters == null) {
                    respond {
                        content = translate("character.error.noMatchingCharacter")
                    }
                } else {
                    val paginator = respondingStandardPaginator {
                        for (character in characters) {
                            page {
                                apply(createCharacterEmbed(character))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }

        publicMessageCommand {
            name = "Search Character"
            action {
                val characters = aniList.findCharacter(targetMessages.first().content)

                if (characters == null) {
                    respond {
                        content = translate("character.error.noMatchingCharacter")
                    }
                } else {
                    val paginator = respondingStandardPaginator {
                        for (character in characters) {
                            page {
                                apply(createCharacterEmbed(character))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }
    }

    inner class CharacterArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime/manga character."
        }
    }

}