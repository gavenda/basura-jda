package basura.ext

import basura.embed.createCharacterEmbed
import basura.graphql.AniList
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject
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
    }

    inner class CharacterArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime/manga character."
        }
    }

}