package basura.ext

import basura.PAGINATOR_TIMEOUT
import basura.embed.createCharacterEmbed
import basura.graphql.AniList
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.focusedOption
import dev.kord.core.behavior.interaction.suggestString
import org.koin.core.component.inject
import respondingStandardPaginator
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
                        timeoutSeconds = PAGINATOR_TIMEOUT
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
                        timeoutSeconds = PAGINATOR_TIMEOUT
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

            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value as String
                val characterNames = aniList.findCharacterNames(typed).take(25)

                suggestString {
                    for (characterName in characterNames) {
                        choice(characterName, characterName)
                    }
                }
            }
        }
    }

}