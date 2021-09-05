package basura.embed

import basura.*
import basura.graphql.anilist.*
import net.dv8tion.jda.api.entities.MessageEmbed

/**
 * Generates an embed given the media and a user's media list.
 */
fun generateMediaEmbed(media: Media, mediaList: List<MediaList>?, pageNo: Int, pageTotal: Int): MessageEmbed {
    val season = if (media.season != MediaSeason.UNKNOWN) media.season.displayName else "-"
    val seasonYear = if (media.seasonYear != 0) media.seasonYear else "-"
    val score = media.meanScore.toStars()
    var duration = if (media.duration != 0) "${media.duration} minutes" else "-"
    val episodes = if (media.episodes != 0) "${media.episodes}" else "-"
    val episodicFormats = listOf(
        MediaFormat.ONA,
        MediaFormat.OVA,
        MediaFormat.TV,
        MediaFormat.SPECIAL
    )

    val completed = StringBuilder()
    val planned = StringBuilder()
    val inProgress = StringBuilder()
    val paused = StringBuilder()
    val dropped = StringBuilder()
    val notOnList = StringBuilder()
    val repeating = StringBuilder()

    if (mediaList != null) {
        val embedMedias = mediaList
            .filter { it.mediaId == media.id }
            .map { ml ->
                EmbedMedia(
                    discordName = ml.user?.name,
                    status = ml.status,
                    score = ml.score,
                    progress = ml.progress
                )
            }
            .sortedWith(compareBy({ it.progress }, { it.discordName }))

        for (embedMedia in embedMedias) {
            when (embedMedia.status) {
                MediaListStatus.COMPLETED -> {
                    if (embedMedia.score == 0f) {
                        // Display a ? if no score. (0 indicates no score on AniList)
                        completed.append("- ${embedMedia.discordName} ‣ (-)\n")
                    } else {
                        // Display the score otherwise.
                        completed.append("- ${embedMedia.discordName} ‣ ${embedMedia.score.toStars()} (${embedMedia.score})\n")
                    }
                }
                MediaListStatus.CURRENT -> {
                    inProgress.append("- ${embedMedia.discordName} ‣ [${embedMedia.progress}]\n")
                }
                MediaListStatus.DROPPED -> {
                    dropped.append("- ${embedMedia.progress} ‣ [${embedMedia.progress}]\n")
                }
                MediaListStatus.PAUSED -> {
                    paused.append("- ${embedMedia.discordName} ‣ [${embedMedia.progress}]\n")
                }
                MediaListStatus.PLANNING -> {
                    planned.append("- ${embedMedia.discordName}\n")
                }
                MediaListStatus.REPEATING -> {
                    if (embedMedia.score == 0f) {
                        completed.append("- ${embedMedia.discordName} ‣ (-)\n")
                    }
                    // Display the score otherwise.
                    else {
                        repeating.append("- ${embedMedia.discordName} ‣ ${embedMedia.score.toStars()} (${embedMedia.score}) [Episode: ${embedMedia.progress}]\n")
                    }
                }
                else -> {
                    notOnList.append("- ${embedMedia.discordName}\n")
                }
            }
        }
    }

    // Add 'per episode' for TV, OVA, ONA and Specials.
    if (duration != "-" && episodicFormats.contains(media.format)) {
        duration += " per episode"
    }

    val mediaDescription = StringBuilder()

    if (media.title?.romaji != null && media.title.english != null) {
        mediaDescription.append("_(Romaji: ${media.title.romaji})_\n")
    }
    if (media.title?.native != null) {
        mediaDescription.append("_(Native: ${media.title.native})_\n")
    }

    val actualDescription = media.description
        .htmlClean()
        .abbreviate(DESCRIPTION_LIMIT)

    mediaDescription.append("\n")
    mediaDescription.append(actualDescription)

    return Embed {
        title = media.title?.english ?: media.title?.romaji
        description = mediaDescription.toString()
        thumbnail = media.coverImage?.extraLarge
        url = media.siteUrl
        color = 0xFF0000

        // First row
        field {
            name = "Type"
            value = media.type.displayName
            inline = true
        }
        field {
            name = "Status"
            value = media.status.displayName
            inline = true
        }

        if (season == "-" && seasonYear == "-") {
            field {
                name = "Season"
                value = "?"
                inline = true
            }
        } else {
            field {
                name = "Season"
                value = "$season $seasonYear"
                inline = true
            }
        }

        // Second row
        field {
            name = "Rating"
            value = "$score (${media.meanScore})"
            inline = true
        }
        field {
            name = "Popularity"
            value = "${media.popularity}"
            inline = true
        }
        field {
            name = "Favorites"
            value = "${media.favourites}"
            inline = true
        }

        // Third row
        field {
            name = "Episodes"
            value = episodes
            inline = true
        }
        field {
            name = "Duration"
            value = duration
            inline = true
        }
        field {
            name = "Format"
            value = media.format.displayName
            inline = true
        }

        if (media.genres.isNotEmpty()) {
            // Fourth row
            field {
                name = "Genres"
                value = media.genres.joinToString(
                    separator = " - "
                ) {
                    "`$it`"
                }
                inline = false
            }
        }

        // User scores
        if (inProgress.isNotEmpty()) {
            field {
                name = "In Progress"
                value = inProgress.toString()
                inline = false
            }
        }

        if (repeating.isNotEmpty()) {
            field {
                name = "Rewatching"
                value = repeating.toString()
                inline = false
            }
        }

        if (completed.isNotEmpty()) {
            field {
                name = "Completed"
                value = completed.toString()
                inline = false
            }
        }

        if (dropped.isNotEmpty()) {
            field {
                name = "Dropped"
                value = dropped.toString()
                inline = false
            }
        }

        if (planned.isNotEmpty()) {
            field {
                name = "Planned"
                value = planned.toString()
                inline = false
            }
        }

        if (notOnList.isNotEmpty()) {
            field {
                name = "Not On List"
                value = notOnList.toString()
                inline = false
            }
        }

        val mediaRank = StringBuilder()
        val mediaRankAscending = media.rankings
            .sortedBy { it.rank }

        val allTimeRank = mediaRankAscending
            .firstOrNull {
                it.type == MediaRankType.RATED && it.allTime
            }
        val seasonRank = mediaRankAscending
            .firstOrNull {
                it.type == MediaRankType.RATED && !it.allTime && it.season != MediaSeason.UNKNOWN
            }

        if (allTimeRank != null) {
            mediaRank.append("Rank #${allTimeRank.rank} (${media.format.displayName}) ${Typography.bullet} ")
        }
        if (seasonRank != null) {
            mediaRank.append("Rank #${seasonRank.rank} (${media.format.displayName}) of ${seasonRank.season.displayName} ${seasonRank.year} ${Typography.bullet} ")
        }

        // Add ranking info
        if (mediaRank.isNotEmpty()) {
            author {
                name = mediaRank.toString().dropLast(3)
            }
        }

        // Add page number
        footer {
            name = "Page $pageNo of $pageTotal"
        }
    }
}
