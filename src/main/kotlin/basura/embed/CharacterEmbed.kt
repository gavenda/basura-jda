package basura.embed

import basura.*
import basura.discord.interaction.PaginatedMessage
import basura.graphql.anilist.Character
import basura.graphql.anilist.MediaType

fun pagedCharacterEmbed(character: Character, pageNo: Int, pageTotal: Int): PaginatedMessage {
    val characterTitle = StringBuilder().apply {
        val native = character.name?.native
        append(character.name?.full)
        if (native != null) {
            append(" (${native})")
        }
    }
    val animeAppearance = StringBuilder()
    val mangaAppearance = StringBuilder()
    val aliases = StringBuilder()

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
        ?.filter { it.isNotEmpty() }
        ?.distinctBy { it }
        ?.forEach {
            aliases.appendIfNotMax("- ${it.trimEnd()}\n", FIELD_LIMIT)
        }

    // Remove spoilers, fix new lines, clean up html bullshit
    val resultDescription = character.description
        .replace(Regex("(?s)~!.*?!~"), "")
        .replace("\n\n\n", "\n")
        .aniClean()
        .weirdHtmlClean()
        .abbreviate(DESCRIPTION_LIMIT)
        .dropLastWhile { it != '\n' }

    val characterEmbed = Embed {
        title = characterTitle.toString()
        description = resultDescription
        thumbnail = character.image?.large
        url = character.siteUrl
        color = 0xFF0000

        if (animeAppearance.isNotEmpty()) {
            field {
                name = "Anime Appearances"
                value = animeAppearance
                    .toString()
                    .abbreviate(FIELD_LIMIT)
                    .dropLastWhile { it != '\n' }
                inline = false
            }
        }

        if (mangaAppearance.isNotEmpty()) {
            field {
                name = "Manga Appearances"
                value = mangaAppearance
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
            value = character.favourites.toString()
            inline = true
        }

        if (pageTotal > 1) {
            // Add page number
            footer {
                name = "Page $pageNo of $pageTotal"
            }
        }
    }

    return PaginatedMessage(
        message = Message(embed = characterEmbed),
        urlName = "View on AniList",
        urlHref = character.siteUrl
    )
}
