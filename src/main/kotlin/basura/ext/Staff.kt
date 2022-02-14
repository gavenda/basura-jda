package basura.ext

import basura.embed.createCharacterEmbed
import basura.embed.createStaffEmbed
import basura.graphql.AniList
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
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
        }
    }

}