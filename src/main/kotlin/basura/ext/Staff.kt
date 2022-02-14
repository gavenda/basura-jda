package basura.ext

import basura.PAGINATOR_TIMEOUT
import basura.embed.createStaffEmbed
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

class Staff : Extension() {
    override val name: String = "staff"
    override val bundle: String = "staff"
    private val aniList by inject<AniList>()

    override suspend fun setup() {
        publicSlashCommand(::CharacterArgs) {
            name = "staff"
            description = "Looks up the name of an anime/manga staff."
            action {
                val staffs = aniList.findStaff(arguments.query)

                if (staffs == null) {
                    respond {
                        content = translate("staff.error.noMatchingStaff")
                    }
                } else {
                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        for (staff in staffs) {
                            page {
                                apply(createStaffEmbed(staff))
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }

        publicMessageCommand {
            name = "Search Staff"
            action {
                val staffs = aniList.findStaff(targetMessages.first().content)

                if (staffs == null) {
                    respond {
                        content = translate("staff.error.noMatchingStaff")
                    }
                } else {
                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        for (staff in staffs) {
                            page {
                                apply(createStaffEmbed(staff))
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
            description = "Name of the anime/manga staff."
            autoComplete {
                if (!focusedOption.focused) return@autoComplete
                val typed = focusedOption.value as String
                val staffNames = aniList.findStaffNames(typed).take(25)

                suggestString {
                    for (staffName in staffNames) {
                        choice(staffName, staffName)
                    }
                }
            }
        }
    }

}