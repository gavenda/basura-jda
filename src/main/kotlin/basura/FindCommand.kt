package basura

import basura.db.users
import basura.discord.await
import basura.discord.interaction.sendPaginator
import basura.discord.onCommand
import basura.embed.generateMediaEmbed
import basura.graphql.AniList
import basura.graphql.anilist.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import org.kodein.di.instance
import org.ktorm.database.Database
import org.ktorm.entity.map

fun JDA.addFindCommands() {
    val aniList by kodein.instance<AniList>()

    onCommand("find") { event ->
        event.awaitDeferReply()

        try {
            val query = event.requiredOption("query").asString
            val media = aniList.findMedia(query)

            if(media == null) {
                event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
                return@onCommand
            }

            val mediaList = lookupMediaList(media)

            event.sendMediaResults(media, mediaList)
        } catch (e: ErrorResponseException) {
            if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                log.debug("Received unknown message error, but is being ignored.")
            } else {
                log.error("Error sending find result", e)
            }
        } catch (e: Exception) {
            log.error("Error sending find result", e)
            event.sendUnknownError()
        }
    }
    onCommand("anime") { event ->
        event.awaitDeferReply()

        try {
            val query = event.requiredOption("query").asString
            val media = aniList.findMediaByType(query, MediaType.ANIME)

            if(media == null) {
                event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
                return@onCommand
            }

            val mediaList = lookupMediaList(media)

            event.sendMediaResults(media, mediaList)
        } catch (e: ErrorResponseException) {
            if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                log.debug("Received unknown message error, but is being ignored.")
            } else {
                log.error("Error sending find result", e)
            }
        } catch (e: Exception) {
            log.error("Error sending find result", e)
            event.sendUnknownError()
        }
    }
    onCommand("manga") { event ->
        event.awaitDeferReply()

        try {
            val query = event.requiredOption("query").asString
            val media = aniList.findMediaByType(query, MediaType.MANGA)

            if(media == null) {
                event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
                return@onCommand
            }

            val mediaList = lookupMediaList(media)

            event.sendMediaResults(media, mediaList)
        } catch (e: ErrorResponseException) {
            if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                log.debug("Received unknown message error, but is being ignored.")
            } else {
                log.error("Error sending find result", e)
            }
        } catch (e: Exception) {
            log.error("Error sending find result", e)
            event.sendUnknownError()
        }
    }
    onCommand(Command.RANKING) { event ->
        event.awaitDeferReply()

        try {
            val amount = event.requiredOption("amount").asLong.toInt()
            val format = event.getOption("format")?.asString
            val season = event.getOption("season")?.asString
            val seasonYear = event.getOption("year")?.asLong?.toInt()

            val mediaSeason = season?.let { MediaSeason.valueOf(it) }
            val mediaFormat = format?.let { MediaFormat.valueOf(it) } ?: MediaFormat.TV

            val formats = listOf(mediaFormat)
            val media = aniList.findMediaByRanking(
                amount = amount,
                formatIn = formats,
                season = mediaSeason,
                seasonYear = seasonYear
            )

            if(media == null) {
                event.sendLocalizedMessage(LocaleMessage.Find.NoMatchingMedia)
                return@onCommand
            }

            val mediaList = lookupMediaList(media)

            event.sendMediaResults(media, mediaList)
        } catch (e: ErrorResponseException) {
            if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                log.debug("Received unknown message error, but is being ignored.")
            } else {
                log.error("Error sending find result", e)
            }
        } catch (e: Exception) {
            log.error("Error sending find result", e)
            event.sendUnknownError()
        }
    }
}

internal suspend fun lookupMediaList(medias: List<Media>?): List<MediaList>? {
    val db by kodein.instance<Database>()
    val aniList by kodein.instance<AniList>()

    return aniList.findScoreByUsersAndMedias(
        userIds = db.users.map { it.aniListId },
        mediaIds = medias?.map { it.id }
    )
}

suspend fun SlashCommandEvent.sendMediaResults(media: List<Media>, mediaList: List<MediaList>?) {
    val embeds = media.mapIndexed { i, m ->
        generateMediaEmbed(m, mediaList, (i + 1), media.size)
    }.toTypedArray()

    hook.sendPaginator(*embeds).await()
}

