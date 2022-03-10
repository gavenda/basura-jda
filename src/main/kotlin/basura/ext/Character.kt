package basura.ext

import basura.AppDispatchers
import basura.PAGINATOR_TIMEOUT
import basura.abbreviate
import basura.action
import basura.embed.createCharacterEmbed
import basura.graphql.AniList
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.component.inject
import respondingStandardPaginator

class Character : Extension() {
    override val name: String = "character"
    override val bundle: String = "character"
    private val aniList by inject<AniList>()
    private val log = KotlinLogging.logger { }

    override suspend fun setup() {
        publicSlashCommand(::CharacterArgs) {
            name = "character"
            description = "Looks up the name of an anime/manga character."
            action(AppDispatchers.IO) {
                findCharacter(arguments.query)
            }
        }

        publicMessageCommand {
            name = "Search Character"
            action(AppDispatchers.IO) {
                findCharacter(targetMessages.first().content)
            }
        }
    }

    private suspend fun ApplicationCommandContext.findCharacter(query: String) {
        if (this !is PublicInteractionContext) return

        val characters = aniList.findCharacter(query)

        log.info { "Looking up character [ query = $query, userId = ${user.id} ]" }

        if (characters == null || characters.isEmpty()) {
            respond {
                content = translate("character.error.noMatchingCharacter")
            }
            return
        }

        val paginator = respondingStandardPaginator {
            timeoutSeconds = PAGINATOR_TIMEOUT
            characters.forEach { character ->
                page {
                    apply(createCharacterEmbed(character))
                }
            }
        }

        paginator.send()
    }

    inner class CharacterArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime/manga character."

            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value

                suggestString {
                    aniList.findCharacterNames(typed)
                        .take(25)
                        .forEach { characterName ->
                            choice(characterName.abbreviate(100), characterName)
                        }
                }
            }
        }
    }

}