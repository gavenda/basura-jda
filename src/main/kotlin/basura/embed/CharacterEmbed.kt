package basura.embed

import basura.*
import basura.graphql.anilist.Character
import basura.graphql.anilist.MediaType
import net.dv8tion.jda.api.entities.MessageEmbed


fun generateCharacterEmbed(character: Character, pageNo: Int, pageTotal: Int): MessageEmbed {
    val characterTitle = StringBuilder().apply {
        val native = character.name?.native
        append(character.name?.full)
        if (native != null) {
            append(" (${native})")
        }
    }
    val animeAppearance = StringBuilder()
    val mangaAppearance = StringBuilder()
    val alternateNames = StringBuilder()

    val mediaNodes = character.media?.nodes
    val mediaEdges = character.media?.edges

    if (mediaNodes != null && mediaEdges != null) {
        val mediaZip = mediaNodes.zip(mediaEdges)

        for (pair in mediaZip) {
            val (media, edge) = pair
            // Ensure not null
            if (media != null && edge != null) {
                val mediaTitle = media.title?.english ?: media.title?.romaji
                val appearance = "- [${mediaTitle}](${media.siteUrl}) [${edge.characterRole?.displayName}]\n"

                when (media.type) {
                    MediaType.ANIME -> {
                        animeAppearance.appendIfNotMax(appearance, FIELD_LIMIT)
                    }
                    MediaType.MANGA -> {
                        mangaAppearance.appendIfNotMax(appearance, FIELD_LIMIT)
                    }
                    // Do nothing
                    else -> {
                    }
                }
            }
        }
    }

    // Weirdly enough, duplicate names exists.
    character.name?.alternative
        ?.filterNotNull()
        ?.distinctBy { it }
        ?.forEach {
            alternateNames.appendIfNotMax("- ${it.trimEnd()}\n", FIELD_LIMIT)
        }

    // Remove spoilers, fix new lines, clean up html bullshit
    val resultDescription = character.description
        .replace(Regex("(?s)~!.*?!~"), "")
        .replace("\n\n\n", "\n")
        .aniClean()
        .weirdHtmlClean()
        .abbreviate(DESCRIPTION_LIMIT)

    return Embed {
        title = characterTitle.toString()
        description = resultDescription
        thumbnail = character.image?.large
        url = character.siteUrl
        color = 0xFF0000

        if (animeAppearance.isNotEmpty()) {
            field {
                name = "Anime Appearances"
                value = animeAppearance.toString()
                inline = false
            }
        }

        if (mangaAppearance.isNotEmpty()) {
            field {
                name = "Manga Appearances"
                value = mangaAppearance.toString()
                inline = false
            }
        }

        if (alternateNames.isNotEmpty()) {
            field {
                name = "Alternate Names"
                value = alternateNames.toString()
                inline = false
            }
        }

        field {
            name = "AniList ID"
            value = character.id.toString()
            inline = true
        }

        field {
            name = "Favorites"
            value = character.favourites.toString()
            inline = true
        }

        // Add page number
        footer {
            name = "Page $pageNo of $pageTotal"
        }
    }
}
