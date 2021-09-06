package basura.embed

import basura.*
import basura.graphql.anilist.Staff
import net.dv8tion.jda.api.entities.MessageEmbed

fun generateStaffEmbed(staff: Staff, pageNo: Int, pageTotal: Int): MessageEmbed {
    val staffTitle = StringBuilder().apply {
        val native = staff.name?.native
        append(staff.name?.full)
        if (native != null) {
            append(" (${native})")
        }
    }
    val charactersVoiced = StringBuilder()
    val workedOn = StringBuilder()
    val aliases = StringBuilder()

    val characterNodes = staff.characters?.nodes
    val characterEdges = staff.characters?.edges
    val staffMediaNodes = staff.staffMedia?.nodes
    val staffMediaEdges = staff.staffMedia?.edges

    if (characterNodes != null && characterEdges != null) {
        val zip = characterNodes.zip(characterEdges)

        for (pair in zip) {
            val (node, edge) = pair
            // Ensure not null
            if (node != null && edge != null) {
                val title = node.name?.full ?: node.name?.native
                val appearance = "- [${title}](${node.siteUrl}) [${edge.role?.displayName}]\n"
                charactersVoiced.append(appearance)
            }
        }
    }

    if (staffMediaNodes != null && staffMediaEdges != null) {
        val zip = staffMediaNodes.zip(staffMediaEdges)

        for (pair in zip) {
            val (node, edge) = pair
            // Ensure not null
            if (node != null && edge != null) {
                val title = node.title?.english ?: node.title?.romaji
                val appearance = "- [${title}](${node.siteUrl}) [${edge.staffRole}]\n"
                workedOn.append(appearance)
            }
        }
    }

    // Weirdly enough, duplicate names exists.
    staff.name?.alternative
        ?.filterNotNull()
        ?.filter { it.isNotEmpty() }
        ?.distinctBy { it }
        ?.forEach {
            aliases.appendIfNotMax("- ${it.trimEnd()}\n", FIELD_LIMIT)
        }

    // Remove spoilers, fix new lines, clean up html bullshit
    val resultDescription = staff.description
        .replace(Regex("(?s)~!.*?!~"), "")
        .replace("\n\n\n", "\n")
        .aniClean()
        .weirdHtmlClean()
        .abbreviate(DESCRIPTION_LIMIT)
        .dropLastWhile { it != '\n' }

    return Embed {
        title = staffTitle.toString()
        description = resultDescription
        thumbnail = staff.image?.large
        url = staff.siteUrl
        color = 0xFF0000

        if (charactersVoiced.isNotEmpty()) {
            field {
                name = "Characters Voiced"
                value = charactersVoiced
                    .toString()
                    .abbreviate(FIELD_LIMIT)
                    .dropLastWhile { it != '\n' }
                inline = false
            }
        }

        if (workedOn.isNotEmpty()) {
            field {
                name = "Worked On"
                value = workedOn
                    .toString()
                    .abbreviate(FIELD_LIMIT)
                    .dropLastWhile { it != '\n' }
                inline = false
            }
        }

        if (aliases.isNotEmpty()) {
            field {
                name = "Aliases"
                value = aliases
                    .toString()
                    .abbreviate(FIELD_LIMIT)
                inline = false
            }
        }

        field {
            name = "Favorites"
            value = staff.favourites.toString()
            inline = true
        }

        // Add page number
        footer {
            name = "Page $pageNo of $pageTotal"
        }
    }
}