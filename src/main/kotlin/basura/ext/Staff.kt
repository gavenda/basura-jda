package basura.ext

import basura.AppDispatchers
import basura.PAGINATOR_TIMEOUT
import basura.abbreviate
import basura.action
import basura.embed.createStaffEmbed
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
import mu.KotlinLogging
import org.koin.core.component.inject
import respondingStandardPaginator

class Staff : Extension() {
    override val name: String = "staff"
    override val bundle: String = "staff"
    private val aniList by inject<AniList>()
    private val log = KotlinLogging.logger { }

    override suspend fun setup() {
        publicSlashCommand(::CharacterArgs) {
            name = "staff"
            description = "Looks up the name of an anime/manga staff."
            action(AppDispatchers.IO) {
                findStaff(arguments.query)
            }
        }

        publicMessageCommand {
            name = "Search Staff"
            action(AppDispatchers.IO) {
                findStaff(targetMessages.first().content)
            }
        }
    }

    private suspend fun ApplicationCommandContext.findStaff(query: String) {
        if (this !is PublicInteractionContext) return

        val staffs = aniList.findStaff(query)

        log.info { "Looking up staff [ query = $query, userId = ${user.id} ]" }

        if (staffs == null || staffs.isEmpty()) {
            respond {
                content = translate("staff.error.noMatchingStaff")
            }
            return
        }
        val paginator = respondingStandardPaginator {
            timeoutSeconds = PAGINATOR_TIMEOUT
            staffs.forEach { staff ->
                page {
                    apply(createStaffEmbed(staff))
                }
            }
        }

        paginator.send()
    }


    inner class CharacterArgs : Arguments() {
        val query by string {
            name = "query"
            description = "Name of the anime/manga staff."
            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value

                suggestString {
                    aniList.findStaffNames(typed).take(25).forEach { staffName ->
                        choice(staffName.abbreviate(100), staffName)
                    }
                }
            }
        }
    }

}